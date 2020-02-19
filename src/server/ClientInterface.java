/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.rmi.RemoteException;

/**
 *
 * @author jferr
 */
public interface ClientInterface extends java.rmi.Remote {
    
    byte[] signNonce(String nonce) throws RemoteException;
    void Print(String mensagem) throws RemoteException;
}
