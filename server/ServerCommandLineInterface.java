package server;

import static utils.Configuration.verbose;

public class ServerCommandLineInterface
{
	static class ExitListener extends Thread
	{
		public java.util.Scanner scanner = null;
		public Database db = null;
		public Integer port = null;

		@Override
		public void run()
		{
			System.out.println("Press '" + utils.Configuration.settings.get("ExitCommand") + "' to exit.\nOr type any SQL query here to run it.");
			try
			{
				while (true)
				{
					System.out.print("SQL command ('" + utils.Configuration.settings.get("HelpCommand") + "' for help): ");
					String string = scanner.nextLine();
					if (string.equals(utils.Configuration.settings.get("ExitCommand")))
					{
						System.exit(0);	
					}
					else if (string.equals(utils.Configuration.settings.get("HelpCommand")))
					{
						System.out.println
						(
							"'" + utils.Configuration.settings.get("RootResetPasswordOnServer") + "' - Reset the root password."
						);
					}
					else if (string.equals(utils.Configuration.settings.get("RootResetPasswordOnServer")))
					{
						try
						{
							synchronized (db)
							{
								if (db.resetRootPassword(client.ClientCommandLineInterface.getPasswordFromConsole(scanner, "Type in the new password: ")) == 1)
								{
									System.out.println("Correctly reset the root password.");
								}
								else
								{
									System.out.println("Could not reset the root password.");
								}
							}
						}
						catch (java.sql.SQLException exc)
						{
							System.out.println("Unable apply the change in the database.");
							// Log
						}
						catch (java.security.NoSuchAlgorithmException exc)
						{
							System.out.println("The hashing algorithm was not found.");
							// Log
						}
						catch (java.security.spec.InvalidKeySpecException exc)
						{
							System.out.println("Unable to create generate a key specification.");
						}
					}
					else
					{
						try
						{
							String stringx = db.runQuery(string);
							System.out.println("Raw result: " + stringx);
							System.out.println(client.ServerReturnData.getPrettyStringWithoutObject(stringx));
						}
						catch (Database.DatabaseUninitializedException exc)
						{
							verbose("The database is not initialized: attempting to re-initialize.");
						}
						catch (client.ServerReturnData.InvalidInputException exc)
						{
							verbose("The return string was empty.");
						}
					}
				}
			}
			catch (java.util.NoSuchElementException exc)
			{
				verbose("Command line scanner forced to exit.");
			}
		}
	}

	public static Integer queryPort(java.util.Scanner scanner)
	{
		try
		{
			System.out.print("Enter the port to listen and send to (leave blank for default): ");
			String portnumber = scanner.nextLine();
			Integer port = null;
			if (!portnumber.equals(""))
				port = Integer.parseInt(portnumber);
			return port;
		}
		catch (java.util.NoSuchElementException exc)
		{
			verbose("The scanner could not read the elements, defaulting to standard port.");
			return null;
		}
	}

	public static void commandLineInterface()
	{
		Server server = null;
		Database db = null;
		try
		{
			db = new Database(utils.Configuration.settings.get("DBConnection"));
		}
		catch (Database.CouldNotConnectAndSetupDatabaseConnection exc)
		{
			verbose("Unable to connect to the database. Check if the database is not already in use. If it is not; try making dbreset or setup.");
		}
		catch (Database.CouldNotFindEncryptionAlgorithm exc)
		{
			verbose("Unable to find the correct encryption algorithm as specified in the settings.conf file.");
		}
		catch (Database.KeySpecInvalidException exc)
		{
			verbose("The keyspec for the database is invalid.");
		}
		try
		{
			server = new Server(utils.Configuration.settings);
			Runtime.getRuntime().addShutdownHook(new Server.ServerFinalizer(db));
			java.util.Scanner scanner = new java.util.Scanner(System.in);

			Integer port = queryPort(scanner);

			ExitListener exit_listener = new ExitListener();
			exit_listener.scanner =  scanner;
			exit_listener.db = db;
			exit_listener.port = port;
			exit_listener.start();

			while (true)
			{
				try
				{
					String message = "";
					
					message = server.waitForMessage(port);
					if (message == null)
						continue;
					java.util.ArrayList<String> message_parts = utils.Utils.splitAndUnescapeString(message);
					for (int i = 0; i < message_parts.size(); ++i)
						System.out.println(message_parts.get(i));
					if (message_parts.size() == 3)
					{
						synchronized(db)
						{
							server.respondToMessage(db.execute(message_parts.get(0), message_parts.get(1), message_parts.get(2)));
						}
					}
					else
					{
						server.respondToMessage("Invalid: Amount of tokens do not match the desired amount of 3 tokens.");
					}
				}
				catch (Server.PortUnavailableException exc)
				{
					System.out.println("The port you specified '" + String.valueOf(port) + "' is already in use.");
					queryPort(scanner);
				}
				catch (Server.BlockSizeTooLargeException exc)
				{
					server.respondToMessage("You have sent a block that is too large to be accepted by the server. Largest size is: " + utils.Configuration.settings.getInt("maxblocksize"));
				}
				catch (javax.crypto.BadPaddingException exc)
				{
					verbose("Client was unable to respond, probably because of a query regarding known hosts.");
				}
			}
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	public static void generateKeys()
	{
		try
		{
			(new Server(utils.Configuration.settings)).generatePublicAndPrivateKey();
		}
		catch (java.security.NoSuchAlgorithmException exc)
		{
			verbose("The algorithm for generating public and private keys is unavailable.");
			exc.printStackTrace();
		}
		catch (java.security.NoSuchProviderException exc)
		{
			verbose("The provider for generating public and private keys is unavailable.");
			exc.printStackTrace();
		}
		catch (java.io.IOException exc)
		{
			verbose("Unable to store the new key.");
			// Log.log(Log.Severity.KEYGEN, "The key was not able to be stored", exc.getStackTrace());
		}
	}

	public static void printHelp()
	{
		System.out.println("Help for server CLI");
	}
}