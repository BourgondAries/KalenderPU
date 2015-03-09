package client;

public class ServerReturnData
{
	private java.util.ArrayList<String> final_set = new java.util.ArrayList<>();
	private Integer columns = 0;

	public static class InvalidInputException extends Exception { InvalidInputException() {} InvalidInputException(Throwable exc) { super(exc); } }

	public ServerReturnData()
	{}

	public ServerReturnData(String input) throws InvalidInputException
	{
		parseInput(input);
	}

	public void parseInput(String input) throws InvalidInputException
	{
		if (input == null || input.length() == 0)
			throw new InvalidInputException();
		int location = input.indexOf(" ");
		try
		{
			columns = Integer.parseInt(input.substring(0, location == -1 ? input.length() : location).trim());
		}
		catch (java.lang.NumberFormatException exc)
		{
			throw new InvalidInputException(exc);
		}
		input = input.substring(input.indexOf(" ") + 1);
		java.util.ArrayList<String> result_set = utils.Utils.splitAndUnescapeString(input);
		final_set = new java.util.ArrayList<>();
		for (String str : result_set)
		{
			final_set.addAll(utils.Utils.splitAndUnescapeString(str));
		}
	}

	public static String getPrettyStringWithoutObject(String input) throws InvalidInputException
	{
		return (new ServerReturnData(input)).getPrettyStringRepresentation();
	}

	public String getPrettyStringRepresentation()
	{
		int i = 0;
		StringBuilder string_builder = new StringBuilder();
		for (String tmp : final_set)
		{
			if (++i > columns)
			{
				string_builder.append('\n');
				i = 1;
			}
			string_builder.append("'" + tmp + "', ");
		}
		return string_builder.toString().trim();
	}
}