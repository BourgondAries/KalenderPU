package utils;

public class Utils
{
	public static String fileToString(String filename) throws java.io.IOException
	{
		return new java.util.Scanner(new java.io.File(filename)).useDelimiter("\\Z").next();
	}

	public static byte[] fileToBytes(String filename) throws java.io.IOException
	{
		java.nio.file.Path path = java.nio.file.Paths.get(filename);
      	return java.nio.file.Files.readAllBytes(path);
	}

	public static byte[] encrypt(byte[] inp_bytes, java.security.PublicKey key) throws Exception
	{
		return encrypt(inp_bytes, key, Configuration.settings.get("xform"));
	}

	public static byte[] decrypt(byte[] inp_bytes, java.security.PrivateKey key) throws Exception
	{
		return decrypt(inp_bytes, key, Configuration.settings.get("xform"));
	}

	public static byte[] encrypt(byte[] inpBytes, java.security.PublicKey key, String xform) throws Exception
	{
	    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(xform);
	    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
	    return cipher.doFinal(inpBytes);
	}

	public static byte[] decrypt(byte[] inpBytes, java.security.PrivateKey key, String xform) throws Exception
	{
	    javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(xform);
	    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key);
	    return cipher.doFinal(inpBytes);
	}

	public static java.security.KeyPair getNewKeyPair()
	{
		try
		{
			java.security.KeyPairGenerator keygen = java.security.KeyPairGenerator.getInstance(Configuration.settings.get("keypairgen"));
			java.security.SecureRandom random = java.security.SecureRandom.getInstance(Configuration.settings.get("SecureRandomRNG"), Configuration.settings.get("SecureRandomProvider"));
			keygen.initialize(Configuration.settings.getInt("keylength"), random);
			return keygen.generateKeyPair();
		}
		catch (java.security.NoSuchAlgorithmException exc_obj)
		{
			System.out.println(exc_obj);
		}
		catch (java.security.NoSuchProviderException exc_obj)
		{
			System.out.println(exc_obj);
		}
		return null;
	}

	public static java.security.PublicKey bytesToPublicKey(byte[] bytes)
	{
		try
		{
			java.security.spec.X509EncodedKeySpec pubkey_spec = new java.security.spec.X509EncodedKeySpec(bytes);
			java.security.KeyFactory key_factory = java.security.KeyFactory.getInstance(Configuration.settings.get("keypairgen"));
			return key_factory.generatePublic(pubkey_spec);
		}
		catch (java.security.NoSuchAlgorithmException exc_obj)
		{
			System.out.println(exc_obj);
		}
		catch (java.security.spec.InvalidKeySpecException exc_obj)
		{
			System.out.println(exc_obj);
		}
		return null;
	}

	public static java.security.PrivateKey bytesToPrivateKey(byte[] bytes)
	{
		try
		{
			java.security.spec.PKCS8EncodedKeySpec pubkey_spec = new java.security.spec.PKCS8EncodedKeySpec(bytes);
			java.security.KeyFactory key_factory = java.security.KeyFactory.getInstance(Configuration.settings.get("keypairgen"));
			return key_factory.generatePrivate(pubkey_spec);
		}
		catch (java.security.NoSuchAlgorithmException exc_obj)
		{
			System.out.println(exc_obj);
		}
		catch (java.security.spec.InvalidKeySpecException exc_obj)
		{
			System.out.println(exc_obj);
		}
		return null;
	}

	public static java.security.PublicKey getServerPublicKey() throws java.io.IOException
	{
		byte[] pubkey_raw = fileToBytes(Configuration.settings.get("ServerPublicKeyFile"));
		return bytesToPublicKey(pubkey_raw);
	}

	public static java.security.PrivateKey getServerPrivateKey() throws java.io.IOException
	{
		byte[] pubkey_raw = fileToBytes(Configuration.settings.get("ServerPrivateKeyFile"));
		return bytesToPrivateKey(pubkey_raw);
	}

	private static final String HEXES = "0123456789ABCDEF";

	static String getHex(byte[] raw) 
	{
	    final StringBuilder hex = new StringBuilder(2 * raw.length);
	    for (final byte b : raw) 
	    {
	        hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
	    }
    	return hex.toString();
	}	
}