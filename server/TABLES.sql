CREATE TABLE User
(
	userId  	int 		NOT NULL AUTO_INCREMENT,
	rank 		int,
	fname 		varchar(255),
	lname 		varchar(255),
	hashedPW 	varchar(255),
	PRIMARY KEY (userid), 
);

CREATE TABLE PersonalEvent
(
	eventId 	int NOT NULL AUTO_INCREMENT,
	description	varchar(255),
	time 		timestamp,
	userId 		int,
	PRIMARY KEY (eventId),
	FOREIGN KEY (userId) REFERENCES USER(userid),
);

CREATE TABLE Booking
( 
	bookingId		int NOT NULL AUTO_INCREMENT,
	adminId			int NOT NULL,
	description		varchar(255),
	bookingName		varchar(255),
	roomId			int,
	PRIMARY KEY (bookingId),
	FOREIGN KEY (roomId) REFERENCES ROOM(roomId),
	FOREIGN KEY (adminId) REFERENCES USER(userid),
);

CREATE TABLE Group
(
	groupId 	int 		NOT NULL,
	rank		int			NOT NULL,
	groupName	varchar(255), 
	parentGroupId int,
	CHECK (groupId != parentGroupId),
	PRIMARY KEY (groupid),
	FOREIGN KEY (parentGroupId) REFERENCES GROUP(groupId),
);

CREATE TABLE Groupmember
(
	userId		int		NOT NULL,
	groupId		int		NOT NULL,
	PRIMARY KEY (userId, groupId),
	FOREIGN KEY (userId) REFERENCES USER (userId),
	FOREIGN KEY (groupId) REFERENCES GROUP (groupId),
);
CREATE TABLE Invitiation
(
	userId 		int NOT NULL,
	bookingId 	int NOT NULL,
	status		boolean,
	PRIMARY KEY (userId, bookingId),
);

CREATE TABLE Notification
(
	n_Id 			int NOT NULL AUTO_INCREMENT,
	message 		varchar(255),
	duration		varchar(255),
	seen 			boolean,
	bookingId		int NOT NULL,
	groupId 		int, 
	userId 			int, 
	CHECK (groupId != NULL OR userId != NULL),
	PRIMARY KEY (n_Id),
	FOREIGN KEY (bookingId) REFERENCES Booking(bookingId),
	FOREIGN KEY (groupId) REFERENCES Group(groupId),
	FOREIGN KEY (userId) REFERENCES User(userId),
);

CREATE TABLE Room
(
	roomId		int NOT NULL AUTO_INCREMENT,
	size 		int,
	location	varchar(255),
);