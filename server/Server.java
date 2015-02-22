package server;

import static utils.Configuration.verbose;

public class Server
{

	private static String db_url = "jdbc:derby:database";
	private static java.sql.Connection conn = null;
    private static java.sql.Statement stmt = null;

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

		// createConnection();
		// conn = java.sql.DriverManager.getConnection("jdbc:derby:derbyDB;shutdown=true");
		// conn.close();
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

	private static void createConnection()
    {
        try
        {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
            conn = java.sql.DriverManager.getConnection(db_url);

            java.sql.PreparedStatement prepstmt = conn.prepareStatement("SELECT * FROM SystemUser WHERE username=?");
			prepstmt.setString(1, "root");
			java.sql.ResultSet result = prepstmt.executeQuery();
			if (result.next() == false)
			{
	        	java.sql.PreparedStatement statement 
					= conn.prepareStatement
						(
							"INSERT INTO SystemUser (rank, username, fname, lname, hashedPW) VALUES ("
							+ "0, 'root', '', '', '" + PasswordHash.createHash("root") + "')"
							, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY
						);
				statement.execute();
			}
        }
        catch (Exception except)
        {
            except.printStackTrace();
        }
    }

    public static class ServerFinalizer extends Thread
	{
		Server server;

		ServerFinalizer(Server server)
		{
			this.server = server;
		}

		public void run()
		{
			/*try
			{*/
				verbose("UNIMPLEMENTED Closing database gracefully.");
				
			/*}
			catch (java.io.IOException exc)
			{
				verbose("Unable to code database.");
			}*/
		}
	}

	public static void commandLineInterface()
	{
		Server server = null;
		try
		{
			server = new Server(utils.Configuration.settings);
			Runtime.getRuntime().addShutdownHook(new ServerFinalizer(server));
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
				for (String ins : message_parts)
					System.out.println(ins);
				server.respondToMessage("OK");
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

	public void respondToMessage(String string)
	{
		try
		{
			output_to_client.write(utils.Utils.encrypt(utils.Utils.escapeSpaces(string).getBytes(), client_public_key));
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
			last_message = new String(utils.Utils.decrypt(bytes, server_private_key));
			System.out.println(">" + last_message);
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void handleLastMessage() throws java.io.IOException, Exception
	{
		verbose("Delegating input to the input handler.");

		java.util.ArrayList<String> client_input = utils.Utils.splitAndUnescapeString(last_message);
		verbose("User: " + client_input.get(0));
		verbose("Password: " + client_input.get(1));
		verbose("Message: " + client_input.get(2));

		java.sql.PreparedStatement prepstmt = conn.prepareStatement("SELECT hashedPW FROM SystemUser WHERE username=?");
		prepstmt.setString(1, client_input.get(0));
		java.sql.ResultSet result = prepstmt.executeQuery();
		if (result.next())
		{
			if (PasswordHash.validatePassword(client_input.get(1), result.getString(1)))
				System.out.println("USER VALIDATED!");
			else 
				System.out.println("INVALID PASS");
		}
		else
		{
			// USER NOT FOUND!
		}
		/*
		System.out.println("Executing query " + last_message);
		try
		{
			if 
			(
				last_message.startsWith("UPDATE") 
				|| last_message.startsWith("INSERT")
				|| last_message.startsWith("EXECUTE")
				|| last_message.startsWith("INSERT")
			)
			{
				java.sql.PreparedStatement statement = conn.prepareStatement(last_message, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
				statement.execute();
			}
			else
			{
				stmt = conn.createStatement(); //(last_message, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
				java.sql.ResultSet result = stmt.executeQuery(last_message);
				while (result.next())
					System.out.println("'" + result.getInt(1) + ", " + result.getString(2) + "'");	
			}
		}
		catch (Exception exc)
		{
			System.out.println("An exception ocurred during execution: " + exc.toString());
		}
		*/
	}


	private void finishConnection() throws java.io.IOException
	{
		verbose("Cleaning up the connection.");
		server_socket.close();
	}

}