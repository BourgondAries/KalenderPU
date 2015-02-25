package server;

public class User
{
	public int user_id, rank;
	public String username, fname, lname, hashedPW;

	public User(int user_id, int rank, String username, String fname, String lname, String hashedPW)
	{
		this.user_id = user_id;
		this.rank = rank;
		this.username = username;
		this.fname = fname;
		this.lname = lname;
		this.hashedPW = hashedPW;
	}

	public User getUser()
	{
		return this;
	}
}