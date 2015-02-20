package server;

public class Server
{
	private static utils.Configuration settings = null;

	private static java.net.ServerSocket server_socket;
	private static java.net.Socket client_socket;
	private static java.io.InputStream input_from_client;
	private static java.io.OutputStream output_to_client;
	private static java.security.PrivateKey server_private_key;
	private static java.security.PublicKey client_public_key;
	private static byte[] bytes;
	private static String last_message;

	public static void main(String[] args)
	{
		System.out.println("The server is running!");
		while (true)
		{
			try
			{
				settings = utils.Configuration.loadDefaultConfiguration();

				while (true)
				{
					waitForIncomingConnection();
					setup2WayCommunicationChannels();
					sendPublicKeyToClient();
					getPublicKeyFromClient();
					readIncomingbytes();
					handleLastMessage();
					finishConnection();
				}		
			}
			catch (java.net.BindException exc_obj) { try { finishConnection(); } catch (java.io.IOException exc_object) { System.out.println("Unable to unbind"); } }
			catch (java.net.SocketException exc_obj) { System.out.println(exc_obj); }
			catch (Exception exc_obj) { System.out.println(exc_obj); }
		}
	}

	public static void waitForIncomingConnection() throws java.io.IOException
	{
		server_socket = new java.net.ServerSocket(Integer.parseInt(settings.get("port")));
		System.out.println("Waiting for a response");
		client_socket = server_socket.accept();
		System.out.println("Got acceptance");
	}

	public static void setup2WayCommunicationChannels() throws java.io.IOException
	{
		input_from_client = client_socket.getInputStream();
		output_to_client = client_socket.getOutputStream();
	}
	
	public static java.security.PrivateKey sendPublicKeyToClient()
	{
		try
		{
			java.security.KeyPair pair = utils.Utils.getNewKeyPair();
			server_private_key = pair.getPrivate();
			java.security.PublicKey public_key = pair.getPublic();
			output_to_client.write(public_key.getEncoded());
			System.out.println(new String(public_key.getEncoded()));
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
		return null;
	}

	public static void getPublicKeyFromClient()
	{
		byte[] bytes = new byte[Integer.parseInt(settings.get("keylength"))];
		try
		{
			int number = input_from_client.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, number);
			System.out.println(new String(bytes));
			try
			{
				java.security.spec.X509EncodedKeySpec pubkey_spec = new java.security.spec.X509EncodedKeySpec(bytes);
				java.security.KeyFactory key_factory = java.security.KeyFactory.getInstance(settings.get("keypairgen"));
				client_public_key = key_factory.generatePublic(pubkey_spec);
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

	public static void readIncomingbytes() throws java.io.IOException
	{
		bytes = new byte[1024];
		int code = input_from_client.read(bytes);
		bytes = java.util.Arrays.copyOf(bytes, code);
		try
		{
			// System.out.println(new String(bytes));
			last_message = new String(utils.Utils.decrypt(bytes, server_private_key));
			System.out.println(last_message);
		}
		catch (Exception exc_obj)
		{
			exc_obj.printStackTrace();
		}
	}

	public static void handleLastMessage() throws java.io.IOException, Exception
	{
		// Need to check the validity: check if the grammar is correct.
		// If correct, apply the update. Let the client know that it was succesful.
		boolean success = true;
		if (success)
			notifyToClientOperationSuccess();
	}

	public static void notifyToClientOperationSuccess() throws java.io.IOException, Exception
	{
		output_to_client.write(utils.Utils.encrypt("success".getBytes(), client_public_key));
	}

	public static void finishConnection() throws java.io.IOException
	{
		server_socket.close();
	}


}