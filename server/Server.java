package server;

import static utils.Configuration.verbose;

public class Server
{

    ////////////////////////////////////////////////////////////
	// SERVER PROGRAM ENTRY ////////////////////////////////////
	////////////////////////////////////////////////////////////
	public static void main(String[] args) throws java.sql.SQLException, java.io.IOException
	{
		ArgumentHandler arghandler = initializeConfiguration(args);

		if (arghandler.hasOption("cli"))
			commandLineInterface();
		else if (arghandler.hasOption("gui"))
			; // start gui
		else if (arghandler.hasOption("test"))
			; // Run tests
		else if (arghandler.hasOption("keygen"))
			(new Server(utils.Configuration.settings)).generatePublicAndPrivateKey();
		else if (arghandler.hasOption("help"))
			printHelp();
		else
			printHelp();
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
		Database db;

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
				verbose("Unable to close database.");
			}
		}
	}

	public static void commandLineInterface()
	{
		Server server = null;
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		try
		{
			server = new Server(utils.Configuration.settings);
			Runtime.getRuntime().addShutdownHook(new ServerFinalizer(db));
			java.util.Scanner scanner = new java.util.Scanner(System.in);
			System.out.print("Enter the port to listen and send to (leave blank for default): ");
			String portnumber = scanner.nextLine();
			Integer port = null;
			if (!portnumber.equals(""))
				port = Integer.parseInt(portnumber);

			while (true)
			{
				String message = server.waitForMessage(port);
				if (message == null)
					continue;
				java.util.ArrayList<String> message_parts = utils.Utils.splitAndUnescapeString(message);
				if (message_parts.size() == 3)
				{
					server.respondToMessage(db.execute(message_parts.get(0), message_parts.get(1), message_parts.get(2)));
				}
				else
				{
					server.respondToMessage("INVALID: Amount of tokens do not match the desired amount of 3 tokens.");
				}
			}
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	public static void printHelp()
	{
		System.out.println("Help for server CLI");
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


	public Server(utils.Configuration settings) throws java.io.IOException
	{
		this.settings = settings;
		loadTheKeysIntoMemory();
	}

	public String waitForMessage(Integer port)
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
		catch (java.net.BindException exc_obj) { try { finishConnection(); } catch (java.io.IOException exc_object) { verbose("Unable to unbind"); } }
		catch (java.net.SocketException exc_obj) { verbose(exc_obj.toString()); }
		catch (Exception exc_obj) { verbose(exc_obj.toString()); }
		return null;
	}

	public void getSymmetricKeyFromClient()
	{
		verbose("Fetching symmetric key.");
		try
		{
			byte[] bytes = new byte[settings.getInt("keylength")];
			int code = input_from_client.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, code);
			bytes = utils.Utils.decrypt(bytes, server_private_key);
			symmetric_key = new javax.crypto.spec.SecretKeySpec(bytes, "AES");
			verbose("Fetched symkey: '" + new String(symmetric_key.getEncoded()) + "'");
			verbose("Symkey stored");
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void respondToMessage(String string)
	{
		verbose("Attempting to send back: '" + string + "'");
		try
		{
			byte[] bytes = utils.Utils.encryptSymmetric(utils.Utils.escapeSpaces(string).getBytes(), symmetric_key);
			output_to_client.write(bytes);
			output_to_client.flush();
			finishConnection();
			
		}
		catch (java.io.IOException exc_object) 
		{ 
			verbose("Unable to unbind");
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	/// Generates a private and public key and stores it inside 2 files in the root folder.
	public void generatePublicAndPrivateKey() throws java.io.IOException, java.io.FileNotFoundException
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
		verbose("Waiting for a response");
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
			catch (java.security.NoSuchAlgorithmException exc_obj)
			{
				verbose(exc_obj.toString());
			}
			catch (java.security.spec.InvalidKeySpecException exc_obj)
			{
				verbose(exc_obj.toString());
			}
		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
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

	private void readIncomingbytes() throws java.io.IOException
	{
		verbose("Reading incoming bytes.");
		byte[] bytes = new byte[settings.getInt("keylength")];
		int code = input_from_client.read(bytes);
		bytes = java.util.Arrays.copyOf(bytes, code);
		try
		{
			// System.out.println(new String(bytes));
			last_message = new String(utils.Utils.decryptSymmetric(bytes, symmetric_key));
			System.out.println(">" + last_message);
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void finishConnection() throws java.io.IOException
	{
		verbose("Cleaning up the connection.");
		server_socket.close();
	}

}