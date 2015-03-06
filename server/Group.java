package server;

import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class Group
{
	public int group_id, group_rank;
	public String group_name;
	public User group_admin, group_member; //hvor staar dette om i SQL?
	public ArrayList<User> group_members;
	public ArrayList<Group> sub_groups;
	private Database database;	

	
	public Group(int group_id, int group_rank, String group_name, User group_admin)
	{
		ArrayList<User> group_members = new ArrayList<User>();
		ArrayList<Group> sub_groups = new ArrayList<Group>();
		this.group_id = group_id;
		this.group_rank = group_rank;
		this.group_name = group_name;
		this.group_admin = group_admin;
		this.group_member = group_member;
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		ResultSet rs = new ResultSet();
		PreparedStatement prep_statement = new PreparedStatement();
	}

	//save- og get metoder
	public static Group getGroup(int group_id)
	{
		prep_statement = db.prepareStatement("SELECT * FROM SystemGroup WHERE groupId=?");
		prep_statement.setString(1, group_id);
		rs = prep_statement.executeQuery(prep_statement);
		return rs;
	}

	public static void addGroup(String group_name, User group_admin)
	{
		prep_statement = db.prepareStatement("INSERT INTO SystemGroup (groupName, groupAdmin) VALUES (?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
		prep_statement.setString(1, group_name);
		prep_statement.setString(2, group_admin);
		prep_statement.executeUpdate();
	}
}

