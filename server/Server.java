package server;

public class Server
{
	private static utils.Configuration settings = null;

	static java.net.ServerSocket server_socket;
	static java.net.Socket client_socket;
	static java.io.InputStream input_from_client;
	static java.io.OutputStream output_to_client;
	static java.security.PrivateKey private_key;
	static byte[] bytes;
	static String last_message;

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

	public static void readIncomingbytes() throws java.io.IOException
	{
		bytes = new byte[1024];
		int code = input_from_client.read(bytes);
		bytes = java.util.Arrays.copyOf(bytes, code);
		try
		{
			// System.out.println(new String(bytes));
			last_message = new String(utils.Utils.decrypt(bytes, private_key, settings.get("xform")));
			System.out.println(last_message);
		}
		catch (Exception exc_obj)
		{
			exc_obj.printStackTrace();
		}
	}

	public static void handleLastMessage()
	{
		// Need to check the validity: check if the grammar is correct.
		// If correct, apply the update. Let the client know that it was succesful.
		boolean success = true;
		if (success)
			notifyToClientOperationSuccess();
	}

	public static void finishConnection() throws java.io.IOException
	{
		server_socket.close();
	}

	public static void notifyToClientOperationSuccess()
	{

	}

	public static java.security.PrivateKey sendPublicKeyToClient()
	{
		try
		{
			java.security.KeyPair pair = utils.Utils.getNewKeyPair();
			private_key = pair.getPrivate();
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
}