/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author jferr
 */
public class RMISSLClientSocketFactory implements RMIClientSocketFactory, Serializable{
    
    @Override
    public Socket createSocket(String host, int port) throws IOException{
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(host,port);
        return socket;
    }
    
    @Override
    public int hashCode(){
        return getClass().hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        else if(obj==null || getClass() != obj.getClass()){
            return false;
        }
        return true;
    }
}
