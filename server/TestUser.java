package server;

import static org.junit.Assert.*;
//@Test

public class TestUser
{
	
	public static void fieldsShouldBeSet() 
	{
		int id = 0;
		int rank = 1;
		String u_name = "oar", fname = "Ole Andreas" , lname = "Ramsdal", hashedPW = "hash";

		User user = new User(id,rank,u_name,fname,lname,hashedPW);

		//Tests
		assertEquals(id, user.user_id);
		assertEquals(rank, user.rank);
		assertEquals(u_name, user.username);
		assertEquals(fname, user.fname);
		assertEquals(lname, user.lname);
		assertEquals(hashedPW, user.hashedPW);
	}
	public static void main(String[] args)
	{
		fieldsShouldBeSet();
	}
}

