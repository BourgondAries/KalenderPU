package server;

import static org.junit.Assert.*;

public class TestDatabase
{
	Database db = null;
	User rand_user = null;
	User user_root = null;


	@org.junit.Test
	public void testWrongPasswordWhenExecute() throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException, java.io.IOException
	{	
		utils.Configuration.loadDefaultConfiguration();
		User user_root = new User(1, 1, "root", "", "", PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		db.execute(user_root.username, "password_gerp", utils.Configuration.settings.get("PassCheck"));
		assertTrue(db.getStatus(Database.Status.INCORRECT_PASSWORD));

	}

	@org.junit.Test
	public void testUsernameNotExisting() throws java.io.IOException
	{	
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		db.execute("nonexistingname", "password", utils.Configuration.settings.get("PassCheck"));
		assertTrue(db.getStatus(Database.Status.NONEXISTENT_USER));
	}

	@org.junit.Test
	public void testRegisterCommand()  throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		User rand_user = addRandUserToDatabase();

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT COUNT(*) FROM systemUser WHERE username=?");
		prep_statement.setString(1, rand_user.username);
		String result = Database.resultToString(prep_statement.executeQuery());

		assertEquals(Integer.parseInt("" + result.charAt(0)), 1);
	}
	
	//@org.junit.Rule
  	//public org.junit.rules.ExpectedException exception = org.junit.rules.ExpectedException.none();

	@org.junit.Test 
	public void testAddingDuplicateUserShouldFail() throws Exception
	{

		utils.Configuration.loadDefaultConfiguration();
		setUp();
		
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
		User rand_user = addRandUserToDatabase();

		db.executeWithValidUser
		(
			rand_user, 
			utils.Configuration.settings.getAndEscape("ChangePassCommand")
			+ " " 
			+ utils.Utils.escapeSpaces("newPW")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT hashedPW FROM systemUser WHERE username =?");
		prep_statement.setString(1, rand_user.username);
		String result = Database.resultToString(prep_statement.executeQuery());
		String[] parts = result.split(" ");
		assertTrue(PasswordHash.validatePassword("newPW", parts[1]));
	}

	@org.junit.Test
	public void testUserCannotChangeOthersPass() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setUp();
		User rand_user = addRandUserToDatabase();
		db.executeWithValidUser
		(
			rand_user,
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
		User rand_user = addRandUserToDatabase();
		setUp();
		db.executeWithValidUser
		(
			user_root,
			utils.Configuration.settings.getAndEscape("ChangePassOfCommand")
			+ " "
			+ utils.Utils.escapeSpaces(rand_user.username)
			+ " "
			+ utils.Utils.escapeSpaces("newPW")
		);
		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT hashedPW FROM systemUser WHERE username =?");
		prep_statement.setString(1, rand_user.username);
		String result = Database.resultToString(prep_statement.executeQuery());
		String[] parts = result.split(" ");
		assertTrue(PasswordHash.validatePassword("newPW", parts[1]));
	}

	@org.junit.Test
	public void testAddNewEventCommand() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setUp();

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
		String result = Database.resultToString(prep_statement.executeQuery());
		assertTrue(result.length() > 2 );
	}

	@org.junit.Test
	public void testFindSystemUser() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		setUp();
		User rand_user = addRandUserToDatabase();


		db.executeWithValidUser
		(
			rand_user,
			utils.Configuration.settings.getAndEscape("FindPersonCommand")
			+ " "							
			+ utils.Utils.escapeSpaces("name")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT systemUserId, username, rank, fname, lname FROM SystemUser WHERE fname LIKE ? OR lname LIKE ?");
		prep_statement.setString(1, "Test%");
		prep_statement.setString(2, "Test%");
		String result = Database.resultToString(prep_statement.executeQuery());
		assertTrue(result.length() > 2 );
	}

	private void setUp() throws Exception
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

	private User addRandUserToDatabase() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		User user_root = new User(1, 1, "root", "", "", PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		String rand_str = utils.Utils.makeRandomString(8);
		db.executeWithValidUser
		(
			user_root, 
			utils.Configuration.settings.getAndEscape("RegisterCommand")
			+ " "
			+ utils.Utils.escapeSpaces(rand_str)
			+ " "
			+ utils.Utils.escapeSpaces("1")
			+ " "
			+ utils.Utils.escapeSpaces("TestFname")
			+ " "
			+ utils.Utils.escapeSpaces("TestLname")
			+ " "
			+ utils.Utils.escapeSpaces("12345")
		);

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT systemUserId FROM systemUser WHERE username =?");
		prep_statement.setString(1, rand_str);
		String rand_str_userid = Database.resultToString(prep_statement.executeQuery());

		String[] parts = rand_str_userid.split(" ");
		User rndUser = new User(Integer.parseInt("" + parts[1]), 5, rand_str, "", "", PasswordHash.createHash("12345"));
		return  rndUser;
	}
}

/* Eksempel:

	@org.junit.Test(expected=IndexOutOfBoundsException.class)
	public void testIndexOutOfBoundsException() 
	{
    java.util.ArrayList emptyList = new java.util.ArrayList();
    Object o = emptyList.get(0);
	}
*/

