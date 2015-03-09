SERVER_LIBS=".;./commons-cli-1.2.jar;./derby.jar"
SERVER_RUNPATH=".;./bin;./commons-cli-1.2.jar;./derby.jar"
CLIENT_LIBS=".;commons-cli-1.2.jar;./derby.jar;./derbyclient.jar"
CLIENT_RUNPATH="bin;./commons-cli-1.2.jar;./derby.jar;./derbyclient.jar"
DBRESET_RUNPATH=".;derby.jar;derbytools.jar"
BIN_MAP=bin
TEST_LIBS=".;./commons-cli-1.2.jar;./derby.jar;./junit-4.12.jar;./hamcrest-all-1.3.jar"

setup:
	mkdir -p bin
	$(MAKE) dbreset

cserver:
	javac -classpath $(SERVER_LIBS) -d $(BIN_MAP) server/Server.java

cclient:
	javac -classpath $(SERVER_LIBS) -d $(BIN_MAP) client/Client.java

key:
	$(MAKE) cserver
	java -classpath $(SERVER_RUNPATH) server.Server --keygen

pserver:
	$(MAKE) cserver
	java -classpath $(SERVER_RUNPATH) server.Server --cli

vserver:
	$(MAKE) cserver
	java -classpath $(SERVER_RUNPATH) server.Server -v --cli

pclient:
	$(MAKE) cclient
	java -classpath $(CLIENT_RUNPATH) client.Client --cli

vclient:
	$(MAKE) cclient
	java -classpath $(CLIENT_RUNPATH) client.Client -v --cli

gclient:
	$(MAKE) cclient
	java -classpath $(CLIENT_RUNPATH) client.Client -v --gui

dbreset:
	rm -rf database
	java -classpath $(DBRESET_RUNPATH) -Djdbc.drivers=org.apache.derby.jdbc.EmbeddedDriver org.apache.derby.tools.ij < ijcommands.txt

exampledb:
	java -classpath $(CLIENT_RUNPATH) client.Client -v --cli < exampledatabase.txt

clean:
	find . -name "*.class" | xargs rm

mclient:
	javac -d bin client/MinimalClient.java
	java -cp "bin" client.MinimalClient

mserver:
	javac -d bin server/MinimalServer.java
	java -cp "bin" server.MinimalServer

sclient:
	javac client/MinimalClient.java
	java -Djavax.net.ssl.trustStore=mySrvKeystore -Djavax.net.ssl.trustStorePassword=123456 client.MinimalClient

sserver:
	javac server/MinimalServer.java
	java -Djavax.net.ssl.keyStore=mySrvKeystore -Djavax.net.ssl.keyStorePassword=123456 server.MinimalServer

SERVER_LIBS_MAC=".:./commons-cli-1.2.jar:./derby.jar"
SERVER_RUNPATH_MAC=".:./bin:./commons-cli-1.2.jar:./derby.jar"
CLIENT_LIBS_MAC=".:commons-cli-1.2.jar:./derby.jar:./derbyclient.jar"
CLIENT_RUNPATH_MAC="bin:./commons-cli-1.2.jar:./derby.jar:./derbyclient.jar"
DBRESET_RUNPATH_MAC=".:derby.jar:derbytools.jar"

setup-mac:
	mkdir -p bin
	$(MAKE) dbreset-mac

pserver-mac:
	javac -classpath $(SERVER_LIBS_MAC) -d bin server/Server.java
	java -classpath $(SERVER_RUNPATH_MAC) .server.Server --cli

vserver-mac:
	javac -classpath $(SERVER_LIBS_MAC) -d bin server/Server.java
	java -classpath $(SERVER_RUNPATH_MAC) server.Server -v --cli

pclient-mac:
	javac -classpath $(CLIENT_LIBS_MAC) -d bin client/Client.java
	java -classpath $(CLIENT_RUNPATH_MAC) client.Client --cli

vclient-mac:
	javac -classpath $(CLIENT_LIBS_MAC) -d bin client/Client.java
	java -classpath $(CLIENT_RUNPATH_MAC) client.Client -v --cli

dbreset-mac:
	rm -rf database
	java -classpath $(DBRESET_RUNPATH_MAC) -Djdbc.drivers=org.apache.derby.jdbc.EmbeddedDriver org.apache.derby.tools.ij < ijcommands.txt

clean-mac:
	find . -name "*.class" | xargs rm

testDatabase:
	javac -cp $(TEST_LIBS) server/TestDatabase.java
	java -cp $(TEST_LIBS) org.junit.runner.JUnitCore server.TestDatabase
