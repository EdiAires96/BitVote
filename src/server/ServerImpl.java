/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import bitvote.Candidato;
import bitvote.Nonce;
import bitvote.SignatureUtils;
import bitvote.StringUtils;
import bitvote.VoteChain;
import bitvote.VotersWallet;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import p2p.Servidor;

/**
 *
 * @author jferr
 */
public class ServerImpl extends UnicastRemoteObject implements Server {

    private static final int PORT = 2019;
    private static VoteChain objBlockChain;
    private SQLiteBD bd;

    public ServerImpl() throws Exception {
        super(PORT, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());
    }

    @Override
    public String sayHello() {
        return "Hello World!";
    }

    //0 se a chave publica não é válida
    // nonce é gerado e retornado
    private synchronized long criticalOperation(String pubkey) {

        long nonce = StringUtils.generateNonce();

        //verifica se a chave publica existe na bd
        bd = new SQLiteBD();
        String query = "SELECT * FROM User WHERE pubkey=?;";
        PreparedStatement prstmt = bd.returnPrStmt(query);
        ResultSet res = null;
        try {
            prstmt.setString(1, pubkey);
            res = prstmt.executeQuery();

            //se a chave esta na base de dados 
            if (res.next()) {
                bd.closeBD();
                return nonce;
            } //senao existe returnamos 0
            else {
                bd.closeBD();
                return 0;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }
        bd.closeBD();
        return 0;
    }

    @Override
    //Registo dos votantes
    public synchronized String regist(String name, long idNumber) {

        String keys = "";

        //Se já está registado retorna ""
        bd = new SQLiteBD();
        String query = "SELECT * FROM User WHERE idnumber=?;";
        PreparedStatement prstmt = bd.returnPrStmt(query);
        ResultSet res = null;
        try {
            prstmt.setLong(1, idNumber);
            res = prstmt.executeQuery();
            if (res.next()) {
                bd.closeBD();
                return keys;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        VotersWallet wallet = new VotersWallet(); //Criar uma carteira
        keys = StringUtils.getStringFromKey(wallet.publicKey) + "," + StringUtils.getStringFromKey(wallet.privateKey); // Retorna um par de chaves ao votante

        //bd = new SQLiteBD(); // Insere a chave publica do votante e o seu nome na bd       
        String insert = "INSERT INTO User(pubkey,name,idnumber) VALUES(?,?,?);";
        prstmt = bd.returnPrStmt(insert);
        try {
            prstmt.setString(1, StringUtils.getStringFromKey(wallet.publicKey));
            prstmt.setString(2, name);
            prstmt.setLong(3, idNumber);
            prstmt.executeUpdate();
            bd.closeBD(); //fecha bd e encripta
        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        return keys;
    }

    /*@Override
    public String loginStepOne(String pubkey) {
        String stringNonce = "";
        publickey = pubkey;

        //verifica se a chave publica existe na bd
        SQLiteBD bd = new SQLiteBD();
        String query = "SELECT * FROM User WHERE pubkey=?;";
        PreparedStatement prstmt = bd.returnPrStmt(query);
        ResultSet res = null;
        try {
            prstmt.setString(1, pubkey);
            res = prstmt.executeQuery();
            //senao existe retorna ""
            if (!res.next()) {
                bd.closeBD();
                return stringNonce;
            }
            bd.closeBD();

        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        //gera nonce
        nonce = StringUtils.generateNonce();
        stringNonce = String.valueOf(nonce);

        return stringNonce;
    }*/
    
    @Override
    public synchronized Boolean login(String pubkey, ClientInterface client) {

        PublicKey pk = null;

        long nonce = criticalOperation(pubkey);
        if (nonce == 0) {
            return false;
        }

        try {
            pk = StringUtils.getPublicKeyFromString(pubkey);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        byte[] bNonce = "".getBytes();
        try {
            bNonce = client.signNonce(String.valueOf(nonce));
            System.out.println("Passou no cliente e remoto");
        } catch (RemoteException ex) {
            System.out.println("Login RemoteExcption on Client");
        }

        boolean isLoged = false;
        try {
            // verifica o nonce assinado
            isLoged = SignatureUtils.verifyString(String.valueOf(nonce), bNonce, pk);
        } catch (Exception ex1) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex1);
        }

        return isLoged;

    }

    @Override
    public String requestKey() {
        return "af1129c47f20f6394915309852624a8b9202abdeb6b696e540b41b5b4e9442b8";
    }

    @Override
    public synchronized String sendElectionsList() {

        String electionsList = "";
        bd = new SQLiteBD();
        Statement stmt = bd.returnStmt();
        ResultSet res;
        try {
            res = stmt.executeQuery("SELECT * FROM Election WHERE status=1;");
            while (res.next()) {
                electionsList = electionsList + "***********************************\n" 
                                              + "Nome: " + res.getString(2) + "\n"
                                              + "ID: " + res.getInt(1) + "\n"
                                              + "***********************************\n";
            }
            bd.closeBD();
        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        return electionsList;
    }

    @Override
    public synchronized ArrayList<Nonce> sendVotesList(int electionid, String pubkey, ClientInterface client) {

        //operação critica o utiliazador tera de provar que possui a chave privada para a chave publica dada
        //caso não se verifique retorna null
        PublicKey pk = null;
        long nonce = criticalOperation(pubkey);
        if (nonce == 0) {
            return null;
        }

        try {
            pk = StringUtils.getPublicKeyFromString(pubkey);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        byte[] bNonce = "".getBytes();
        try {
            bNonce = client.signNonce(String.valueOf(nonce));
        } catch (RemoteException ex) {
            System.out.println("sendVoteList RemoteExcption on Client");
        }

        try {
            // verifica o nonce assinado
            if (!SignatureUtils.verifyString(String.valueOf(nonce), bNonce, pk)) {
                return null;
            }
        } catch (Exception ex1) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex1);
        }
        ///////////////////////////////////////////////

        ArrayList<Nonce> votesList = new ArrayList<>();
        long lnonce;
        
        //gera um nonce para cada candidato
        bd = new SQLiteBD();
        ResultSet res;
        ResultSet res2;
        Statement stmt = bd.returnStmt();

        PreparedStatement prstmt = bd.returnPrStmt("SELECT * FROM Election WHERE id=? AND status=1;");
        try {
            prstmt.setInt(1, electionid);
            res = prstmt.executeQuery();
            if (!res.next()) {
                bd.closeBD();
                return null;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        prstmt = bd.returnPrStmt("SELECT * FROM Result WHERE electionid=?;");

        try {
            prstmt.setInt(1, electionid);
            res = prstmt.executeQuery();
            while (res.next()) {

                lnonce = StringUtils.generateNonce();
                res2 = stmt.executeQuery("SELECT * FROM Candidate WHERE id=" + res.getInt(1));

                if (objBlockChain.blockchainHadVoted(StringUtils.getPublicKeyFromString(pubkey), electionid)) {
                    bd.closeBD();
                    return votesList;
                }

                if (res2.next()) {
                    votesList.add(new Nonce(lnonce, res2.getInt(1), res2.getString(2)));

                }
                stmt.close();
                stmt = bd.returnStmt();
                stmt.executeUpdate("Insert Into Nonce(id,candidateid,pubkey) VALUES(" + lnonce + "," + res.getInt(1) + ",'" + pubkey + "');");
            }
            bd.closeBD();

        } catch (Exception ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        return votesList;
    }

    @Override
    public synchronized String sendAllElectionsList() {

        String electionsList = "";
        bd = new SQLiteBD();
        Statement stmt = bd.returnStmt();
        ResultSet res;
        try {
            res = stmt.executeQuery("SELECT * FROM Election;");
            while (res.next()) {
                electionsList = electionsList + "***********************************\n" 
                                              + "Nome: " + res.getString(2) + "\n"
                                              + "ID: " + res.getInt(1) + "\n"
                                              + "***********************************\n";
            }
            bd.closeBD();
        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        return electionsList;
    }

    public ArrayList<Candidato> winerCount(ArrayList<Candidato> candidates) {
        ArrayList<Candidato> winner = new ArrayList<>();
        int maior = candidates.get(0).getTotal();
        winner.add(candidates.get(0));
        for (int i = 1; i < candidates.size(); i++) {
            if (candidates.get(i).getTotal() == maior) {
                winner.add(candidates.get(i));
            } else if (candidates.get(i).getTotal() > maior) {
                winner.clear();
                winner.add(candidates.get(i));
                maior = candidates.get(i).getTotal();
            }
        }
        return winner;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public synchronized String statusOfElection(int electionid, String pubkey, ClientInterface client) {

        //operação critica o utiliazador tera de provar que possui a chave privada para a chave publica dada
        //caso não se verifique retorna null
        PublicKey pk = null;
        long nonce = criticalOperation(pubkey);
        if (nonce == 0) {
            return null;
        }

        try {
            pk = StringUtils.getPublicKeyFromString(pubkey);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        byte[] bNonce = "".getBytes();
        try {
            bNonce = client.signNonce(String.valueOf(nonce));
        } catch (RemoteException ex) {
            System.out.println("sendVoteList RemoteExcption on Client");
        }

        try {
            // verifica o nonce assinado
            if (!SignatureUtils.verifyString(String.valueOf(nonce), bNonce, pk)) {
                return null;
            }
        } catch (Exception ex1) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex1);
        }
        ///////////////////////////////////////////////

        String sRes = "";
        ResultSet res = null, res2;
        ArrayList<Nonce> nonceList = new ArrayList<>();

        //retorna o voto do votante
        //caso não tenha votado retorna ""
        bd = new SQLiteBD();
        PreparedStatement prstmt = bd.returnPrStmt("SELECT * FROM Result WHERE electionid=?"); 
        Statement stmt;
        int i = 0;
        try {
            prstmt.setInt(1, electionid);
            res = prstmt.executeQuery();
            while (res.next()) {

                stmt = bd.returnStmt();
                res2 = stmt.executeQuery("SELECT * FROM Nonce WHERE candidateid=" + res.getInt(1) + " AND pubkey='" + pubkey + "';");  //////////// And pubkey se res2
                i++;
                if (res2.next()) {

                    nonceList.add(new Nonce(res2.getLong(1), pk, res2.getInt(2), electionid, res.getString(2)));
                }

            }
            bd.closeBD();
            if(i==0){
                return sRes;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        int candidateid = objBlockChain.verificaVotoDoCandidato(nonceList);

        if (candidateid == 0) {
            sRes = "O seu voto nulo, foi aprovado!\n";
            
        } else if (candidateid == -1) {
            sRes = "Status: Não Votou, ou o seu voto ainda não foi aprovado!\n";
        }

        bd = new SQLiteBD();
        stmt = bd.returnStmt();
        try {
            res = stmt.executeQuery("SELECT name FROM Candidate WHERE id=" + candidateid + ";");
            if (res.next()) {
                sRes = "Status: O seu voto no Candidato " + res.getString(1) + ", foi aprovado!\n";
            }
            bd.closeBD();
        } catch (SQLException e) {
            System.out.println("statusofElection on Query Candidate for name");
            bd.closeBD();
        }

        // se as Eleições terminaram então retornar também os seu resultados
        ArrayList<Candidato> candidates = new ArrayList<>();
        bd = new SQLiteBD();
        prstmt = bd.returnPrStmt("SELECT * FROM Election WHERE id=?;");
        try {
            prstmt.setInt(1, electionid);
            res = prstmt.executeQuery();
            if (res.next()) {
                if (res.getInt(4) == 1) {
                    bd.closeBD();
                    return sRes;
                } else {
                    sRes = sRes + "***********************************\nResultado das eleições\n***********************************\nVotos Nulos ->" + res.getInt(3) + "\n";
                    prstmt = bd.returnPrStmt("SELECT * FROM Result WHERE electionid=?;");
                    prstmt.setInt(1, electionid);
                    res = prstmt.executeQuery();
                    while (res.next()) {
                        stmt = bd.returnStmt();
                        res2 = stmt.executeQuery("SELECT name FROM Candidate WHERE id=" + res.getInt(1) + ";");
                        if (res2.next()) {
                            sRes = sRes + res2.getString(1) + " ->" + res.getInt(3) + "\n";
                            candidates.add(new Candidato(res.getInt(1), res.getInt(3), res2.getString(1)));
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("statusofElection on Query Election");
            bd.closeBD();
        }
        bd.closeBD();

        if (!candidates.isEmpty()) {

            ArrayList<Candidato> winer = winerCount(candidates);

            if (winer.size() > 1) {
                sRes = sRes + "\n***********************************\nEmpate\n***********************************\n";
                for (Candidato c : winer) {
                    sRes = sRes + c.getName() + "\n";
                }
            } else {
                sRes = sRes + "\n***********************************\nVencedor\n***********************************\n" + winer.get(0).getName();
            }
        }

        return sRes;
    }

    private static void startElections() {

        SQLiteBD bd = new SQLiteBD();
        Statement stmt = null;
        ResultSet res;
        int electionid = -1;

        String insert = "INSERT INTO Election(name,nullvotes,status) VALUES(?,0,1);";

        System.out.println("\n\n***********************************\n"
                + "Nova Eleição\n"
                + "***********************************");
        System.out.print("Nome das eleições: ");
        String name = Read.readString();

        PreparedStatement prstmt = bd.returnPrStmt(insert);

        try {
            prstmt.setString(1, name);
            prstmt.executeUpdate();

            // obtem o id da eleição
            stmt = bd.returnStmt();
            res = stmt.executeQuery("SELECT * FROM Election ORDER BY id DESC LIMIT 1;");

            if (res.next()) {
                electionid = res.getInt(1);
            }
            bd.closeBD();

        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        System.out.print("Número de candidatos: ");
        int n = Read.readCandidatesNumber();

        bd = new SQLiteBD();
        for (int i = 0; i < n; i++) {

            System.out.print("Nome do Candidato: ");

            name = Read.readString();
            insert = "INSERT INTO Candidate(name) VALUES(?);";
            prstmt = bd.returnPrStmt(insert);
            try {
                prstmt.setString(1, name);
                prstmt.executeUpdate();

                // obtem id do Candidato inserido
                stmt = bd.returnStmt();
                res = stmt.executeQuery("SELECT * FROM Candidate ORDER BY id DESC LIMIT 1;");
                int candidateid = -1;
                if (res.next()) {
                    candidateid = res.getInt(1);
                }

                stmt.close();

                //inserção na tabela de Resultados para mais tarde registar o nº de votos
                insert = "INSERT INTO Result(candidateid,electionid,votesnumber) VALUES(?,?,?);";
                prstmt = bd.returnPrStmt(insert);
                prstmt.setInt(1, candidateid);
                prstmt.setInt(2, electionid);
                prstmt.setInt(3, 0);
                prstmt.executeUpdate();
            } catch (SQLException ex) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                bd.closeBD();
            }

        }

        bd.closeBD();
    }

    private static void endElection() {

       SQLiteBD bd = new SQLiteBD();
        PreparedStatement prstmt = null;

        Statement stmt = bd.returnStmt();
        ResultSet res, res2;
        ArrayList<Nonce> nonceList = new ArrayList<Nonce>();
        ArrayList<Integer> ids = new ArrayList<>();
        PublicKey pk = null;

        System.out.println("\n\n***********************************\n"
                + "Eleições a Decorrer\n"
                + "***********************************");
        int i = 0;
        try {
            res = stmt.executeQuery("SELECT * FROM Election WHERE status=1;");
            while (res.next()) {
                ids.add(res.getInt(1));
                System.out.println("***********************************");
                System.out.println("Nome: " + res.getString(2));
                System.out.println("ID: " + res.getInt(1));
                System.out.println("***********************************");
                i++;
            }
            bd.closeBD();
        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (i == 0) {
            return;
        }

        System.out.print("ID das Eleições que pretende terminar: ");
        int electionid = 0;
        while (true) {
            electionid = Read.readPositiveInt();
            if (ids.contains(electionid)) {
                break;
            }
        }

        bd = new SQLiteBD();
        stmt = bd.returnStmt();
        try {
            stmt.executeUpdate("UPDATE Election SET status=0 WHERE id=" + electionid + ";");
            bd.closeBD();
        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        //contar os votos
        bd = new SQLiteBD();
        prstmt = bd.returnPrStmt("SELECT * FROM Result WHERE electionid=?;");
        try {
            prstmt.setInt(1, electionid);
            res = prstmt.executeQuery();

            i = 0;
            while (res.next()) {

                stmt = bd.returnStmt();
                res2 = stmt.executeQuery("SELECT * FROM Nonce WHERE candidateid=" + res.getInt(1) + ";");
                i++;
                while (res2.next()) {
                    try {
                        pk = StringUtils.getPublicKeyFromString(res2.getString(3));
                    } catch (Exception ex) {
                        Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                        bd.closeBD();
                    }
                    nonceList.add(new Nonce(res2.getLong(1), pk, res2.getInt(2), electionid, ""));
                }
            }
            bd.closeBD();

        } catch (SQLException ex) {
            Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            bd.closeBD();
        }

        ArrayList<Candidato> candidatesList = objBlockChain.contaVotos(electionid, i + 1, nonceList);

        //inserir os resultados na bd
        bd = new SQLiteBD();
        for (Candidato c : candidatesList) {

            //votos nulos
            if (c.getId() == 0) {
                prstmt = bd.returnPrStmt("UPDATE Election SET nullvotes=? WHERE id=?;");
                try {
                    prstmt.setInt(1, c.getTotal());
                    prstmt.setInt(2, electionid);
                    prstmt.executeUpdate();
                    prstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                    bd.closeBD();
                }
            }

            //votos d0 candidato c
            prstmt = bd.returnPrStmt("UPDATE Result SET votesnumber=? WHERE candidateid=?;");
            try {
                prstmt.setInt(1, c.getTotal());
                prstmt.setInt(2, c.getId());
                prstmt.executeUpdate();
                prstmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                bd.closeBD();
            }
        }
        bd.closeBD();

    }

    /*
    private static VoteChain createBlockchain() throws Exception {
        VoteChain votechain = new VoteChain();
        //Create and minning genesis block
        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addVote(votechain.genesisVote);
        votechain.addBlock(genesis);

        return votechain;
    }
     */
    @Override
    public VoteChain getBlockChain() {
        return objBlockChain;
    }

    @Override
    public void atualizaBlockChain(VoteChain VC) {
        objBlockChain = VC;

    }

    public static void main(String[] args) throws IOException {

        // Create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            // Create SSL-based registry
            Registry registry = LocateRegistry.createRegistry(PORT, new RMISSLClientSocketFactory(), new RMISSLServerSocketFactory());

            ServerImpl obj = new ServerImpl();

            // Bind this object instance to the name "HelloServer"
            registry.rebind("Server", obj);

            System.out.println("Server bound in registry");
        } catch (Exception e) {
            System.out.println("ServerImpl err: " + e.getMessage());
            e.printStackTrace();
        }

        //Criar bd se não existir
        File fBD = new File("BitVote.db");
        if (!fBD.exists()) {
           SQLiteBD bd = new SQLiteBD();
            bd.createBD();
        }

        try {
            objBlockChain = new VoteChain();
        } catch (Exception ex) {
            //Logger.getLogger(ServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro ao criar a blockchain");
        }
        System.out.println(objBlockChain.lastBlock().getHash());

        new Thread(()
                -> {
            Servidor servidor = new Servidor();
        }
        ).start();

        //Menu para implementar os requisitos do servidor
        new Thread(()
                -> {
            int sair = 0;

            String menu = "\n\n"
                    + "***********************************\n"
                    + "*             BitVote             *\n"
                    + "***********************************\n"
                    + "*             Servidor            *\n"
                    + "*                                 *\n"
                    + "*              Menu               *\n"
                    + "*          1 - Iniciar Eleições   *\n"
                    + "*          2 - Terminar Eleições  *\n"
                    + "*          0 - Sair               *\n"
                    + "*                                 *\n"
                    + "***********************************\n"
                    + "Opção: ";

            while (sair == 0) {
                System.out.print(menu);

                Scanner in = new Scanner(System.in);
                int op = in.nextInt();

                switch (op) {
                    case 1:
                        startElections();
                        break;
                    case 2:
                        endElection();
                        break;
                    case 0:
                        sair = 1;
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opção errada");
                }
            }
        }
        ).start();
    }
}
