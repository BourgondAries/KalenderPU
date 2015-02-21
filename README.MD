Quick Documentation

"trusted" - File containing all public keys of trusted servers. Used by clients only.
"privatekey" - File containing a server's private key.
"publickey" - File containing a server's public key.
"settings.conf" - Configuration file for mappings. These are accessed by utils.Utils.Configuration.settings.get(String).

The program is divided into a server and a client component. Both communicate over the port specified in settings.conf.