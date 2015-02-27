package utils;

public class Utils
{

	public static String makeRandomString(int n)
	{
		java.util.Random random = new java.util.Random();
		random.setSeed(System.currentTimeMillis());
		StringBuilder rand_string = new StringBuilder();
		rand_string.setLength(n);
		for (int i = 0; i < n; i++)
		{
			rand_string.setCharAt(i, (char) (32 + random.nextInt(126 - 32))); 
		}
		return rand_string.toString();
	}

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
		byte[] privkey_raw = fileToBytes(Configuration.settings.get("ServerPrivateKeyFile"));
		return bytesToPrivateKey(privkey_raw);
	}

	private static final String HEXES = "0123456789ABCDEF";

	public static String getHex(byte[] raw) 
	{
	    final StringBuilder hex = new StringBuilder(2 * raw.length);
	    for (final byte b : raw) 
	    {
	        hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
	    }
    	return hex.toString();
	}

	public static String escapeSpaces(String to_escape)
	{
		StringBuilder sbldr = new StringBuilder(to_escape);
		for (int i = 0; i < sbldr.length(); ++i)
		{
			if (sbldr.charAt(i) == ' ')
			{
				sbldr.insert(i, '\\');
				i += 1;
			}
		}
		return sbldr.toString();
	}

	public static String unescapeSpaces(String to_unescape)
	{
		StringBuilder sbldr = new StringBuilder(to_unescape);
		for (int i = 0; i < sbldr.length() - 1; ++i)
		{
			if (sbldr.charAt(i) == '\\')
			{
				if (sbldr.charAt(i + 1) == ' ')
				{
					sbldr.deleteCharAt(i);
				}
			}
		}
		return sbldr.toString();
	}

	public static java.util.ArrayList<String> splitAndUnescapeString(String message)
	{
		java.util.ArrayList<String> splits = new java.util.ArrayList<>();
		int last_split_pos = 0;
		for (int i = 0; i < message.length() - 1; ++i)
		{
			if (message.charAt(i) == '\\' && message.charAt(i + 1) == ' ')
			{
				i += 1;
				continue;
			}
			if (message.charAt(i) == ' ')
			{
				splits.add(utils.Utils.unescapeSpaces(message.substring(last_split_pos, i)));
				last_split_pos = i + 1;
			}
		}
		splits.add(utils.Utils.unescapeSpaces(message.substring(last_split_pos)));
		return splits;
	}

	public final static java.security.Key generateSymmetricKey(String specstring) throws java.security.NoSuchAlgorithmException
	{
		javax.crypto.KeyGenerator kg = javax.crypto.KeyGenerator.getInstance(specstring);
		java.security.SecureRandom random = new java.security.SecureRandom();
		kg.init(random);
		return kg.generateKey();
	}

	public static final byte[] encryptSymmetric(final byte[] message, final java.security.Key key, String ciphertype)
	{
		try
		{
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ciphertype);
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    		javax.crypto.spec.IvParameterSpec ivspec = new javax.crypto.spec.IvParameterSpec(iv);
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, ivspec);
			byte[] raw = cipher.doFinal(message);
			return raw;
		}
		catch (Exception exc)
		{
			System.out.println(exc);
			return null;
		}
	}

	public static final String decryptSymmetric(final byte[] message, final java.security.Key key, String ciphertype)
	{
		try
		{
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ciphertype);
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    		javax.crypto.spec.IvParameterSpec ivspec = new javax.crypto.spec.IvParameterSpec(iv);
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, ivspec);
			byte[] raw = cipher.doFinal(message);
			return new String(raw);
		}
		catch (Exception exc)
		{
			System.out.println(exc);
			return null;
		}
	}

}