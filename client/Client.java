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
			Cli.commandLineInterface();
		else if (arghandler.hasOption("gui"))
			useGuiInterface();
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

	public static void useGuiInterface()
	{
		Client client = new Client(utils.Configuration.settings);
		Runtime.getRuntime().addShutdownHook(new ClientFinalizer(client));
		Gui gui = new Gui();
		gui.begin(client);
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
			bytes = new byte[settings.getInt("blocklength")];
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
				if (isContained(cert, i, settings.get("PublicKeySeparator").getBytes("UTF-8")))
				{
					byte[] copy = java.util.Arrays.copyOfRange(cert, 0, i);
					server_public_keys.add(copy);
					cert = java.util.Arrays.copyOfRange(cert, i + settings.get("PublicKeySeparator").getBytes("UTF-8").length, cert.length);
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
		bytes = new byte[settings.getInt("blocklength")];
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
			output_to_server.write(utils.Utils.encryptSymmetric(data.getBytes("UTF-8"), symmetric_key, settings.get("SymmetricCipher")));
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
			int length = 0;
			bytes = new byte[0];
			byte[] temporary = new byte[settings.getInt("blocklength")];
			do
			{
				length = input_from_server.read(temporary);
				temporary = java.util.Arrays.copyOf(temporary, length);
				byte[] total = new byte[bytes.length + temporary.length];
				System.arraycopy(bytes, 0, total, 0, bytes.length);
				System.arraycopy(temporary, 0, total, bytes.length, temporary.length);
				bytes = total;
				System.out.println(length);
			}
			while (length != -1 && length == settings.getInt("blocklength"));
			last_message = new String(utils.Utils.decryptSymmetric(bytes, symmetric_key, settings.get("SymmetricCipher")));
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void storeAllTrustedKeys() throws java.io.IOException
	{
		verbose("Storing the trusted keys in '" + settings.get("TrustedServers") + "'.");
		java.io.FileOutputStream trusted = new java.io.FileOutputStream(settings.get("TrustedServers"));
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			trusted.write(server_public_keys.get(i));
			trusted.write(settings.get("PublicKeySeparator").getBytes("UTF-8"));
		}
	}
}