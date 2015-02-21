CREATE TABLE Groupmember(
userid		int		NOT NULL,
groupid		int		NOT NULL,
PRIMARY KEY (userid, groupid),
FOREIGN KEY (userid) REFERENCES User(userid),


)