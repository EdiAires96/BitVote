/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import bitvote.AES;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author jferr
 */
public class SQLiteBD {
    
    private Connection c;
    private Statement stmt;
    private PreparedStatement prstmt;
    private byte[] key; 
    private byte[] iv;
    public static int flag = 0;
    
    
    //construtor vazio
    public SQLiteBD(){
        flag = flag + 1;
        File fBD = new File("BitVote.db");
        if (fBD.exists()) {
            encryptionDB('d');
        }
        
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:BitVote.db");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SQLiteBD.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    //returna o statement da bd
    public Statement returnStmt(){
        
        stmt = null;
        try{
            stmt = c.createStatement();
            
        }catch(Exception e){
            System.out.println(e.getClass().getName() +": "+e.getMessage());
            System.exit(0);
        }
        return stmt;
    }
    
    //returna o preparedstatement da bd
    public PreparedStatement returnPrStmt(String sql){
        
        prstmt = null;
        try{
  
            prstmt = c.prepareStatement(sql);
            
        }catch(Exception e){
            System.out.println(e.getClass().getName() +": "+e.getMessage());
            System.exit(0);
        }
        return prstmt;
    }
    
    
    public void closeBD(){
        try{
            if (stmt != null) {
                stmt.close();
            } else if (prstmt != null) {
                prstmt.close();
            }

            c.close();
            flag = flag -1;
            if(flag==0){
                encryptionDB('e');
            }
        }
        catch(SQLException e){
            encryptionDB('e');
        }
        
    }
    
    //cria a bd e tabela file
    public void createBD(){
        
        c = null;
        try{
   
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:BitVote.db");
            
        }catch(Exception e){
            System.out.println(e.getClass().getName() +": "+e.getMessage());
            System.exit(0);
        }
        
        System.out.println("Opended database successfuly");
        
        stmt = null;
        
        try{
            stmt = c.createStatement();
           
            String sql = "CREATE TABLE IF NOT EXISTS User "+
                  "(pubkey TEXT PRIMARY KEY NOT NULL,"+
                    "name NVARCHAR(100) NOT NULL,"+
                    "idnumber INTEGER NOT NULL"
                    + ");"  ;
            
            stmt.executeUpdate(sql); 
            
            sql = "CREATE TABLE IF NOT EXISTS Candidate "+
                  "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                  "name NVARCHAR(100) NOT NULL"
                    + ");"  ;
            
            stmt.executeUpdate(sql);
            
            sql = "CREATE TABLE IF NOT EXISTS Nonce "+
                  "(id INTEGER PRIMARY KEY NOT NULL,"+
                  "candidateid INTEGER NOT NULL,"+
                  "pubkey TEXT NOT NULL,"+
                  "FOREIGN KEY(candidateid) REFERENCES Candidate(id),"+
                  "FOREIGN KEY(pubkey) REFERENCES User(pubkey)"  
                    + ");"  ;
            
            stmt.executeUpdate(sql);
            
            sql = "CREATE TABLE IF NOT EXISTS Election "+
                  "(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"+
                  "name NVARCHAR(100) NOT NULL," +
                  "nullvotes INTEGER NOT NULL,"+
                  "status INTEGER NOT NULL"
                    + ");"  ;
             
            stmt.executeUpdate(sql);
            
            sql = "CREATE TABLE IF NOT EXISTS Result "
                    + "(candidateid UNSIGNED INT NOT NULL,"
                    + "electionid INTEGER NOT NULL,"
                    + "votesnumber INTEGER NOT NULL,"
                    + "PRIMARY KEY (candidateid, electionid)"
                    + "FOREIGN KEY(candidateid) REFERENCES Candidate(id),"
                    + "FOREIGN KEY(electionid) REFERENCES Election(id)"
                    + ");";  
            
            stmt.executeUpdate(sql);
            
            closeBD();
            
            
        }catch(Exception e){
            System.out.println(e.getClass().getName() +": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfuly");
        
    }
    
    private static boolean isBase64(String stringBase64) {
        String regex
                = "([A-Za-z0-9+/]{4})*"
                + "([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)";

        Pattern patron = Pattern.compile(regex);

        if (!patron.matcher(stringBase64).matches()) {
            return false;
        } else {
            return true;
        }
    }
    
    private void encryptionDB(char c){
        
        String outString="";
        
         try {
            InputStream inputStream = new FileInputStream("BitVote.db");
            Reader inputStreamReader = new InputStreamReader(inputStream,"ISO-8859-1");

            int data = inputStreamReader.read();
            while (data != -1) {
                char theChar = (char) data;
                outString += theChar;
                data = inputStreamReader.read();
            }

            inputStreamReader.close();
        } catch (Exception ex) {
            Logger.getLogger(SQLiteBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        key = DatatypeConverter.parseHexBinary("8d333136f2a3c763ca594e0b2b6aecc94b276b80deee97bc0e37b5511c42c4d8");
     
       
        
        if(c=='e'){
            try {
                if(isBase64(outString)){
                    return;
                }
                
                iv = AES.generateIV();
                FileOutputStream fout = new FileOutputStream("iv.txt");
                String ivHex = DatatypeConverter.printHexBinary(iv);
                fout.write(ivHex.getBytes());
     
                byte[] cripto = AES.encrypt(outString, key, iv);
                
                fout = new FileOutputStream("BitVote.db");
                
                byte[] encoded = Base64.getEncoder().encode(cripto);               
                fout.write(encoded);
                fout.close();
            } catch (Exception ex) {
                Logger.getLogger(SQLiteBD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(c=='d'){
            try {
                if(!isBase64(outString)){
                    return;
                }
                
                String ivString = "";
                try {
                    FileReader filer = new FileReader("iv.txt");
                    char[] buff = new char[1];
                    while (filer.read(buff) > 0) {
                        ivString += buff[0];
                    }
                    filer.close();
                } catch (Exception ex) {
                    Logger.getLogger(SQLiteBD.class.getName()).log(Level.SEVERE, null, ex);
                }

                iv =  DatatypeConverter.parseHexBinary(ivString);
                
                byte[] decoded = Base64.getDecoder().decode(outString.getBytes("ISO-8859-1"));
                String pt = AES.decrypt(decoded, key, iv);
                FileOutputStream out = new FileOutputStream("BitVote.db");
                out.write(pt.getBytes("ISO-8859-1"));
                out.close();
            } catch (Exception ex) {
                Logger.getLogger(SQLiteBD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
    }
}