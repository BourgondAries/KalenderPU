package server;

import static utils.Configuration.verbose;

public class Server
{

    ////////////////////////////////////////////////////////////
	// SERVER PROGRAM ENTRY ////////////////////////////////////
	////////////////////////////////////////////////////////////
	public static void main(String[] args)
	{
		ArgumentHandler arghandler = initializeConfiguration(args);

		if (arghandler.hasOption("cli"))
			ServerCommandLineInterface.commandLineInterface();
		else if (arghandler.hasOption("gui"))
			; // start gui
		else if (arghandler.hasOption("test"))
			; // Run tests
		else if (arghandler.hasOption("keygen"))
			ServerCommandLineInterface.generateKeys();
		else if (arghandler.hasOption("help"))
			ServerCommandLineInterface.printHelp();
		else
			ServerCommandLineInterface.printHelp();
	}

	public static ArgumentHandler initializeConfiguration(String[] args)
	{
		utils.Configuration settings = null;
		try { settings = utils.Configuration.loadDefaultConfiguration(); }
		catch ( java.io.IOException ioexc ) { System.out.println("Unable to load configuration data: " + ioexc); System.exit(1); }

		ArgumentHandler argument_handler = new ArgumentHandler(args);
		utils.Configuration.verbose_mode = argument_handler.hasOption("v");
		return argument_handler;
	}

    public static class ServerFinalizer extends Thread
	{
		private Database db;

		ServerFinalizer(Database db)
		{
			this.db = db;
		}

		public void run()
		{
			try
			{
				verbose("Closing database gracefully.");
				db.closeDatabase();	
			}
			catch (java.sql.SQLException exc)
			{
				verbose("Unable to close database. Exiting without closing properly.");
			}
			catch (NullPointerException exc)
			{
				verbose("Database was not initialized: skipping graceful close.");
			}
		}
	}

    ////////////////////////////////////////////////////////////
	// SERVER OBJECT DEPENDENT DEFINITIONS /////////////////////
	////////////////////////////////////////////////////////////

	private utils.Configuration settings = null;

	private java.net.ServerSocket 		server_socket;
	private java.net.Socket 			client_socket;

	private java.io.InputStream 		input_from_client;
	private java.io.OutputStream 		output_to_client;

	private java.security.PrivateKey 	server_private_key;
	private java.security.PublicKey 	server_public_key;
	private java.security.PublicKey 	client_public_key;

	private java.security.Key 			symmetric_key;

	private String last_message;

	public static class PortUnavailableException extends Error
	{
		public String message;

		public PortUnavailableException(String message)
		{
			this.message = message;
		}
	}

	public static class BlockSizeTooLargeException extends Exception
	{

	}

	public Server(utils.Configuration settings) throws java.io.IOException
	{
		this.settings = settings;
		loadTheKeysIntoMemory();
	}

	public String waitForMessage(Integer port) throws Exception
	{
		try
		{
			waitForIncomingConnection(port);
			setup2WayCommunicationChannels();
			announceServerPublicKey();
			getPublicKeyFromClient();
			sendCertificateToClient(signClientsPublicKey());
			getSymmetricKeyFromClient();
			readIncomingbytes();
			String tmp = last_message;
			last_message = null;
			return tmp;
		}
		catch (java.net.BindException exc) { throw new PortUnavailableException("The specified port already in use."); }
		catch (java.net.SocketException exc) { verbose(exc.toString()); }
		return null;
	}

	public void getSymmetricKeyFromClient()
		throws
			javax.crypto.BadPaddingException,
			javax.crypto.NoSuchPaddingException,
			java.security.NoSuchAlgorithmException,
			javax.crypto.IllegalBlockSizeException,
			java.security.InvalidKeyException,
			java.io.IOException
	{
		verbose("Fetching symmetric key.");

		byte[] bytes = new byte[settings.getInt("keylength")];
		int code = input_from_client.read(bytes);
		bytes = java.util.Arrays.copyOf(bytes, code);
		bytes = utils.Utils.decrypt(bytes, server_private_key);
		symmetric_key = new javax.crypto.spec.SecretKeySpec(bytes, settings.get("SymmetricSpec"));
	}

	public void respondToMessage(String string)
	{
		verbose("Attempting to send back: '" + string + "'");
		try
		{
			byte[] bytes = utils.Utils.encryptSymmetric(string.getBytes("UTF-8"), symmetric_key, settings.get("SymmetricCipher"));
			output_to_client.write(bytes);
			output_to_client.flush();
			client_socket.shutdownOutput();
			finishConnection();
			
		}
		catch (java.io.IOException excect) 
		{ 
			verbose("Unable to unbind");
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	/// Generates a private and public key and stores it inside 2 files in the root folder.
	public void generatePublicAndPrivateKey()
		throws
			java.io.IOException,
			java.io.FileNotFoundException,
			java.security.NoSuchAlgorithmException,
			java.security.NoSuchProviderException
	{
		verbose("Creating public and private key pair.");
		java.security.KeyPair pair = utils.Utils.getNewKeyPair();
		java.io.FileOutputStream output = new java.io.FileOutputStream(utils.Configuration.settings.get("ServerPublicKeyFile"));
		output.write(pair.getPublic().getEncoded());
		output = new java.io.FileOutputStream(utils.Configuration.settings.get("ServerPrivateKeyFile"));
		output.write(pair.getPrivate().getEncoded());
	}

	private void loadTheKeysIntoMemory() throws java.io.IOException
	{
		verbose("Loading keys into memory.");
		server_public_key = utils.Utils.getServerPublicKey();
		server_private_key = utils.Utils.getServerPrivateKey();
	}

	private void waitForIncomingConnection(Integer port) throws java.io.IOException
	{
		verbose("Waiting for incoming connection...");
		server_socket = new java.net.ServerSocket((port == null ? settings.getInt("port") : port));
		client_socket = server_socket.accept();
		client_socket.setSoTimeout(settings.getInt("SocketTimeOut"));
	}

	private void setup2WayCommunicationChannels() throws java.io.IOException
	{
		verbose("Setting up 2-way communication.");
		input_from_client = client_socket.getInputStream();
		output_to_client = client_socket.getOutputStream();
	}

	private void announceServerPublicKey() throws Exception
	{
		verbose("Broadcasting server's public key.");
		output_to_client.write(server_public_key.getEncoded());
	}

	private void getPublicKeyFromClient()
	{
		verbose("Fetching the public key from the client.");
		byte[] bytes = new byte[settings.getInt("keylength")];
		try
		{
			int number = input_from_client.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, number);
			try
			{

				java.security.spec.X509EncodedKeySpec pubkey_spec = new java.security.spec.X509EncodedKeySpec(bytes);
				java.security.KeyFactory key_factory = java.security.KeyFactory.getInstance(settings.get("keypairgen"));
				client_public_key = key_factory.generatePublic(pubkey_spec);
			}
			catch (java.security.NoSuchAlgorithmException exc)
			{
				verbose(exc.toString());
			}
			catch (java.security.spec.InvalidKeySpecException exc)
			{
				verbose(exc.toString());
			}
		}
		catch (java.io.IOException exc)
		{
			verbose(exc.toString());
		}
	}

	private byte[] signClientsPublicKey() throws java.security.NoSuchAlgorithmException, java.io.IOException, java.security.SignatureException, java.security.InvalidKeyException
	{
		verbose("Signing the client's public key.");
		java.security.Signature signature = java.security.Signature.getInstance(utils.Configuration.settings.get("SignMethod"));
		signature.initSign(server_private_key);
		signature.update(client_public_key.getEncoded());
		return signature.sign();
	}

	private void sendCertificateToClient(byte[] signature) throws java.io.IOException
	{
		verbose("Sending signed public key to client.");
		output_to_client.write(signature);
	}

	private void readIncomingbytes() throws Exception
	{
		verbose("Reading incoming bytes.");

		int length = 0;
		byte[] bytes = new byte[0];
		byte[] temporary = new byte[settings.getInt("blocklength")];
		do
		{
			length = input_from_client.read(temporary);
			if (length == -1)
				break;
			temporary = java.util.Arrays.copyOf(temporary, length);
			byte[] total = new byte[bytes.length + temporary.length];
			System.arraycopy(bytes, 0, total, 0, bytes.length);
			System.arraycopy(temporary, 0, total, bytes.length, temporary.length);
			bytes = total;
			if (bytes.length > settings.getInt("maxblocksize"))
				throw new BlockSizeTooLargeException();
		}
		while (length != -1 && length == settings.getInt("blocklength"));
		try
		{
			// System.out.println(new String(bytes));
			last_message = new String(utils.Utils.decryptSymmetric(bytes, symmetric_key, settings.get("SymmetricCipher")));
			System.out.println(">" + last_message);
			System.out.println("hallo3");
		}
		catch (Exception exc)
		{
			System.out.println("hallo4");
			verbose(exc.toString());

		}
	}

	private void finishConnection() throws java.io.IOException
	{
		verbose("Cleaning up the connection.");
		server_socket.close();
	}

}