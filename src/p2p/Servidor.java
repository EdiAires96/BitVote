/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Servidor {

    private ServerSocket ss;
    private Socket s;
    private Connection c;
    public static ArrayList<Socket> connections = new ArrayList<Socket>();
    public static ArrayList<ObjectOutputStream> Lista_oos = new ArrayList<ObjectOutputStream>();
    public static ArrayList<Ligacao> Ligacoes = new ArrayList<Ligacao>();
    private Ligacao lig;

    public Servidor() {
        try {
            ss = new ServerSocket(2222);
            while (true) {
                s = ss.accept();

                ObjectOutputStream oos = null;
                oos = new ObjectOutputStream(s.getOutputStream());

                lig = new Ligacao(s, oos);
                Ligacoes.add(lig);

                //Deteta uma nova conexão e cria um thread
                c = new Connection(s,lig);

                //Atualiza a lista de sockets ativos
                connections.add(s);
                System.out.println("Lista de Sockets: " + connections.toString());
                System.out.println("Adress: " + s.getInetAddress().getHostAddress());

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
