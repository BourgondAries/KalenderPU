
CREATE TABLE USER(
userid  	int 			NOT NULL AUTO_INCREMENT,
rank 		int 			NOT NULL,
fname 		varchar(255) 	NOT NULL,
lname 		varchar(255) 	NOT NULL,
hashedPW 	varchar()		NOT NULL,
PRIMARY KEY userid 

);
CREATE TABLE EVENT()
eventid 	int NOT NULL,
message 	varchar(255),
time 		timestamp,

)

CREATE TABLE ( )

CREATE TABLE GROUP(
groupid 	int 		NOT NULL,
rank		int			NOT NULL,
groupName	varchar(255),
PRIMARY KEY (groupid),
FOREIGN KEY (groupid) REFERENCES GROUP(groupid)
);

CREATE TABLE Groupmember(
userid		int		NOT NULL,
groupid		int		NOT NULL,
PRIMARY KEY (userid, groupid),
FOREIGN KEY (userid) REFERENCES USER (userid),
FOREIGN KEY (groupid) REFERENCES GROUP (groupid)


);

CREATE TABLE NOTIFICATION(
notificationid 		int NOTNULL,
message 			varchar(255),
duration			

)