/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitvote;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 *
 * @author paulo
 */
public class StringUtils {
    //Short hand helper to turn Object into a json string
	/*public static String getJson(Object o) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(o);
	}*/
	
	//Returns difficulty string target, to compare to hash. eg difficulty of 5 will return "00000"  
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}
	
	public static String getStringFromKey(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
        public static PublicKey getPublicKeyFromString(String key) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] decodeKey = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodeKey);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA","BC");
            PublicKey pk = keyFactory.generatePublic(keySpec);
            return pk;
        }
        public static PrivateKey getPrivateKeyFromString(String key) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            byte[] decodeKey = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodeKey);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA","BC");
            PrivateKey pk = keyFactory.generatePrivate(keySpec);
            return pk;
        }
        
        public static String getStringFromSignature(byte[] SignatureBytes){
            return Base64.getEncoder().encodeToString(SignatureBytes);
        }
        public static long generateNonce(){
            SecureRandom sc = new SecureRandom();
            long nonce = sc.nextLong();
            if(nonce < 0){
                nonce = nonce *(-1);
            }
            return nonce;
        }
        
        public static String getMerkleRoot(ArrayList<Vote> votes) {
            int count = votes.size();

            List<String> previousTreeLayer = new ArrayList<String>();
            for(Vote vote : votes) {
                    previousTreeLayer.add(vote.voteId);
            }
            List<String> treeLayer = previousTreeLayer;

            while(count > 1) {
                    treeLayer = new ArrayList<String>();
                    for(int i=1; i < previousTreeLayer.size(); i+=2) {
                            treeLayer.add(HashUtils.hashFuncSHA256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
                    }
                    count = treeLayer.size();
                    previousTreeLayer = treeLayer;
            }

            String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
            return merkleRoot;
	}
        
        public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException{
            KeysGeneration kg = new KeysGeneration();
            KeyPair keyPair = new KeysGeneration().generateKeys();
            PublicKey pk = keyPair.getPublic();
            PrivateKey sk = keyPair.getPrivate();
            
            StringUtils su = new StringUtils();
            String pks = getStringFromKey(pk);
            PublicKey pubk = su.getPublicKeyFromString(pks);
            System.out.println(""+pubk.equals(pk));
            
            String sks = getStringFromKey(sk);
            PrivateKey privk = su.getPrivateKeyFromString(sks);
            System.out.println(""+privk.equals(sk));
        }
        
}

