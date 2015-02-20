package server;

public class Server
{
	private static utils.Configuration settings = null;
	public static void main(String[] args)
	{
		System.out.println("The server is running!");
		try
		{
			settings = utils.Configuration.loadDefaultConfiguration();

			while (true)
			{
				// init
				java.net.ServerSocket server_socket = new java.net.ServerSocket(Integer.parseInt(settings.get("port")));
				System.out.println("Waiting for acceptance");

				java.net.Socket client_socket = server_socket.accept();
				System.out.println("Got acceptance");

				java.io.InputStream input_from_client = client_socket.getInputStream();
				java.io.OutputStream output_to_client = client_socket.getOutputStream();

				java.security.PrivateKey private_key = sendPublicKeyToClient(output_to_client);

				byte[] bytes = new byte[1000];
				int code = input_from_client.read(bytes);
				bytes = java.util.Arrays.copyOf(bytes, code);
				try
				{
					System.out.println(new String(bytes));
					System.out.println(new String(decrypt(bytes, private_key, settings.get("xform"))));
				}
				catch (Exception exc_obj)
				{
					exc_obj.printStackTrace();
				}
				System.out.println("read: " + code + " bytes");
				server_socket.close();
			}		
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
		
	}

	public static java.security.KeyPair getNewKeyPair()
	{
		try
		{
			java.security.KeyPairGenerator keygen = java.security.KeyPairGenerator.getInstance(settings.get("keypairgen"));
			java.security.SecureRandom random = java.security.SecureRandom.getInstance(settings.get("SecureRandomRNG"), settings.get("SecureRandomProvider"));
			keygen.initialize(Integer.parseInt(settings.get("keylength")), random);
			return keygen.generateKeyPair();
		}
		catch (java.security.NoSuchAlgorithmException exc_obj)
		{
			System.out.println(exc_obj);
		}
		catch (java.security.NoSuchProviderException exc_obj)
		{
			System.out.println(exc_obj);
		}
		return null;
	}

	public static java.security.PrivateKey sendPublicKeyToClient(java.io.OutputStream output_to_client)
	{
		try
		{
			java.security.KeyPair pair = getNewKeyPair();
			java.security.PrivateKey private_key = pair.getPrivate();
			java.security.PublicKey public_key = pair.getPublic();
			output_to_client.write(public_key.getEncoded());
			System.out.println(new String(public_key.getEncoded()));
			return private_key;
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
		return null;
	}

	private static byte[] decrypt(byte[] inpBytes, java.security.PrivateKey key, String xform) throws Exception
	{
	    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(xform);
	    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
	    return cipher.doFinal(inpBytes);
	}
}