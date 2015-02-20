package client;

public class Client
{
	public static void main(String[] args)
	{
		try
		{
			java.net.Socket client_socket = new java.net.Socket("localhost", 7777);
			java.io.OutputStream out_stream = client_socket.getOutputStream();
			java.util.Scanner sc = new java.util.Scanner(System.in);
			String write = sc.nextLine();
			out_stream.write(write.getBytes());
			out_stream.flush();
		}
		catch (java.io.IOException exc_obj)
		{
			System.out.println(exc_obj);
		}
	}
}