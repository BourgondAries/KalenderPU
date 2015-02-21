CREATE TABLE Group(
groupid 	int 		NOT NULL,
groupName	varchar(255),
PRIMARY KEY (groupid),
FOREIGN KEY (userid) REFERENCES Groupmember (userid)
)