package client;

import static utils.Configuration.verbose;

public class Client
{

	////////////////////////////////////////////////////////////
	// CLIENT PROGRAM ENTRY ////////////////////////////////////
	////////////////////////////////////////////////////////////

	private static byte[] public_server_key_separator = null;
	private static utils.Configuration settings = null;

	public static void main(String[] args)
	{
		initializeConfiguration(args);
	}

	public static void initializeConfiguration(String[] args)
	{
		try { settings = utils.Configuration.loadDefaultConfiguration(); }
		catch ( java.io.IOException ioexc ) { System.out.println("Unable to load configuration data: " + ioexc); System.exit(1); }
		public_server_key_separator = settings.get("PublicKeySeparator").getBytes();

		ArgumentHandler argument_handler = new ArgumentHandler(args);
		if (argument_handler.print_help)
		{
			printHelp();
			System.exit(0);
		}
		utils.Configuration.verbose_mode = argument_handler.is_verbose;
		if (argument_handler.hasOption("cli"))
			commandLineInterface();
	}

	public static void printHelp()
	{
		System.out.println
		(
			"Help text for this program."
		);
	}

	public static void commandLineInterface()
	{
		Client client = null;
		try
		{
			client = new Client();
			java.util.Scanner scanner = new java.util.Scanner(System.in);
			System.out.print("Enter your username: ");
			String login_info = scanner.nextLine();
			System.out.print("Enter your password: ");
			java.io.Console console = System.console();
			login_info = login_info + " " + new String(console.readPassword());


			while (scanner.hasNextLine())
			{
				String line = scanner.nextLine();
				if (line.equalsIgnoreCase(settings.get("ExitCommand")))
					break;
				if (client.sendData(login_info + " " + line) == false)
				{
					System.out.print("WARNING: The certificate presented by remote does not appear to be trusted.\nDo you want to add remote to the list of trusted servers? (yes/no): ");
					while (true)
					{
						String result = scanner.nextLine();
						if (result.equals("yes"))
						{
							client.addPublicServerKeyToTrusted();
							client.sendWhenTrusted(login_info + " " + line);
							break;
						}
						else if (result.equals("no"))
							break;
						else
							System.out.print("Please enter \"yes\" or \"no\": ");
					}
				}
			}
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
		finally
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

	public Client()
	{
		try
		{
			bytes = new byte[settings.getInt("keylength")];
			loadTrustedServers();
			generatePair();
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public boolean sendData(String data)
	{
		try
		{
			connectAndSetUpChannels();
			sendClientPublicKeyToServer();
			getServerPublicKeyFromServer(); // For checking whether we already have this one later.
			getCertificateFromServer(); // Aka client's encrypted public key
			if (queryWhetherItIsTrusted() == false)
				return false;
			sendWhenTrusted(data);
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
		return true;
	}

	public void sendWhenTrusted(String data) throws Exception
	{
		if (verifyAuthenticity())
		{
			verbose("Server authenticated!");
			writeMessageToServer(data);
			getServerResponse();
		}
		else
		{
			verbose("Server NOT authenticated!");
		}
		client_socket.close();
	}

	public static boolean isContained(byte[] bigger, int index, byte[] smaller)
	{
		for (int i = 0; i < smaller.length && i + index < bigger.length; ++i)
		{
			if (bigger[i + index] != smaller[i])
				return false;
		}
		return true;
	}

	public void loadTrustedServers()
	{
		verbose("Loading trusted servers into memory.");
		try
		{
			byte[] cert = utils.Utils.fileToBytes(utils.Configuration.settings.get("TrustedServers"));
			for (int i = 0; i < cert.length; ++i)
			{
				if (isContained(cert, i, public_server_key_separator))
				{
					byte[] copy = java.util.Arrays.copyOfRange(cert, 0, i);
					server_public_keys.add(copy);
					cert = java.util.Arrays.copyOfRange(cert, i + public_server_key_separator.length, cert.length);
					i = 0;
				}
			}

		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void connectAndSetUpChannels() throws java.net.UnknownHostException, java.io.IOException
	{
		verbose("Connecting to foreign host.");
		client_socket = new java.net.Socket(settings.get("hostname"), settings.getInt("port"));
		output_to_server = client_socket.getOutputStream();
		input_from_server = client_socket.getInputStream();
	}

	public void getServerPublicKeyFromServer()
	{
		verbose("Waiting for host public key.");
		bytes = new byte[settings.getInt("keylength")];
		try
		{
			int number = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, number);
			// System.out.println(new String(bytes));
			server_public_key = utils.Utils.bytesToPublicKey(bytes);
		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void getCertificateFromServer() throws java.io.IOException
	{
		length = input_from_server.read(bytes);
	}

	public boolean queryWhetherItIsTrusted() 
	{
		verbose("Testing whether the key is trusted.");
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			if (java.util.Arrays.equals(server_public_keys.get(i), server_public_key.getEncoded()))
				return true;
		}

		return false;
	}

	public void addPublicServerKeyToTrusted()
	{
		server_public_keys.add(server_public_key.getEncoded());
	}

	public boolean verifyAuthenticity() throws Exception
	{
		verbose("Verifying authenticity.");
		bytes = java.util.Arrays.copyOf(bytes, length);
		java.security.Signature sig = java.security.Signature.getInstance(utils.Configuration.settings.get("SignMethod"));
		sig.initVerify(server_public_key);
		sig.update(client_public_key.getEncoded());
		return sig.verify(bytes);
		
	}

	public void generatePair()
	{
		verbose("Generating keypair.");
		java.security.KeyPair pair = utils.Utils.getNewKeyPair();
		client_private_key = pair.getPrivate();
		client_public_key = pair.getPublic();
	}

	public void sendClientPublicKeyToServer() throws java.io.IOException
	{
		output_to_server.write(client_public_key.getEncoded());
	}

	public void writeMessageToServer(String data)
	{
		verbose("Sending packets to the server...");
		try
		{
			bytes = utils.Utils.encrypt(data.getBytes(), server_public_key);
			output_to_server.write(bytes);
			output_to_server.flush();
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void getServerResponse()
	{
		verbose("Verifying server integrity.");
		try
		{
			bytes = new byte[settings.getInt("keylength")];
			int length = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, length);
			last_message = new String(utils.Utils.decrypt(bytes, client_private_key));
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void storeAllTrustedKeys() throws java.io.IOException
	{
		verbose("Storing the trusted keys in \"" + settings.get("TrustedServers") + "\"");
		java.io.FileOutputStream trusted = new java.io.FileOutputStream(settings.get("TrustedServers"));
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			trusted.write(server_public_keys.get(i));
			trusted.write(public_server_key_separator);
		}
	}
}