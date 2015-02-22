package server;

import org.apache.commons.cli.*;

class ArgumentHandler
{
	private CommandLine cmd = null;
	
	ArgumentHandler(String[] args)
	{
		Options opts = new Options(); 
		opts.addOption(new Option("v", false, "verbose mode"));
		opts.addOption(new Option("help", false, "Print help text for the user."));
		opts.addOption(new Option("keygen", false, "Generate a new key."));
		opts.addOption(new Option("cli", false, "Start the command line interface."));
		CommandLineParser parser = new PosixParser();

		try
		{
			cmd = parser.parse(opts, args);
		}
		catch (ParseException exc)
		{
			System.out.println(exc);
		}
	}

	public boolean hasOption(String option)
	{
		return cmd.hasOption(option);
	}
}