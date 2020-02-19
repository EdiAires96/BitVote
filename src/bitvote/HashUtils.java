/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitvote;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author paulo
 */
public class HashUtils{
    public static String hashFuncSHA256(String input){
        try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                //Applies sha256 to our input, 
                byte[] hash = digest.digest(input.getBytes("UTF-8"));

                StringBuilder hexString = new StringBuilder(); // This will contain hash as hexidecimal
                for (int i = 0; i < hash.length; i++) {
                        String hex = Integer.toHexString(0xff & hash[i]);
                        if(hex.length() == 1) hexString.append('0');
                        hexString.append(hex);
                }
                return hexString.toString();
            }
            catch(UnsupportedEncodingException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
    }
}
