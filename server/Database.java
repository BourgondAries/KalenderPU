package server;

import static utils.Configuration.verbose;

public class Database
{
	private String db_url = null;
	private java.sql.Connection connection = null;
	private java.util.BitSet status = new java.util.BitSet(Status.values().length);

	public static class CouldNotConnectAndSetupDatabaseConnection extends Exception { CouldNotConnectAndSetupDatabaseConnection(Throwable exc) { super(exc); } }
	public static class CouldNotFindEncryptionAlgorithm extends Exception { CouldNotFindEncryptionAlgorithm(Throwable exc) { super(exc); } }
	public static class KeySpecInvalidException extends Exception { KeySpecInvalidException(Throwable exc) { super(exc); } }
	public static class DatabaseUninitializedException extends Exception { DatabaseUninitializedException(Throwable exc) { super(exc); } }

	public Database(String db_url) throws CouldNotConnectAndSetupDatabaseConnection, CouldNotFindEncryptionAlgorithm, KeySpecInvalidException
	{
		this.db_url = db_url;
		try
		{
	        // Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
	        connection = java.sql.DriverManager.getConnection(db_url);

	        java.sql.PreparedStatement prepstatement = connection.prepareStatement("SELECT * FROM SystemUser WHERE username=?");
			prepstatement.setString(1, "root");
			java.sql.ResultSet result = prepstatement.executeQuery();
			if (result.next() == false)
			{
	        	java.sql.PreparedStatement statement 
					= connection.prepareStatement
						(
							"INSERT INTO SystemUser (rank, username, fname, lname, hashedPW) VALUES (0, 'root', '', '', ?)"
							, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY
						);
				try 
				{
					statement.setString(1, PasswordHash.createHash("root"));
				}
				catch (java.security.NoSuchAlgorithmException exc)
				{
					throw new CouldNotFindEncryptionAlgorithm(exc);
				}
				catch (java.security.spec.InvalidKeySpecException exc)
				{
					throw new KeySpecInvalidException(exc);
				}
				statement.execute();
			}
		}
		catch (java.sql.SQLException exc)
		{
			throw new CouldNotConnectAndSetupDatabaseConnection(exc);
		}
	}

	public static enum Status
	{
		INCORRECT_PASSWORD,
		NONEXISTENT_USER,
		CREATED_NEW_USER,
		USER_ALREADY_EXISTS,
		NON_ROOT_TRIED_TO_CHANGE_OTHERS_PASS,
		NON_ROOT_TRIED_TO_DELETE_USER

	}

	public boolean getStatus(Status state_check)
	{
		return status.get(state_check.ordinal());
	}

	private void setStatus(Status state)
	{
		status.set(state.ordinal());
	}

	public void clearStatus()
	{
		status.clear();
	}

	public void closeDatabase() throws java.sql.SQLException
	{
		connection.close();
	}

	// Only call this method from within the server. Runs a query without bounds. For sysadmin purposes ONLY!
	public String runQuery(String query) throws DatabaseUninitializedException
	{
		try
		{
			java.sql.PreparedStatement statement = connection.prepareStatement(query);
			if 
			(
				query.startsWith("UPDATE")
				|| query.startsWith("DELETE")
				|| query.startsWith("INSERT INTO")
				|| query.startsWith("ALTER TABLE")
				|| query.startsWith("DROP")
			)
				return String.valueOf(statement.executeUpdate());
			else
				return resultToString(statement.executeQuery());
		}
		catch (java.sql.SQLException exc)
		{
			return exc.toString();
		}
		catch (NullPointerException exc)
		{
			throw new DatabaseUninitializedException(exc);
		}
	}

	public java.sql.PreparedStatement getPreparedStatement(String query) throws java.sql.SQLException
	{
		return connection.prepareStatement(query);
	}

	public String execute(String username, String password, String query)
	{
		try
		{
			verbose("Delegating input to the input handler.");
			java.sql.PreparedStatement prepstatement = connection.prepareStatement("SELECT * FROM SystemUser WHERE username=?");
			prepstatement.setString(1, username);
			java.sql.ResultSet result = prepstatement.executeQuery();
			if (result.next())
			{
				if (PasswordHash.validatePassword(password, result.getString("hashedPW")))
				{
					User user = new User(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6));
					//System.out.println(query);
					return executeWithValidUser(user, query);
				}
				else
				{
					setStatus(Status.INCORRECT_PASSWORD);
					return "Invalid password for user '" + username + "'.";
				}
			}
			else
			{
				setStatus(Status.NONEXISTENT_USER);
				return "Login username '" + username + "' does not exist.";
			}
		}
		catch (java.sql.SQLException exc)
		{
			verbose(exc.toString());
			return "Unable to execute the query. Please contact the system administrator.";
		}
		catch (java.security.NoSuchAlgorithmException exc)
		{
			verbose(exc.toString());
			return "Unable to hash correctly.";
		}
		catch (java.security.spec.InvalidKeySpecException exc)
		{
			verbose(exc.toString());
			return "Unable to hash correctly.";
		}
	}

	public int resetRootPassword(String password)
		throws 
			java.security.NoSuchAlgorithmException,
			java.security.spec.InvalidKeySpecException,
			java.sql.SQLException
	{
		java.sql.PreparedStatement prepstatement = connection.prepareStatement("UPDATE SystemUser SET hashedPW=? WHERE username='root'");
		prepstatement.setString(1, PasswordHash.createHash(password));
		return prepstatement.executeUpdate();
	}

	public static String resultToString(java.sql.ResultSet result)
	{
		try
		{
			java.sql.ResultSetMetaData resultmetadata = result.getMetaData();
			int columns = resultmetadata.getColumnCount();

			String answer = String.valueOf(columns);
			String tmp = "";
			for (int i = 1; i < columns + 1; ++i)
			{
				tmp = resultmetadata.getColumnName(i);
				answer += " " + utils.Utils.escapeSpaces(tmp);
			}
			tmp = "";
			while (result.next())
			{
				if (columns > 0)
				{
					if (result.getString(1) != null)
						tmp = utils.Utils.escapeSpaces(result.getString(1));
					else
						tmp = "null";
				}
				for (int i = 1; i < columns; ++i)
				{
					if (result.getString(i + 1) != null)
						tmp += " " + utils.Utils.escapeSpaces(result.getString(i + 1));
					else
						tmp += " null";
				}
				answer += " " + utils.Utils.escapeSpaces(tmp);
			}
			return answer;
		}
		catch (java.sql.SQLException exc)
		{
			exc.printStackTrace(); 
		}
		return "";
	}

	public String executeWithValidUser(User user, String query)
	{
		try
		{
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
			for (int i = 0; i < parts.size(); ++i)
				verbose("Parts: '" + parts.get(i) + "'.");
			utils.Configuration coms = utils.Configuration.settings;


			if (parts.get(0).equals(coms.get("RegisterCommand")))
			{
				if (user.username.equals("root"))
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO SystemUser (username, rank, fname, lname, hashedPW) VALUES (?, ?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
					statement.setString(1, parts.get(1));
					statement.setInt(2, Integer.parseInt(parts.get(2)));
					statement.setString(3, parts.get(3));
					statement.setString(4, parts.get(4));
					statement.setString(5, PasswordHash.createHash(parts.get(5)));
					try
					{
						int n = statement.executeUpdate();
						if (n == 1)
						{
							setStatus(Status.CREATED_NEW_USER);
							return "User '" + parts.get(1) + "' registered!";
						}
					}
					catch (java.sql.SQLException exc)
					{
						setStatus(Status.USER_ALREADY_EXISTS);
						return "It's likely that the user you're trying to add (" + parts.get(1) + ") already exists.";
					}
				}
				else
				{
					return "You do not have the privilege to register users.";
				}
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("StatusCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM PersonalEvent WHERE systemUserId=?");
				statement.setInt(1, user.user_id);
				statement.setMaxRows(5);
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("DeleteUserCommand")))
			{
				if (user.username.equals("root"))
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("DELETE FROM SystemUser WHERE username=?");
					statement.setString(1, parts.get(1));
					int result = statement.executeUpdate();
					if (result == 0)
						return "Could not delete this user. The user probably doesn't exist.";
					if (result > 0)
						return "Deleted the user and associated data.";
				}
				setStatus(Status.NON_ROOT_TRIED_TO_DELETE_USER);
				return "You are not allowed to delete users.";
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("CreateGroupCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO SystemGroup (groupAdminId, groupName) VALUES (?, ?)");
				statement.setInt(1, user.user_id);
				statement.setString(2, parts.get(1));
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("DeleteGroupCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("DELETE FROM SystemGroup WHERE groupName=? AND groupAdminId=?");
				statement.setString(1, parts.get(1));
				statement.setInt(2, user.user_id);
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("AddToGroupCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT groupId FROM SystemGroup WHERE groupName=?");
				statement.setString(1, parts.get(2));
				java.sql.ResultSet result = statement.executeQuery();

				if (result.next())
				{
					int group_id = result.getInt(1);

					statement = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
					statement.setString(1, parts.get(1));
					result = statement.executeQuery();

					if (result.next())
					{
						int user_id = result.getInt(1);

						statement = connection.prepareStatement("INSERT INTO GroupMember (systemUserId, groupId) VALUES (?, ?)");
						statement.setInt(1, user_id);
						statement.setInt(2, group_id);
						return String.valueOf(statement.executeUpdate());
					}
				}
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("RemoveFromGroupCommand")))
			{
				// First find the group id of the group name:
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT groupId FROM SystemGroup WHERE groupName=?");
				statement.setString(1, parts.get(2));
				java.sql.ResultSet result = statement.executeQuery();

				if (result.next())
				{
					int group_id = result.getInt(1);

					statement = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
					statement.setString(1, parts.get(1));
					result = statement.executeQuery();

					if (result.next())
					{
						int user_id = result.getInt(1);

						statement = connection.prepareStatement("DELETE FROM GroupMember WHERE systemUserId=? AND groupId=?");
						statement.setInt(1, user_id);
						statement.setInt(2, group_id);
						return String.valueOf(statement.executeUpdate());
					}
				}
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("GetAllSubordinateUsersCommand")))
			{
				// First find the group id of the group name:
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT groupId FROM SystemGroup WHERE groupName=?");
				statement.setString(1, parts.get(1));
				java.sql.ResultSet result = statement.executeQuery();

				if (result.next())
				{
					int group_id = result.getInt(1);

					statement = connection.prepareStatement("SELECT * FROM GroupMember INNER JOIN SystemUser ON SystemUser.systemUserId=GroupMember.systemUserId WHERE groupId=?");
					statement.setInt(1, group_id);
					return resultToString(statement.executeQuery());
				}
			}
			else if (parts.get(0).equals(utils.Configuration.settings.get("SetGroupParent")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT groupId FROM SystemGroup WHERE groupName=?");
				statement.setString(1, parts.get(1));
				java.sql.ResultSet result = statement.executeQuery();

				if (result.next())
				{
					int group_id_super = result.getInt(1);

					statement = connection.prepareStatement("UPDATE SystemGroup SET parentGroupId=? WHERE groupName=?");
					statement.setInt(1, group_id_super);
					statement.setString(2, parts.get(2));
					return String.valueOf(statement.executeUpdate());
				}
			}
			else if (parts.get(0).equals(coms.get("InviteGroupToBookingCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId FROM Groupmember WHERE groupId=?");
				statement.setInt(1, Integer.valueOf(parts.get(1)));
				java.sql.ResultSet result = statement.executeQuery();
				int n = 0;
				while (result.next())
				{
					++n;
					int uid = result.getInt(1);
					inviteUserToBooking(uid, Integer.valueOf(parts.get(2)));

				}
				return "Invited " + n + " users.";
			}
			else if (parts.get(0).equals(coms.get("ChangePassOfCommand")))
			{
				if (user.username.equals("root"))
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE SystemUser SET hashedPW=? WHERE username=?");
					statement.setString(1, PasswordHash.createHash(parts.get(2)));
					statement.setString(2, parts.get(1));
					return String.valueOf(statement.executeUpdate());
				}
				else
				{
					setStatus(Status.NON_ROOT_TRIED_TO_CHANGE_OTHERS_PASS);
					return "Only root can change other users' passwords.";
				}
			}
			else if (parts.get(0).equals(coms.get("ChangePassCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE SystemUser SET hashedPW=? WHERE systemUserId=?");
				statement.setString(1, PasswordHash.createHash(parts.get(1)));
				statement.setInt(2, user.user_id);
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("NewEventCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO PersonalEvent (description, time, timeEnd, systemUserId, warnTime) VALUES (?, ?, ?, ?, ?)");
				statement.setString(1, parts.get(1));
				statement.setTimestamp(2, java.sql.Timestamp.valueOf(parts.get(2)));
				statement.setTimestamp(3, java.sql.Timestamp.valueOf(parts.get(3)));
				statement.setInt(4, user.user_id);
				statement.setInt(5, parts.get(4).equals("") ? 0 : Integer.valueOf(parts.get(4)));
				int affected = statement.executeUpdate();

				Overlap overlap = isOverLappingWithAnything(user.user_id, java.sql.Timestamp.valueOf(parts.get(2)), java.sql.Timestamp.valueOf(parts.get(3)));

				switch (overlap)
				{
					case EVENT:
						sendNotificationToUser(user.user_id, null, "The event overlaps with an already existing event.");
						break;
					case BOOKING:
						sendNotificationToUser(user.user_id, null, "The event overlaps with an already existing booking.");
						break;
					case BOTH:
						sendNotificationToUser(user.user_id, null, "The event overlaps with an already existing event and booking.");
						break;
					default:
						break;
				}
				return String.valueOf(affected);
			}
			else if (parts.get(0).equals(coms.get("GetEventsCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT description, time, timeEnd FROM PersonalEvent WHERE systemUserId=? ORDER BY time ASC");
				statement.setInt(1, user.user_id);
				return resultToString(statement.executeQuery());
			}

			else if (parts.get(0).equals(coms.get("GetInvitesCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM Invitation WHERE systemUserId=?");
				statement.setInt(1, user.user_id);
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("SeeMyBookingsCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM Invitation INNER JOIN Booking ON Booking.bookingId=Invitation.bookingId WHERE (systemUserId=? AND status=1) ORDER BY timeBegin ASC");
				statement.setInt(1, user.user_id);
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("SeeMyOwnBookingsCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM Booking WHERE adminId=?");
				statement.setInt(1, user.user_id);
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("FindPersonCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId, username, rank, fname, lname FROM SystemUser WHERE fname LIKE ? OR lname LIKE ?");
				statement.setString(1, parts.get(1));
				statement.setString(2, parts.get(1));
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("FindGroupCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM SystemGroup WHERE groupName LIKE ?");
				statement.setString(1, parts.get(1));
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("GetCalendarCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement
					(
						"SELECT * FROM PersonalEvent WHERE systemUserId=? AND time >= ? AND time <= ? ORDER BY time"
					);
				statement.setInt(1, user.user_id);

				// Need to return all entries in the calendar for this month.
				if (parts.size() == 4 && parts.get(3).equals("")) // "CMD 'year' 'month' 'day'"
				{
					verbose("Creating timestamp: '" + parts.get(1) + "-" + parts.get(2) + "-01 00:00:00'");
					statement.setTimestamp(2, java.sql.Timestamp.valueOf(parts.get(1) + "-" + parts.get(2) + "-01 00:00:00"));
					if (parts.get(2).equals("12"))
						statement.setTimestamp(3, java.sql.Timestamp.valueOf(String.valueOf(Integer.valueOf(parts.get(1)) + 1) + "-" + parts.get(2) + "-01 00:00:00"));
					else
					{
						verbose("Creating timestamp: '" + parts.get(1) + "-" + String.format("%02d", Integer.valueOf(parts.get(2)) + 1) + "-01 00:00:00'");
						statement.setTimestamp(3, java.sql.Timestamp.valueOf(parts.get(1) + "-" + String.format("%02d", Integer.valueOf(parts.get(2)) + 1) + "-01 00:00:00"));
					}
					java.sql.ResultSet result = statement.executeQuery();
					if (result == null)
						return "We got a null result.";
					else 
					{
						System.out.println("Not a null result");
						return resultToString(result);
					}
				}
				else if (parts.size() == 4)
				{	
					statement.setTimestamp(2, java.sql.Timestamp.valueOf(parts.get(1) + "-" + parts.get(2) + "-" + parts.get(3) + " 00:00:00"));
					statement.setTimestamp(3, java.sql.Timestamp.valueOf(parts.get(1) + "-" + parts.get(2) + "-" + parts.get(3) + " 23:59:59"));
					java.sql.ResultSet result = statement.executeQuery();
					
					return resultToString(result);
				}
				else // Incorrect data size...
				{
					return "The data input is invalid: GetCalendarCommand.";
				}
			}
			else if (parts.get(0).equals(coms.get("GetCalendarOfCommand")))
			{
				
				java.sql.PreparedStatement statement = connection.prepareStatement
				(
					"SELECT * FROM PersonalEvent WHERE systemUserId=(SELECT systemUserId FROM SystemUser WHERE username=?) AND time >= ? AND time <= ? ORDER BY time DESC"
				);
				statement.setString(1, parts.get(1));
				verbose("Creating timestamp: '" + parts.get(2) + "-" + parts.get(3) + "-01 00:00:00'");
				System.out.println(java.sql.Timestamp.valueOf(parts.get(2) + "-" + parts.get(3) + "-01 00:00:00"));
				statement.setTimestamp(2, java.sql.Timestamp.valueOf(parts.get(2) + "-" + parts.get(3) + "-01 00:00:00"));
				if (parts.get(3).equals("12"))
					statement.setTimestamp(3, java.sql.Timestamp.valueOf(String.valueOf(Integer.valueOf(parts.get(2)) + 1) + "-" + parts.get(3) + "-01 00:00:00"));
				else
				{
					verbose("Creating timestamp: '" + parts.get(2) + "-" + String.format("%02d", Integer.valueOf(parts.get(3)) + 1) + "-01 00:00:00'");
					statement.setTimestamp(3, java.sql.Timestamp.valueOf(parts.get(2) + "-" + String.format("%02d", Integer.valueOf(parts.get(3)) + 1) + "-01 00:00:00"));
				}
				java.sql.ResultSet result = statement.executeQuery();
				if (result == null)
					return "We got a null result.";
				else 
				{
					System.out.println("Not a null result");
					return resultToString(result);
				}
			}
			else if (parts.get(0).equals(coms.get("RegisterRoomCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO Room (roomName, size, location) VALUES (?, ?, ?)");
				statement.setString(1, parts.get(1));
				statement.setInt(2, Integer.parseInt(parts.get(2)));
				statement.setString(3, parts.get(3));
				String return_value = String.valueOf(statement.executeUpdate());

				statement = connection.prepareStatement("SELECT roomId FROM Room WHERE roomName=?");
				statement.setString(1, parts.get(1));
				java.sql.ResultSet result = statement.executeQuery();

				if (result.next())
				{
					// Add an empty dummy booking to make the room available.
					statement = connection.prepareStatement("INSERT INTO Booking (adminId, roomId, time, timeEnd) VALUES (?, ?, ?, ?)");
					statement.setInt(1, 1);
					statement.setInt(2, result.getInt(1));
					statement.setTimestamp(3, java.sql.Timestamp.valueOf("1970-01-01 00:00:00"));
					statement.setTimestamp(4, java.sql.Timestamp.valueOf("1970-01-01 00:00:00"));
					return String.valueOf(statement.executeUpdate());
				}
				return return_value;
			}
			else if (parts.get(0).equals(coms.get("RoomBookingCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT Room.roomId, timeBegin, timeEnd FROM Room, Booking WHERE Room.roomId=Booking.roomId AND (timeBegin<? AND timeEnd>? OR timeBegin<? AND timeEnd>? OR timeBegin>=? AND timeEnd<=?) AND Room.roomId=?");

				java.sql.Timestamp begin_time = java.sql.Timestamp.valueOf(parts.get(5)); 
				java.sql.Timestamp end_time = java.sql.Timestamp.valueOf(parts.get(6)); 

				statement.setTimestamp(1, begin_time);
				statement.setTimestamp(2, begin_time);
				statement.setTimestamp(3, end_time);
				statement.setTimestamp(4, end_time);
				statement.setTimestamp(5, begin_time);
				statement.setTimestamp(6, end_time);
				statement.setInt(7, Integer.valueOf(parts.get(3)));
				java.sql.ResultSet result = statement.executeQuery();
				if (!result.next())
				{
					// Then actual register the room under the user.
					statement = connection.prepareStatement("INSERT INTO Booking (adminId, bookingName, description, roomId, warnTime, timeBegin, timeEnd) VALUES (?, ?, ?, ?, ?, ?, ?)");
					statement.setInt(1, user.user_id);
					statement.setString(2, parts.get(1));
					statement.setString(3, parts.get(2));
					statement.setInt(4, Integer.valueOf(parts.get(3)));
					statement.setTimestamp(5, java.sql.Timestamp.valueOf(parts.get(4)));
					statement.setTimestamp(6, java.sql.Timestamp.valueOf(parts.get(5)));
					statement.setTimestamp(7, java.sql.Timestamp.valueOf(parts.get(6)));
					return String.valueOf(statement.executeUpdate());
				}
				return "Could not register the room, it was already occupied.";
			}
			else if (parts.get(0).equals(coms.get("RoomBookingWithNameCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT Room.roomId, timeBegin, timeEnd FROM Room, Booking WHERE Room.roomId=Booking.roomId AND (timeBegin<? AND timeEnd>? OR timeBegin<? AND timeEnd>? OR timeBegin>=? AND timeEnd<=?) AND Room.roomId=(SELECT roomId FROM Room WHERE roomName=?)");

				java.sql.Timestamp begin_time = java.sql.Timestamp.valueOf(parts.get(5)); 
				java.sql.Timestamp end_time = java.sql.Timestamp.valueOf(parts.get(6)); 

				statement.setTimestamp(1, begin_time);
				statement.setTimestamp(2, begin_time);
				statement.setTimestamp(3, end_time);
				statement.setTimestamp(4, end_time);
				statement.setTimestamp(5, begin_time);
				statement.setTimestamp(6, end_time);
				statement.setString(7, parts.get(3));
				java.sql.ResultSet result = statement.executeQuery();
				if (!result.next())
				{
					// Then actual register the room under the user.
					statement = connection.prepareStatement("INSERT INTO Booking (adminId, bookingName, description, roomId, warnTime, timeBegin, timeEnd) VALUES (?, ?, ?, (SELECT roomId FROM Room WHERE roomName=?), ?, ?, ?)");
					statement.setInt(1, user.user_id);
					statement.setString(2, parts.get(1));
					statement.setString(3, parts.get(2));
					statement.setString(4, parts.get(3));
					statement.setTimestamp(5, java.sql.Timestamp.valueOf(parts.get(4)));
					statement.setTimestamp(6, java.sql.Timestamp.valueOf(parts.get(5)));
					statement.setTimestamp(7, java.sql.Timestamp.valueOf(parts.get(6)));
					return String.valueOf(statement.executeUpdate());
				}
				return "Could not register the room, it was already occupied.";
			}
			else if (parts.get(0).equals(coms.get("RemoveRoomBookingCommand")))
			{
				if (!user.username.equals("root"))
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("DELETE FROM Booking WHERE bookingId=? AND adminId=?");
					statement.setInt(1, Integer.valueOf(parts.get(1)));
					statement.setInt(2, user.user_id);
					int affected = statement.executeUpdate();
					statement = connection.prepareStatement("DELETE FROM Invitation WHERE bookingId=?");
					statement.setInt(1, Integer.valueOf(parts.get(1)));
					return String.valueOf(statement.executeUpdate() + affected);
				}
				else
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("DELETE FROM Booking WHERE bookingId=?");
					statement.setInt(1, Integer.valueOf(parts.get(1)));
					return String.valueOf(statement.executeUpdate());
				}
			}
			else if (parts.get(0).equals(coms.get("RoomBookingInviteCommand")))
			{
				// Function of (String systemUserName, Int booking_id)
				String result_string = "";
				if (parts.get(3).toLowerCase().equals("yes"))
				{
					result_string = sendNotificationToUser(parts.get(1), Integer.valueOf(parts.get(2)), parts.get(4));
				}

				Overlap overlap = isOverLappingWithAnything(convertUsernameToId(parts.get(1)), getBookingTimeBegin(Integer.valueOf(parts.get(2))), getBookingTimeEnd(Integer.valueOf(parts.get(2))));

				switch (overlap)
				{
					case EVENT:
						sendNotificationToUser(user.user_id, null, "The booking overlaps with an already existing event.");
						sendNotificationToUser(convertUsernameToId(parts.get(1)), null, "The invite overlaps with an already existing event.");
						break;
					case BOOKING:
						sendNotificationToUser(user.user_id, null, "The booking overlaps with an already existing booking.");
						sendNotificationToUser(convertUsernameToId(parts.get(1)), null, "The invite overlaps with an already existing booking: " + parts.get(2));
						break;
					case BOTH:
						sendNotificationToUser(user.user_id, null, "The booking overlaps with an already existing event and booking.");
						sendNotificationToUser(convertUsernameToId(parts.get(1)), null, "The invite overlaps with an already existing event and booking(" + parts.get(2) + ")");
						break;
					default:
						break;
				}

				return result_string + inviteUserToBooking(parts.get(1), Integer.valueOf(parts.get(2)));
			}
			else if (parts.get(0).equals(coms.get("RoomBookingUninviteCommand")))
			{
				String result_string = "";
				if (parts.get(3).toLowerCase().equals("yes"))
				{
					result_string = sendNotificationToUser(parts.get(1), Integer.valueOf(parts.get(2)), parts.get(4));
				}
				return result_string + uninviteUserToBooking(parts.get(1), Integer.valueOf(parts.get(2)));
			}
			else if (parts.get(0).equals(coms.get("RoomBookingInviteWithNameCommand")))
			{
				// Function of (String systemUserName, Int booking_id)
				String result_string = "";
				if (parts.get(3).toLowerCase().equals("yes"))
				{
					result_string = sendNotificationToUser(parts.get(1), Integer.valueOf(parts.get(2)), parts.get(4));
				}
				return result_string + inviteUserToBooking(parts.get(1), parts.get(2));
			}
			else if (parts.get(0).equals(coms.get("ChangeBooking")))
			{
				// Function of (String systemUserName, Int booking_id)
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE Booking SET roomId=?, timeBegin=?, timeEnd=? WHERE bookingId=?");
				statement.setInt(1, Integer.valueOf(parts.get(2)));
				statement.setTimestamp(2, java.sql.Timestamp.valueOf(parts.get(3)));
				statement.setTimestamp(3, java.sql.Timestamp.valueOf(parts.get(4)));
				statement.setInt(4, Integer.valueOf(parts.get(1)));
				String.valueOf(statement.executeUpdate());

				statement = connection.prepareStatement("SELECT adminId FROM Booking WHERE bookingId=?");
				statement.setInt(1, Integer.valueOf(parts.get(1)));
				java.sql.ResultSet result = statement.executeQuery();
				int users = 0;
				while (result.next())
				{
					++users;
					sendNotificationToUser(result.getInt(1), Integer.valueOf(parts.get(1)), "Changed the booking time of booking id: " + Integer.valueOf(parts.get(1)) + " to " + parts.get(2) + " until " + parts.get(3));
				}
				return "Sent notifications to: " + users + ".";
			}
			else if (parts.get(0).equals(coms.get("ChangeBookingTime")))
			{
				// Function of (String systemUserName, Int booking_id)
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE Booking SET timeBegin=?, timeEnd=? WHERE bookingId=?");
				statement.setTimestamp(1, java.sql.Timestamp.valueOf(parts.get(2)));
				statement.setTimestamp(2, java.sql.Timestamp.valueOf(parts.get(3)));
				statement.setInt(3, Integer.valueOf(parts.get(1)));
				String.valueOf(statement.executeUpdate());

				statement = connection.prepareStatement("SELECT adminId FROM Booking WHERE bookingId=?");
				statement.setInt(1, Integer.valueOf(parts.get(1)));
				java.sql.ResultSet result = statement.executeQuery();
				int users = 0;
				while (result.next())
				{
					++users;
					sendNotificationToUser(result.getInt(1), Integer.valueOf(parts.get(1)), "Changed the booking time of booking id: " + Integer.valueOf(parts.get(1)) + " to " + parts.get(2) + " until " + parts.get(3));
				}
				return "Sent notifications to: " + users + ".";
			}
			else if (parts.get(0).equals(coms.get("RoomBookingAcceptInviteCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE Invitation SET status=1 WHERE systemUserId=? AND bookingId=?");
				System.out.println("Accepting invite...");
				statement.setInt(1, user.user_id);
				statement.setString(2, parts.get(1));
				String result_string = String.valueOf(statement.executeUpdate());

				java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT username FROM systemUser WHERE systemUserId=(SELECT adminId FROM booking WHERE bookingId=?)");
				s1.setString(1, parts.get(1));
				java.sql.ResultSet result = s1.executeQuery();

				if (result.next())
				{
					result_string += sendNotificationToUser(result.getString(1), Integer.parseInt(parts.get(1)), "'" + user.username + "' accepted your booking invitation");
				}

				return result_string;
			}
			else if (parts.get(0).equals(coms.get("FindUserId")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId, username, rank, fname, lname FROM SystemUser WHERE systemUserId=?");
				statement.setInt(1, Integer.valueOf(parts.get(1)));
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("SeeBookingInvitedCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT bookingId, status, wantsWarning, SystemUser.systemUserId, username, fname, lname FROM Invitation, SystemUser WHERE bookingId=? AND Invitation.systemUserId=SystemUser.systemUserId");
				statement.setInt(1, Integer.valueOf(parts.get(1)));
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("RoomBookingDenyInviteCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE Invitation SET status=-1 WHERE systemUserId=? AND bookingId=?");
				statement.setInt(1, user.user_id);
				statement.setInt(2, Integer.valueOf(parts.get(1)));

				String result_string = String.valueOf(statement.executeUpdate());

				java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT username FROM systemUser WHERE systemUserId=(SELECT adminId FROM booking WHERE bookingId=?)");
				s1.setString(1, parts.get(1));
				java.sql.ResultSet result = s1.executeQuery();
				//System.out.println(result.getString(1));
				if (result.next())
				{
					result_string += sendNotificationToUser(result.getString(1), Integer.parseInt(parts.get(1)), "'" + user.username + "' denied your booking invitation");
				}

				statement = connection.prepareStatement("SELECT * FROM Booking WHERE adminId=? AND bookingId=?");
				statement.setInt(1, user.user_id);
				statement.setInt(2, Integer.valueOf(parts.get(1)));
				java.sql.ResultSet result_set_1 = statement.executeQuery();
				if (result_set_1.next())
				{
					statement = connection.prepareStatement("DELETE FROM Invitation WHERE bookingId=?");
					statement.setInt(1, Integer.valueOf(parts.get(1)));
					String ret_data = "Removed the booking, affected: " + statement.executeUpdate() + " users";

					statement = connection.prepareStatement("DELETE FROM Notification WHERE bookingId=?");
					statement.setInt(1, Integer.valueOf(parts.get(1)));
					statement.executeUpdate();

					statement = connection.prepareStatement("DELETE FROM Booking WHERE bookingId=?");
					statement.setInt(1, Integer.valueOf(parts.get(1)));
					statement.executeUpdate();

					return ret_data;
				}

				return result_string;
			}
			else if (parts.get(0).equals(coms.get("RoomFind")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM Room");
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("RoomFindCommand")))
			{
				java.sql.PreparedStatement statement = null;
				if (parts.get(1).equals(""))
				{
					if (parts.get(2).equals(""))
					{
						statement = connection.prepareStatement("SELECT * FROM Room");
						return resultToString(statement.executeQuery());
					}
					else
					{
						statement = connection.prepareStatement("SELECT * FROM Room WHERE size<=?");
						statement.setInt(1, Integer.valueOf(parts.get(2)));
						return resultToString(statement.executeQuery());
					}
				}
				else
				{
					if (parts.get(2).equals(""))
					{
						statement = connection.prepareStatement("SELECT * FROM Room WHERE size>=?");
						statement.setInt(1, Integer.valueOf(parts.get(1)));
						return resultToString(statement.executeQuery());
					}
					else
					{
						statement = connection.prepareStatement("SELECT * FROM Room WHERE size<=? AND size>=?");
						statement.setInt(1, Integer.valueOf(parts.get(2)));
						statement.setInt(2, Integer.valueOf(parts.get(1)));
						return resultToString(statement.executeQuery());
					}
				}
			}
			else if (parts.get(0).equals(coms.get("FindPersonCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId, username, rank, fname, lname FROM SystemUser WHERE fname LIKE ? OR lname LIKE ?");
				statement.setString(1, parts.get(1));
				statement.setString(2, parts.get(1));
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("SeeOwnGroups")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT SystemGroup.groupId, SystemGroup.groupName, SystemGroup.parentGroupId FROM SystemUser, GroupMember, SystemGroup WHERE SystemUser.systemUserId=? AND SystemUser.systemUserId=GroupMember.systemUserId AND GroupMember.groupId=SystemGroup.groupId");
				statement.setInt(1, user.user_id);
				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("CheckBookingTime")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT Room.roomId, Room.roomName FROM Room WHERE Room.roomId NOT IN (SELECT DISTINCT Room.roomId FROM Room, Booking WHERE Room.roomId=Booking.roomId AND (timeBegin<=? AND timeEnd>=? OR timeBegin<=? AND timeEnd>=? OR timeBegin>=? AND timeEnd<=?))");
				java.sql.Timestamp begin_time = java.sql.Timestamp.valueOf(parts.get(1));
				java.sql.Timestamp end_time = java.sql.Timestamp.valueOf(parts.get(2));

				statement.setTimestamp(1, begin_time);
				statement.setTimestamp(2, begin_time);
				statement.setTimestamp(3, end_time);
				statement.setTimestamp(4, end_time);
				statement.setTimestamp(5, begin_time);
				statement.setTimestamp(6, end_time);
				return resultToString(statement.executeQuery());
			}

			else if (parts.get(0).equals(coms.get("SeeOwnNotifications")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE Notification SET seen=true WHERE systemUserId=?");
				statement.setInt(1, user.user_id);
				statement.executeUpdate();

				statement = connection.prepareStatement("SELECT * FROM Notification WHERE systemUserId=?");
				statement.setInt(1, user.user_id);
				return resultToString(statement.executeQuery());

			}

			else if (parts.get(0).equals(coms.get("SendEcho")))
			{
				return parts.get(1);
			}

			else if (parts.get(0).equals(coms.get("PassCheck")))
			{
				return "Password is valid.";
			}
			else
				return "No handler for the command: " + parts.get(0);
		}
		catch (Exception exc)
		{
			verbose("An exception ocurred during execution: " + exc.toString());
			exc.printStackTrace();
			return exc.toString();
		}
		return "Impossible.";
	}

	public java.sql.Timestamp getBookingTimeBegin(int booking_id) 
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement statement = connection.prepareStatement("SELECT timeBegin FROM Booking WHERE bookingId=?");
		statement.setInt(1, booking_id);
		java.sql.ResultSet result = statement.executeQuery();
		if (result.next())
			return result.getTimestamp(1);
		else
			return java.sql.Timestamp.valueOf("1970-01-01 00:00:00");
	}

	public java.sql.Timestamp getBookingTimeEnd(int booking_id) 
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement statement = connection.prepareStatement("SELECT timeEnd FROM Booking WHERE bookingId=?");
		statement.setInt(1, booking_id);
		java.sql.ResultSet result = statement.executeQuery();
		if (result.next())
			return result.getTimestamp(1);
		else
			return java.sql.Timestamp.valueOf("1970-01-01 00:00:00");
	}

	public String uninviteUserToBooking(String username_to_invite, String booking_name)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT bookingId FROM Booking WHERE bookingName=?");
		s1.setString(1, booking_name);
		java.sql.ResultSet result = s1.executeQuery();

		if (result.next())
		{
			int booking_id = result.getInt(1);
			if (result.next())
			{
				return "Too many bookings with the same name.";
			}
			return uninviteUserToBooking(username_to_invite, booking_id);
		}

		return "No such user found '" + username_to_invite + "'."; 
	}

	public String uninviteUserToBooking(String username_to_invite, int booking_id)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
		s1.setString(1, username_to_invite);
		java.sql.ResultSet result = s1.executeQuery();

		if (result.next())
			return uninviteUserToBooking(result.getInt(1), booking_id);

		return "No such user found '" + username_to_invite + "'."; 
	}

	public String uninviteUserToBooking(int user_id, int booking_id)
	{
		java.sql.PreparedStatement statement = null;
		String result_string = "";
		try 
		{
			statement = connection.prepareStatement("DELETE FROM Invitation WHERE systemUserId=? AND bookingId=?");
			statement.setInt(1, user_id);
			statement.setInt(2, booking_id);
			result_string = String.valueOf(statement.executeUpdate());
		}
		catch (java.sql.SQLException exc)
		{
			return "User could not be uninvited";
		}

		return result_string;
	}

	public String inviteUserToBooking(String username_to_invite, String booking_name)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT bookingId FROM Booking WHERE bookingName=?");
		s1.setString(1, booking_name);
		java.sql.ResultSet result = s1.executeQuery();

		if (result.next())
		{
			int booking_id = result.getInt(1);
			if (result.next())
			{
				return "Too many bookings with the same name.";
			}
			return inviteUserToBooking(username_to_invite, booking_id);
		}

		return "No such user found '" + username_to_invite + "'."; 
	}

	public String inviteUserToBooking(String username_to_invite, int booking_id)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
		s1.setString(1, username_to_invite);
		java.sql.ResultSet result = s1.executeQuery();

		if (result.next())
			return inviteUserToBooking(result.getInt(1), booking_id);

		return "No such user found '" + username_to_invite + "'."; 
	}

	public String inviteUserToBooking(int user_id, int booking_id)
	{
		java.sql.PreparedStatement statement = null;
		String result_string = "";
		try 
		{
			statement = connection.prepareStatement("INSERT INTO Invitation (systemUserId, bookingId) VALUES (?, ?)");
			statement.setInt(1, user_id);
			statement.setInt(2, booking_id);
			result_string = String.valueOf(statement.executeUpdate());
		}
		catch (java.sql.SQLException exc)
		{
			return "User already invited" ;
		}

		return result_string;
	}

	public String sendNotificationToUser(String user_name, Integer booking_id, String message)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
		s1.setString(1, user_name);
		java.sql.ResultSet result = s1.executeQuery();

		if (result.next())
		{
			s1 = connection.prepareStatement("INSERT INTO Notification (message, bookingId, systemUserId) VALUES (?, ?, ?)");
			s1.setString(1, message);
			s1.setInt(2, booking_id);
			s1.setInt(3, result.getInt(1));
		}
			
		return String.valueOf(s1.executeUpdate());
	}

	public String sendNotificationToUser(int user_id, Integer booking_id, String message)
		throws
			java.sql.SQLException
	{
		if (booking_id != null)
		{
			java.sql.PreparedStatement s1 = connection.prepareStatement("INSERT INTO Notification (message, bookingId, systemUserId) VALUES (?, ?, ?)");
			s1.setString(1, message);
			s1.setInt(2, booking_id);
			s1.setInt(3, user_id);
			return String.valueOf(s1.executeUpdate());
		}
		else
		{
			java.sql.PreparedStatement s1 = connection.prepareStatement("INSERT INTO Notification (message, systemUserId) VALUES (?, ?)");
			s1.setString(1, message);
			s1.setInt(2, user_id);
			return String.valueOf(s1.executeUpdate());
		}
	}

	public static enum Overlap
	{
		BOTH,
		BOOKING,
		EVENT,
		NOTHING
	}

	public Overlap isOverLappingWithAnything(String username, java.sql.Timestamp time_start, java.sql.Timestamp time_end)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
		statement.setString(1, username);
		java.sql.ResultSet result = statement.executeQuery();
		if (result.next())
		{
			return isOverLappingWithAnything(result.getInt(1), time_start, time_end);
		}
		else
		{
			// throw user not found exception
			return Overlap.NOTHING;
		}
	}

	int convertUsernameToId(String username)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
		statement.setString(1, username);
		java.sql.ResultSet result = statement.executeQuery();
		if (result.next())
			return result.getInt(1);
		else
			return -1;
	}

	public Overlap isOverLappingWithAnything(int user_id, java.sql.Timestamp time_start, java.sql.Timestamp time_end)
		throws
			java.sql.SQLException
	{
		java.sql.PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM PersonalEvent WHERE PersonalEvent.systemUserId=? AND (time<=? AND timeEnd>=? OR time<=? AND timeEnd>=? OR time>=? AND timeEnd<=?)");
		java.sql.Timestamp begin_time = time_start;
		java.sql.Timestamp end_time = time_end;

		statement.setInt(1, user_id);
		statement.setTimestamp(2, begin_time);
		statement.setTimestamp(3, begin_time);
		statement.setTimestamp(4, end_time);
		statement.setTimestamp(5, end_time);
		statement.setTimestamp(6, begin_time);
		statement.setTimestamp(7, end_time);
		java.sql.ResultSet result = statement.executeQuery();
		boolean overlaps_with_event = false;
		if (result.next())
		{
			int result_count = result.getInt(1);
			if (result_count > 0)
				overlaps_with_event = true;
		}


		statement = connection.prepareStatement("SELECT COUNT(*) FROM Invitation, Booking WHERE Invitation.bookingId=Booking.bookingId AND Invitation.status=1 AND Invitation.systemUserId=? AND (timeBegin<=? AND timeEnd>=? OR timeBegin<=? AND timeEnd>=? OR timeBegin>=? AND timeEnd<=?)");

		statement.setInt(1, user_id);
		statement.setTimestamp(2, begin_time);
		statement.setTimestamp(3, begin_time);
		statement.setTimestamp(4, end_time);
		statement.setTimestamp(5, end_time);
		statement.setTimestamp(6, begin_time);
		statement.setTimestamp(7, end_time);
		result = statement.executeQuery();
		boolean overlaps_with_accepted_booking_invitation = false;
		if (result.next())
		{
			int result_count = result.getInt(1);
			if (result_count > 0)
				overlaps_with_accepted_booking_invitation = true;
		}




		if (overlaps_with_event && overlaps_with_accepted_booking_invitation)
			return Overlap.BOTH;
		else if (overlaps_with_accepted_booking_invitation)
			return Overlap.BOOKING;
		else if (overlaps_with_event)
			return Overlap.EVENT;
		else
			return Overlap.NOTHING;
	}

}