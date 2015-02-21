package client;

import static utils.Configuration.verbose;

public class Client
{
	private utils.Configuration settings = null;

	private java.net.Socket 			client_socket;

	private java.io.InputStream 		input_from_server;
	private java.io.OutputStream 	output_to_server;
	
	private java.security.PrivateKey client_private_key;
	private java.security.PublicKey 	server_public_key;
	private java.security.PublicKey  client_public_key;
	private java.util.ArrayList<byte[]> server_public_keys = new java.util.ArrayList<>();

	private byte[] bytes = null;
	private int 	  length = 0;
	private String last_message;

	private static byte[] public_server_key_separator = "#======================|======================#".getBytes();

	private java.util.Scanner sc = new java.util.Scanner(System.in);

	private ArgumentHandler argument_handler = null;

	public static void main(String[] args)
	{
		while (true)
		{
			new Client(args);
		}
	}

	public Client(String[] args)
	{
		argument_handler = new ArgumentHandler(args);
		if (argument_handler.print_help)
		{
			printHelp();
			System.exit(0);
		}
		utils.Configuration.verbose_mode = argument_handler.is_verbose;

		try
		{
			settings = utils.Configuration.loadDefaultConfiguration();
			bytes = new byte[Integer.parseInt(settings.get("keylength"))];
			loadTrustedServers();
			connectAndSetUpChannels();
			generatePairAndSendPublicKeyToServer();
			getPublicKeyFromServer(); // For checking whether we already have this one later.
			getCertificateFromServer();
			queryWhetherItIsTrusted();
			if (verifyAuthenticity())
			{
				System.out.println("Server authenticated!");
				writeMessageToServer();
				ensureCorrectServerResponse();
			}
			else
			{
				System.out.println("Server NOT authenticated!");
			}
			storeAllTrustedKeys();
			
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public static void printHelp()
	{
		System.out.println("Help text for this program.");
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
		client_socket = new java.net.Socket(settings.get("hostname"), Integer.parseInt(settings.get("port")));
		output_to_server = client_socket.getOutputStream();
		input_from_server = client_socket.getInputStream();
	}

	public void getPublicKeyFromServer()
	{
		verbose("Waiting for host public key.");
		bytes = new byte[Integer.parseInt(settings.get("keylength"))];
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

	public void queryWhetherItIsTrusted() 
	{
		verbose("Testing whether the key is trusted.");
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			if (java.util.Arrays.equals(server_public_keys.get(i), server_public_key.getEncoded()))
				return;
		}

		System.out.print("WARNING: The certificate presented by remote does not appear to be trusted.\nDo you want to add remote to the list of trusted servers? (yes/no): ");
		
		while (true)
		{
			String result = sc.next();
			if (result.equals("yes"))
			{
				server_public_keys.add(server_public_key.getEncoded());
				break;
			}
			else if (result.equals("no"))
				break;
			else
				System.out.print("Please enter \"yes\" or \"no\": ");
		}
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

	public void generatePairAndSendPublicKeyToServer()
	{
		verbose("Generating keypair to send to the remote.");
		try
		{
			java.security.KeyPair pair = utils.Utils.getNewKeyPair();
			client_private_key = pair.getPrivate();
			client_public_key = pair.getPublic();
			output_to_server.write(client_public_key.getEncoded());
			// System.out.println(new String(client_public_key.getEncoded()));
		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void writeMessageToServer()
	{
		verbose("Sending packets to the server...");
		String write = sc.nextLine();
		try
		{
			bytes = utils.Utils.encrypt(write.getBytes(), server_public_key);
			//System.out.println(new String(bytes));
			output_to_server.write(bytes);
			output_to_server.flush();
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}
	
	public void ensureCorrectServerResponse()
	{
		verbose("Verifying server integrity.");
		try
		{
			bytes = new byte[1024];
			int length = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, length);
			// System.out.println(new String(bytes));
			last_message = new String(utils.Utils.decrypt(bytes, client_private_key));
			System.out.println(last_message);
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public void storeAllTrustedKeys() throws java.io.IOException
	{
		verbose("Storing the trusted keys in \"" + utils.Configuration.settings.get("TrustedServers") + "\"");
		java.io.FileOutputStream trusted = new java.io.FileOutputStream(utils.Configuration.settings.get("TrustedServers"));
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			trusted.write(server_public_keys.get(i));
			trusted.write(public_server_key_separator);
		}
	}
}