package client;

public class Client
{
	private static utils.Configuration settings = null;

	private static java.net.Socket 			client_socket;

	private static java.io.InputStream 		input_from_server;
	private static java.io.OutputStream 	output_to_server;
	
	private static java.security.PrivateKey client_private_key;
	private static java.security.PublicKey 	server_public_key;
	private static java.security.PublicKey  client_public_key;
	private static java.util.ArrayList<byte[]> server_public_keys = new java.util.ArrayList<>();

	private static byte[] bytes = new byte[2048];
	private static String last_message;

	private static byte[] public_server_key_separator = "==============================================".getBytes();

	public static void main(String[] args)
	{

		try
		{
			settings = utils.Configuration.loadDefaultConfiguration();

			loadTrustedServers();

			connectAndSetUpChannels();
			generatePairAndSendPublicKeyToServer();
			getPublicKeyFromServer(); // For checking whether we already have this one later.
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
			System.out.println(exc_obj);
		}
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

	private static final String    HEXES    = "0123456789ABCDEF";

	static String getHex(byte[] raw) 
	{
	    final StringBuilder hex = new StringBuilder(2 * raw.length);
	    for (final byte b : raw) 
	    {
	        hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
	    }
    	return hex.toString();
	}	

	public static void loadTrustedServers()
	{
		try
		{
			byte[] cert = utils.Utils.fileToBytes(utils.Configuration.settings.get("TrustedServers"));
			for (int i = 0; i < cert.length; ++i)
			{
				if (isContained(cert, i, public_server_key_separator))
				{
					byte[] copy = java.util.Arrays.copyOfRange(cert, 0, i);
					// System.out.println(getHex(copy));
					// System.out.println("=======================");
					server_public_keys.add(copy);
					cert = java.util.Arrays.copyOfRange(cert, i + public_server_key_separator.length, cert.length);
					i = 0;
				}
			}

		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println("What!");
		}
	}

	public static void connectAndSetUpChannels() throws java.net.UnknownHostException, java.io.IOException
	{
		client_socket = new java.net.Socket(settings.get("hostname"), Integer.parseInt(settings.get("port")));
		output_to_server = client_socket.getOutputStream();
		input_from_server = client_socket.getInputStream();
	}

	public static void getPublicKeyFromServer()
	{
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
			System.out.println(exc_obj);
		}
	}

	public static void queryWhetherItIsTrusted()
	{
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			if (java.util.Arrays.equals(server_public_keys.get(i), server_public_key.getEncoded()))
				return;
		}

		System.out.println("WARNING: The certificate presented by remote does not appear to be trusted. Do you want to add remote to the list of trusted servers?");
		server_public_keys.add(server_public_key.getEncoded());
	}

	public static boolean verifyAuthenticity() throws Exception
	{
		int l = input_from_server.read(bytes);
		bytes = java.util.Arrays.copyOf(bytes, l);
		java.security.Signature sig = java.security.Signature.getInstance("SHA1withRSA");
		sig.initVerify(server_public_key);
		sig.update(client_public_key.getEncoded());
		return sig.verify(bytes);
	}

	public static void generatePairAndSendPublicKeyToServer()
	{
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
			System.out.println(exc_obj);
		}
	}

	public static void writeMessageToServer()
	{
		java.util.Scanner sc = new java.util.Scanner(System.in);
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
			exc_obj.printStackTrace();
		}
	}
	
	public static void ensureCorrectServerResponse()
	{
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
			exc_obj.printStackTrace();
		}
	}

	public static void storeAllTrustedKeys() throws java.io.IOException
	{
		java.io.FileOutputStream trusted = new java.io.FileOutputStream(utils.Configuration.settings.get("TrustedServers"));
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			trusted.write(server_public_keys.get(i));
			trusted.write(public_server_key_separator);
		}
	}
}