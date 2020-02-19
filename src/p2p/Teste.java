/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Vitor
 */
public class Teste {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // TODO code application logic here
        System.out.print("1- Servidor 2-Cliente: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        int num = Integer.parseInt(in.readLine());
        System.out.println("Numero: " + num);
        if (num == 1) {
            Servidor servidor = new Servidor();
        } else {
            //Cliente cliente = new Cliente("localhost");
        }
    }
    
}
