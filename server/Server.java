package server;

public class Server
{
	public static void main(String[] args)
	{
		System.out.println("The server is running!");
		
		try
		{
			java.net.ServerSocket server_socket = new java.net.ServerSocket(7777);
			System.out.println("Waiting for acceptance");
			java.net.Socket client_socket = server_socket.accept();
			System.out.println("Got acceptance");
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
	}
}