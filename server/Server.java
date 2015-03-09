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
		{
			try
			{
				(new Server(utils.Configuration.settings)).generatePublicAndPrivateKey();
			}
			catch (java.security.NoSuchAlgorithmException exc)
			{

			}
			catch (java.security.NoSuchProviderException exc)
			{

			}
		}
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

	static class ExitListener extends Thread
	{
		public java.util.Scanner scanner = null;
		public Database db = null;

		@Override
		public void run()
		{
			System.out.println("Press '" + utils.Configuration.settings.get("ExitCommand") + "' to exit.\nOr type any SQL query here to run it.");
			try
			{
				while (true)
				{
					System.out.print("SQL command: ");
					String string = scanner.nextLine();
					if (string.equals(utils.Configuration.settings.get("ExitCommand")))
					{
						System.exit(0);	
					}
					else
					{
						try
						{
							String stringx = db.runQuery(string);
							System.out.println("Raw result: " + stringx);
							System.out.println(client.ServerReturnData.getPrettyStringWithoutObject(stringx));
						}
						catch (Database.DatabaseUninitializedException exc)
						{
							verbose("The database is not initialized: attempting to re-initialize.");
						}
						catch (client.ServerReturnData.InvalidInputException exc)
						{
							verbose("The return string was empty.");
						}
					}
				}
			}
			catch (java.util.NoSuchElementException exc)
			{
				verbose("Command line scanner forced to exit.");
			}
		}
	}

	public static Integer queryPort(java.util.Scanner scanner)
	{
		System.out.print("Enter the port to listen and send to (leave blank for default): ");
		String portnumber = scanner.nextLine();
		Integer port = null;
		if (!portnumber.equals(""))
			port = Integer.parseInt(portnumber);
		return port;
	}

	public static class BlockSizeTooLargeException extends Exception
	{

	}

	public static void commandLineInterface()
	{
		Server server = null;
		Database db = null;
		try
		{
			db = new Database(utils.Configuration.settings.get("DBConnection"));
		}
		catch (Database.CouldNotConnectAndSetupDatabaseConnection exc)
		{
			verbose("Unable to connect to the database. Check if the database is not already in use. If it is not; try making dbreset or setup.");
		}
		catch (Database.CouldNotFindEncryptionAlgorithm exc)
		{
			verbose("Unable to find the correct encryption algorithm as specified in the settings.conf file.");
		}
		catch (Database.KeySpecInvalidException exc)
		{
			verbose("The keyspec for the database is invalid.");
		}
		try
		{
			server = new Server(utils.Configuration.settings);
			Runtime.getRuntime().addShutdownHook(new ServerFinalizer(db));
			java.util.Scanner scanner = new java.util.Scanner(System.in);

			Integer port = queryPort(scanner);

			ExitListener exit_listener = new ExitListener();
			exit_listener.scanner =  scanner;
			exit_listener.db = db;
			exit_listener.start();

			while (true)
			{
				try
				{
					String message = "";
					
					message = server.waitForMessage(port);
					if (message == null)
						continue;
					java.util.ArrayList<String> message_parts = utils.Utils.splitAndUnescapeString(message);
					for (int i = 0; i < message_parts.size(); ++i)
						System.out.println(message_parts.get(i));
					if (message_parts.size() == 3)
					{
						server.respondToMessage(db.execute(message_parts.get(0), message_parts.get(1), message_parts.get(2)));
					}
					else
					{
						server.respondToMessage("Invalid: Amount of tokens do not match the desired amount of 3 tokens.");
					}
				}
				catch (PortUnavailableException exc)
				{
					System.out.println("The port you specified '" + String.valueOf(port) + "' is already in use.");
					queryPort(scanner);
				}
				catch (BlockSizeTooLargeException exc)
				{
					server.respondToMessage("You have sent a block that is too large to be accepted by the server. Largest size is: " + utils.Configuration.settings.getInt("maxblocksize"));
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

	public static class PortUnavailableException extends Error
	{
		public String message;

		public PortUnavailableException(String message)
		{
			this.message = message;
		}
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
	{
		verbose("Fetching symmetric key.");
		try
		{
			byte[] bytes = new byte[settings.getInt("keylength")];
			int code = input_from_client.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, code);
			bytes = utils.Utils.decrypt(bytes, server_private_key);
			symmetric_key = new javax.crypto.spec.SecretKeySpec(bytes, settings.get("SymmetricSpec"));
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
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
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	private void finishConnection() throws java.io.IOException
	{
		verbose("Cleaning up the connection.");
		server_socket.close();
	}

}