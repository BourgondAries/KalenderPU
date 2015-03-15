package server;

import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.LinkedList;

public class Group
{
	public int group_id, parent_group_id, group_admin_id;
	public String group_name;
	public User group_admin, group_member; //hvor staar dette om i SQL?
	public ArrayList<User> group_members;
	public ArrayList<Group> sub_groups;
	public Database db;	
	public LinkedList queue;
	//public ResultSet rs;

	
	public Group(int group_id, int group_admin_id, String group_name, int parent_group_id) throws Exception
	{
		ArrayList<User> group_members = new ArrayList<User>();
		ArrayList<Group> sub_groups = new ArrayList<Group>();
		this.group_id = group_id;
		this.parent_group_id = parent_group_id;
		this.group_admin_id = group_admin_id;
		this.group_name = group_name;
		//this.group_admin = group_admin;
		this.group_member = group_member;
		db = new Database(utils.Configuration.settings.get("DBConnection"));
		//rs = new ResultSet();
		//prep_statement = new PreparedStatement();
	}


	//save- og get metoder
	public static Group getGroup(int group_id, Database db) throws Exception
	{
		PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM SystemGroup WHERE groupId=?");
		prep_statement.setInt(1, group_id);
		String result_string = db.resultToString(prep_statement.executeQuery());
		ArrayList<String> parts = utils.Utils.splitAndUnescapeString(result_string);
		int columns = Integer.parseInt(parts.get(0));
		parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
		return new Group(Integer.parseInt(parts.get(0)), Integer.parseInt(parts.get(1)), parts.get(2), Integer.parseInt(parts.get(3)));
	}

	//

	public void removeGroup(Group gruppe, int user_id, Database db) throws Exception
	{
		if (isAdmin(gruppe.group_admin_id, user_id))
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

			// at parent ikke er en subgruppe av gruppa
		
	}

	public void addSubGroup(int parent_group_id, int group_id, Database db) throws Exception 
	{
		Group parent = getGroup(parent_group_id, db);
		Group sub = getGroup(group_id, db);
		bfsSearch(sub, parent, db);
		parent.sub_groups.add(sub);			
	}

	// BFS-søk:
	public void bfsSearch(Group root, Group parent, Database db) throws Exception
	{
		LinkedList<Group> queue = new LinkedList<Group>(); // de som er oppdaget
		//List output = new List(); // "bfs-treet"
		Group current;
		//output.add(root);
		queue.add(root);
		while (queue.size() != 0)
		{
			current = queue.pop();
			if (current.group_id == parent.group_id)
			{
				Group busted = current.getGroup(group_id, db);
				busted.getGroup(busted.parent_group_id, db).sub_groups.remove(busted);
			}
			for (int i = 0; i < current.sub_groups.size(); i++)
			{ 
				if (!queue.contains(current.sub_groups.get(i)))
				{
					queue.add(current.sub_groups.get(i));
					//output.add(current.sub_groups.get(i));
				}
			// oppdaget alle nabonoder til current, bytter current til foerst i koen gjennom while-løkka
			}
		}
		 //fant ingen sykler, alt ok!
	}


	public void removeSubGroup(int parent_group_id, int group_id, int user_id, Database db) throws Exception
	{
		Group parent = getGroup(parent_group_id, db);
		Group sub = getGroup(group_id, db);
		if ((user_id == parent.group_admin_id) || user_id == 1) //root har 1 som id
		{
			parent.sub_groups.remove(sub);
		}
	}

	public void addMember(int group_admin_id, int new_user_id, Group gruppe, Database db) throws Exception
	{
		if (isAdmin(gruppe.group_admin_id, group_admin_id)) //eller isAdmin(gruppe.group_admin, this.group_admin)
		{
			PreparedStatement prep_statement = db.getPreparedStatement("INSERT INTO Groupmember (systemUserId, groupId) VALUES (?, ?)");
			prep_statement.setInt(1, new_user_id);
			prep_statement.setInt(2, gruppe.group_id);
			prep_statement.executeUpdate();
		//	gruppe.group_members.add(User.getUser(new_user_id)); // maa sjekkes med Hans' kode for riktig navn
		} 
	}

	public void removeMember(int group_admin_id, int user_id, Group gruppe, Database db) throws Exception
	{
		if (isAdmin(gruppe.group_admin_id, user_id))
		{
			PreparedStatement prep_statement = db.getPreparedStatement("DELETE FROM Groupmember WHERE systemUserId=?");
			prep_statement.setInt(1, user_id);
			prep_statement.executeUpdate();
		//	gruppe.group_members.remove(User.getUser(user_id)); 
		} 
	}
	 
	public void changeAdmin(int old_admin_id, int new_admin_id, Group gruppe, Database db) throws Exception
	{
		if (isAdmin(gruppe.group_admin_id, old_admin_id)) //kun den davaerende admin som kan endre admin, ikke den "nye"
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

