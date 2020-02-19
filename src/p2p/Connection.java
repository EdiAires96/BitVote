/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p;

import bitvote.Vote;
import bitvote.VoteChain;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vitor
 */
public class Connection extends Thread {

    private Socket S;
    private Ligacao lig;

    public Connection(Socket s, Ligacao Lig) {
        super();
        S = s;
        lig = Lig;
        start();
    }

    public void ShareVote(Vote mensagem, Socket s) throws IOException {
        for (Ligacao con : Servidor.Ligacoes) {
            if (con.getS().equals(s)){
                //Não mandar ao proprio cliente
                continue;
            }
            //Indicar ao cliente que será enviado um voto
            con.getOos().writeObject("send-Voto");
            con.getOos().flush();
           
            //Fazer broadcast do voto
            con.getOos().writeObject(mensagem);
            con.getOos().flush();
        }
    }

    public void ShareBlockChain(VoteChain mensagem, Socket s) throws IOException {
        for (Ligacao con : Servidor.Ligacoes) {
            if (con.getS().equals(s)){
                //Não mandar ao proprio cliente
                continue;
            }
            
            //Indicar ao cliente que será enviada a BlockChain
            con.getOos().writeObject("send-BlockChain");
            con.getOos().flush();
            
            //Fazer broadcast da BlockChain
            con.getOos().writeObject(mensagem);
            con.getOos().flush();
        }
    }

    public void run() {
        try {
            ObjectInputStream ois = null;
            ois = new ObjectInputStream(S.getInputStream());

            while (true) {
                Vote voto = null;
                VoteChain blockChain = null;

                //tipoObjecto - Voto ou BlockChain
                String tipoObjecto = (String) ois.readObject();
             
                if (tipoObjecto.equals("send-Voto")) {
                    System.out.println("Servidor: Voto recebido");
                    voto = (Vote) ois.readObject();
                    //Juntar o proprio socket para não enviar ao próprio
                    ShareVote(voto, S);
                } else {
                    System.out.println("Servidor: Blockchain recebida");
                    blockChain = (VoteChain) ois.readObject();
                    //Juntar o proprio socket para não enviar ao próprio
                    ShareBlockChain(blockChain, S);
                }

            }

        } catch (SocketException ex) {
            System.out.println("Desliguei-me no connection!");
            Servidor.connections.remove(S);

            //remover oos
            Servidor.Ligacoes.remove(lig);

            System.out.println("Lista de Sockets: " + Servidor.connections.toString());
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
