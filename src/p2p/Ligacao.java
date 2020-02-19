/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p;

import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Vitor
 */
public class Ligacao {
    private Socket s;
    private ObjectOutputStream oos;

    public Ligacao(Socket s, ObjectOutputStream oos) {
        this.s = s;
        this.oos = oos;
    }

    public Socket getS() {
        return s;
    }

    public void setS(Socket s) {
        this.s = s;
    }

    public ObjectOutputStream getOos() {
        return oos;
    }

    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }
    
    
}
