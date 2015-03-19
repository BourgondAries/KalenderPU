## Setup ##
1. Install "make". A command-line program. On GNU/Linux you may issue `sudo apt-get install make`. On Windows install [http://sourceforge.net/projects/mingw/files/Installer/](MingW); then add the folder where mingw32-make.exe is located into the "system path" (All command line invocations search each system path for the program to execute).
2. `make setup` - On windows you may invoke `mingw32-make.exe`. This sets up the database in a completely fresh state. Note that the database will have 1 user named "root". This user will be added by the server program. `ij>` may take a while. Just let it run until it exits. On mac use the specified mac extensions such as "make setup-mac"
3. `make key` - Run this to generate a public/private key pair for the server.
4. Run the server (`make vserver` or `make vserver-mac` or `mingw32-make.exe vserver`)
5. Run the client (from another terminal, `make vclient` or `make vclient-mac` or `mingw32-make.exe vclient`).

## Sending a Message ##
Now that the client and server are running, press enter on the server terminal. This will set the standard port. The standard port is contained in settings.conf under the key "port". Now press enter on the client to set the hostname to be default (also found in settings.conf under "hostname"). Doing this skips setting the port of the client.  
You enter "root" as username and password (in the client terminal). Note that the password field should not echo your input. Press enter after completing your password.  
After you have written your password and pressed enter; you will get a result from the server. If your password was wrong you will be queried to retry. Use the "relogin" command to attempt another login. You can write any command and use "help" to get a list of all commands. All commands that take operands or arguments have a helping wizard. The guide will take you through the various steps in assigning data to the fields. When the final field is complete, your data is sent to the server where it will be checked, and stored or queried if correct. You will receive the data within a small timeframe (depending on the network of course).

## Making Backups ##
Run the shell script backup.sh to make a backup.

## Inserting the example database ##
To insert the example database run `make dbreset`, then run the server once via `make vserver`. Open a new terminal and run `make exampledb`. This will issue certain commands and insert an example database.

# Programming Specifics #

## Quick Documentation ##

"trusted" - File containing all public keys of trusted servers. Used by clients only.  
"privatekey" - File containing a server's private key.  
"publickey" - File containing a server's public key.  
"settings.conf" - Configuration file for mappings. These are accessed by utils.Utils.Configuration.settings.get(String).  

The program is divided into a server and a client component. Both communicate over the port specified in settings.conf.

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
