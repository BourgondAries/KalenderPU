
CREATE TABLE User(
userId  	int 			NOT NULL AUTO_INCREMENT,
rank 		int,
fname 		varchar(255),
lname 		varchar(255),
hashedPW 	varchar(255),
PRIMARY KEY (userid) 
);

CREATE TABLE Event()
eventId 	int NOT NULL AUTO_INCREMENT,
message 	varchar(255),
time 		timestamp,
PRIMARY KEY (eventId)
FOREIGN KEY (userid) REFERENCES USER(userid)
FOREIGN KEY (groupid) REFERENCES GROUP(groupid)
);

CREATE TABLE Booking( 
bookingId		int NOT NULL AUTO_INCREMENT,
adminId	int NOT NULL,
description		varchar(255),
bookingName		varchar(255),
PRIMARY KEY (bookingId)
FOREIGN KEY (roomId) REFERENCES ROOM(roomId)
FOREIGN KEY (adminId) REFERENCES USER(userid)
)

CREATE TABLE Group(
groupId 	int 		NOT NULL,
rank		int			NOT NULL,
groupName	varchar(255), 
parrentGroupId int CHECK (groupId != parrentGroupId)
PRIMARY KEY (groupid),
FOREIGN KEY (parrentGroupId) REFERENCES GROUP(groupId)
);

CREATE TABLE Groupmenber(
userid		int		NOT NULL,
groupid		int		NOT NULL,
PRIMARY KEY (userid, groupid),
FOREIGN KEY (userid) REFERENCES USER (userid),
FOREIGN KEY (groupid) REFERENCES GROUP (groupid)
);
CREATE TABLE Invitiation(
)

CREATE TABLE Notification(
notificationId 		int NOTNULL AUTO_INCREMENT,
message 			varchar(255),
duration			varchar(255),
seen 				bit,
);

CREATE TABLE Room(
roomId		int NOT NULL AUTO_INCREMENT,
size 		int
location	varchar(255),
);
