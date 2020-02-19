/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitvote;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.util.encoders.Hex;


public class AES 
{
	
	public static byte[] generateKey() throws NoSuchAlgorithmException
	{
		KeyGenerator key = KeyGenerator.getInstance("AES"); key.init(128);
		SecretKey s = key.generateKey(); 
		byte[] keyInBytes = s.getEncoded();
		return keyInBytes;		
	}
	
	public static byte[] generateIV()
	{
		SecureRandom secureRandom = new SecureRandom();
		byte[] iv = new byte[16]; 
		secureRandom.nextBytes(iv);
		return iv;		
	}

	public static byte[] encrypt(String str, byte[] secretKey, byte[] iv) throws Exception
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding","BC");
		SecretKeySpec key = new SecretKeySpec(secretKey, "AES");
		cipher.init(Cipher.ENCRYPT_MODE,key,new IvParameterSpec(iv));
		byte [] cipherText=cipher.doFinal(str.getBytes("UTF-8"));
		return cipherText;
	}	

	public  static String decrypt(byte[] cipherText, byte[] secretKey, byte[] iv) throws Exception
	{
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding","BC");
		SecretKeySpec key = new SecretKeySpec(secretKey, "AES");
		cipher.init(Cipher.DECRYPT_MODE,key,new IvParameterSpec(iv));
		return new String(cipher.doFinal(cipherText));
	}
	
	/*
	public static void main(String[] args) throws NoSuchAlgorithmException, Exception 
	{
		
		String plaintext = "OLA MUNDO";
		System.out.println(plaintext);
	
		CifraAES cifraAES = new CifraAES();
		byte[] iv = cifraAES.generateIV();
		
		byte[] key= cifraAES.generateKey();
		System.out.println("KEY = " + new String(Hex.encode(key)));
		
		byte[] cipher = cifraAES.encrypt(plaintext, key, iv);
		System.out.println("CipherText ="+cipher);
		
		String plaintext2 = cifraAES.decrypt(cipher, key, iv);
		System.out.println(plaintext2);
		
										
	}
	*/
}
