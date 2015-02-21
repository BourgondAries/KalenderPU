package client;

import org.apache.commons.cli.*;

class ArgumentHandler
{
	public boolean is_verbose = false;
	public boolean print_help = false;

	ArgumentHandler(String[] args)
	{
		Options opts = new Options(); 
		opts.addOption(new Option("v", false, "Verbose mode."));
		opts.addOption(new Option("help", false, "Print help text for the user."));
		opts.addOption(new Option("clear", false, "Clears the trusted list of servers."));
		CommandLineParser parser = new PosixParser();

		try
		{
			CommandLine cmd = parser.parse(opts, args);
			
			is_verbose = cmd.hasOption("v");
			print_help = cmd.hasOption("help");

			if (cmd.hasOption("clear"))
			{
				java.nio.channels.FileChannel out = new java.io.FileOutputStream(utils.Configuration.settings.get("TrustedServers"), true).getChannel();
				out.truncate(0);
				out.close();
				System.exit(0);
			}
		}
		catch (java.io.IOException ioexc)
		{
			System.out.println(ioexc);
		}
		catch (ParseException exc)
		{
			System.out.println(exc);
		}
	}
}