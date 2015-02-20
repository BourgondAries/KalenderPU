package utils;

public class Utils
{
	public static String fileToString(String filename) throws java.io.IOException
	{
		return new java.util.Scanner(new java.io.File(filename)).useDelimiter("\\Z").next();
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
			keygen.initialize(Integer.parseInt(Configuration.settings.get("keylength")), random);
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
}