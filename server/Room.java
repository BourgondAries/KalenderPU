package server;
import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

public class Room{
/*	
	public int roomID, size
	public String location, roomName

	public Room(int roomID, int size, String location, String roomName){
		this.roomID = roomID;
		this.size = size;
		this.location = location;
		this.roomName = roomName;
	}

	public static Room getRoom(int roomID){
		prepStatement = connection.prepareStatement("SELECT * FROM Room WHERE roomID =?");
		prepStatement.setString(1, roomID);
		rs = executeQuery();
		return rs;
	}


	public static void addRoom(int size, String location, String roomName){
		query = query.substring(query.indexOf(" ") + 1);
		java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(query);
		prepStatement = connection.prepareStatement("INSERT INTO Room (size,location,roomName) VALUES (?, ?, ?)", java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE, java.sql.ResultSet.CONCUR_READ_ONLY);
		prepstatement.setInt(1, parts.get(0));
		prepStatement.setString(2, parts.get(1));
		prepStatement.setString(3, parts.get(2));
		rs = prepStatement.executeQuery();	
	}

	public static boolean checkAvailabilityOfRoom(int roomID, String timeString){
		java.sql.Timestamp timeStamp = java.sql.Timestamp.valueOf(timeString);


	}

*/
}
