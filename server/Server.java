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