package server;

import org.apache.commons.cli.*;

class ArgumentHandler
{
	public boolean is_verbose = false;
	public boolean print_help = false;
	public boolean keygen = false;

	ArgumentHandler(String[] args)
	{
		Options opts = new Options(); 
		opts.addOption(new Option("v", false, "verbose mode"));
		opts.addOption(new Option("help", false, "Print help text for the user."));
		opts.addOption(new Option("keygen", false, "Generate a new key."));
		CommandLineParser parser = new PosixParser();

		try
		{
			CommandLine cmd = parser.parse(opts, args);
			
			is_verbose = cmd.hasOption("v");
			print_help = cmd.hasOption("help");
			keygen = cmd.hasOption("keygen");
		}
		catch (ParseException exc)
		{
			System.out.println(exc);
		}
	}
}