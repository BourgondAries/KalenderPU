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
		NON_ROOT_TRIED_TO_CHANGE_OTHERS_PASS
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
		catch (Exception exc)
		{
			verbose(exc.toString());
			return "Unable to execute the query. Please contact the system administrator.";
		}
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
		catch (Exception exc)
		{
			exc.printStackTrace(); 
		}
		finally
		{
			return "";
		}
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
				if (user.rank < 10)
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
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("GetEventsCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT description, time FROM PersonalEvent WHERE systemUserId=? ORDER BY time ASC");
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
			else if (parts.get(0).equals(coms.get("GetCalendarCommand")))
			{
				// Need to return all entries in the calendar for this month.
				if (parts.size() == 4 && parts.get(3).equals("")) // "CMD 'year' 'month' 'day'"
				{
					java.sql.PreparedStatement statement = connection.prepareStatement
					(
						"SELECT * FROM PersonalEvent WHERE systemUserId=? AND time >= ? AND time <= ? ORDER BY time"
					);
					statement.setInt(1, user.user_id);
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
						try
						{
							return resultToString(result);
						}
						catch (Exception exc)
						{
							exc.printStackTrace();
						}
					}
				}
				else // Incorrect data size...
				{

				}
			}
			else if (parts.get(0).equals(coms.get("RegisterRoomCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO Room (roomName, size, location) VALUES (?, ?, ?)");
				statement.setString(1, parts.get(1));
				statement.setInt(2, Integer.parseInt(parts.get(2)));
				statement.setString(3, parts.get(3));
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("RoomBookingCommand")))
			{

				// Have to check if the room is available.

				// Then actual register the room under the user.
				java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO Booking (adminId, bookingName, description, roomId, warnTime, timeBegin, timeEnd) VALUES (?, ?, ?, ?, ?, ?, ?)");
				statement.setInt(1, user.user_id);
				statement.setString(2, parts.get(1));
				statement.setString(3, parts.get(2));
				statement.setInt(4, Integer.valueOf(parts.get(3)));
				statement.setTimestamp(5, java.sql.Timestamp.valueOf(parts.get(4)));
				statement.setTimestamp(6, java.sql.Timestamp.valueOf(parts.get(5)));
				statement.setTimestamp(7, java.sql.Timestamp.valueOf(parts.get(6)));
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("RemoveRoomBookingCommand")))
			{
				if (!user.username.equals("root"))
				{
					java.sql.PreparedStatement statement = connection.prepareStatement("DELETE FROM Booking WHERE bookingId=? AND adminId=?");
					statement.setInt(1, Integer.valueOf(parts.get(1)));
					statement.setInt(2, user.user_id);
					return String.valueOf(statement.executeUpdate());
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
				java.sql.PreparedStatement s1 = connection.prepareStatement("SELECT systemUserId FROM SystemUser WHERE username=?");
				s1.setString(1, parts.get(1));
				java.sql.ResultSet result = s1.executeQuery();
				if (result.next())
				{
					int invite_id = result.getInt(1);
					java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO Invitation (systemUserId, bookingId) VALUES (?, ?)");
					statement.setInt(1, invite_id);
					statement.setInt(2, Integer.valueOf(parts.get(2)));
					return String.valueOf(statement.executeUpdate());
				}
			}
			else if (parts.get(0).equals(coms.get("RoomBookingAcceptInviteCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE Invitation SET status=true WHERE systemUserId=? AND bookingId=?");
				statement.setString(1, parts.get(user.user_id));
				statement.setString(2, parts.get(1));
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("RoomBookingDenyInviteCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE Invitation SET status=false WHERE systemUserId=? AND bookingId=?");
				statement.setString(1, parts.get(user.user_id));
				statement.setString(2, parts.get(1));
				return String.valueOf(statement.executeUpdate());
			}
			else if (parts.get(0).equals(coms.get("RoomFind")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT * FROM Room");

				return resultToString(statement.executeQuery());
			}
			else if (parts.get(0).equals(coms.get("FindPersonCommand")))
			{
				java.sql.PreparedStatement statement = connection.prepareStatement("SELECT systemUserId, username, rank, fname, lname FROM SystemUser WHERE fname LIKE ? OR lname LIKE ?");
				statement.setString(1, parts.get(1));
				statement.setString(2, parts.get(1));
				return resultToString(statement.executeQuery());
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
			return exc.toString();
		}
		return "Impossible.";
	}
}