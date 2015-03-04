package server;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class Room{
	
	public int roomID, room

	public Room getRoom(int roomID){
		prepStatement = connection.prepareStatement("SELECT * FROM Room WHERE roomID =?");
		prepStatement.setString(1, roomID);
		rs = executeQuery();
		return rs;
	}


	public void addRoom(int groupID, int groupRank, String groupName, User groupAdmin){
		query = query.substring(query.indexOf(" ") + 1);
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
		prepStatement = connection.prepareStatement("INSERT INTO SystemGroup (groupID, groupRank, groupName, groupAdmin) VALUES (?, ?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
		prepstatement.setInt(1, parts.get(0));
		prepStatement.setInt(2, parts.get(1));
		prepStatement.setString(3, parts.get(2));
		prepStatement.setString(4, parts.get(3));
		rs = prepStatement.executeQuery();
		
	}

}
