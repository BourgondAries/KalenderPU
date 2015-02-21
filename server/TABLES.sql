
CREATE TABLE USER(
userid  	int 			NOT NULL AUTO_INCREMENT,
rank 		int 			NOT NULL,
fname 		varchar(255) 	NOT NULL,
lname 		varchar(255) 	NOT NULL,
hashedPW 	varchar()		NOT NULL,
PRIMARY KEY userid 
);

CREATE TABLE EVENT()
eventId 	int NOT NULL AUTO_INCREMENT,
message 	varchar(255),
time 		timestamp,
);

CREATE TABLE BOOKING( 
bookingId		int NOT NULL AUTO_INCREMENT)

CREATE TABLE GROUP(
groupid 	int 		NOT NULL,
rank		int			NOT NULL,
groupName	varchar(255),
PRIMARY KEY (groupid),
FOREIGN KEY (groupid) REFERENCES GROUP(groupid)
);

CREATE TABLE GROUPMENBER(
userid		int		NOT NULL,
groupid		int		NOT NULL,
PRIMARY KEY (userid, groupid),
FOREIGN KEY (userid) REFERENCES USER (userid),
FOREIGN KEY (groupid) REFERENCES GROUP (groupid)
);

CREATE TABLE NOTIFICATION(
notificationId 		int NOTNULL AUTO_INCREMENT,
message 			varchar(255),
duration			varchar(255),
seen 				boolean,
);
CREATE TABLE ROOM(
roomId		int NOT NULL AUTO_INCREMENT,
size 		int
location	varchar(255),
);
CREATE TABLE BOOKING(
bookingId 	int 		NOT NULL,
bookingName	varchar(255),
)