package client;

import org.apache.commons.cli.*;

class ArgumentHandler
{
	private CommandLine cmd = null;

	ArgumentHandler(String[] args)
	{
		Options opts = new Options(); 
		opts.addOption(new Option("v", false, "Verbose mode."));
		opts.addOption(new Option("help", false, "Print help text for the user."));
		opts.addOption(new Option("clear", false, "Clears the trusted list of servers."));
		opts.addOption(new Option("cli", false, "Uses the command line interface."));
		opts.addOption(new Option("gui", false, "Uses a graphical user interface."));
		CommandLineParser parser = new PosixParser();

		try
		{
			cmd = parser.parse(opts, args);

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

	public boolean hasOption(String option)
	{
		return cmd.hasOption(option);
	}
}