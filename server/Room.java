package server;

import java.util.ArrayList; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//import java.io.IOException;
//import java.utils.Configuration;

public class Room
{

	public int room_id, size;
	public String location, room_name;
	public Database db;
	ArrayList<Integer> available_rooms;
	public static class CouldNotConnectAndSetupDatabaseConnection extends Exception { CouldNotConnectAndSetupDatabaseConnection(Throwable exc) { super(exc); } }
	

	public Room(int room_id, int size, String location, String room_name) throws Exception
	{
		this.room_id = room_id;
		this.size = size;
		this.location = location;
		this.room_name = room_name;
		this.available_rooms = new ArrayList<Integer>();

		db = new Database(utils.Configuration.settings.get("DBConnection"));
	}

	
	public static Room getRoom(int room_id, Database db) throws Exception
	{
		try
		{
			PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM ROOM WHERE roomId=?");
			prep_statement.setInt(1, room_id);
			String result_string = db.resultToString(prep_statement.executeQuery());
			java.util.ArrayList<String> parts = utils.Utils.splitAndUnescapeString(result_string);
			int columns = Integer.parseInt(parts.get(0));
			parts = utils.Utils.splitAndUnescapeString(parts.get(columns + 1));
			return new Room(Integer.parseInt(parts.get(0)), Integer.parseInt(parts.get(1)), parts.get(2), parts.get(3));
		}
		catch (java.sql.SQLException exc)
		{
			throw new CouldNotConnectAndSetupDatabaseConnection(exc);
		}
	}


	public static void addRoom(int size, String location, String room_name, Database db) throws Exception
	{
		try
		{
			PreparedStatement prep_statement = db.getPreparedStatement("INSERT INTO Room (size, location, roomName) VALUES (?, ?, ?)");
			prep_statement.setInt(1, size);
			prep_statement.setString(2, location);
			prep_statement.setString(3, room_name);
			prep_statement.executeUpdate();	
		}
		catch (java.sql.SQLException exc)
		{
			throw new CouldNotConnectAndSetupDatabaseConnection(exc);
		}
	}

	public ArrayList<Integer> findAvailableRoom(String start_time, String end_time, Database db) throws Exception
	{
		try
		{
			java.sql.Timestamp time_stamp_start = java.sql.Timestamp.valueOf(start_time);
			java.sql.Timestamp time_stamp_end = java.sql.Timestamp.valueOf(end_time);
			PreparedStatement prep_statement = db.getPreparedStatement("SELECT * FROM BOOKING (timeBegin, timeEnd) LEFT JOIN ROOM ON ROOM.roomId = BOOKING.roomId WHERE (timeBegin<? AND timeEnd>?) AND ROOM.roomId IS NULL");
			prep_statement.setString(1, end_time);
			prep_statement.setString(2, start_time);
			ResultSet rs = prep_statement.executeQuery();
			while (rs.next())
			{
				int available_room = rs.getInt("roomId");
				available_rooms.add(available_room);
			}
			//something = (available_rooms.size() == 0) ? false : true;
			return available_rooms;
		}
		catch (java.sql.SQLException exc)
		{
			throw new CouldNotConnectAndSetupDatabaseConnection(exc);
		}	
	}

	public static boolean isAvailable(ArrayList<Integer> available_rooms, int room_id) 
	{
		return available_rooms.contains(room_id);
	}


// hente bookings som er i det tidsintervallet, finn alle opptatte rom. Gaa inn paa room og "fjern" de rommene som er opptatte og returner en oversikt
// gammel_greie = "SELECT * FROM BOOKING (timeBegin, timeEnd) WHERE timeBegin<? AND timeEnd>?
//			(SELECT * FROM ROOM WHERE ROOM.roomId != BOOKING.roomId)"

	


}
