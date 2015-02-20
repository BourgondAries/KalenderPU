package client;

public class Client
{
	private static utils.Configuration settings = null;
	static java.io.OutputStream output_to_server;
	static java.io.InputStream input_from_server;
	static java.security.PrivateKey private_key;

	public static void main(String[] args)
	{
		try
		{
			settings = utils.Configuration.loadDefaultConfiguration();

			java.net.Socket client_socket = new java.net.Socket(settings.get("hostname"), Integer.parseInt(settings.get("port")));
			output_to_server = client_socket.getOutputStream();
			input_from_server = client_socket.getInputStream();

			java.security.PublicKey public_key = getPublicKeyFromServer();

			java.util.Scanner sc = new java.util.Scanner(System.in);
			String write = sc.nextLine();
			try
			{
				output_to_server.write(utils.Utils.encrypt(write.getBytes(), public_key, settings.get("xform")));
			}
			catch (Exception exc_obj)
			{
				exc_obj.printStackTrace();
			}
			output_to_server.flush();
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
	}

	public static java.security.PublicKey getPublicKeyFromServer()
	{
		byte[] bytes = new byte[Integer.parseInt(settings.get("keylength"))];
		try
		{
			int number = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, number);
			System.out.println(new String(bytes));
			try
			{
				java.security.spec.X509EncodedKeySpec pubkey_spec = new java.security.spec.X509EncodedKeySpec(bytes);
				java.security.KeyFactory key_factory = java.security.KeyFactory.getInstance(settings.get("keypairgen"));
				return key_factory.generatePublic(pubkey_spec);
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
		return null;
	}

	public static java.security.PrivateKey sendPublicKeyToServer()
	{
		try
		{
			java.security.KeyPair pair = utils.Utils.getNewKeyPair();
			private_key = pair.getPrivate();
			java.security.PublicKey public_key = pair.getPublic();
			output_to_server.write(public_key.getEncoded());
			System.out.println(new String(public_key.getEncoded()));
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
		return null;
	}
	
}