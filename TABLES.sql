CREATE TABLE SystemUser
(
	SystemUserId  	int 		NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	rank 		int,
	fname 		varchar(255),
	lname 		varchar(255),
	salt		varchar(255),
	hashedPW 	varchar(255),
	PRIMARY KEY (SystemUserId)
);

CREATE TABLE PersonalEvent
(
	eventId 	int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	description	varchar(255),
	time 		timestamp,
	SystemUserId 		int,
	PRIMARY KEY (eventId),
	FOREIGN KEY (SystemUserId) REFERENCES SystemUser(SystemUserId)
);

CREATE TABLE Room
(
	roomId		int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	size 		int,
	location	varchar(255),
	PRIMARY KEY (roomId)
);

CREATE TABLE Booking
( 
	bookingId		int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	adminId			int NOT NULL,
	description		varchar(255),
	bookingName		varchar(255),
	roomId			int,
	PRIMARY KEY (bookingId),
	FOREIGN KEY (roomId) REFERENCES ROOM(roomId),
	FOREIGN KEY (adminId) REFERENCES SystemUser(SystemUserId)
);

CREATE TABLE SystemGroup
(
	groupId 	int 		NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	rank		int			NOT NULL,
	groupName	varchar(255), 
	parentGroupId int,
	CHECK (groupId != parentGroupId),
	PRIMARY KEY (groupId),
	FOREIGN KEY (parentGroupId) REFERENCES SystemGroup(groupId)
);

CREATE TABLE Groupmember
(
	SystemUserId		int		NOT NULL,
	groupId		int		NOT NULL,
	PRIMARY KEY (SystemUserId, groupId),
	FOREIGN KEY (SystemUserId) REFERENCES SystemUser (SystemUserId),
	FOREIGN KEY (groupId) REFERENCES SystemGroup (groupId)
);
CREATE TABLE Invitation
(
	SystemUserId 		int NOT NULL,
	bookingId 	int NOT NULL,
	status		boolean,
	PRIMARY KEY (SystemUserId, bookingId)
);

CREATE TABLE Notification
(
	n_Id 			int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	message 		varchar(255),
	duration		varchar(255),
	seen 			boolean,
	bookingId		int NOT NULL,
	groupId 		int, 
	SystemUserId 			int, 
	CHECK (groupId IS NOT NULL OR SystemUserId IS NOT NULL),
	PRIMARY KEY (n_Id),
	FOREIGN KEY (bookingId) REFERENCES Booking(bookingId),
	FOREIGN KEY (groupId) REFERENCES SystemGroup(groupId),
	FOREIGN KEY (SystemUserId) REFERENCES SystemUser(SystemUserId)
);
