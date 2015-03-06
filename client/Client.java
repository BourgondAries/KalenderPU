package client;

import static utils.Configuration.verbose;

public class Client
{

	////////////////////////////////////////////////////////////
	// CLIENT PROGRAM ENTRY ////////////////////////////////////
	////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		ArgumentHandler arghandler = initializeConfiguration(args);

		if (arghandler.hasOption("cli"))
			commandLineInterface();
		else if (arghandler.hasOption("gui"))
			; // start gui
		else if (arghandler.hasOption("test"))
			; // Run tests
		else if (arghandler.hasOption("help"))
			printHelp();
		else
			printHelp();
	}

	public static ArgumentHandler initializeConfiguration(String[] args)
	{
		utils.Configuration settings = null;
		try { settings = utils.Configuration.loadDefaultConfiguration(); }
		catch ( java.io.IOException ioexc ) { System.out.println("Unable to load configuration data: " + ioexc); System.exit(1); }

		ArgumentHandler argument_handler = new ArgumentHandler(args);
		utils.Configuration.verbose_mode = argument_handler.hasOption("v");
		return argument_handler;
	}

	public static class ClientFinalizer extends Thread
	{
		Client client;

		ClientFinalizer(Client client)
		{
			this.client = client;
		}

		public void run()
		{
			try
			{
				client.storeAllTrustedKeys();
			}
			catch (java.io.IOException exc)
			{
				verbose("Unable to store public server keys");
			}
		}
	}

	public static String getPasswordFromConsole(java.util.Scanner scanner, String message)
	{
		java.io.Console console = System.console();
		if (console == null)
		{
			System.out.println("No console found: typing is echod.n" + message);
			return scanner.nextLine();
		}
		else
		{
			System.out.print(message);
			return new String(console.readPassword());
		}

	}

	public static String commandLineSendData(Client client, String host, Integer port, String login_info, String command, java.util.Scanner scanner) throws Exception
	{
		verbose("Sending data: '" + login_info + " " + command + "'.");
		if (client.sendData(login_info + " " + command, host, port) == false)
		{
			System.out.print("WARNING: The certificate presented by remote does not appear to be trusted.nDo you want to add remote to the list of trusted servers? (yes/no): ");
			while (true)
			{
				String result = scanner.nextLine();
				if (result.equals("yes"))
				{
					client.addPublicServerKeyToTrusted();
					client.sendWhenTrusted(login_info + " " + command);
					java.util.ArrayList<String> answer = utils.Utils.splitAndUnescapeString(client.fetchResponse());
					System.out.println("Server response:");
					for (String string : answer)
						System.out.println(string);
					break;
				}
				else if (result.equals("no"))
					break;
				else
					System.out.print("Please enter \"yes\" or \"no\": ");
			}
		}
		else
		{
			String response = client.fetchResponse();
			verbose("Direct response: " + response);
			if (response != null)
			{
				/*
				java.util.ArrayList<String> answer = utils.Utils.splitAndUnescapeString(response);
				System.out.println("Server response:");
				for (String string : answer)
					System.out.println(string);
				*/
				return response;
			}
			else
				System.out.println("No response from server");
		}
		return null;
	}

	public static String setLoginInfo(java.util.Scanner scanner)
	{
		System.out.print("Enter your username: ");
		String login_info = utils.Utils.escapeSpaces(scanner.nextLine());
		login_info = login_info + " " + utils.Utils.escapeSpaces(getPasswordFromConsole(scanner, "Enter your password: "));
		return login_info;
	}

	public static void commandLineInterface()
	{
		Client client = null;
		try
		{
			client = new Client(utils.Configuration.settings);
			Runtime.getRuntime().addShutdownHook(new ClientFinalizer(client));
			java.util.Scanner scanner = new java.util.Scanner(System.in);
			System.out.print("Enter the hostname (leave blank for default): ");
			String host = scanner.nextLine();
			Integer port = null;
			if (host.equals("") == false)
			{
				System.out.print("Enter the port (leave blank for default): ");
				String portstring = scanner.nextLine();
				if (portstring.equals(""))
				{
					port = utils.Configuration.settings.getInt("port");
				}
				else 
					port = Integer.parseInt(portstring);
			}
			else
			{
				host = utils.Configuration.settings.get("hostname");
				port = utils.Configuration.settings.getInt("port");
			}
			String login_info = setLoginInfo(scanner);
			System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, utils.Utils.escapeSpaces(utils.Utils.escapeSpaces(utils.Configuration.settings.get("StatusCommand"))), scanner)));

			System.out.print("Command (type 'help' for info): ");
			while (scanner.hasNextLine())
			{
				String line = scanner.nextLine();
				if (line.equalsIgnoreCase(utils.Configuration.settings.get("ExitCommand")))
					break;
				if (line.equalsIgnoreCase("help"))
				{
					System.out.println
					(
						"help - print this help text."
						+ "n'" + utils.Configuration.settings.get("ExitCommand") + "' - exit the client."
						+ "n'" + utils.Configuration.settings.get("RegisterCommand") + "' - register a new user."
						+ "n'" + utils.Configuration.settings.get("ChangePassOfCommand") + "' - change a user password, must be root."
						+ "n'" + utils.Configuration.settings.get("ChangePassCommand") + "' - change your own password."
						+ "n'" + utils.Configuration.settings.get("NewEventCommand") + "' - create a new personal event."
						+ "n'" + utils.Configuration.settings.get("GetEventsCommand") + "' - fetch all unfetched events."
						+ "n'" + utils.Configuration.settings.get("RegisterRoomCommand") + "' - register a new room."
						+ "n'" + utils.Configuration.settings.get("FindPersonCommand") + "' - find a person in the database."
						+ "n'" + utils.Configuration.settings.get("GetCalendarCommand") + "' - get the current user's calendar."
						+ "n'" + utils.Configuration.settings.get("ChangeUser") + "' - Login as another user."
						+ "n'" + utils.Configuration.settings.get("StatusCommand") + "' - Get the status of events, bookings, etc."
						+ "n'" + utils.Configuration.settings.get("RoomBookingCommand") + "' - Book a room."
						+ "n'" + utils.Configuration.settings.get("RemoveRoomBookingCommand") + "' - Unbook a room."
						+ "n'" + utils.Configuration.settings.get("RoomBookingInviteCommand") + "' - Invite people to your booking."
						+ "n'" + utils.Configuration.settings.get("RoomBookingAcceptInviteCommand") + "' - Accept someone's room booking invitation."
						+ "n'" + utils.Configuration.settings.get("RoomBookingDenyInviteCommand") + "' - Deny someone's room booking invitation."
						+ "n'" + utils.Configuration.settings.get("RoomFind") + "' - Deny someone's room booking invitation."
						+ "n'" + utils.Configuration.settings.get("GetInvitesCommand") + "' - Get all invites aimed at you."
						+ "n'" + utils.Configuration.settings.get("SeeMyBookingsCommand") + "' - See all the bookings you own."
					);
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("ChangeUser")))
				{
					login_info = setLoginInfo(scanner);
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RegisterCommand")))
				{
					System.out.print("Enter a new username: ");
					String username = scanner.nextLine();
					System.out.print("Enter a rank (positive integer): ");
					String rank = scanner.nextLine();
					System.out.print("Enter the first name: ");
					String fname = scanner.nextLine();
					System.out.print("Enter the last name: ");
					String lname = scanner.nextLine();
					String password;
					do 
					{
						password = getPasswordFromConsole(scanner, "Enter the password for the new user: ");
						String passcheck = getPasswordFromConsole(scanner, "Enter the password again: ");
						if (password.equals(passcheck) == false)
							System.out.println("Passwords do not match, retry.");
						else
							break;
					}
					while (true);
					line = 
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("RegisterCommand")
							+ " "
							+ utils.Utils.escapeSpaces(username)
							+ " "
							+ utils.Utils.escapeSpaces(rank)
							+ " "
							+ utils.Utils.escapeSpaces(fname)
							+ " "
							+ utils.Utils.escapeSpaces(lname)
							+ " "
							+ utils.Utils.escapeSpaces(password)
						);
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
					if (result != null)
					{
						System.out.println(result);
					}
					else
					{
						System.out.println("Server failed to respond.");
					}
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("ChangePassOfCommand")))
				{
					System.out.print("Enter the username to change the password of: ");
					String username = scanner.nextLine();
					String password;
					do 
					{
						password = getPasswordFromConsole(scanner, "Enter the new password for the user: ");
						String passcheck = getPasswordFromConsole(scanner, "Enter the password again: ");
						if (password.equals(passcheck) == false)
							System.out.println("Passwords do not match, retry.");
						else
							break;
					}
					while (true);
					line = 
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("ChangePassOfCommand")
							+ " "
							+ utils.Utils.escapeSpaces(username)
							+ " "
							+ utils.Utils.escapeSpaces(password)
						);
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
					if (result != null)
					{
						if (result.equals("1"))
						{
							System.out.println("Server response: 'OK: Changed password.'");
						}
						else
						{
							System.out.println("Server response: 'ERR: Something went wrong.'");
						}
					}
					else
					{
						System.out.println("Server failed to respond.");
					}
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("ChangePassCommand")))
				{
					String password;
					do 
					{
						password = getPasswordFromConsole(scanner, "Enter the new password for the user: ");
						String passcheck = getPasswordFromConsole(scanner, "Enter the password again: ");
						if (password.equals(passcheck) == false)
							System.out.println("Passwords do not match, retry.");
						else
							break;
					}
					while (true);
					line = 
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("ChangePassCommand")
							+ " "
							+ utils.Utils.escapeSpaces(password)
						);
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
					if (result != null)
					{
						if (result.equals("1"))
						{
							System.out.println("Server response: 'OK: Changed password.'");
						}
						else
						{
							System.out.println("Server response: 'ERR: Something went wrong.'");
						}
					}
					else
					{
						System.out.println("Server failed to respond.");
					}
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("NewEventCommand")))
				{
					System.out.print("Enter a description of the event: ");
					String description = scanner.nextLine();
					System.out.print("Enter when the event starts of the format 'yyyy-mm-dd hh:mm:ss': ");
					String datetime_start = scanner.nextLine();
					System.out.print("Enter when the event ends of the format 'yyyy-mm-dd hh:mm:ss': ");
					String datetime_end = scanner.nextLine();
					System.out.print("How many minutes before the event do you want to be warned (Blank for no warning): ");
					String warn_minutes = scanner.nextLine();

					line = 
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("NewEventCommand")
							+ " "
							+ utils.Utils.escapeSpaces(description)
							+ " "
							+ utils.Utils.escapeSpaces(datetime_start)
							+ " "
							+ utils.Utils.escapeSpaces(datetime_end)
							+ " "
							+ utils.Utils.escapeSpaces(warn_minutes)
						);
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("GetEventsCommand")))
				{
					line =
						utils.Utils.escapeSpaces(utils.Configuration.settings.getAndEscape("GetEventsCommand"));
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("GetInvitesCommand")))
				{
					line =
						utils.Utils.escapeSpaces(utils.Configuration.settings.getAndEscape("GetInvitesCommand"));
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("SeeMyBookingsCommand")))
				{
					line =
						utils.Utils.escapeSpaces(utils.Configuration.settings.getAndEscape("SeeMyBookingsCommand"));
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RegisterRoomCommand")))
				{
					System.out.print("Enter the new room name: ");
					String room_name = scanner.nextLine();
					System.out.print("Enter the new room's capacity (people): ");
					String room_cap = scanner.nextLine();
					System.out.print("Enter the new room's location: ");
					String room_location = scanner.nextLine();
					line = 
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("RegisterRoomCommand")
							+ " "
							+ utils.Utils.escapeSpaces(room_name)
							+ " "
							+ utils.Utils.escapeSpaces(room_cap)
							+ " "
							+ utils.Utils.escapeSpaces(room_location)
						);
					String result = commandLineSendData(client, host, port, login_info, line, scanner);
					if (result != null)
					{
						if (result.equals("1"))
						{
							System.out.println("Server response: 'OK: Room registered.'");
						}
						else
						{
							System.out.println("Server response: 'ERR: Something went wrong.'");
						}
					}
					else
					{
						System.out.println("Server failed to respond.");
					}
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("FindPersonCommand")))
				{
					System.out.print("Enter the name you'd like to search for: ");
					String like = scanner.nextLine();
					line =
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("FindPersonCommand")
							+ " "							
							+ utils.Utils.escapeSpaces(like)
						);
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("GetCalendarCommand")))
				{
					System.out.print("Enter the year: ");
					String year = scanner.nextLine();
					System.out.print("Enter the month: ");
					String month = scanner.nextLine();
					System.out.print("Enter the day (leave blank for entire month): ");
					String day = scanner.nextLine();
					line =
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("GetCalendarCommand")
							+ " "							
							+ utils.Utils.escapeSpaces(year)
							+ " "
							+ utils.Utils.escapeSpaces(month)
							+ " "
							+ utils.Utils.escapeSpaces(day)
						);
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomBookingCommand")))
				{
					System.out.println("Enter the title of the booking: ");
					String title = scanner.nextLine();
					System.out.println("Enter the description of the booking: ");
					String description = scanner.nextLine();
					System.out.println("Enter the room id to book: ");
					String room_id = scanner.nextLine();
					System.out.println("Warning time (yyyy-mm-dd HH:MM:ss date format): ");
					String warntime = scanner.nextLine();
					System.out.println("From (yyyy-mm-dd HH:MM:ss date format): ");
					String from = scanner.nextLine();
					System.out.println("To (yyyy-mm-dd HH:MM:ss date format): ");
					String to = scanner.nextLine();
					line =
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("RoomBookingCommand")
							+ " "
							+ utils.Utils.escapeSpaces(title)
							+ " "
							+ utils.Utils.escapeSpaces(description)
							+ " "
							+ utils.Utils.escapeSpaces(room_id)
							+ " "
							+ utils.Utils.escapeSpaces(warntime)
							+ " "
							+ utils.Utils.escapeSpaces(from)
							+ " "
							+ utils.Utils.escapeSpaces(to)
						);
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RemoveRoomBookingCommand")))
				{
					System.out.println("The booking which you would like to remove: ");
					String booking_id = scanner.nextLine();
					line =
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("RemoveRoomBookingCommand")
							+ " "
							+ utils.Utils.escapeSpaces(booking_id)
						);
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomBookingInviteCommand")))
				{
					System.out.println("Whom to invite (username, enter blank to continue): ");
					String users = scanner.nextLine();
					System.out.println("Booking id to invite to: ");
					String booking_id = scanner.nextLine();
					line =
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("RoomBookingInviteCommand")
							+ " "
							+ utils.Utils.escapeSpaces(users)
							+ " "
							+ utils.Utils.escapeSpaces(booking_id)
						);
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomBookingAcceptInviteCommand")))
				{
					System.out.println("Select the booking id you like to accept: ");
					String booking_id = scanner.nextLine();
					line =
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("RoomBookingAcceptInviteCommand")
							+ " "
							+ utils.Utils.escapeSpaces(booking_id)
						);
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomBookingDenyInviteCommand")))
				{
					System.out.println("Select the booking id you like to deny: ");
					String booking_id = scanner.nextLine();
					line =
						utils.Utils.escapeSpaces
						(
							utils.Configuration.settings.getAndEscape("RoomBookingDenyInviteCommand")
							+ " "
							+ utils.Utils.escapeSpaces(booking_id)
						);
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomFind")))
				{
					line =
						utils.Utils.escapeSpaces(utils.Configuration.settings.getAndEscape("RoomFind"));
					System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
				}
				else if (line.equalsIgnoreCase("smug pepe"))
				{
					System.out.println(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,,,,,.-+ssssssssssssss+:,,,,,,,:ossssssss+:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,,,-sdhyyyyyyyyyyyyyyyyhhs.,,:+dyyyyyyyyyyhhho:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,-ydhyyyyyyyyyyyyyyyyyyyyhdyhhyyyyyyyyyyyyyyyyds.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,.shhyyyyyyyyyyyyyyyyyyyyyyyymhyyyyyyyyyyyyyyyyyyhd+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n-dhyyyyyyyyyyyyyyyyyyyyyyyyyyhmyyyyyyyyyyyyyyyyyyyyds.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\ndhyyyyyyyyyyhddddddhhhhhhhhhhdmdhyyyyyyyyyyyyyyyyyyydy.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyddhhyyyyyyyyyyyyyyyyyhddhhhhddddhhhhhdddhhhdo,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyhdhyyyyyyyyyyyyyyyyyyyyyyhdmdhyyyyyyyyyyyyhhhdm+.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmhyyyyyyyyyyyyyyyhdhs:.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmhyyyyyyyyyyyyyyyyyhdy:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyhhhhhhhhhhhhhhhhhhhhhhhyhmhyyyyyyyyyyyyyyyyyyhdo.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyhhdddhhhhhhhdddddddmddddhhhdddmhyyyyyyyyyhhdhhddhhddy+:.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyddhhhhdddhhdhhhhhdddhhhhdddyyydmdyyyyyyyyhdddhhhhhhhhdmmy/.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyddhhddhhyyyyyyyyyyyyyyhddhhhdddhyydmyyyyyhddhddddhhhhhddhhdms,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyymdydmhyyyyyyyyyyyyhhhhhhyyhdddmddmhyddyyyddhddddhyyyhyyyhddyd.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyydddmddhso///+smdhmNMNMMMd/::::/sdhddymyyyydhdmmNNNNdo+yhyhNmd,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyhhhys+.oMMhoMMMhsoNNs      :mydmdyyhdosM/hMMNddm. .-/yhm-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyhyyhdmMMMoNMM/./NMs       sdyddyhm.yMMmNMMy,.N+    ,oN-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyydddyyyhdmmNNMMMMMM/       ,yhyhyds,dMMhmMMNddN: ,,/ydd-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyydmdyyyyyyyhmmNNm:...../syydmhyhhymNNNNNNNNmhossydhh+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyhddhdddhhhhyyhhhhhhhhyyhdmyyhhhyyyyyyyyyyyyyyhmh.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyhhhhdhhhhhhddmdhyyyyyhdNmddhhhyyyyhhdNms-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyhhhyyyyyyyyyyyyyyyyyyyyyyyyyyydmhyyyyyyyyyyhdddyyyyyhdhdhyhm:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyhhyyyyyyyyyyyyyyyyyyyyyyyyyyhmdhyyyyyyyyyyyyyyyhmdyyyyyyyyyyym:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyhddhhhyyyyyyyyyyyyyyyyyyyhddmdyyyyyyyyyyyyyyyyyyyyddyyyyyyyyyydmy.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyydddmdhhhhyyyyyyyyyyyyyyhdddhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyymhdo,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyymhhhhhddddhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyddhd+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyddhyhdddhhhhdddddddhhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyydmhh:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyhdddhhhdmddhhyyhhhhhhdddhhhhhhhyyyyyyyyyyyyyyyyyyyyyyyyydddddh.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyydmdhhhhhdddddhhhhyyhhhhhhhhdddddddddddddddddddddddddhhddm-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyhhddddhhyhhhhhdddddddddhdhhhhhhhhhhhhhhhhhhhhhhhhhdmdhhs,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyhhddhyyyyyyyyyyyyhhhhhhhhhhhhhhhhhhhhhhhhhhhhhyyhd+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyhddddhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyydy:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyhhddddddddhhhhyyyyyyyyyyyyyyyyyyyyyhhhdmm:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhhhhhhdhdddddddddddddddddddddhhhym.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhhhhhyyyydo,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhddhhhddddhs.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhhdddhyyyyyhhdh-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdmhyyyyyyyyyyyyhhy+:.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdhddhhdmmdyyyyyyhhdhhyyyyyhhyo.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyddhyyymmhyyddyyhddddy/hhhddyyyyhhs.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmyyyyhmhyhmhdddhhh+.+mddhyyyyyyyhy:,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhddyyyyhhdmhhyhho:/smhhyyyyyyyyyyhmo.,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmhyyyyymhhs++ydhyyyyyyyyyyyyyyyyhds.,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyymyyyyymy..ydyyyyyyyydmhyyyyyyyyyydo,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdyyyyyhhhmyyyyyyyhmdyyyyyyyhyyyyydy-,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdyyyyyyyhNmmhyyyddhyyyyyydmhyyyhyyh+,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmmyyyyyyyyyyhdmdhmyyyyyyddhyyyydmyyyd.,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyydy-/myyyyyyyyyyyyyyddyyyhmdyyyyhmdyyyyhs,,,,,,,,,,,,,,,,,,,,,");
				}
				else if (line.equalsIgnoreCase("jon is kill"))
				{
					System.out.println("\"no\"");
				}
				else
				{
					System.out.println("Defaulting to checking password.n" + commandLineSendData(client, host, port, login_info, utils.Configuration.settings.getAndEscape("PassCheck"), scanner));
				}
				System.out.print("Command (type 'help' for info): ");
			}
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	public static void printHelp()
	{
		System.out.println
		(
			"Help text for this program."
		);
	}

	////////////////////////////////////////////////////////////
	// START OF OBJECT DEPENDENT DEFINITIONS ///////////////////
	////////////////////////////////////////////////////////////

	private java.net.Socket 			client_socket;
	private java.io.InputStream 		input_from_server;
	private java.io.OutputStream 		output_to_server;
	private java.security.PrivateKey 	client_private_key;
	private java.security.PublicKey 	server_public_key;
	private java.security.PublicKey  	client_public_key;
	private java.util.ArrayList<byte[]> server_public_keys = new java.util.ArrayList<>();
	private byte[] 	bytes = null;
	private int 	length = 0;
	private String 	last_message;
	private utils.Configuration settings;
	private java.security.Key symmetric_key;

	public Client(utils.Configuration settings)
	{
		this.settings = settings;
		try
		{
			bytes = new byte[settings.getInt("keylength")];
			loadTrustedServers();
			generatePair();
			generateSymmetric();
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public boolean sendData(String data, String host, int port)
	{
		try
		{
			connectAndSetUpChannels(host, port);
			sendClientPublicKeyToServer();
			getServerPublicKeyFromServer(); // For checking whether we already have this one later.
			getCertificateFromServer(); // Aka client's encrypted public key
			if (queryWhetherItIsTrusted() == false)
				return false;
			sendSymmetricKey();
			sendWhenTrusted(data);
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
		return true;
	}

	public void sendSymmetricKey() throws java.io.IOException, Exception
	{
		verbose("Sending symmetric key.");
		output_to_server.write(utils.Utils.encrypt(symmetric_key.getEncoded(), server_public_key));
	}

	public void sendWhenTrusted(String data) throws Exception
	{
		if (verifyAuthenticity())
		{
			verbose("Server authenticated.");
			writeMessageToServer(data);
			getServerResponse();
		}
		else
		{
			verbose("Server failed to authenticate");
		}
		client_socket.close();
	}

	public String fetchResponse()
	{
		String tmp = last_message;
		last_message = null;
		return tmp;
	}

	public void addPublicServerKeyToTrusted()
	{
		server_public_keys.add(server_public_key.getEncoded());
	}

	private static boolean isContained(byte[] bigger, int index, byte[] smaller)
	{
		for (int i = 0; i < smaller.length && i + index < bigger.length; ++i)
		{
			if (bigger[i + index] != smaller[i])
				return false;
		}
		return true;
	}

	private void loadTrustedServers()
	{
		verbose("Loading trusted servers into memory.");
		try
		{
			byte[] cert = utils.Utils.fileToBytes(utils.Configuration.settings.get("TrustedServers"));
			for (int i = 0; i < cert.length; ++i)
			{
				if (isContained(cert, i, settings.get("PublicKeySeparator").getBytes()))
				{
					byte[] copy = java.util.Arrays.copyOfRange(cert, 0, i);
					server_public_keys.add(copy);
					cert = java.util.Arrays.copyOfRange(cert, i + settings.get("PublicKeySeparator").getBytes().length, cert.length);
					i = 0;
				}
			}

		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void connectAndSetUpChannels(String host, int port) throws java.net.UnknownHostException, java.io.IOException
	{
		verbose("Connecting to foreign host.");
		client_socket = new java.net.Socket(host, port);
		client_socket.setSoTimeout(settings.getInt("SocketTimeOut"));
		output_to_server = client_socket.getOutputStream();
		input_from_server = client_socket.getInputStream();
	}


	private void getServerPublicKeyFromServer()
	{
		verbose("Waiting for host public key.");
		bytes = new byte[settings.getInt("keylength")];
		try
		{
			int number = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, number);
			server_public_key = utils.Utils.bytesToPublicKey(bytes);
		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void getCertificateFromServer() throws java.io.IOException
	{
		length = input_from_server.read(bytes);
	}

	private boolean queryWhetherItIsTrusted() 
	{
		verbose("Testing whether the key is trusted.");
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			if (java.util.Arrays.equals(server_public_keys.get(i), server_public_key.getEncoded()))
				return true;
		}

		return false;
	}

	private boolean verifyAuthenticity() throws Exception
	{
		verbose("Verifying authenticity.");
		bytes = java.util.Arrays.copyOf(bytes, length);
		java.security.Signature sig = java.security.Signature.getInstance(utils.Configuration.settings.get("SignMethod"));
		sig.initVerify(server_public_key);
		sig.update(client_public_key.getEncoded());
		return sig.verify(bytes);
		
	}

	private void generatePair()
	{
		verbose("Generating keypair.");
		java.security.KeyPair pair = utils.Utils.getNewKeyPair();
		client_private_key = pair.getPrivate();
		client_public_key = pair.getPublic();
	}

	private void generateSymmetric()
	{
		verbose("Generating symmetric key.");
		try
		{
			symmetric_key = utils.Utils.generateSymmetricKey(settings.get("SymmetricSpec"));
		}
		catch (Exception exc)
		{
			verbose(exc.toString());
		}
	}

	private void sendClientPublicKeyToServer() throws java.io.IOException
	{
		output_to_server.write(client_public_key.getEncoded());
	}

	private void writeMessageToServer(String data)
	{
		verbose("Sending packets to the server...");
		try
		{
			output_to_server.write(utils.Utils.encryptSymmetric(data.getBytes(), symmetric_key, settings.get("SymmetricCipher")));
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void getServerResponse()
	{
		verbose("Retrieving server response.");
		try
		{
			bytes = new byte[settings.getInt("keylength")];
			int length = input_from_server.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, length);
			last_message = new String(utils.Utils.decryptSymmetric(bytes, symmetric_key, settings.get("SymmetricCipher")));
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	private void storeAllTrustedKeys() throws java.io.IOException
	{
		verbose("Storing the trusted keys in '" + settings.get("TrustedServers") + "'.");
		java.io.FileOutputStream trusted = new java.io.FileOutputStream(settings.get("TrustedServers"));
		for (int i = 0; i < server_public_keys.size(); ++i)
		{
			trusted.write(server_public_keys.get(i));
			trusted.write(settings.get("PublicKeySeparator").getBytes());
		}
	}
}