package server;

import static utils.Configuration.verbose;

public class Server
{
	private static utils.Configuration settings = null;

	private static java.net.ServerSocket 	server_socket;
	private static java.net.Socket 			client_socket;

	private static java.io.InputStream 		input_from_client;
	private static java.io.OutputStream 	output_to_client;

	private static java.security.PrivateKey server_private_key;
	private static java.security.PublicKey 	server_public_key;
	private static java.security.PublicKey 	client_public_key;

	private static byte[] bytes;
	private static String last_message;

	private static ArgumentHandler argument_handler = null;

	public static void main(String[] args) throws java.io.IOException
	{
		argument_handler = new ArgumentHandler(args);
		if (argument_handler.print_help)
		{
			printHelp();
			System.exit(0);
		}
		utils.Configuration.verbose_mode = argument_handler.is_verbose;

		settings = utils.Configuration.loadDefaultConfiguration();

		branchIfKeygenArgumentGivenAndExit();
		loadTheKeysIntoMemory();
		
		while (true)
		{
			try
			{
				while (true)
				{
					waitForIncomingConnection();
					setup2WayCommunicationChannels();
					announceServerPublicKey();
					getPublicKeyFromClient();
					sendCertificateToClient(signClientsPublicKey());
					readIncomingbytes();
					handleLastMessage();
					finishConnection();
				}		
			}
			catch (java.net.BindException exc_obj) { try { finishConnectionWithError(); } catch (java.io.IOException exc_object) { System.out.println("Unable to unbind"); } }
			catch (java.net.SocketException exc_obj) { System.out.println(exc_obj); }
			catch (Exception exc_obj) { System.out.println(exc_obj); }
		}
	}

	public static void printHelp()
	{
		System.out.println("Help for server CLI");
	}

	/// Generates a private and public key and stores it inside 2 files in the root folder.
	public static void branchIfKeygenArgumentGivenAndExit() throws java.io.IOException, java.io.FileNotFoundException
	{
		if (argument_handler.keygen)
		{
			verbose("Creating public and private key pair.");
			java.security.KeyPair pair = utils.Utils.getNewKeyPair();
			java.io.FileOutputStream output = new java.io.FileOutputStream(utils.Configuration.settings.get("ServerPublicKeyFile"));
			output.write(pair.getPublic().getEncoded());
			output = new java.io.FileOutputStream(utils.Configuration.settings.get("ServerPrivateKeyFile"));
			output.write(pair.getPrivate().getEncoded());
			System.exit(0);
		}
	}

	public static void loadTheKeysIntoMemory() throws java.io.IOException
	{
		verbose("Loading keys into memory.");
		server_public_key = utils.Utils.getServerPublicKey();
		server_private_key = utils.Utils.getServerPrivateKey();
	}

	public static void waitForIncomingConnection() throws java.io.IOException
	{
		verbose("Waiting for incoming connection...");
		server_socket = new java.net.ServerSocket(Integer.parseInt(settings.get("port")));
		verbose("Waiting for a response");
		client_socket = server_socket.accept();
		client_socket.setSoTimeout(3000);
	}

	public static void setup2WayCommunicationChannels() throws java.io.IOException
	{
		verbose("Setting up 2-way communication.");
		input_from_client = client_socket.getInputStream();
		output_to_client = client_socket.getOutputStream();
	}

	public static void announceServerPublicKey() throws Exception
	{
		verbose("Broadcasting server's public key.");
		output_to_client.write(server_public_key.getEncoded());
	}

	public static void getPublicKeyFromClient()
	{
		verbose("Fetching the public key from the client.");
		byte[] bytes = new byte[Integer.parseInt(settings.get("keylength"))];
		try
		{
			int number = input_from_client.read(bytes);
			bytes = java.util.Arrays.copyOf(bytes, number);
			// System.out.println(new String(bytes));
			try
			{
				java.security.spec.X509EncodedKeySpec pubkey_spec = new java.security.spec.X509EncodedKeySpec(bytes);
				java.security.KeyFactory key_factory = java.security.KeyFactory.getInstance(settings.get("keypairgen"));
				client_public_key = key_factory.generatePublic(pubkey_spec);
			}
			catch (java.security.NoSuchAlgorithmException exc_obj)
			{
				verbose(exc_obj.toString());
			}
			catch (java.security.spec.InvalidKeySpecException exc_obj)
			{
				verbose(exc_obj.toString());
			}
		}
		catch (java.io.IOException exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public static byte[] signClientsPublicKey() throws java.security.NoSuchAlgorithmException, java.io.IOException, java.security.SignatureException, java.security.InvalidKeyException
	{
		verbose("Signing the client's public key.");
		java.security.Signature signature = java.security.Signature.getInstance(utils.Configuration.settings.get("SignMethod"));
		signature.initSign(server_private_key);
		signature.update(client_public_key.getEncoded());
		return signature.sign();
	}

	public static void sendCertificateToClient(byte[] signature) throws java.io.IOException
	{
		verbose("Sending signed public key to client.");
		output_to_client.write(signature);
	}

	public static void readIncomingbytes() throws java.io.IOException
	{
		verbose("Reading incoming bytes.");
		bytes = new byte[1024];
		int code = input_from_client.read(bytes);
		bytes = java.util.Arrays.copyOf(bytes, code);
		try
		{
			// System.out.println(new String(bytes));
			last_message = new String(utils.Utils.decrypt(bytes, server_private_key));
			System.out.println(last_message);
		}
		catch (Exception exc_obj)
		{
			verbose(exc_obj.toString());
		}
	}

	public static void handleLastMessage() throws java.io.IOException, Exception
	{
		verbose("Delegating input to the input handler.");
		// Need to check the validity: check if the grammar is correct.
		// If correct, apply the update. Let the client know that it was succesful.


		boolean success = true;
		if (success)
			notifyToClientOperationSuccess();
	}

	public static void notifyToClientOperationSuccess() throws java.io.IOException, Exception
	{
		verbose("Notifying to the client that the input was valid.");
		output_to_client.write(utils.Utils.encrypt("success".getBytes(), client_public_key));
	}

	public static void finishConnection() throws java.io.IOException
	{
		verbose("Cleaning up the connection.");
		server_socket.close();
	}

	public static void finishConnectionWithError() throws java.io.IOException
	{
		verbose("Reporting the error to the client.");
		try
		{
			output_to_client.write(utils.Utils.encrypt("ERROR_TOO_LONG_POSSIBLE".getBytes(), client_public_key));
		}
		catch (Exception exc_obj)
		{
			verbose("Could not write to client.");
		}
		server_socket.close();
	}



}