package client;

import static utils.Configuration.verbose;

public class Client
{

	////////////////////////////////////////////////////////////
	// CLIENT PROGRAM ENTRY ////////////////////////////////////
	////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		ArgumentHandler arghandler = initializeConfiguration(args);

		if (arghandler.hasOption("cli"))
			commandLineInterface();
		else if (arghandler.hasOption("gui"))
			; // start gui
		else if (arghandler.hasOption("test"))
			; // Run tests
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

	public static class ClientFinalizer extends Thread
	{
		Client client;

		ClientFinalizer(Client client)
		{
			this.client = client;
		}

		public void run()
		{
			try
			{
				client.storeAllTrustedKeys();
			}
			catch (java.io.IOException exc)
			{
				verbose("Unable to store public server keys");
			}
		}
	}

	public static String getPasswordFromConsole(java.util.Scanner scanner, String message)
	{
		java.io.Console console = System.console();
		if (console == null)
		{
			System.out.println("No console found: typing is echod.\n" + message);
			return scanner.nextLine();
		}
		else
		{
			System.out.print(message);
			return new String(console.readPassword());
		}

	}

	public static String commandLineSendData(Client client, String host, Integer port, String login_info, String command, java.util.Scanner scanner) throws Exception
	{
		verbose("Sending data: '" + login_info + " " + command + "'.");
		if (client.sendData(login_info + " " + command, host, port) == false)
		{
			System.out.print("WARNING: The certificate presented by remote does not appear to be trusted.\nDo you want to add remote to the list of trusted servers? (yes/no): ");
			while (true)
			{
				String result = scanner.nextLine();
				if (result.equals("yes"))
				{
					client.addPublicServerKeyToTrusted();
					client.sendWhenTrusted(login_info + " " + command);
					java.util.ArrayList<String> answer = utils.Utils.splitAndUnescapeString(client.fetchResponse());
					System.out.println("Server response:");
					for (String string : answer)
						System.out.println(string);
					break;
				}
				else if (result.equals("no"))
					break;
				else
					System.out.print("Please enter \"yes\" or \"no\": ");
			}
		}
		else
		{
			String response = client.fetchResponse();
			verbose("Direct response: " + response);
			if (response != null)
			{
				/*
				java.util.ArrayList<String> answer = utils.Utils.splitAndUnescapeString(response);
				System.out.println("Server response:");
				for (String string : answer)
					System.out.println(string);
				*/
				return response;
			}
			else
				System.out.println("No response from server");
		}
		return null;
	}

	public static void commandLineInterface()
	{
		Client client = null;
		try
		{
			client = new Client(utils.Configuration.settings);
			Runtime.getRuntime().addShutdownHook(new ClientFinalizer(client));
			java.util.Scanner scanner = new java.util.Scanner(System.in);
			System.out.print("Enter the hostname (leave blank for default): ");
			String host = scanner.nextLine();
			Integer port = null;
			if (host.equals("") == false)
			{
				System.out.print("Enter the port (leave blank for default): ");
				String portstring = scanner.nextLine();
				if (portstring.equals(""))
				{
					port = utils.Configuration.settings.getInt("port");
				}
				else 
					port = Integer.parseInt(portstring);
			}
			else
			{
				host = utils.Configuration.settings.get("hostname");
				port = utils.Configuration.settings.getInt("port");
			}
			System.out.print("Enter your username: ");
			String login_info = utils.Utils.escapeSpaces(scanner.nextLine());
			login_info = login_info + " " + utils.Utils.escapeSpaces(getPasswordFromConsole(scanner, "Enter your password: "));

			while (scanner.hasNextLine())
			{
				String line = utils.Utils.escapeSpaces(scanner.nextLine());
				if (line.equalsIgnoreCase(utils.Configuration.settings.get("ExitCommand")))
					break;
				if (line.equalsIgnoreCase("help"))
				{
					System.out.println
					(
						".help - print this help text"
						+ "\n.register - start new user registration"
					);
				}
				else if (line.equalsIgnoreCase("register"))
				{
					System.out.print("Enter a new username: ");
					String username = scanner.nextLine();
					System.out.print("Enter a rank (positive integer): ");
					String rank = scanner.nextLine();
					System.out.print("Enter the first name: ");
					String fname = scanner.nextLine();
					System.out.print("Enter the last name: ");
					String lname = scanner.nextLine();
					String password;
					do 
					{
						password = getPasswordFromConsole(scanner, "Enter the password for the new user: ");
						String passcheck = getPasswordFromConsole(scanner, "Enter the password again: ");
						if (password.equals(passcheck) == false)
							System.out.println("Passwords do not match, retry.");
						else
							break;
					}
					while (true);
					line = 
						utils.Utils.escapeSpaces
						(
							"REGISTER "
							+ utils.Utils.escapeSpaces(username)
							+ " "
							+ utils.Utils.escapeSpaces(rank)
							+ " "
							+ utils.Utils.escapeSpaces(fname)
							+ " "
							+ utils.Utils.escapeSpaces(lname)
							+ " "
							+ utils.Utils.escapeSpaces(password)
						);
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
					if (result != null)
					{
						if (result.equals("1"))
						{
							System.out.println("Server response: 'OK: Registered new user.'");
						}
						else
						{
							System.out.println("Server response: 'ERR: User already exists.'");
						}
					}
					else
					{
						System.out.println("Server failed to respond.");
					}
				}
				else if (line.equalsIgnoreCase("new_event"))
				{
					System.out.print("Enter a description of the event: ");
					String description = scanner.nextLine();
					System.out.print("Enter a date-time of the format 'yyyy-mm-dd hh:mm:ss': ");
					String datetime = scanner.nextLine();
					line = 
						utils.Utils.escapeSpaces
						(
							"NEW_EVENT "
							+ utils.Utils.escapeSpaces(description)
							+ " "
							+ utils.Utils.escapeSpaces(datetime)
						);
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
				}
				else if (line.equalsIgnoreCase("get_events"))
				{
					line =
						utils.Utils.escapeSpaces("GET_EVENTS");
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
					int columns = Integer.parseInt(result.substring(0, result.indexOf(" ") + 1).trim());
					result = result.substring(result.indexOf(" ") + 1);
					java.util.ArrayList<String> result_set = utils.Utils.splitAndUnescapeString(result);
					java.util.ArrayList<String> final_set = new java.util.ArrayList<>();
					for (String str : result_set)
					{
						final_set.addAll(utils.Utils.splitAndUnescapeString(str));
					}

					int i = 0;
					for (String tmp : final_set)
					{
						if (i++ % 2 == 0)
							System.out.println();
						System.out.println(tmp);
					}
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
		System.out.println
		(
			"Help text for this program."
		);
	}

	////////////////////////////////////////////////////////////
	// START OF OBJECT DEPENDENT DEFINITIONS ///////////////////
	////////////////////////////////////////////////////////////

	private java.net.Socket 			client_socket;
	private java.io.InputStream 		input_from_server;
	private java.io.OutputStream 		output_to_server;
	private java.security.PrivateKey 	client_private_key;
	private java.security.PublicKey 	server_public_key;
	private java.security.PublicKey  	client_public_key;
	private java.util.ArrayList<byte[]> server_public_keys = new java.util.ArrayList<>();
	private byte[] 	bytes = null;
	private int 	length = 0;
	private String 	last_message;
	private utils.Configuration settings;
	private java.security.Key symmetric_key;

	public Client(utils.Configuration settings)
	{
		this.settings = settings;
		try
		{
			bytes = new byte[settings.getInt("keylength")];
			loadTrustedServers();
			generatePair();
			generateSymmetric();
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public boolean sendData(String data, String host, int port)
	{
		try
		{
			connectAndSetUpChannels(host, port);
			sendClientPublicKeyToServer();
			getServerPublicKeyFromServer(); // For checking whether we already have this one later.
			getCertificateFromServer(); // Aka client's encrypted public key
			if (queryWhetherItIsTrusted() == false)
				return false;
			sendSymmetricKey();
			sendWhenTrusted(data);
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
		return true;
	}

	public void sendSymmetricKey() throws java.io.IOException, Exception
	{
		verbose("Sending symmetric key.");
		output_to_server.write(utils.Utils.encrypt(symmetric_key.getEncoded(), server_public_key));
	}

	public void sendWhenTrusted(String data) throws Exception
	{
		if (verifyAuthenticity())
		{
			verbose("Server authenticated.");
			writeMessageToServer(data);
			getServerResponse();
		}
		else
		{
			verbose("Server failed to authenticate");
		}
		client_socket.close();
	}

	public String fetchResponse()
	{
		String tmp = last_message;
		last_message = null;
		return tmp;
	}

	public void addPublicServerKeyToTrusted()
	{
		server_public_keys.add(server_public_key.getEncoded());
	}

	private static boolean isContained(byte[] bigger, int index, byte[] smaller)
	{
		for (int i = 0; i < smaller.length && i + index < bigger.length; ++i)
		{
			if (bigger[i + index] != smaller[i])
				return false;
		}
		return true;
	}

	private void loadTrustedServers()
	{
		verbose("Loading trusted servers into memory.");
		try
		{
			byte[] cert = utils.Utils.fileToBytes(utils.Configuration.settings.get("TrustedServers"));
			for (int i = 0; i < cert.length; ++i)
			{
				if (isContained(cert, i, settings.get("PublicKeySeparator").getBytes()))
				{
					byte[] copy = java.util.Arrays.copyOfRange(cert, 0, i);
					server_public_keys.add(copy);
					cert = java.util.Arrays.copyOfRange(cert, i + settings.get("PublicKeySeparator").getBytes().length, cert.length);
					i = 0;
				}
			}

		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void connectAndSetUpChannels(String host, int port) throws java.net.UnknownHostException, java.io.IOException
	{
		verbose("Connecting to foreign host.");
		client_socket = new java.net.Socket(host, port);
		client_socket.setSoTimeout(settings.getInt("SocketTimeOut"));
		output_to_server = client_socket.getOutputStream();
		input_from_server = client_socket.getInputStream();
	}


	private void getServerPublicKeyFromServer()
	{
		verbose("Waiting for host public key.");
		bytes = new byte[settings.getInt("keylength")];
		try
		{
			int number = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, number);
			server_public_key = utils.Utils.bytesToPublicKey(bytes);
		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void getCertificateFromServer() throws java.io.IOException
	{
		length = input_from_server.read(bytes);
	}

	private boolean queryWhetherItIsTrusted() 
	{
		verbose("Testing whether the key is trusted.");
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			if (java.util.Arrays.equals(server_public_keys.get(i), server_public_key.getEncoded()))
				return true;
		}

		return false;
	}

	private boolean verifyAuthenticity() throws Exception
	{
		verbose("Verifying authenticity.");
		bytes = java.util.Arrays.copyOf(bytes, length);
		java.security.Signature sig = java.security.Signature.getInstance(utils.Configuration.settings.get("SignMethod"));
		sig.initVerify(server_public_key);
		sig.update(client_public_key.getEncoded());
		return sig.verify(bytes);
		
	}

	private void generatePair()
	{
		verbose("Generating keypair.");
		java.security.KeyPair pair = utils.Utils.getNewKeyPair();
		client_private_key = pair.getPrivate();
		client_public_key = pair.getPublic();
	}

	private void generateSymmetric()
	{
		verbose("Generating symmetric key.");
		try
		{
			symmetric_key = utils.Utils.generateSymmetricKey(settings.get("SymmetricSpec"));
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	private void sendClientPublicKeyToServer() throws java.io.IOException
	{
		output_to_server.write(client_public_key.getEncoded());
	}

	private void writeMessageToServer(String data)
	{
		verbose("Sending packets to the server...");
		try
		{
			output_to_server.write(utils.Utils.encryptSymmetric(data.getBytes(), symmetric_key, settings.get("SymmetricCipher")));
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void getServerResponse()
	{
		verbose("Retrieving server response.");
		try
		{
			bytes = new byte[settings.getInt("keylength")];
			int length = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, length);
			last_message = new String(utils.Utils.decryptSymmetric(bytes, symmetric_key, settings.get("SymmetricCipher")));
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void storeAllTrustedKeys() throws java.io.IOException
	{
		verbose("Storing the trusted keys in \"" + settings.get("TrustedServers") + "\".");
		java.io.FileOutputStream trusted = new java.io.FileOutputStream(settings.get("TrustedServers"));
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			trusted.write(server_public_keys.get(i));
			trusted.write(settings.get("PublicKeySeparator").getBytes());
		}
	}
}