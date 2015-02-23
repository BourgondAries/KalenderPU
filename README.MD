# Quick Documentation #

"trusted" - File containing all public keys of trusted servers. Used by clients only.  
"privatekey" - File containing a server's private key.  
"publickey" - File containing a server's public key.  
"settings.conf" - Configuration file for mappings. These are accessed by utils.Utils.Configuration.settings.get(String).  

The program is divided into a server and a client component. Both communicate over the port specified in settings.conf.

## Setup ##
1. `make setup` - this sets up the database in a completely fresh state. Note that the daabase will have 1 user named "root". This user will be added by the server program. Also install the program known as `make`. This program is a generic build-file interpreter that executes commands within the build-file. On windows you may have to download [http://sourceforge.net/projects/mingw/files/Installer/](MingW32). On GNU/Linux you may issue `sudo apt-get install make`. On windows you may invoke `mingw32-make.exe`. `ij>` may take a while. Just let it run until it exits.
2. Run the server (`make vserver`)
3. Run the client (from another terminal, `make vclient`).

## Sending a Message ##
Now that the client and server are running, press enter on the server terminal. This will set the standard port. Now press enter on the client to set the hostname to be default.  
You enter "root" as username and password. Note that the password field does not echo your input.  
After you have written your password and pressed enter, you can write a message. The next enter will submit the message to the server, and you will get a response. After getting a response you can again write a message. Type exit to exit the client.