package server;

import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class GroupSettings extends Group
{
	public int group_id, group_rank;
	public String group_name;
	public User group_admin, group_member; //hvor staar dette om i SQL?
	public ArrayList<User> group_members;
	public ArrayList<Group> sub_groups;
	private Database database;	


	public GroupSettings()
	{
		ArrayList<User> group_members = new ArrayList<User>();
		ArrayList<Group> sub_groups = new ArrayList<Group>();
		this.group_id = group_id;
		this.group_rank = group_rank;
		this.group_name = group_name;
		this.group_admin = group_admin;
		//this.group_member = group_member;
		Database db = new Database(utils.Configuration.settings.get("DBConnection"));
		ResultSet rs = new ResultSet();
		PreparedStatement prep_statement = new PreparedStatement();
	}

	public static void removeGroup(Group gruppe)
	{
		prep_Statement = db.prepareStatement("DELETE FROM SystemGroup WHERE groupId=?");
		gruppe.sub_groups.clear();
		prep_statement.setInt(1, group_id);
		prep_statement.executeUpdate();
	}
			
	public void addMember(User user, Group gruppe)
	{
		gruppe.group_members.add(user);
		prep_statement = db.prepareStatement("INSERT INTO Groupmember (systemUserId, groupId) VALUES (?, ?)");
		prep_statement.setInt(1, user.user_id);
		prep_statement.setInt(2, gruppe.group_id);
		prep_statement.executeUpdate();
	}

	public void removeMember(User user, Group gruppe){
		gruppe.group_members.remove(user);
		prep_statement = db.prepareStatement("DELETE FROM Groupmember WHERE systemUserId=?");
		prep_statement.setInt(1, user.user_id);
		prep_statement.executeUpdate();

	}
	 
	public void changeAdmin(User user, Group gruppe)
	{
		gruppe.group_admin = user;
		prep_statement = db.prepareStatement("UPDATE SystemGroup SET groupAdminId=? WHERE groupId=?");
		prep_statement.setInt(1, user.user_id);
		prep_statement.setInt(2, gruppe.group_id);
		prep_statement.executeUpdate();
	}

	public boolean isAdmin(User user)
	{
		if (user == group_admin)
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}

}