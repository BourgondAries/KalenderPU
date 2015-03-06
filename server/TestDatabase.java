package server;

import static org.junit.Assert.*;

public class TestDatabase
{
	
	@org.junit.Test
	public void testWrongPasswordWhenExecute() throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,java.io.IOException
	{	
		utils.Configuration.loadDefaultConfiguration();
		User userR = new User(0,1,"root","","",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		//assertEquals(0,1);
		db.execute(userR.username,"password_gerp", utils.Configuration.settings.get("PassCheck"));
		assertTrue(db.getStatus(Database.Status.INCORRECT_PASSWORD));

	}

	@org.junit.Test
	public void testUsernameNotExisting() throws java.io.IOException
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
		User randUser = addRandUser();

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT COUNT(*) FROM systemUser WHERE username=?");
		prep_statement.setString(1, randUser.username);
		String result = Database.resultToString(prep_statement.executeQuery());

		assertEquals(Integer.parseInt("" + result.charAt(0)),1);
	}
	
	//@org.junit.Rule
  	//public org.junit.rules.ExpectedException exception = org.junit.rules.ExpectedException.none();

	@org.junit.Test 
	public void testAddingDuplicateUserShouldFail() throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,java.io.IOException, java.sql.SQLException
	{

		utils.Configuration.loadDefaultConfiguration();
		User userR = new User(0,1,"root", "" ,"",PasswordHash.createHash("root"));
		String rndStr = utils.Utils.makeRandomString(8);
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));

		db.executeWithValidUser(userR, 
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
							+ utils.Utils.escapeSpaces("12345"));

		assertTrue(db.getStatus(Database.Status.USER_ALREADY_EXISTS));
	}
	@org.junit.Test 
	public void testChangeYourOwnPassword() throws Exception
	{
		
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		User randUser = addRandUser();

		db.executeWithValidUser(randUser, 
							utils.Configuration.settings.getAndEscape("ChangePassCommand")
							+ " " 
							+ utils.Utils.escapeSpaces("newPW"));

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT hashedPW FROM systemUser WHERE username =?");
		prep_statement.setString(1, randUser.username);
		String result = Database.resultToString(prep_statement.executeQuery());
		String[] parts = result.split(" ");
		assertTrue(PasswordHash.validatePassword("newPW", parts[1]));
	}

	@org.junit.Test
	public void testUserCannotChangeOthersPass() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		User randUser = addRandUser();
		User userR = new User(0,1,"root", "" ,"",PasswordHash.createHash("root"));
		System.out.println(randUser.username);
		db.executeWithValidUser(randUser , utils.Configuration.settings.getAndEscape("ChangePassOfCommand")
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
		utils.Configuration.loadDefaultConfiguration();
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		User randUser = addRandUser();
		User userR = new User(0,1,"root", "" ,"",PasswordHash.createHash("root"));

		db.executeWithValidUser(userR, utils.Configuration.settings.getAndEscape("ChangePassOfCommand")
							+ " "
							+ utils.Utils.escapeSpaces(randUser.username)
							+ " "
							+ utils.Utils.escapeSpaces("newPW")
							);
		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT hashedPW FROM systemUser WHERE username =?");
		prep_statement.setString(1, randUser.username);
		String result = Database.resultToString(prep_statement.executeQuery());
		String[] parts = result.split(" ");
		assertTrue(PasswordHash.validatePassword("newPW", parts[1]));
	}

	private User addRandUser() throws Exception
	{
		utils.Configuration.loadDefaultConfiguration();
		User userR = new User(0,1,"root", "" ,"",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		String rndStr = utils.Utils.makeRandomString(8);
		//System.out.println(rndStr);
		db.executeWithValidUser(userR, 
							utils.Configuration.settings.getAndEscape("RegisterCommand")
							+ " "
							+ utils.Utils.escapeSpaces(rndStr)
							+ " "
							+ utils.Utils.escapeSpaces("1")
							+ " "
							+ utils.Utils.escapeSpaces("TestFname")
							+ " "
							+ utils.Utils.escapeSpaces("TestLname")
							+ " "
							+ utils.Utils.escapeSpaces("12345"));

		java.sql.PreparedStatement prep_statement = db.getPreparedStatement("SELECT systemUserId FROM systemUser WHERE username =?");
		prep_statement.setString(1, rndStr);
		String rndStrUserId = Database.resultToString(prep_statement.executeQuery());

		String[] parts = rndStrUserId.split(" ");
		User rndUser = new User(Integer.parseInt("" + parts[1]),5,rndStr,"","",PasswordHash.createHash("12345"));
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

