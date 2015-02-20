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

	private static java.util.HashMap<NonTerminals, java.util.HashMap<

	public static boolean validateInput(String input_string)
	{
		// LL(1) parser:
		java.util.Stack stack = new java.util.ArrayList<String>();
		stack.push("S");
	}
}