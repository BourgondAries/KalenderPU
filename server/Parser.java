package server;

public class Parser
{
	private enum NonTerminals
	{
		START,
		FINISH
	}

	private enum Terminals
	{
		UPDATE,
		DELETE
	}

	private static java.util.HashMap<NonTerminals, java.util.HashMap<String, String>> table = new java.util.HashMap<>();

	public static void initialize()
	{
		table.put(START, new java.util.HashMap<String, String>()); 
		table.get(START).put("UPDATE", "Something and some stuff");
	}

	public static boolean validateInput(String input_string)
	{
		// LL(1) parser:
		java.util.Stack stack = new java.util.ArrayList<String>();
		stack.push("S");
	}
}