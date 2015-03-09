package client;

import static utils.Configuration.verbose;

public class Cli
{
	public static String getPasswordFromConsole(java.util.Scanner scanner, String message)
	{
		java.io.Console console = System.console();
		if (console == null)
		{
			System.out.println("No console found: typing is echoed.\n" + message);
			return scanner.nextLine();
		}
		else
		{
			System.out.print(message);
			return new String(console.readPassword());
		}

	}

	public static String commandLineSendData(Client client, String host, Integer port, String login_info, String command, java.util.Scanner scanner)
		throws 
			Client.UnableToVerifyAuthenticityException,
			Client.AsymmetricKeyInvalidException,
			Client.SymmetricKeyTooLargeForAsymmetricEncryptionException,
			Client.UnableToEncryptAsymmetrically,
			Client.AsymmetricKeyInvalidException,
			java.net.UnknownHostException,
			Client.UnableToSendSymmetricKeyToTheServerException
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
		}
		catch (Client.UnableToGenerateAsymmetricKeyPair exc)
		{
			verbose("Unable to generate asymmetric key pair.");
			exc.printStackTrace();
			System.exit(1);
		}
		catch (Client.UnableToGenerateSymmetricKey exc)
		{
			verbose("Unable to generate symmetric key.");
			exc.printStackTrace();
			System.exit(1);
		}
		Runtime.getRuntime().addShutdownHook(new Client.ClientFinalizer(client));
		java.util.Scanner scanner = new java.util.Scanner(System.in);
		String host = null;
		Integer port = null;
		String login_info = null;

		while (true)
		{
			System.out.print("Enter the hostname (leave blank for default): ");
			host = scanner.nextLine();
			port = null;
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
			login_info = setLoginInfo(scanner);
			try
			{
				System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, utils.Utils.escapeSpaces(utils.Utils.escapeSpaces(utils.Configuration.settings.get("StatusCommand"))), scanner)));
			}
			catch (ServerReturnData.InvalidInputException exc)
			{
				System.out.println("The server has responded with empty data. This can be a network anomaly, just retry.");
				continue;
			}
			catch (Client.SymmetricKeyTooLargeForAsymmetricEncryptionException exc)
			{
				System.out.println("We can't get an encrypted channel to work at this momemt. Contact the system administrator.");
				// Log.log(Log.Severity.SECURITY, "It appears the symmetric key is too large for asymmetric encryption. Increase the key size in settings.conf.");
				// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				continue;
			}
			catch (Client.UnableToEncryptAsymmetrically exc)
			{
				System.out.println("We can't encrypt your data right now.");
				// Log.log(Log.Severity.SECURITY, "It appears the requested method for encryption is not present.");
				// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				continue;
			}
			catch (Client.AsymmetricKeyInvalidException exc)
			{
				System.out.println("There was a miscommunication with the server.");
				// Log.log(Log.Severity.SECURITY, "The asymmetric key is invalid.");
				// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				continue;
			}
			catch (java.net.UnknownHostException exc)
			{
				System.out.println("The host is unknown...");
				// Log.log(Log.Severity.INPUT, "The host is unknown, see the stack trace.");
				// Log.log(Log.Severity.INPUT, exc.getStackTrace());
				continue;
			}
			catch (Client.UnableToSendSymmetricKeyToTheServerException exc)
			{
				System.out.println("Unable to communicate with the server");
				// Log.log(Log.Severity.SECURITY, "The symmetric key could not be sent to the server.");
				// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				continue;
			}
			catch (Client.UnableToVerifyAuthenticityException exc)
			{
				System.out.println("Sorry, we were unable to check the authenticity of the server. We'll retry connecting.");
				continue;
			}
		
			System.out.print("Command (type 'help' for info): ");
			while (scanner.hasNextLine())
			{
				try
				{
					String line = scanner.nextLine();
					if (line.equalsIgnoreCase(utils.Configuration.settings.get("ExitCommand")))
						break;
					if (line.equalsIgnoreCase("help"))
					{
						System.out.println
						(
							"help - print this help text."
							+ "\n'" + utils.Configuration.settings.get("ExitCommand") + "' - exit the client."
							+ "\n'" + utils.Configuration.settings.get("RegisterCommand") + "' - register a new user."
							+ "\n'" + utils.Configuration.settings.get("ChangePassOfCommand") + "' - change a user password, must be root."
							+ "\n'" + utils.Configuration.settings.get("ChangePassCommand") + "' - change your own password."
							+ "\n'" + utils.Configuration.settings.get("NewEventCommand") + "' - create a new personal event."
							+ "\n'" + utils.Configuration.settings.get("GetEventsCommand") + "' - fetch all unfetched events."
							+ "\n'" + utils.Configuration.settings.get("RegisterRoomCommand") + "' - register a new room."
							+ "\n'" + utils.Configuration.settings.get("FindPersonCommand") + "' - find a person in the database."
							+ "\n'" + utils.Configuration.settings.get("GetCalendarCommand") + "' - get the current user's calendar."
							+ "\n'" + utils.Configuration.settings.get("ChangeUser") + "' - Login as another user."
							+ "\n'" + utils.Configuration.settings.get("StatusCommand") + "' - Get the status of events, bookings, etc."
							+ "\n'" + utils.Configuration.settings.get("RoomBookingCommand") + "' - Book a room."
							+ "\n'" + utils.Configuration.settings.get("RemoveRoomBookingCommand") + "' - Unbook a room."
							+ "\n'" + utils.Configuration.settings.get("RoomBookingInviteCommand") + "' - Invite people to your booking."
							+ "\n'" + utils.Configuration.settings.get("RoomBookingAcceptInviteCommand") + "' - Accept someone's room booking invitation."
							+ "\n'" + utils.Configuration.settings.get("RoomBookingDenyInviteCommand") + "' - Deny someone's room booking invitation."
							+ "\n'" + utils.Configuration.settings.get("RoomFind") + "' - Deny someone's room booking invitation."
							+ "\n'" + utils.Configuration.settings.get("GetInvitesCommand") + "' - Get all invites aimed at you."
							+ "\n'" + utils.Configuration.settings.get("SeeMyBookingsCommand") + "' - See all the bookings you own."
							+ "\n'" + utils.Configuration.settings.get("ReconnectCommand") + "' - Reconnect to any other server."
						);
					}
					else if (line.equalsIgnoreCase(utils.Configuration.settings.get("ReconnectCommand")))
					{
						break;
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
						System.out.println("Defaulting to checking password: " + commandLineSendData(client, host, port, login_info, utils.Configuration.settings.getAndEscape("PassCheck"), scanner));
					}
					System.out.print("Command (type 'help' for info): ");
				}
				catch (Client.UnableToVerifyAuthenticityException exc)
				{
					System.out.println("Sorry, we could not verify the server's authenticity. Check the log file for more details.");
				}
				catch (ServerReturnData.InvalidInputException exc)
				{
					System.out.println("The server has responded with empty data. This can be a network anomaly, just retry.");
				}
				catch (Client.SymmetricKeyTooLargeForAsymmetricEncryptionException exc)
				{
					System.out.println("We can't get an encrypted channel to work at this momemt. Contact the system administrator.");
					// Log.log(Log.Severity.SECURITY, "It appears the symmetric key is too large for asymmetric encryption. Increase the key size in settings.conf.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				}
				catch (Client.UnableToEncryptAsymmetrically exc)
				{
					System.out.println("We can't encrypt your data right now.");
					// Log.log(Log.Severity.SECURITY, "It appears the requested method for encryption is not present.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				}
				catch (Client.AsymmetricKeyInvalidException exc)
				{
					System.out.println("There was a miscommunication with the server.");
					// Log.log(Log.Severity.SECURITY, "The asymmetric key is invalid.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				}
				catch (java.net.UnknownHostException exc)
				{
					System.out.println("The host is unknown...");
					// Log.log(Log.Severity.INPUT, "The host is unknown, see the stack trace.");
					// Log.log(Log.Severity.INPUT, exc.getStackTrace());
				}
				catch (Client.UnableToSendSymmetricKeyToTheServerException exc)
				{
					System.out.println("Unable to communicate with the server");
					// Log.log(Log.Severity.SECURITY, "The symmetric key could not be sent to the server.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
				}
			}
		}
	}
}