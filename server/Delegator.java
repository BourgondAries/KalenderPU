package server;

public class Delegator
{
	private java.sql.Connection db_connection;
	private String[] input;

	public Delegator(String input_data, java.sql.Connection dbconnection)
	{
		input = input_data.split(" ");
		db_connection = dbconnection;
	}

	public String execute()
	{
		switch (input[0])
		{
			case "GET":
				get();
				break;
			case "UPDATE":
				update();
				break;
			default:
				break;
		}
		return null;
	}

	public String get()
	{
		return null;
	}

	public String update()
	{
		return null;
	}

}