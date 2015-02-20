package client;

public class Client
{
	public static void main(String[] args)
	{
		try
		{
			java.net.Socket client_socket = new java.net.Socket("localhost", 7777);
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
	}
}