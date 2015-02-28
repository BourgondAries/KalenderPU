package server;

import static org.junit.Assert.*;

public class TestDatabase
{
	
	@org.junit.Test
	public static void testWrongPasswordWhenExecute() throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,java.io.IOException
	{	
		utils.Configuration.loadDefaultConfiguration();
		User test_user = new User(0, 1, "root", "", "", PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		//assertEquals(0,1);
		assertEquals(db.execute(test_user.username, "gerp", "query"), "Invalid password for user '" + test_user.username + "'.");

	}

	@org.junit.Test
	public static void testUsernameNotExisting() throws java.io.IOException
	{	
		utils.Configuration.loadDefaultConfiguration();
		//User test_user = new User(0,1,"root","","",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		assertEquals(db.execute("nonexistingname", "pass", "query"), "Login username 'nonexistingname' does not exist.");
	}

	@org.junit.Test
	public static void testRegisterCommand()  throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,java.io.IOException, java.sql.SQLException
	{
		utils.Configuration.loadDefaultConfiguration();
		User test_user = new User(0,1,"root", "", "", PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		String random_string = utils.Utils.makeRandomString(8);
		//System.out.println(random_string);
		db.executeWithValidUser
		(
			test_user, 
			utils.Configuration.settings.getAndEscape("RegisterCommand")
			+ " "
			+ utils.Utils.escapeSpaces(random_string)
			+ " "
			+ utils.Utils.escapeSpaces("1")
			+ " "
			+ utils.Utils.escapeSpaces("TestFname")
			+ " "
			+ utils.Utils.escapeSpaces("TestLname")
			+ " "
			+ utils.Utils.escapeSpaces("12345")
		);

		String result = db.runQuery("SELECT COUNT(*) FROM systemUser WHERE username = '" + random_string + "'");
		assertEquals(Integer.parseInt("" + result.charAt(0)), 1);
	}

	public static void main(String[] args)
	{
		utils.Configuration.verbose_mode = true;
		try
		{
			testWrongPasswordWhenExecute();
			testUsernameNotExisting();
			testRegisterCommand();
		}
		catch(java.security.NoSuchAlgorithmException nsae)
		{
			//TODO
		}
		catch(java.security.spec.InvalidKeySpecException ikse)
		{
			//TODOjava.io.IOException e
		}
		catch(java.io.IOException e)
		{

		}
		catch(Exception e)
		{

		}	
	}
}