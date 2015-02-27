package server;

import static org.junit.Assert.*;

public class TestDatabase
{
	//@org.junit.Test
	public static void testWrongPasswordWhenExecute() throws java.security.NoSuchAlgorithmException, java.security.spec.InvalidKeySpecException
	{	
		utils.Configuration.loadDefaultConfiguration();
		User userR = new User(0,1,"root","","",PasswordHash.createHash("root"));
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		assertEquals(db.execute(userR.username,"gerp", "query"),"Invalid password for user '" + userR.username + "'.");

	}

	public static void main(String[] args)
	{	try
		{
			testWrongPasswordWhenExecute();
		}
		catch(java.security.NoSuchAlgorithmException nsae)
		{
			//TODO
		}
		catch(java.security.spec.InvalidKeySpecException ikse)
		{
			//TODO
		}
		
	}
}