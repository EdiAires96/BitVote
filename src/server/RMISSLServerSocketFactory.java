/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

/**
 *
 * @author jferr
 */
public class RMISSLServerSocketFactory implements RMIServerSocketFactory{
    /*
     * Create one SSLServerSocketFactory, so we can reuse sessions
     * created by previous sessions of this SSLContext.
     */
    
    private SSLServerSocketFactory ssf = null;
    
    public RMISSLServerSocketFactory() throws Exception {
       try{
           //set up key manager to do server authentication
           SSLContext ctx;
           KeyManagerFactory kmf;
           KeyStore ks;

           char[] passphrase = "passphrase".toCharArray();
           ks = KeyStore.getInstance("JKS");
           ks.load(new FileInputStream("testkeys"), passphrase);

           kmf = KeyManagerFactory.getInstance("SunX509");
           kmf.init(ks, passphrase);

           ctx = SSLContext.getInstance("TLS");
           ctx.init(kmf.getKeyManagers(), null, null);

           ssf = ctx.getServerSocketFactory();
       }
       catch(Exception e){
           e.printStackTrace();
           throw e;
       }
    }
    
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return ssf.createServerSocket(port);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }
}
