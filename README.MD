# Quick Documentation #

"trusted" - File containing all public keys of trusted servers. Used by clients only.  
"privatekey" - File containing a server's private key.  
"publickey" - File containing a server's public key.  
"settings.conf" - Configuration file for mappings. These are accessed by utils.Utils.Configuration.settings.get(String).  

The program is divided into a server and a client component. Both communicate over the port specified in settings.conf.

## Setup ##
1. Install "make". A command-line program. On GNU/Linux you may issue `sudo apt-get install make`. On Windows install [http://sourceforge.net/projects/mingw/files/Installer/](MingW); then add the folder where mingw32-make.exe is located into the "system path" (All command line invocations search each system path for the program to execute).
2. `make setup` - On windows you may invoke `mingw32-make.exe`. This sets up the database in a completely fresh state. Note that the database will have 1 user named "root". This user will be added by the server program. `ij>` may take a while. Just let it run until it exits. On mac use the specified mac extensions such as "make setup-mac"
2. Run the server (`make vserver` or `make vserver-mac` or `mingw32-make.exe vserver`)
3. Run the client (from another terminal, `make vclient` or `make vclient-mac` or `mingw32-make.exe vclient`).

## Sending a Message ##
Now that the client and server are running, press enter on the server terminal. This will set the standard port. Now press enter on the client to set the hostname to be default.  
You enter "root" as username and password. Note that the password field does not echo your input.  
After you have written your password and pressed enter, you can write a message. The next enter will submit the message to the server, and you will get a response. After getting a response you can again write a message. Type exit to exit the client.


"Is RSA With AES Secure?
I'm creating a server and client programme and after reading about all that can go wrong with encryption I am still wondering if I am doing something terribly destructive.

Here is my setup. The code is in Java:
```
// Client
private java.net.Socket 			client_socket;
private java.io.InputStream 		input_from_server;
private java.io.OutputStream 		output_to_server;
private java.security.PrivateKey 	client_private_key;
private java.security.PublicKey 	server_public_key, trusted_server;
private java.security.PublicKey  	client_public_key;
private java.security.Key 			symmetric_key;

// Server
private java.net.ServerSocket 		server_socket;
private java.net.Socket 			client_socket;
private java.io.InputStream 		input_from_client;
private java.io.OutputStream 		output_to_client;
private java.security.PrivateKey 	server_private_key;
private java.security.PublicKey 	server_public_key;
private java.security.PublicKey 	client_public_key;
private java.security.Key 			symmetric_key;
```
I then perform the following actions:
Client:

	// First create the RSA key pairs
	java.security.KeyPairGenerator keygen = java.security.KeyPairGenerator.getInstance("RSA");
	java.security.SecureRandom random = java.security.SecureRandom.getInstance("SHA1PRNG", "SUN");
	keygen.initialize(512, random);
	java.security.KeyPair pair = keygen.generateKeyPair();
	client_private_key = pair.getPrivate();
	client_public_key = pair.getPublic();

	// Then generate the symmetric key
	javax.crypto.KeyGenerator kg = javax.crypto.KeyGenerator.getInstance("AES");
	random = new java.security.SecureRandom();
	kg.init(random);
	symmetric_key = kg.generateKey();