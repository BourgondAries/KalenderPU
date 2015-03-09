CREATE TABLE SystemUser
(
	systemUserId  	int 		NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	rank 		int,
	username 	varchar(255) 	UNIQUE, 
	fname 		varchar(255),
	lname 		varchar(255),
	hashedPW 	varchar(255),

	PRIMARY KEY (systemUserId)
);

INSERT INTO SystemUser (systemUserID, rank,username,fname,lname,hashedPW)
VALUES 
(1,1, 'root', '', '', PasswordHash.createHash("root"),
(2,1, 'Fredrik80', 'Fredrik', 'Larsen', PasswordHash.createHash("qwerty")),
(3,5, 'skogen', 'Lars', 'Skogen', PasswordHash.createHash("qwerty")),
(4,5, 'bruker123', 'Lisbeth', 'Hagen', PasswordHash.createHash("qwerty")),
(5,5, 'karUll', 'Karianne', 'Ullstein', PasswordHash.createHash("qwerty")),
(6,5, 'ropert', 'Robert', 'Hagen', PasswordHash.createHash("qwerty"));

CREATE TABLE PersonalEvent
(
	eventId 	int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	description	varchar(255),
	time 		timestamp,
	systemUserId 		int,
	timeEnd		timestamp,
	warntime    int,
	PRIMARY KEY (eventId),
	FOREIGN KEY (systemUserId) REFERENCES SystemUser(systemUserId)
);

INSERT INTO PersonalEvent (eventId, description, time, systemUserId, timeEnd, warntime)
VALUES
(1, "Kopiere notater", 2015-03-15 14:50:00, 5, 2015-03-15 15:00:00, 15),
(1, "Kjøpe lunsj", 2015-03-15 12:25:00, 2, 2015-03-15 12:35:00, 20),
(1, "Få ny PC-skjerm", 2015-03-23 10:00:00, 2, 2015-03-23 10:30:00 20),
(1, "Fikse printeren", 2015-03-23 08:30:00, 6, 2015-03-23 09:30:00 60);

CREATE TABLE Room
(
	roomId		int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	size 		int,
	location	varchar(255),
	roomName	varchar(255) UNIQUE,
	PRIMARY KEY (roomId)
);

INSERT INTO Room (roomId, size,location,roomName)
VALUES
(1,20,'2. etasje, innerst i østre korridor', 'Møterom 1'),
(2,15,'2. etasje, innerst i vestre korridor', 'Møterom 2'),
(3,10,'1. etasje', 'Hulen'),
(4,25,'5. etasje', 'Panorama')

CREATE TABLE Booking
(
	bookingId		int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	adminId			int NOT NULL,
	description		varchar(255),
	bookingName		varchar(255),
	roomId			int,
	warntime		timestamp,
	timeBegin		timestamp,
	timeEnd			timestamp,
	PRIMARY KEY (bookingId),
	FOREIGN KEY (roomId) REFERENCES ROOM(roomId),
	FOREIGN KEY (adminId) REFERENCES SystemUser(systemUserId)
);

INSERT INTO Booking (bookingId, adminId, description, bookingName, roomId, warntime, timeBegin, timeEnd)
VALUES
(1, 2, 'Vi skal planlegge strategiprosessen for for videre utvikling av produktet', 'Planleggingsmøte', 4, 2015-03-23 08:30:00, 2015-03-23 09:00:00, 2015-03-23 11:00:00),
(2, 2, 'Del 2 av strategiplansleggingsprosessen' , 'Planleggingsmøte 2', 1, 2015-03-26 08:30:00, 2015-03-26 09:00:00, 2015-03-26 11:00:00),
(3, 3, 'Infomøte om nyansettelser', 'Infomøte', 2015-03-26 14:30:00, 2015-03-26 15:00:00, 2015-03-26 15:45:00);

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
	systemUserId		int		NOT NULL,
	groupId		int		NOT NULL,
	PRIMARY KEY (systemUserId, groupId),
	FOREIGN KEY (systemUserId) REFERENCES SystemUser (systemUserId),
	FOREIGN KEY (groupId) REFERENCES SystemGroup (groupId)
);

CREATE TABLE Invitation
(
	systemUserId 		int NOT NULL,
	bookingId 			int NOT NULL,
	status				boolean WITH DEFAULT false,
	wantsWarning		boolean WITH DEFAULT true,
	PRIMARY KEY (systemUserId, bookingId)
);

INSERT INTO Invitation (systemUserId,bookingId)
VALUES
(3,1),
(5,1),
(4,1);
(3,2),
(5,2),
(4,2),
(2,6),
(3,6),
(4,6),
(5,6),
(6,6);

CREATE TABLE Notification
(
	n_Id 			int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	message 		varchar(255),
	duration		varchar(255),
	seen 			boolean,
	bookingId		int NOT NULL,
	groupId 		int, 
	systemUserId 			int, 
	CHECK (groupId IS NOT NULL OR systemUserId IS NOT NULL),
	PRIMARY KEY (n_Id),
	FOREIGN KEY (bookingId) REFERENCES Booking(bookingId),
	FOREIGN KEY (groupId) REFERENCES SystemGroup(groupId),
	FOREIGN KEY (systemUserId) REFERENCES SystemUser(systemUserId)
);
