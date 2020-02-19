/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitvote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;


public class SignatureUtils {
    private static int BUFSIZE = 8192;
    
    public SignatureUtils(){
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
     
    public static byte[] signString(String text, PrivateKey privateKey) throws InvalidKeyException, Exception{
       byte[] data = text.getBytes("UTF-8");
       
       Signature sig = Signature.getInstance("ECDSA", "BC");
       sig.initSign(privateKey);
       sig.update(data);
       
       byte[] signatureBytes = sig.sign();
       return signatureBytes;
    }
    
    public static Boolean verifyString(String text, byte[] signatureBytes, PublicKey publicKey)throws InvalidKeyException, Exception{
        byte[] data = text.getBytes("UTF-8");
        
        Signature sig = Signature.getInstance("ECDSA", "BC");
        sig.initVerify(publicKey);
        sig.update(data);
        
        return(sig.verify(signatureBytes));
        
    }
    public byte[] signFile(File fileToSign, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("ECDSA", "BC");
        signature.initSign(privateKey);
        processData(fileToSign, signature);
        return signature.sign();
    }

    public boolean verifyFile(File fileToVerify, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("ECDSA", "BC");
        signature.initVerify(publicKey);
        processData(fileToVerify, signature);
        return signature.verify(signatureBytes);
    }
    
    private static void processData(File f, Signature s) throws Exception {
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f), BUFSIZE);
        byte[] buf = new byte[BUFSIZE];
        int numRead;
        while ((numRead = bis.read(buf)) > 0) {
            s.update(buf, 0, numRead);
        }
        bis.close();
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, Exception{
        //Gerar Par de chaves
        /*
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        
        KeyPair kp = kpg.generateKeyPair();
        PrivateKey privateKey = kp.getPrivate();
        PublicKey publicKey = kp.getPublic();
        //Assinar e verificar em Ficheiros
        File f = new File("Text.txt");
        
        byte[] assinatura = signFile(f, privateKey);
        System.out.println(new BASE64Encoder().encode(assinatura));
        
        System.out.println(""+verifyFile(f,assinatura, publicKey));
        
        //Assinar e Verificar em Strings
        String s = "Ola eu sou o Paulo\nAquele também";
        byte[] assinaturaString = signString(s, privateKey);
        System.out.println(new BASE64Encoder().encode(assinaturaString));
        
        System.out.println(""+verifyString(s,assinaturaString, publicKey));*/
        
        
        
    }
  
   
}
