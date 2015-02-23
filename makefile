SERVER_LIBS=".:commons-cli-1.2.jar:derby.jar"
SERVER_RUNPATH="bin:commons-cli-1.2.jar:derby.jar"
CLIENT_LIBS=".:commons-cli-1.2.jar:derby.jar:derbyclient.jar"
CLIENT_RUNPATH="bin:commons-cli-1.2.jar:derby.jar:derbyclient.jar"

setup:
	mkdir -p bin
	$(MAKE) dbreset

pserver:
	javac -classpath $(SERVER_LIBS) -d bin server/Server.java
	java -classpath $(SERVER_RUNPATH) server.Server --cli

vserver:
	javac -classpath $(SERVER_LIBS) -d bin server/Server.java
	java -classpath $(SERVER_RUNPATH) server.Server -v --cli

pclient:
	javac -classpath $(CLIENT_LIBS) -d bin client/Client.java
	java -classpath $(CLIENT_RUNPATH) client.Client --cli

vclient:
	javac -classpath $(CLIENT_LIBS) -d bin client/Client.java
	java -classpath $(CLIENT_RUNPATH) client.Client -v --cli

dbreset:
	rm -rf database
	java -classpath ".:derby.jar:derbytools.jar" -Djdbc.drivers=org.apache.derby.jdbc.EmbeddedDriver org.apache.derby.tools.ij < ijcommands.txt

clean:
	find . -name "*.class" | xargs rm

# INSERT INTO SystemUser (rank, fname, lname) VALUES (32, 'hi', 'there')