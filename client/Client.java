package client;

public class Client
{
	private static utils.Configuration settings = null;
	public static void main(String[] args)
	{

		try
		{
			settings = utils.Configuration.loadDefaultConfiguration();

			java.net.Socket client_socket = new java.net.Socket(settings.get("hostname"), Integer.parseInt(settings.get("port")));
			java.io.OutputStream out_stream = client_socket.getOutputStream();
			java.io.InputStream in_stream = client_socket.getInputStream();

			java.security.PublicKey public_key = getPublicKeyFromServer(in_stream);

			java.util.Scanner sc = new java.util.Scanner(System.in);
			String write = sc.nextLine();
			try
			{
				out_stream.write(encrypt(write.getBytes(), public_key, settings.get("xform")));
			}
			catch (Exception exc_obj)
			{
				exc_obj.printStackTrace();
			}
			out_stream.flush();
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
	}

	public static java.security.PublicKey getPublicKeyFromServer(java.io.InputStream in_stream)
	{
		byte[] bytes = new byte[Integer.parseInt(settings.get("keylength"))];
		try
		{
			int number = in_stream.read(bytes);
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

	private static byte[] encrypt(byte[] inpBytes, java.security.PublicKey key, String xform) throws Exception
	{
	    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(xform);
	    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
	    return cipher.doFinal(inpBytes);
	}
}