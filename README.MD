Quick Documentation

"trusted" - File containing all public keys of trusted servers. Used by clients only.
"privatekey" - File containing a server's private key.
"publickey" - File containing a server's public key.
"settings.conf" - Configuration file for mappings. These are accessed by utils.Utils.Configuration.settings.get(String).

The program is divided into a server and a client component. Both communicate over the port specified in settings.conf.

## How to Run the Server or Client ##

First compile using "javac -cp ".;commons-cli-1.2.jar;derby.jar" server/Server.java", then run using "java -cp ".;commons-cli-1.2.jar;derby.jar" server.Server".
For the client; first compile: "javac -cp ".;commons-cli-1.2.jar;derbyclient.jar" client/Client.java", then run using "java -cp ".;commons-cli-1.2.jar;derbyclient.jar" client.Client".