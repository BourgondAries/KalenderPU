package server;
/*
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GroupSettings
{
	public int group_id, group_admin_id, group_parent_id;
	public String group_name;
	public User group_admin; //hvor staar det om dette i SQL-tabellen?
	public ArrayList<User> group_members;
	public ArrayList<Group> sub_groups;
	public Database db;



	public GroupSettings(int group_id, int group_admin_id, String group_name, int group_parent_id) throws Exception
	{
		this.group_members = new ArrayList<User>();
		//this.sub_groups = new ArrayList<Group>();
		this.group_id = group_id;
		this.group_parent_id = group_parent_id;
		this.group_admin_id = group_admin_id;
		this.group_name = group_name;
		//this.group_admin = group_admin;
		db = new Database(utils.Configuration.settings.get("DBConnection"));
	}

	public static void removeGroup(Group gruppe, User user1, Database db) throws Exception
	{
		if (isAdmin(group_admin_id, user1))
		{
			PreparedStatement prep_statement = db.getPreparedStatement("DELETE FROM SystemGroup WHERE groupId=?");
			prep_statement.setInt(1, gruppe.group_id);
			prep_statement.executeUpdate();
			gruppe.sub_groups.clear();
			gruppe.group_members.clear();
		}
		
	}
//
	public void addGroup(String group_name, int new_admin_id, int parent_group_id, Database db) throws Exception
	{
			PreparedStatement prep_statement = db.getPreparedStatement("INSERT INTO SystemGroup (groupName, groupAdminId, parentGroupId) VALUES (?, ?, ?)");
			prep_statement.setString(1, group_name);
			prep_statement.setInt(2, new_admin_id);
			// sjekker om gruppen har en parent-gruppe:
			if ((parent_group_id == 0) || (parent_group_id < 0))
			{
				prep_statement.setString(3, null);
			} 
			else 
			{
				prep_statement.setInt(3, parent_group_id);
			}
			//gruppen har en parent-gruppe, da maa gruppen legges til i parent-gruppens subgroup-list
			String result_string = db.resultToString(prep_statement.executeQuery());
			ArrayList<String> parts = utils.Utils.splitAndUnescapeString(result_string);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			if (parent_group_id != 0) 
			{
				addSubGroup(parent_group_id, Integer.parseInt(parts.get(0)), db);
			}
		
	}

	public void addSubGroup(int parent_group_id, int group_id, Database db) throws Exception 
	{
		Group parent = Group.getGroup(parent_group_id, db);
		Group sub = Group.getGroup(group_id, db);
		parent.sub_groups.add(sub);	
		
	}

	public void removeSubGroup(int parent_group_id, int group_id, int user_id, Database db) throws Exception
	{
		Group parent = Group.getGroup(parent_group_id, db);
		Group sub = Group.getGroup(group_id, db);
		if ((user_id == parent.group_admin_id) || user_id == 1) //root har 1 som id
		{
			parent.sub_groups.remove(sub);
		}
	}
//

	public void addMember(int group_admin_id, int new_user_id, Group gruppe, Database db) throws Exception
	{
		if (isAdmin(gruppe.group_admin_id, group_admin_id)) //eller isAdmin(gruppe.group_admin, this.group_admin)
		{
			PreparedStatement prep_statement = db.getPreparedStatement("INSERT INTO Groupmember (systemUserId, groupId) VALUES (?, ?)");
			prep_statement.setInt(1, new_user_id);
			prep_statement.setInt(2, gruppe.group_id);
			prep_statement.executeUpdate();
			gruppe.group_members.add(User.loadSystemUser(new_user_id)); // maa sjekkes med Hans' kode for riktig navn
		} 
	}

	public void removeMember(int group_admin_id, int user_id, Group gruppe, Database db) throws Exception
	{
		if (isAdmin(gruppe.group_admin_id, user_id))
		{
			PreparedStatement prep_statement = db.getPreparedStatement("DELETE FROM Groupmember WHERE systemUserId=?");
			prep_statement.setInt(1, user_id);
			prep_statement.executeUpdate();
			gruppe.group_members.remove(User.loadSystemUser(user_id)); 
		} 
	}
	 
	public void changeAdmin(int old_admin_id, int new_admin_id, Group gruppe, Database db) throws Exception
	{
		if (isAdmin(gruppe.group_admin_id, old_admin)) //kun den davaerende admin som kan endre admin, ikke den "nye"
		{
			PreparedStatement prep_statement = db.getPreparedStatement("UPDATE SystemGroup SET groupAdminId=? WHERE groupId=?");
			prep_statement.setInt(1, new_admin_id);
			prep_statement.setInt(2, gruppe.group_id);
			prep_statement.executeUpdate();
			gruppe.group_admin_id = new_admin_id;
		}
		
	}

	public boolean isAdmin(int group_admin_id, int user_id)
	{
		return ((user_id == group_admin_id) ? true : false);
	}
}
*/