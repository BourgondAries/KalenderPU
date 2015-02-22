# Quick Documentation #

"trusted" - File containing all public keys of trusted servers. Used by clients only.
"privatekey" - File containing a server's private key.
"publickey" - File containing a server's public key.
"settings.conf" - Configuration file for mappings. These are accessed by utils.Utils.Configuration.settings.get(String).

The program is divided into a server and a client component. Both communicate over the port specified in settings.conf.

## Setup ##
1. `make setup` - this sets up the database in a completely fresh state. Note that the daabase will have 1 user named "root". This user will be added by the server program. Also install the program known as "make". This program is a generic build-file interpreter that executes commands within the build-file. On windows you may have to download MingW32. On GNU/Linux you may issue "sudo apt-get install make". On windows you may invoke `mingw32-make.exe`.
2. Run the server (`make vserver`)
3. Run the client (from another terminal, `make vclient`).