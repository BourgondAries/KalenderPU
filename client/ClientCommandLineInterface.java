package client;

import static utils.Configuration.verbose;

public class ClientCommandLineInterface
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
			System.out.print("WARNING: The certificate presented by remote does not appear to be trusted.\nDo you want to add remote to the list of trusted servers? (yes/no): ");
			while (true)
			{
				String result = scanner.nextLine();
				if (result.equals("yes"))
				{
					client.addPublicServerKeyToTrusted();	
					client.sendSymmetricKey();
					client.sendWhenTrusted(login_info + " " + command);
					return client.fetchResponse();
				}
				else if (result.equals("no"))
					return "Could not retrieve a response from the server";
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


	public static void printHelp()
	{
		System.out.println
		(
			"Help text for this program."
		);
	}


	public static String repeat(char ch, int number)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < number; ++i)
			sb.append(ch);
		return sb.toString();
	}

	public static String pad100(String in)
	{
		if (100 - in.length() > 0)
			return repeat('=', (100 - in.length()) / 2) + " " + in + " " + repeat('=', (100 - in.length()) / 2 + 1);
		else 
			return in;
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
			try
			{
				server.Logger.Log(exc.toString(), "","");
			}
			catch(java.io.IOException ioexc)
			{
				
			}
			verbose("Unable to generate asymmetric key pair.");
			exc.printStackTrace();
			System.exit(1);
		}
		catch (Client.UnableToGenerateSymmetricKey exc)
		{
			try
			{
				server.Logger.Log(exc.toString(), "","");
			}
			catch(java.io.IOException ioexc)
			{
				
			}
			
			verbose("Unable to generate symmetric key.");
			exc.printStackTrace();
			System.exit(1);
		}
		Runtime.getRuntime().addShutdownHook(new Client.ClientFinalizer(client));
		java.util.Scanner scanner = new java.util.Scanner(System.in);
		String host = null;
		Integer port = null;
		String login_info = null;
		try
		{
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
					try
					{
						server.Logger.Log(exc.toString(), "","");
					}
					catch(java.io.IOException ioexc)
					{
						
					}
					
					System.out.println("The server has responded with empty data. This can be a network anomaly, just retry.");
					continue;
				}
				catch (Client.SymmetricKeyTooLargeForAsymmetricEncryptionException exc)
				{
					try
					{
						server.Logger.Log(exc.toString(), "","");
					}
					catch(java.io.IOException ioexc)
					{
						
					}
					System.out.println("We can't get an encrypted channel to work at this momemt. Contact the system administrator.");
					// Log.log(Log.Severity.SECURITY, "It appears the symmetric key is too large for asymmetric encryption. Increase the key size in settings.conf.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					continue;
				}
				catch (Client.UnableToEncryptAsymmetrically exc)
				{
					try
					{
						server.Logger.Log(exc.toString(), "","");
					}
					catch(java.io.IOException ioexc)
					{
						
					}	
					System.out.println("We can't encrypt your data right now.");
					// Log.log(Log.Severity.SECURITY, "It appears the requested method for encryption is not present.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					continue;
				}
				catch (Client.AsymmetricKeyInvalidException exc)
				{
					try
					{
						server.Logger.Log(exc.toString(), "","");
					}
					catch(java.io.IOException ioexc)
					{
						
					}
					
					System.out.println("There was a miscommunication with the server.");
					// Log.log(Log.Severity.SECURITY, "The asymmetric key is invalid.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					continue;
				}
				catch (java.net.UnknownHostException exc)
				{
					try
					{
						server.Logger.Log(exc.toString(), "","");
					}
					catch(java.io.IOException ioexc)
					{
						
					}
					System.out.println("The host is unknown...");
					// Log.log(Log.Severity.INPUT, "The host is unknown, see the stack trace.");
					// Log.log(Log.Severity.INPUT, exc.getStackTrace());
					continue;
				}
				catch (Client.UnableToSendSymmetricKeyToTheServerException exc)
				{
					// server.Logger.Log(exc.toString(), "","");
					System.out.println("Unable to communicate with the server");
					// Log.log(Log.Severity.SECURITY, "The symmetric key could not be sent to the server.");
					// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					continue;
				}
				catch (Client.UnableToVerifyAuthenticityException exc)
				{
					try
					{
						server.Logger.Log(exc.toString(), "","");
					}
					catch(java.io.IOException ioexc)
					{
						
					}
					
					System.out.println("Sorry, we were unable to check the authenticity of the server. We'll retry connecting.");
					continue;
				}
				catch (java.lang.NullPointerException exc)
				{
					try
					{
						server.Logger.Log(exc.toString(), "","");
					}
					catch(java.io.IOException ioexc)
					{
						
					}
					System.out.println("Nullptrexc");
				}
			
				System.out.print("Command (type 'help' for info): ");
				while (scanner.hasNextLine())
				{
					try
					{
						String line = scanner.nextLine();
						if (line.equalsIgnoreCase(utils.Configuration.settings.get("ExitCommand")))
							break;
						if (line.equalsIgnoreCase(utils.Configuration.settings.get("HelpCommand")))
						{
							System.out.print(
								"" + "\n'" + utils.Configuration.settings.get("HelpCommand") + "' - print this help text."
								+ "\n'" + utils.Configuration.settings.get("ExitCommand") + "' - exit the client."
								+ "\n'" + utils.Configuration.settings.get("ReconnectCommand") + "' - Reconnect to any other server."
								+ "\n"
								+ "\n" + pad100("User management commands")
								+ "\n'" + utils.Configuration.settings.get("ChangeUser") + "' - Login as another user."
								+ "\n'" + utils.Configuration.settings.get("RegisterCommand") + "' - register a new user."
								+ "\n'" + utils.Configuration.settings.get("DeleteUserCommand") + "' - erase a user."
								+ "\n'" + utils.Configuration.settings.get("ChangePassOfCommand") + "' - change a user password, must be root."
								+ "\n'" + utils.Configuration.settings.get("ChangePassCommand") + "' - change your own password."
								+ "\n"
								+ "\n" + pad100("Event")
								+ "\n'" + utils.Configuration.settings.get("NewEventCommand") + "' - create a new personal event."
								+ "\n'" + utils.Configuration.settings.get("GetEventsCommand") + "' - fetch personal events."
								+ "\n"
								+ "\n" + pad100("Find and Search")
								+ "\n'" + utils.Configuration.settings.get("FindPersonCommand") + "' - find a person in the database."
								+ "\n'" + utils.Configuration.settings.get("FindUserId") + "' - find a person in the database by id."

								+ "\n'" + utils.Configuration.settings.get("GetCalendarCommand") + "' - get the current user's calendar." // Partial
								+ "\n'" + utils.Configuration.settings.get("GetCalendarOfCommand") + "' - get the calendar of some user." // Partial
								+ "\n'" + utils.Configuration.settings.get("ChangeUser") + "' - Login as another user."
								+ "\n'" + utils.Configuration.settings.get("StatusCommand") + "' - Get the status of events, bookings, etc." // Partial

								+ "\n'" + utils.Configuration.settings.get("RoomFind") + "' - Find a room."
								+ "\n'" + utils.Configuration.settings.get("RoomFindCommand") + "' - Find a room of specific size."
								+ "\n'" + utils.Configuration.settings.get("CheckBookingTime") + "' - Check to see which rooms are available within a specific time."
								+ "\n'" + utils.Configuration.settings.get("GetAllSubordinateUsersCommand") + "' - Get all connected users of this group."
								+ "\n'" + utils.Configuration.settings.get("FindGroupCommand") + "' - Find a group by name."
								+ "\n'" + utils.Configuration.settings.get("SeeBookingInvitedCommand") + "' - See a list of users that are invited to a booking."
								+ "\n"
								+ "\n" + pad100("Room")

								+ "\n'" + utils.Configuration.settings.get("RegisterRoomCommand") + "' - register a new room."
								+ "\n'" + utils.Configuration.settings.get("RoomBookingCommand") + "' - Book a room."
								+ "\n'" + utils.Configuration.settings.get("RoomBookingWithNameCommand") + "' - Book a room via name."
								+ "\n'" + utils.Configuration.settings.get("RemoveRoomBookingCommand") + "' - Unbook a room."
								+ "\n'" + utils.Configuration.settings.get("RoomBookingInviteCommand") + "' - Invite people to your booking."
								+ "\n'" + utils.Configuration.settings.get("RoomBookingUninviteCommand") + "' - Remove people from a booking."
								+ "\n'" + utils.Configuration.settings.get("RoomBookingAcceptInviteCommand") + "' - Accept someone's room booking invitation."
								+ "\n'" + utils.Configuration.settings.get("RoomBookingDenyInviteCommand") + "' - Deny someone's room booking invitation."
								+ "\n'" + utils.Configuration.settings.get("GetInvitesCommand") + "' - Get all invites aimed at you."
								+ "\n'" + utils.Configuration.settings.get("SeeMyBookingsCommand") + "' - See all the bookings you have accepted."
								+ "\n'" + utils.Configuration.settings.get("SeeMyOwnBookingsCommand") + "' - See all the bookings you own."
								+ "\n'" + utils.Configuration.settings.get("StatusCommand") + "' - Get the status of events, bookings, etc." // Partial
								+ "\n'" + utils.Configuration.settings.get("InviteGroupToBookingCommand") + "' - Invite an entire group to a booking." 
								+ "\n'" + utils.Configuration.settings.get("RoomBookingInviteWithNameCommand") + "' - Invite a person to a booking by the booking name." 
								+ "\n'" + utils.Configuration.settings.get("ChangeBooking") + "' - Change the time and room of the booking."
								+ "\n'" + utils.Configuration.settings.get("ChangeBookingTime") + "' - Change the time of the booking."
								+ "\n"
								+ "\n" + pad100("Group")
								+ "\n'" + utils.Configuration.settings.get("CreateGroupCommand") + "' - Create a group."
								+ "\n'" + utils.Configuration.settings.get("DeleteGroupCommand") + "' - Delete a group."
								+ "\n'" + utils.Configuration.settings.get("AddToGroupCommand") + "' - Add a user to a group."
								+ "\n'" + utils.Configuration.settings.get("RemoveFromGroupCommand") + "' - Remove a user from a group."
								+ "\n'" + utils.Configuration.settings.get("SetGroupParent") + "' - Set the parent group of another group."

								+ "\n'" + utils.Configuration.settings.get("FindGroupCommand") + "' - Find a group by name."
								+ "\n'" + utils.Configuration.settings.get("InviteGroupToBookingCommand") + "' - Invite an entire group to a booking."
								+ "\n'" + utils.Configuration.settings.get("RoomFindCommand") + "' - Find a room of specific size."
								+ "\n'" + utils.Configuration.settings.get("SeeOwnGroups") + "' - See all rooms you're a member of."
								+ "\n'" + utils.Configuration.settings.get("CheckBookingTime") + "' - Check to see which rooms are available within a specific time."
								+ "\n'" + utils.Configuration.settings.get("SeeOwnGroups") + "' - See all groups you're a member of."
								+ "\n"
								+ "\n" + pad100("Notification")
								+ "\n'" + utils.Configuration.settings.get("SeeOwnNotifications") + "' - Se your own notifications.'"
								+ "\n\n"
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
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("SeeBookingInvitedCommand")))
						{
							System.out.print("Which booking ID would you like to view: ");
							String to_view = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("SeeBookingInvitedCommand")
									+ " "
									+ utils.Utils.escapeSpaces(to_view)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("DeleteUserCommand")))
						{
							System.out.print("Type the username to delete: ");
							String username = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("DeleteUserCommand")
									+ " "
									+ utils.Utils.escapeSpaces(username)
								);
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("CreateGroupCommand")))
						{
							System.out.print("Enter the name of the group: ");
							String group_name = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("CreateGroupCommand")
									+ " "
									+ utils.Utils.escapeSpaces(group_name)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("DeleteGroupCommand")))
						{
							System.out.print("Enter the name of the group: ");
							String group_name = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("DeleteGroupCommand")
									+ " "
									+ utils.Utils.escapeSpaces(group_name)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("AddToGroupCommand")))
						{
							System.out.print("Enter the group name to add to: ");
							String group_name = scanner.nextLine();
							System.out.print("Enter the username to add to the group: ");
							String user_to_add = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("AddToGroupCommand")
									+ " "
									+ utils.Utils.escapeSpaces(user_to_add)
									+ " "
									+ utils.Utils.escapeSpaces(group_name)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RemoveFromGroupCommand")))
						{
							System.out.print("Enter the group name to add to: ");
							String group_name = scanner.nextLine();
							System.out.print("Enter the username to remove from the group: ");
							String user_to_remove = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("RemoveFromGroupCommand")
									+ " "
									+ utils.Utils.escapeSpaces(user_to_remove)
									+ " "
									+ utils.Utils.escapeSpaces(group_name)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("GetAllSubordinateUsersCommand")))
						{
							System.out.print("Enter the group name to add to get from: ");
							String group_name = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("GetAllSubordinateUsersCommand")
									+ " "
									+ utils.Utils.escapeSpaces(group_name)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("SetGroupParent")))
						{
							System.out.print("Enter the name of the supergroup: ");
							String group_name = scanner.nextLine();
							System.out.print("Enter the group name of subgroup: ");
							String supergroup = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("SetGroupParent")
									+ " "
									+ utils.Utils.escapeSpaces(group_name)
									+ " "
									+ utils.Utils.escapeSpaces(supergroup)
								);
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
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("SeeMyOwnBookingsCommand")))
						{
							line =
								utils.Utils.escapeSpaces(utils.Configuration.settings.getAndEscape("SeeMyOwnBookingsCommand"));
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
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("FindUserId")))
						{
							System.out.print("Enter the id you'd like to search for: ");
							String like = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("FindUserId")
									+ " "							
									+ utils.Utils.escapeSpaces(like)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("InviteGroupToBookingCommand")))
						{
							System.out.print("Enter the group_id of the group you'd like to invite: ");
							String group_id = scanner.nextLine();
							System.out.print("Enter the booking_id of the booking you'd like to invite to: ");
							String booking_id = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("InviteGroupToBookingCommand")
									+ " "							
									+ utils.Utils.escapeSpaces(group_id)
									+ " "							
									+ utils.Utils.escapeSpaces(booking_id)
								);
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("FindGroupCommand")))
						{
							System.out.print("Enter the group name you'd like to search for: ");
							String like = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("FindGroupCommand")
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
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("GetCalendarOfCommand")))
						{
							System.out.print("Enter the username: ");
							String user = scanner.nextLine();
							System.out.print("Enter the year: ");
							String year = scanner.nextLine();
							System.out.print("Enter the month: ");
							String month = scanner.nextLine();
							System.out.print("Enter the day (leave blank for entire month): ");
							String day = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("GetCalendarOfCommand")
									+ " "							
									+ utils.Utils.escapeSpaces(user)
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
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomBookingWithNameCommand")))
						{
							System.out.println("Enter the title of the booking: ");
							String title = scanner.nextLine();
							System.out.println("Enter the description of the booking: ");
							String description = scanner.nextLine();
							System.out.println("Enter the room name to book: ");
							String room_name = scanner.nextLine();
							System.out.println("Warning time (yyyy-mm-dd HH:MM:ss date format): ");
							String warntime = scanner.nextLine();
							System.out.println("From (yyyy-mm-dd HH:MM:ss date format): ");
							String from = scanner.nextLine();
							System.out.println("To (yyyy-mm-dd HH:MM:ss date format): ");
							String to = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("RoomBookingWithNameCommand")
									+ " "
									+ utils.Utils.escapeSpaces(title)
									+ " "
									+ utils.Utils.escapeSpaces(description)
									+ " "
									+ utils.Utils.escapeSpaces(room_name)
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
							System.out.println("Whom to invite (username): ");
							String users = scanner.nextLine();
							System.out.println("Booking id to invite to: ");
							String booking_id = scanner.nextLine();
							System.out.println("Send notification to the invitee? (yes/no): ");
							String send_note = scanner.nextLine();
							String description = "";
							if (send_note.toLowerCase().equals("yes"))
							{
								System.out.println("Write a message: ");
								description = scanner.nextLine();
							}

							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("RoomBookingInviteCommand")
									+ " "
									+ utils.Utils.escapeSpaces(users)
									+ " "
									+ utils.Utils.escapeSpaces(booking_id)
									+ " "
									+ utils.Utils.escapeSpaces(send_note)
									+ " "
									+ utils.Utils.escapeSpaces(description)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomBookingUninviteCommand")))
						{
							System.out.println("Whom to Uninvite (username): ");
							String users = scanner.nextLine();
							System.out.println("Booking id to invite to: ");
							String booking_id = scanner.nextLine();
							System.out.println("Send notification to the invitee? (yes/no): ");
							String send_note = scanner.nextLine();
							String description = "";
							if (send_note.toLowerCase().equals("yes"))
							{
								System.out.println("Write a message: ");
								description = scanner.nextLine();
							}

							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("RoomBookingUninviteCommand")
									+ " "
									+ utils.Utils.escapeSpaces(users)
									+ " "
									+ utils.Utils.escapeSpaces(booking_id)
									+ " "
									+ utils.Utils.escapeSpaces(send_note)
									+ " "
									+ utils.Utils.escapeSpaces(description)
								);
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomBookingInviteWithNameCommand")))
						{
							System.out.println("Whom to invite (username): ");
							String users = scanner.nextLine();
							System.out.println("Booking name to invite to: ");
							String booking_name = scanner.nextLine();
							System.out.println("Send notification to the invitee? (yes/no): ");
							String send_note = scanner.nextLine();
							String description = "";
							if (send_note.toLowerCase().equals("yes"))
							{
								System.out.println("Write a message: ");
								description = scanner.nextLine();
							}

							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("RoomBookingInviteWithNameCommand")
									+ " "
									+ utils.Utils.escapeSpaces(users)
									+ " "
									+ utils.Utils.escapeSpaces(booking_name)
									+ " "
									+ utils.Utils.escapeSpaces(send_note)
									+ " "
									+ utils.Utils.escapeSpaces(description)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("ChangeBooking")))
						{
							System.out.println("Booking id: ");
							String booking_id = scanner.nextLine();
							System.out.println("Room id to change to: ");
							String room_id = scanner.nextLine();
							System.out.println("From (yyyy-mm-dd HH:MM:ss date format): ");
							String from = scanner.nextLine();
							System.out.println("To (yyyy-mm-dd HH:MM:ss date format): ");
							String to = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("ChangeBookingTime")
									+ " "
									+ utils.Utils.escapeSpaces(booking_id)
									+ " "
									+ utils.Utils.escapeSpaces(room_id)
									+ " "
									+ utils.Utils.escapeSpaces(from)
									+ " "
									+ utils.Utils.escapeSpaces(to)
								);
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("ChangeBookingTime")))
						{
							System.out.println("Booking id: ");
							String booking_id = scanner.nextLine();
							System.out.println("From (yyyy-mm-dd HH:MM:ss date format): ");
							String from = scanner.nextLine();
							System.out.println("To (yyyy-mm-dd HH:MM:ss date format): ");
							String to = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("ChangeBookingTime")
									+ " "
									+ utils.Utils.escapeSpaces(booking_id)
									+ " "
									+ utils.Utils.escapeSpaces(from)
									+ " "
									+ utils.Utils.escapeSpaces(to)
								);
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
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
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("SeeOwnGroups")))
						{
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("SeeOwnGroups")
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("CheckBookingTime")))
						{
							System.out.print("Start time (yyyy-mm-dd HH:MM:ss): ");
							String begin = scanner.nextLine();
							System.out.print("End time (yyyy-mm-dd HH:MM:ss): ");
							String end = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("CheckBookingTime")
									+ " "
									+ utils.Utils.escapeSpaces(begin)
									+ " "
									+ utils.Utils.escapeSpaces(end)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomFind")))
						{
							line =
								utils.Utils.escapeSpaces(utils.Configuration.settings.getAndEscape("RoomFind"));
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("RoomFindCommand")))
						{
							System.out.print("Select the minimum size (blank for 0): ");
							String minsize = scanner.nextLine();
							System.out.print("Select the maximum size (blank for infinite): ");
							String maxsize = scanner.nextLine();
							line =
								utils.Utils.escapeSpaces
								(
									utils.Configuration.settings.getAndEscape("RoomFindCommand")
									+ " "
									+ utils.Utils.escapeSpaces(minsize)
									+ " "
									+ utils.Utils.escapeSpaces(maxsize)
								);
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}
						else if(line.equalsIgnoreCase(utils.Configuration.settings.get("SeeOwnNotifications")))
						{
							line =
								utils.Utils.escapeSpaces(utils.Configuration.settings.getAndEscape("SeeOwnNotifications"));
							System.out.println(ServerReturnData.getPrettyStringWithoutObject(commandLineSendData(client, host, port, login_info, line, scanner)));
						}

						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("SendEcho")))
						{
							System.out.println("Type something you would like to echo: ");
							String echo = scanner.nextLine();

							line =
								utils.Utils.escapeSpaces
								(utils.Configuration.settings.getAndEscape("SendEcho")
									+ " "
									+ utils.Utils.escapeSpaces(echo)
								);
							System.out.println(commandLineSendData(client, host, port, login_info, line, scanner));
						}

						else if (line.equalsIgnoreCase("smug pepe"))
						{
							System.out.println(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,,,,,.-+ssssssssssssss+:,,,,,,,:ossssssss+:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,,,-sdhyyyyyyyyyyyyyyyyhhs.,,:+dyyyyyyyyyyhhho:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,,,-ydhyyyyyyyyyyyyyyyyyyyyhdyhhyyyyyyyyyyyyyyyyds.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n,.shhyyyyyyyyyyyyyyyyyyyyyyyymhyyyyyyyyyyyyyyyyyyhd+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\n-dhyyyyyyyyyyyyyyyyyyyyyyyyyyhmyyyyyyyyyyyyyyyyyyyyds.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\ndhyyyyyyyyyyhddddddhhhhhhhhhhdmdhyyyyyyyyyyyyyyyyyyydy.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyddhhyyyyyyyyyyyyyyyyyhddhhhhddddhhhhhdddhhhdo,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyhdhyyyyyyyyyyyyyyyyyyyyyyhdmdhyyyyyyyyyyyyhhhdm+.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmhyyyyyyyyyyyyyyyhdhs:.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmhyyyyyyyyyyyyyyyyyhdy:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyhhhhhhhhhhhhhhhhhhhhhhhyhmhyyyyyyyyyyyyyyyyyyhdo.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyhhdddhhhhhhhdddddddmddddhhhdddmhyyyyyyyyyhhdhhddhhddy+:.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyddhhhhdddhhdhhhhhdddhhhhdddyyydmdyyyyyyyyhdddhhhhhhhhdmmy/.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyddhhddhhyyyyyyyyyyyyyyhddhhhdddhyydmyyyyyhddhddddhhhhhddhhdms,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyymdydmhyyyyyyyyyyyyhhhhhhyyhdddmddmhyddyyyddhddddhyyyhyyyhddyd.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyydddmddhso///+smdhmNMNMMMd/::::/sdhddymyyyydhdmmNNNNdo+yhyhNmd,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyhhhys+.oMMhoMMMhsoNNs      :mydmdyyhdosM/hMMNddm. .-/yhm-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyhyyhdmMMMoNMM/./NMs       sdyddyhm.yMMmNMMy,.N+    ,oN-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyydddyyyhdmmNNMMMMMM/       ,yhyhyds,dMMhmMMNddN: ,,/ydd-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyydmdyyyyyyyhmmNNm:...../syydmhyhhymNNNNNNNNmhossydhh+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyhddhdddhhhhyyhhhhhhhhyyhdmyyhhhyyyyyyyyyyyyyyhmh.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyhhhhdhhhhhhddmdhyyyyyhdNmddhhhyyyyhhdNms-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyhhhyyyyyyyyyyyyyyyyyyyyyyyyyyydmhyyyyyyyyyyhdddyyyyyhdhdhyhm:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyhhyyyyyyyyyyyyyyyyyyyyyyyyyyhmdhyyyyyyyyyyyyyyyhmdyyyyyyyyyyym:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyhddhhhyyyyyyyyyyyyyyyyyyyhddmdyyyyyyyyyyyyyyyyyyyyddyyyyyyyyyydmy.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyydddmdhhhhyyyyyyyyyyyyyyhdddhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyymhdo,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyymhhhhhddddhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyddhd+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyddhyhdddhhhhdddddddhhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyydmhh:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyhdddhhhdmddhhyyhhhhhhdddhhhhhhhyyyyyyyyyyyyyyyyyyyyyyyyydddddh.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyydmdhhhhhdddddhhhhyyhhhhhhhhdddddddddddddddddddddddddhhddm-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyhhddddhhyhhhhhdddddddddhdhhhhhhhhhhhhhhhhhhhhhhhhhdmdhhs,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyhhddhyyyyyyyyyyyyhhhhhhhhhhhhhhhhhhhhhhhhhhhhhyyhd+,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyhddddhhyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyydy:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyhhddddddddhhhhyyyyyyyyyyyyyyyyyyyyyhhhdmm:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhhhhhhdhdddddddddddddddddddddhhhym.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhhhhhyyyydo,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhddhhhddddhs.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhhdddhyyyyyhhdh-,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdmhyyyyyyyyyyyyhhy+:.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdhddhhdmmdyyyyyyhhdhhyyyyyhhyo.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyddhyyymmhyyddyyhddddy/hhhddyyyyhhs.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmyyyyhmhyhmhdddhhh+.+mddhyyyyyyyhy:,,,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhddyyyyhhdmhhyhho:/smhhyyyyyyyyyyhmo.,,,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmhyyyyymhhs++ydhyyyyyyyyyyyyyyyyhds.,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyymyyyyymy..ydyyyyyyyydmhyyyyyyyyyydo,,,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdyyyyyhhhmyyyyyyyhmdyyyyyyyhyyyyydy-,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhdyyyyyyyhNmmhyyyddhyyyyyydmhyyyhyyh+,,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyhmmyyyyyyyyyyhdmdhmyyyyyyddhyyyydmyyyd.,,,,,,,,,,,,,,,,,,,,,\nyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyydy-/myyyyyyyyyyyyyyddyyyhmdyyyyhmdyyyyhs,,,,,,,,,,,,,,,,,,,,,");
						}
						else if (line.equalsIgnoreCase("jon is kill"))
						{
							System.out.println("\"no\"");
						}
						else if (line.equalsIgnoreCase(utils.Configuration.settings.get("PassCheck")))
						{
							System.out.println("Checking password: " + commandLineSendData(client, host, port, login_info, utils.Configuration.settings.getAndEscape("PassCheck"), scanner));
						}
						else if (line.equalsIgnoreCase(System.getProperty("line.separator")))
						{
						}
						else
						{
							System.out.print("Not a valid command: ");
						}
						System.out.print("Command (type 'help' for info): ");
					}
					catch (Client.UnableToVerifyAuthenticityException exc)
					{
						try
						{
							server.Logger.Log(exc.toString(), "","");
						}
						catch(java.io.IOException ioexc)
						{}
						System.out.println("Sorry, we could not verify the server's authenticity. Check the log file for more details.");
					}
					catch (ServerReturnData.InvalidInputException exc)
						{
						try
						{
							server.Logger.Log(exc.toString(), "","");
						}
						catch(java.io.IOException ioexc)
						{

						}
						System.out.println("The server has responded with empty data. This can be a network anomaly, just retry.");
					}
					catch (Client.SymmetricKeyTooLargeForAsymmetricEncryptionException exc)
					{
						try
						{
							server.Logger.Log(exc.toString(), "","");
						}
						catch(java.io.IOException ioexc)
						{
							
						}
						System.out.println("We can't get an encrypted channel to work at this momemt. Contact the system administrator.");
						// Log.log(Log.Severity.SECURITY, "It appears the symmetric key is too large for asymmetric encryption. Increase the key size in settings.conf.");
						// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					}
					catch (Client.UnableToEncryptAsymmetrically exc)
					{
						try
						{
							server.Logger.Log(exc.toString(), "","");
						}
						catch(java.io.IOException ioexc)
						{
							
						}
						System.out.println("We can't encrypt your data right now.");
						// Log.log(Log.Severity.SECURITY, "It appears the requested method for encryption is not present.");
						// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					}
					catch (Client.AsymmetricKeyInvalidException exc)
					{
						try
						{
							server.Logger.Log(exc.toString(), "","");
						}
						catch(java.io.IOException ioexc)
						{
							
						}
						System.out.println("There was a miscommunication with the server.");
						// Log.log(Log.Severity.SECURITY, "The asymmetric key is invalid.");
						// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					}
					catch (java.net.UnknownHostException exc)
					{
						try
						{
							server.Logger.Log(exc.toString(), "","");
						}
						catch(java.io.IOException ioexc)
						{
							
						}
						System.out.println("The host is unknown...");
						// Log.log(Log.Severity.INPUT, "The host is unknown, see the stack trace.");
						// Log.log(Log.Severity.INPUT, exc.getStackTrace());
					}
					catch (Client.UnableToSendSymmetricKeyToTheServerException exc)
					{
						try
						{
							server.Logger.Log(exc.toString(), "","");
						}
						catch(java.io.IOException ioexc)
						{
							
						}
						System.out.println("Unable to communicate with the server");
						// Log.log(Log.Severity.SECURITY, "The symmetric key could not be sent to the server.");
						// Log.log(Log.Severity.SECURITY, exc.getStackTrace());
					}
				}
			}
		}
		catch (java.util.NoSuchElementException exc)
		{
			try
			{
				server.Logger.Log(exc.toString(), "","");
			}
			catch(java.io.IOException ioexc)
			{
				
			}
			verbose("End of stream, shutting down.");
		}
	}
}