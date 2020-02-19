/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import bitvote.AES;
import bitvote.Nonce;
import bitvote.SignatureUtils;
import bitvote.StringUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import p2p.Cliente;

/**
 *
 * @author jferr
 */
public class Client extends java.rmi.server.UnicastRemoteObject implements ClientInterface{

    private static final int PORT = 2019;
    private static String chavePublica;
    private static String chavePrivada;
    private static ClientInterface remote_client;
    //private static String Host = "DESKTOP-C38TKIF";
    private static String Host = "169.254.86.162";
    //private static String Host = "localhost";
    // Pascoal - DESKTOP-C38TKIF
    // Édi - Asus-Pc
    
    public Client() throws RemoteException {
        super();
    }
    
    @Override
    public byte[] signNonce(String nonce){
        try {
            return SignatureUtils.signString(nonce, StringUtils.getPrivateKeyFromString(chavePrivada));
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "".getBytes();
    }
    
    public void Print(String mensagem){
        System.out.println(mensagem);
    }

    public static int Registo(Server obj) {
        try {
            //Pedir o Nome e o ID ao Utilizador
            System.out.print("Nome: ");
            String sNome = Read.readString();
            System.out.print("Identificador: ");
            long lID = Read.readLong();

            //(Cliente  -> Servidor) Nome - Id
            //(Servidor -> Cliente ) Chaves pk, sk
            String chaves = "blank";
            chaves = obj.regist(sNome, lID);

            //Se utilizador já se encontra registado
            if (chaves.equals("")) {
                System.out.println("Utilizador já se encontra registado! Por favor, efetue o Login!");
                return 1;
            }

            //Efetuar registo
            String[] keys = chaves.split(",");
            String pk, sk;
            pk = keys[0];
            sk = keys[1];

            chavePublica = pk;
            chavePrivada = sk;

            //Gravar num ficheiro
            String fpk = "pk.txt";

            BufferedWriter bw = null;
            FileWriter fw = null;

            //Gravar pk
            fw = new FileWriter(fpk);
            bw = new BufferedWriter(fw);
            bw.write(pk);
            bw.close();
            fw.close();

            //Gravar sk
            byte[] iv = AES.generateIV();;
            byte[] key = DatatypeConverter.parseHexBinary(obj.requestKey());

            FileOutputStream fout = new FileOutputStream("iv_sk.txt");
            String ivHex = DatatypeConverter.printHexBinary(iv);
            fout.write(ivHex.getBytes());

            byte[] cripto = AES.encrypt(sk, key, iv);
            fout = new FileOutputStream("sk.txt");
            byte[] encoded = Base64.getEncoder().encode(cripto);
            fout.write(encoded);
            fout.close();
            
            remote_client = new Client();
            boolean valida = obj.login(chavePublica, remote_client);
            
            if (valida) {
                //Registado e evolui para menu 2
                return 2;
            } else {
                //Erro na assinutura do nonce
                System.out.println("Erro no registo! Por favor tente novamente!");
                return 3;
            }
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    public static String getSK(Server obj) {
        String pt = "";
        String outString = "";
        try {
            InputStream inputStream = new FileInputStream("sk.txt");
            Reader inputStreamReader = new InputStreamReader(inputStream, "ISO-8859-1");

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

        try {
            String ivString = "";
            FileReader filer = new FileReader("iv_sk.txt");
            char[] buff = new char[1];
            while (filer.read(buff) > 0) {
                ivString += buff[0];
            }
            filer.close();

            byte[] iv = DatatypeConverter.parseHexBinary(ivString);
            byte[] key = DatatypeConverter.parseHexBinary(obj.requestKey());

            byte[] decoded = Base64.getDecoder().decode(outString.getBytes("ISO-8859-1"));
            pt = AES.decrypt(decoded, key, iv);
            FileOutputStream out = new FileOutputStream("sk.txt");
            out.write(pt.getBytes("ISO-8859-1"));
            out.close();

            //Voltar a encriptar o ficheiro
            FileOutputStream fout = new FileOutputStream("iv_sk.txt");
            String ivHex = DatatypeConverter.printHexBinary(iv);
            fout.write(ivHex.getBytes());

            byte[] cripto = AES.encrypt(pt, key, iv);
            fout = new FileOutputStream("sk.txt");
            byte[] encoded = Base64.getEncoder().encode(cripto);
            fout.write(encoded);
            fout.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return pt;
    }

    public static int Login(Server obj) {
        try {
            //Ler a pk do ficheiro
            String fpk = "pk.txt";
            BufferedReader br = null;
            FileReader fr = null;
            String pk = null;
            try{
                fr = new FileReader(fpk);
                br = new BufferedReader(fr);
                pk = br.readLine();
            }
            catch(Exception e){
                System.out.println("Ainda não se encontra registado!");
                return 1;
            }
            br.close();
            fr.close();
            chavePublica = pk;

            //Ler o sk e decifrar
            String sk = getSK(obj);
            chavePrivada = sk;

            remote_client = new Client();
            boolean valida = obj.login(pk, remote_client);
            if (valida) {
                //Registado e evolui para menu 2
                return 2;
            } else {
                //Erro na assinutura do nonce
                System.out.println("Erro a efetuar o Login! Por favor tente novamente!");
                return 3;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static void menu2(Server obj) throws NoSuchProviderException, InvalidKeySpecException, Exception {
        Cliente cliente = null;
        
        try {
            //Iniciar conecção ao server e criação do cliente
            cliente = new Cliente(Host, obj);

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        String menu2 = "\n\n"
                     + "***********************************\n"
                     + "*              Menu               *\n"
                     + "*          1 - Votar              *\n"
                     + "*          2 - Status da Eleição  *\n"
                     + "*          0 - Sair               *\n"
                     + "***********************************\n"
                     + "Opção: ";
        int sair = 0;
        while (sair == 0) {
            System.out.print(menu2);
            Scanner in = new Scanner(System.in);
            int op = in.nextInt();
            switch (op) {
                case 1: {
                    //Selecionamos a eleição
                    String eleicoes = obj.sendElectionsList();
                    if (eleicoes.equals("")){
                        System.out.println("Não existem eleições de momento!");
                        break;
                    }
                    System.out.println("***********************************\n"
                                     + "*        Lista de Eleições        *\n"
                                     + "***********************************");
                    System.out.println(eleicoes);
                    
                    System.out.print("Selecionar eleição (ID): ");
                    int id_election = Read.readPositiveInt();
                    
                    remote_client = new Client();
                    ArrayList<Nonce> list_nonces = obj.sendVotesList(id_election,chavePublica, remote_client);
                    
                    if (list_nonces == null) {
                        System.out.println("Eleição não existe, já terminou ou as suas credenciais estão incorretas!");
                        break;
                    }
                    //Se vier vazia já votou
                    if(list_nonces.isEmpty()){
                        System.out.println("O seu voto já foi efetuado para a eleição selecionada! Por favor, aguarde pelo resultdo.");
                        break;
                    }               

                    System.out.println("***********************************\n"
                                     + "*      Lista de Candidatos        *\n"
                                     + "***********************************");
                    for (Nonce n : list_nonces) {
                        System.out.println("Nome: "+n.getNome()+" ID: "+n.getCandidate_id());
                    }
                    System.out.print("Votar no Candidato (ID): ");
                    int candidato = Read.readVotesInt();
                    
                    long flag = 0; //verifica se o id escolhido corresponde a um dos candidatos
                    for (Nonce n : list_nonces) {
                        if(n.getCandidate_id() == candidato){
                            flag = n.getNonce_id();
                        }
                    }
                    
                    if(flag!=0){
                        cliente.makeVote(chavePrivada, chavePublica, flag, id_election);
                    }
                    else{
                        cliente.makeVote(chavePrivada, chavePublica, 0, id_election);
                    }                  
                }
                break;
                case 2: {
                    String eleicoes = obj.sendAllElectionsList();
                    if (eleicoes.equals("")){
                        System.out.println("Não existem eleições de momento!");
                        break;
                    }
                    System.out.println("*********************************\n"
                                     + "Lista de Eleições\n"
                                     + "*********************************");
                    System.out.println(eleicoes);
                    System.out.print("Eleição (ID): ");
                    int id_eleicao = Read.readPositiveInt();
                    remote_client = new Client();
                    String output = obj.statusOfElection(id_eleicao, chavePublica, remote_client);
                    
                    //A eleição selecioanda não existe
                    if(output.equals("")){
                        System.out.println("A eleição indicada não existe, por favor tente novamente!");
                        break;
                    }
                    if(output==null){
                        System.out.println("As suas credenciais não são válidas!");
                    }
                    
                    System.out.println("****************************************\\");
                    System.out.print("Estado do voto: ");
                    System.out.println(output);
                }
                break;
                case 0: {
                    sair = 1;
                    System.exit(0);
                }
                break;
                default: {
                    System.out.println("Opção inválida tente novamente!");
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            System.setSecurityManager(new SecurityManager());
            Registry registry = LocateRegistry.getRegistry(Host, PORT, new RMISSLClientSocketFactory());
            Server obj = (Server) registry.lookup("Server");    
            
            String menu1 = "***********************************\n"
                         + "*             BitVote             *\n"
                         + "***********************************\n"
                         + "*                                 *\n"
                         + "*              Menu               *\n"
                         + "*          1 - Login              *\n"
                         + "*          2 - Registar           *\n"
                         + "*          0 - Sair               *\n"
                         + "*                                 *\n"
                         + "***********************************\n"
                         + "Opção: ";

            int sair = 0;
            while (sair == 0) {
                System.out.print(menu1);
                Scanner in = new Scanner(System.in);
                int op = in.nextInt();
                switch (op) {
                    case 1: {
                        int res = Login(obj);
                        if (res == 2) {
                            //Registado - Passar ao menu 2
                            System.out.println("Login efetuado com sucesso!");
                            menu2(obj);
                            sair = 1;
                        }
                    }
                    break;
                    case 2: {
                        int res = Registo(obj);
                        if (res == 2) {
                            //Registado - Passar ao menu 2
                            System.out.println("Registo efetuado com sucesso!");
                            menu2(obj);
                            sair = 1;
                        }
                    }
                    break;
                    case 0: {
                        sair = 1;
                        System.exit(0);
                    }
                    break;
                    default: {
                        System.out.println("Opção inválida tente novamente!");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro na main do cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
