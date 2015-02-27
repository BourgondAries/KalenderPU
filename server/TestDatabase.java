package server;

import static org.junit.Assert.*;

public class TestDatabase
{
	
	@org.junit.Test
	public static void testWrongPasswordWhenExecute() throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,java.io.IOException
	{	
		utils.Configuration.loadDefaultConfiguration();
		User userR = new User(0,1,"root","","",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		//assertEquals(0,1);
		assertEquals(db.execute(userR.username,"gerp", "query"),"Invalid password for user '" + userR.username + "'.");

	}
	@org.junit.Test
	public static void testUsernameNotExcisting() throws java.io.IOException
	{	
		utils.Configuration.loadDefaultConfiguration();
		//User userR = new User(0,1,"root","","",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		assertEquals(db.execute("nonexistingname", "pass", "query"),"Login username 'nonexistingname' does not exist.");
	}

	@org.junit.Test
	public static void testRegisterCommand()  throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException,java.io.IOException
	{
		utils.Configuration.loadDefaultConfiguration();
		User userR = new User(0,1,"root","","",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		String rndStr = utils.Utils.makeRandomString(8);
		db.executeWithValidUser(userR, "register\\ "+ rndStr +"\\ 1\\ TestFirst\\ TestLast\\ 12345");
	}

	public static void main(String[] args)
	{	try
		{
			testWrongPasswordWhenExecute();
			testUsernameNotExcisting();
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
	}
}