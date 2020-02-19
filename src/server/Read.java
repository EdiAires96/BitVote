/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 *
 * @author Edi
 */
public class Read {

    public static String readString() {

        String s = "";
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            s = in.readLine();
        } catch (IOException e) {
            System.out.println("Erro ao ler fluxo de entrada.");
        }
        return s;
    }

    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString().trim());
            } catch (NumberFormatException e) {
                System.out.println("Não é um inteiro válido!!!");
            }
        }
    }
    
    public static int readIntInInterval(int i) {
        while (true) {
            try {
                
                int number = Integer.parseInt(readString().trim());
                if(number<=0 || number<=i){
                    System.out.println("Insira um número positivo!!");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("Número fora do Intrevalo!!!");
            }
        }
    }
    
    public static int readPositiveInt() {
        while (true) {
            try {

                int number = Integer.parseInt(readString().trim());
                if (number <= 0 ) {
                    System.out.println("Insira um número positivo!!");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("Número fora do Intrevalo!!!");
            }
        }
    }
    
    public static int readVotesInt() {
        while (true) {
            try {
                String n = readString().trim();
                if(n.equals(""))
                    return 0;
                return Integer.parseInt(n);
            } catch (NumberFormatException e) {
                System.out.println("Número fora do Intrevalo!!!");
            }
        }
    }
    
    public static int readCandidatesNumber() {
        while (true) {
            try {

                int number = Integer.parseInt(readString().trim());
                if (number <= 0) {
                    System.out.println("Insira um número positivo!!");
                    continue;
                }
                if(number==1){
                    System.out.println("Insira um número igual ou maior que 2!");
                    continue;
                }
                return number;
            } catch (NumberFormatException e) {
                System.out.println("Número fora do Intrevalo!!!");
            }
        }
    }

    public static Double ReadDouble() {
        while (true) {
            try {
                return Double.valueOf(readString().trim());
            } catch (NumberFormatException e) {
                System.out.println("Não é um double válido!!!");
            }
        }
    }

    public static Float readFloat() {
        while (true) {
            try {
                return Float.parseFloat(readString().trim());
            } catch (NumberFormatException e) {
                System.out.println("Não é um float válido!!!");
            }
        }
    }

    public static boolean readBoolean() {
        while (true) {
            try {
                return Boolean.valueOf(readString().trim()); //o trim tira os espaços em branco e o return
            } catch (Exception e) {
                System.out.println("Não é um boolean válido!!!");
            }
        }
    }

    public static char readChar() {
        while (true) {
            try {
                return readString().trim().charAt(0);
            } catch (Exception e) {
                System.out.println("Não é um char válido!!!");
            }
        }
    }

    public static long readLong() {
        while (true) {
            try {
                return Long.valueOf(readString().trim());
            } catch (NumberFormatException e) {
                System.out.println("Não é um long válido!!!");
            }
        }
    }

}
