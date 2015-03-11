package server;

import static org.junit.Assert.*;

public class TestDatabase
{
	Database db = null;
	User random_user = null;
	User user_root = null;


	@org.junit.Test
	public void testWrongPasswordWhenExecute() throws Exception
	{	
		utils.Configuration.loadDefaultConfiguration();
		User user_root = new User(1, 1, "root", "", "", PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		db.execute(user_root.username, "password_gerp", utils.Configuration.settings.get("PassCheck"));
		assertTrue(db.getStatus(Database.Status.INCORRECT_PASSWORD));
	}

	@org.junit.Test
	public void testUsernameNotExisting() throws Exception
	{	
		utils.Configuration.loadDefaultConfiguration();
		//User userR = new User(0,1,"root","","",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		db.execute("nonexistingname", "password", utils.Configuration.settings.get("PassCheck"));
		assertTrue(db.getStatus(Database.Status.NONEXISTENT_USER));
	}

	@org.junit.Test
	public void testRegisterCommand()  throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		User random_user = addRandomUserToDatabase();

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT COUNT(*) FROM systemUser WHERE username=?");
		prep_statement.setString(1, random_user.username);
		String selection_result= Database.resultToString(prep_statement.executeQuery());
		assertEquals(Integer.parseInt("" + selection_result.charAt(0)), 1);
	}
	
	//@org.junit.Rule
  	//public org.junit.rules.ExpectedException exception = org.junit.rules.ExpectedException.none();

	@org.junit.Test 
	public void testAddingDuplicateUserShouldFail() throws Exception
	{

		utils.Configuration.loadDefaultConfiguration();
		setup();
		
		db.executeWithValidUser
		(
			user_root, 
			utils.Configuration.settings.getAndEscape("RegisterCommand")
			+ " "
			+ utils.Utils.escapeSpaces("root")
			+ " "
			+ utils.Utils.escapeSpaces("1")
			+ " "
			+ utils.Utils.escapeSpaces("TestFname")
			+ " "
			+ utils.Utils.escapeSpaces("TestLname")
			+ " "
			+ utils.Utils.escapeSpaces("12345")
		);

		assertTrue(db.getStatus(Database.Status.USER_ALREADY_EXISTS));
	}
	@org.junit.Test 
	public void testChangeYourOwnPassword() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		User random_user = addRandomUserToDatabase();
		String new_password = "newPW";

		db.executeWithValidUser
		(
			random_user, 
			utils.Configuration.settings.getAndEscape("ChangePassCommand")
			+ " " 
			+ utils.Utils.escapeSpaces(new_password)
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT hashedPW FROM systemUser WHERE username=?");
		prep_statement.setString(1, random_user.username);
		java.util.ArrayList<String> result = utils.Utils.splitAndUnescapeString(Database.resultToString(prep_statement.executeQuery()));
		assertTrue(PasswordHash.validatePassword(new_password, result.get(Integer.parseInt(result.get(0)) + 1)));
	}
	
	//@org.junit.Rule
  	//public org.junit.rules.ExpectedException exception = org.junit.rules.ExpectedException.none();
	@org.junit.Test
	public void testUserCannotChangeOthersPass() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		User random_user = addRandomUserToDatabase();
		db.executeWithValidUser
		(
			random_user,
			utils.Configuration.settings.getAndEscape("ChangePassOfCommand")
			+ " "
			+ utils.Utils.escapeSpaces("root")
			+ " "
			+ utils.Utils.escapeSpaces("newRootPassword")
		);
		assertTrue(db.getStatus(Database.Status.NON_ROOT_TRIED_TO_CHANGE_OTHERS_PASS));
	}

	@org.junit.Test
	public void testRootCanChangeOthersPass() throws Exception
	{
		User random_user = addRandomUserToDatabase();
		setup();
		String new_password = "newPW";
		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("ChangePassOfCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_user.username)
			+ " "
			+ utils.Utils.escapeSpaces(new_password)
		);
		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT hashedPW FROM systemUser WHERE username=?");
		prep_statement.setString(1, random_user.username);
		java.util.ArrayList<String> result = utils.Utils.splitAndUnescapeString(Database.resultToString(prep_statement.executeQuery()));
		assertTrue(PasswordHash.validatePassword(new_password, result.get(Integer.parseInt(result.get(0)) + 1)));
	}

	@org.junit.Test
	public void testAddNewEventCommand() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();

		String rand_str = utils.Utils.makeRandomString(10);
		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("NewEventCommand")
			+ " "
			+ utils.Utils.escapeSpaces(rand_str)
			+ " "
			+ utils.Utils.escapeSpaces("2000-03-20 12:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-20 14:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("30")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM PersonalEvent WHERE description=?");
		prep_statement.setString(1, rand_str);
		String selection_result = Database.resultToString(prep_statement.executeQuery());

		try
		{
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			assertTrue(parts.size() > 0);
		}
		catch (Exception exc)
		{
			fail("Failed to add event");
		}
	}

	@org.junit.Test
	public void testFindSystemUser() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		User random_user = addRandomUserToDatabase();


		db.executeWithValidUser
		(
			random_user,
			utils.Configuration.settings.getAndEscape("FindPersonCommand")
			+ " "							
			+ utils.Utils.escapeSpaces("name")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT systemUserId, username, rank, fname, lname FROM SystemUser WHERE fname LIKE ? OR lname LIKE ?");
		prep_statement.setString(1, "Test%");
		prep_statement.setString(2, "Test%");
		String selection_result = Database.resultToString(prep_statement.executeQuery());

		try
		{
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			assertTrue(parts.size() > 0);
		}
		catch (Exception exc)
		{
			fail("Failed to find a systemUser");
		}
	}

	@org.junit.Test
	public void testRegisterRoom() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		String random_str = utils.Utils.makeRandomString(8);

		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RegisterRoomCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_str)
			+ " "
			+ utils.Utils.escapeSpaces("0")
			+ " "
			+ utils.Utils.escapeSpaces("location")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM room WHERE roomName=?");
		prep_statement.setString(1, random_str);
		String selection_result = Database.resultToString(prep_statement.executeQuery());

		try
		{
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			assertTrue(parts.size() > 0);
		}
		catch (Exception exc)
		{
			fail("Failed to register room");
		}
	}

	@org.junit.Test
	public void testRoomBookingCommand() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		String random_str = utils.Utils.makeRandomString(8);

		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RoomBookingCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_str)
			+ " "
			+ utils.Utils.escapeSpaces("description")
			+ " "
			+ utils.Utils.escapeSpaces("1")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 12:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 13:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 15:00:00")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM Booking WHERE bookingName=?");
		prep_statement.setString(1, random_str);
		String selection_result = Database.resultToString(prep_statement.executeQuery());

		try
		{
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			assertTrue(parts.size() > 0);
		}
		catch (Exception exc)
		{
			fail("Failed to book room");
		}
		
	}

	@org.junit.Test
	public void testFindRoom() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();

		String random_str = utils.Utils.makeRandomString(8);

		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RegisterRoomCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_str)
			+ " "
			+ utils.Utils.escapeSpaces("0")
			+ " "
			+ utils.Utils.escapeSpaces("location")
		);

		String result =
			db.executeWithValidUser
			(
				user_root,
				utils.Configuration.settings.getAndEscape("RoomFind")
			);

		try
		{
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(result);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			assertTrue(parts.size() > 0);
		}
		catch (Exception exc)
		{
			fail("Failed to book room");
		}
		
	}

	@org.junit.Test
	public void testDeleteUser() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		random_user = addRandomUserToDatabase();
		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("DeleteUserCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_user.username)
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT COUNT(*) FROM SystemUser WHERE username=?");
		prep_statement.setString(1, random_user.username);
		String selection_result = Database.resultToString(prep_statement.executeQuery());
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
		int columns = Integer.parseInt(parts.get(2));
		
		assertEquals(0,columns);
	}

	@org.junit.Test
	public void testOnlyRootCanDeleteUser() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		random_user = addRandomUserToDatabase();
		User random_user2 = addRandomUserToDatabase();
		db.executeWithValidUser
		(
			random_user,
			utils.Configuration.settings.getAndEscape("DeleteUserCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_user.username)
		);

		assertTrue(db.getStatus(Database.Status.NON_ROOT_TRIED_TO_DELETE_USER));
	}


	@org.junit.Test
	public void testRoomBookingInviteCommand () throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		random_user = addRandomUserToDatabase();
		String random_str = utils.Utils.makeRandomString(8);

		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RoomBookingCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_str)
			+ " "
			+ utils.Utils.escapeSpaces("description")
			+ " "
			+ utils.Utils.escapeSpaces("1")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 12:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 13:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 15:00:00")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT bookingId FROM Booking WHERE bookingName=?");
		prep_statement.setString(1, random_str);
		String selection_result = Database.resultToString(prep_statement.executeQuery());

		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);

		String new_bookingId = parts.get(0);

		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RoomBookingInviteCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_user.username)
			+ " "
			+ utils.Utils.escapeSpaces(new_bookingId)
		);

		prep_statement = db.getPreparedStatement("SELECT * FROM Invitation WHERE bookingId=?");
		prep_statement.setString(1,new_bookingId);
		selection_result = Database.resultToString(prep_statement.executeQuery());

		parts = utils.Utils.splitAndUnescapeString(selection_result);
		int columns = Integer.parseInt(parts.get(0));
		parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));

		assertTrue(parts.size() > 0);

	}


	@org.junit.Test
	public void testAcceptAndDenyRoomBookingInvitation() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setup();
		random_user = addRandomUserToDatabase();
		String random_str = utils.Utils.makeRandomString(8);

		//Adds random booking to database.
		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RoomBookingCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_str)
			+ " "
			+ utils.Utils.escapeSpaces("description")
			+ " "
			+ utils.Utils.escapeSpaces("1")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 12:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 13:00:00")
			+ " "
			+ utils.Utils.escapeSpaces("2015-03-30 15:00:00")
		);
		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT bookingId FROM Booking WHERE bookingName=?");
		prep_statement.setString(1, random_str);
		String selection_result = Database.resultToString(prep_statement.executeQuery());

		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);

		String new_bookingId = parts.get(0);

		// Invite random user
		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("RoomBookingInviteCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_user.username)
			+ " "
			+ utils.Utils.escapeSpaces(new_bookingId)
		);
		
		//Try to accept invitation
		db.executeWithValidUser
		(
			random_user,
			utils.Configuration.settings.getAndEscape("RoomBookingAcceptInviteCommand")
			+ " "
			+ utils.Utils.escapeSpaces(new_bookingId)
		);

		prep_statement = db.getPreparedStatement("SELECT status FROM Invitation WHERE (systemUserId =? AND bookingId=?)");
		prep_statement.setInt(1, random_user.user_id);
		prep_statement.setString(2, new_bookingId);
		selection_result = db.resultToString(prep_statement.executeQuery());

		parts = utils.Utils.splitAndUnescapeString(selection_result);

		// Test status = true
		assertEquals("true",parts.get(2));

		// Try to deny invitaion
		db.executeWithValidUser
		(
			random_user,
			utils.Configuration.settings.getAndEscape("RoomBookingDenyInviteCommand")
			+ " "
			+ utils.Utils.escapeSpaces(new_bookingId)
		);

		prep_statement = db.getPreparedStatement("SELECT status FROM Invitation WHERE (systemUserId =? AND bookingId=?)");
		prep_statement.setInt(1, random_user.user_id);
		prep_statement.setString(2, new_bookingId);
		selection_result = db.resultToString(prep_statement.executeQuery());

		parts = utils.Utils.splitAndUnescapeString(selection_result);

		//Test status = false
		assertEquals("false",parts.get(2));

	}
/*
	@org.junit.Test
	public void testDenyRoomBookingInvitation() 
	{
		//TODO
	}
	*/
	

	private void setup() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		db = new Database(utils.Configuration.settings.get("DBConnection"));
		java.sql.PreparedStatement prepstatement = db.getPreparedStatement("SELECT * FROM SystemUser WHERE username=?");
		prepstatement.setString(1, "root");
		java.sql.ResultSet result = prepstatement.executeQuery();
		if (result.next())
		{
			user_root = new User(result.getInt(1), result.getInt(2), result.getString(3), result.getString(4), result.getString(5), result.getString(6));
		}
	}

	private User addRandomUserToDatabase() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		User user_root = new User(1, 1, "root", "", "", PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		String random_str = utils.Utils.makeRandomString(8);
		db.executeWithValidUser
		(
			user_root, 
			utils.Configuration.settings.getAndEscape("RegisterCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_str)
			+ " "
			+ utils.Utils.escapeSpaces("1")
			+ " "
			+ utils.Utils.escapeSpaces("TestFname")
			+ " "
			+ utils.Utils.escapeSpaces("TestLname")
			+ " "
			+ utils.Utils.escapeSpaces("12345")
		);



		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM systemUser WHERE username=?");
		prep_statement.setString(1, random_str);
		String selection_result = Database.resultToString(prep_statement.executeQuery());
		// String[] parts = rand_str_userid.split(" "); 
		////////////////////////////////////////////////////////////
		// DANGEROUS ^. DO NOT SPLIT RAW. //////////////////////////
		////////////////////////////////////////////////////////////
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(selection_result);
		int columns = Integer.parseInt(parts.get(0));
		parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
		return new User(Integer.parseInt(parts.get(0)), Integer.parseInt(parts.get(1)), random_str, parts.get(2), parts.get(3), parts.get(4));
	}
}

/* Eksempel:

>>>>>>> 81b82de93d1347b2c2ee06c6994984d352d27618
	@org.junit.Test(expected=IndexOutOfBoundsException.class)
	public void testIndexOutOfBoundsException() 
	{
    java.util.ArrayList emptyList = new java.util.ArrayList();
    Object o = emptyList.get(0);
	}
<<<<<<< HEAD
}
=======
*/
