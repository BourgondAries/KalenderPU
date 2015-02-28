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
After you have written your password and pressed enter, you can write a command. The guide will take you through the various steps in assigning data to the fields. When the final field is complete, your data is sent to the server where it will be checked, and stored if correct.


## Message Protocol For Server ##
The message protocol for sending from the client to the server is of the following format.
USERNAME PASSWORD OPERATION

Where spaces separate the 3 items. There will always be 3 items. If a username or password or operation itself contains spaces, its string is escaped. Here is an example of a username that has a space inside it:
user\ name PASSWORD OPERATION

The above is split into (this happens on the server):
1) 'user name'
2) 'PASSWORD'
3) 'OPERATION'

The same rules go for password and operation. If they contain spaces, they must be escaped.

The Database class will take in a user and an OPERATION. It will at the start split OPERATION into different parts (OPERATION is also escaped.)

## Draft of Second Protocol ##
Escaping is unnecessary. Sets of data are enclosed inside quotes as: "inside this thing".  
A space separates the different strings: "This is a single entity" "as well as this being a single entity".  
For sub-data we may escape the quote with a preceding quote: "This is ""a"" ""single"" ""Entity""".  
The above is escaped into 'This is "a" "single" "entity"'.  
I don't think we need to use this protocol. 