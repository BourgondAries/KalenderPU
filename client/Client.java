package client;

public class Client
{
	private static utils.Configuration settings = null;

	private static java.net.Socket 			client_socket;

	private static java.io.InputStream 		input_from_server;
	private static java.io.OutputStream 	output_to_server;
	
	private static java.security.PrivateKey client_private_key;
	private static java.security.PublicKey 	server_public_key;

	private static byte[] bytes;
	private static String last_message;

	public static void main(String[] args)
	{
		try
		{
			settings = utils.Configuration.loadDefaultConfiguration();

			connectAndSetUpChannels();
			getPublicKeyFromServer();
			generatePairAndSendPublicKeyToServer();
			writeMessageToServer();
			ensureCorrectServerResponse();
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
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
			System.out.println(new String(bytes));
			try
			{
				java.security.spec.X509EncodedKeySpec pubkey_spec = new java.security.spec.X509EncodedKeySpec(bytes);
				java.security.KeyFactory key_factory = java.security.KeyFactory.getInstance(settings.get("keypairgen"));
				server_public_key = key_factory.generatePublic(pubkey_spec);
			}
			catch (java.security.NoSuchAlgorithmException exc_obj)
			{
				System.out.println(exc_obj);
			}
			catch (java.security.spec.InvalidKeySpecException exc_obj)
			{
				System.out.println(exc_obj);
			}
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
	}

	public static void generatePairAndSendPublicKeyToServer()
	{
		try
		{
			java.security.KeyPair pair = utils.Utils.getNewKeyPair();
			client_private_key = pair.getPrivate();
			java.security.PublicKey public_key = pair.getPublic();
			output_to_server.write(public_key.getEncoded());
			System.out.println(new String(public_key.getEncoded()));
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
			output_to_server.write(utils.Utils.encrypt(write.getBytes(), server_public_key));
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
}