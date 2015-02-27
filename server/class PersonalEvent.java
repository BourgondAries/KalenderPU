
	
package server;
import java.util.ArrayList; 
java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.Timestamp;

class PersonalEvent{

	private String description, place;
	private boolean alarm,
	Timestamp time, timeEnd, alarmTime; 
	//private int minutesWarning;

PersonalEvent newPersonalEvent(String description, Timestamp time, Timestamp timeEnd, String place, boolean alarm, int minutesWarning){

	// Handterer tomme felt
	if ("".equals(description)){
		description = "Ny hendelse";
	}
	if ("".equals(place)){
		place = "Ubestemt sted";
	}

	this.description = description;
	this.time = time;
	this.timeEnd = timeEnd;
	this.place = place;
	this. alarm = alarm;
	//this.alarmTime = (this.time.year, this.time.month, this.time.date, this,time.hour, this.time.minute - minutesWarning);
}

String changeTime(Timestamp time){

	this.time = time;
	if (this.alarm){
		this.alarmTime = (this.time.year, this.time.month, this.time.date, this.time.hour, this.time.minute - minutesWarning);
	} else return;

	if (true) return "Denne eventen overlapper kanskje andre eventer.";
}



boolean eventOverlap(event){
	if ( (event.timeEnd.before(this.timeEnd) && event.timeEnd.after(this.time) )|| 
		(event.time.after(this.time) && event.time.before(this.timeEnd) )) {
		return true;

	} else return false;

} 



}