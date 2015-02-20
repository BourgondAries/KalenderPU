package server;

public class Server
{
	public static void main(String[] args)
	{
		System.out.println("The server is running!");
		try
		{
			while (true)
			{
				// init
				javax.net.ServerSocketFactory socket_factory = javax.net.ssl.SSLServerSocketFactory.getDefault();
				java.net.ServerSocket ssl_socket = socket_factory.createServerSocket(7777);
				try
				{
					java.security.KeyPairGenerator keygen = java.security.KeyPairGenerator.getInstance("DSA", "SUN");
					java.security.SecureRandom random = java.security.SecureRandom.getInstance("SHA1PRNG", "SUN");
					keygen.initialize (2048, random);
					java.security.KeyPair pair = keygen.generateKeyPair();
					java.security.PrivateKey private_key = pair.getPrivate();
					java.security.PublicKey public_key = pair.getPublic();
					
					System.out.println(public_key.getEncoded());
				}
				catch (java.security.NoSuchAlgorithmException exc_obj)
				{
					System.out.println(exc_obj);
				}
				catch (java.security.NoSuchProviderException exc_obj)
				{
					System.out.println(exc_obj);
				}
				java.net.ServerSocket server_socket = new java.net.ServerSocket(7777);
				System.out.println("Waiting for acceptance");
				java.net.Socket client_socket = server_socket.accept();
				System.out.println("Got acceptance");
				java.io.InputStream input_from_client = client_socket.getInputStream();
				java.io.OutputStream output_to_client = client_socket.getOutputStream();
				byte[] bytes = new byte[1000];
				int code = input_from_client.read(bytes);
				bytes = java.util.Arrays.copyOf(bytes, code);
				System.out.println(new String(bytes));
				System.out.println("read: " + code + " bytes");
				server_socket.close();
			}		
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
	}
}