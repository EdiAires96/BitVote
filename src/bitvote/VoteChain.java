/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitvote;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;



public class VoteChain implements Serializable{

	public  ArrayList<Block> blockchain;
	public  int difficulty;
	public  Vote genesisVote;
	public VotersWallet voteBase;

	public VoteChain() throws Exception
	{
		blockchain = new ArrayList<Block>();
		difficulty = 5;
		voteBase = new VotersWallet();
		
		//create genesis Vote, which sends 1 vote to all wallets:	
		genesisVote = new Vote(voteBase.publicKey, 0000000000, 0, 0);
		genesisVote.generateSignature(voteBase.privateKey);
		genesisVote.voteId = "0";	
	
		//Create and minning genesis block
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addVote(genesisVote);
		addBlock(genesis);
				
	}
	
	public ArrayList<Block> getBlockchain() {
		return blockchain;
	}
	

	public void setBlockchain(ArrayList<Block> blockchain) {
		this.blockchain = blockchain;
	}
	
	
	
    public  Boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        
        for(Block item : blockchain){
            if(item.getPreviousHash().equals("0")){
                continue;
            }
            
            currentBlock = item;
            previousBlock = blockchain.get(blockchain.indexOf(item)-1);
			
	if (!(currentBlock.getMerkleRoot().equals(StringUtils.getMerkleRoot(currentBlock.getData())))) {
		System.out.println("Current MerkeRoot not equal (list of votes not valid)");
		return false;
	}
            if(!currentBlock.getHash().equals(currentBlock.calculateHash())){
                System.out.println("Current Hash not equal");
                return false;
            }
            
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash())){
                System.out.println("Current Hash not equal");
                return false;
            }
        }
        return true;
    }
	
	public  void addBlock(Block newBlock) 
	{
		newBlock.mineBlock(difficulty);
		blockchain.add(newBlock);
	}
	
	public  Boolean blockchainHadVoted(PublicKey pk, int id_eleicao) 
	{
		//verificação nos blocos minados
		for (Block item : blockchain) {
			for (Vote v : item.getData()) {
				if (v.sender.equals(pk) && v.id_eleicao==id_eleicao) {
					return true;
				}
			}
		}
		return false;
	}
	
	public  Boolean nonMinedBlockhadVoted(PublicKey pk, ArrayList<Vote> votes, int id_eleicao)
	{
            //verificação nos votos que nao foram minados
            if(votes.isEmpty())
                return false;
            for (Vote vote : votes) {
                if(vote.sender.equals(pk) && vote.id_eleicao==id_eleicao)
                    return true;
            }
		return false;
	}
        
	public ArrayList<Candidato> contaVotos (int eleicao_id, int candidates, ArrayList<Nonce> nonces)
	{
            ArrayList<Candidato> totalVotos = new ArrayList<Candidato>();
            totalVotos.add(new Candidato(0, 0,""));
            
            for (Block item : blockchain) {
                    for (Vote v : item.getData()) {
                        if(v.id_eleicao == eleicao_id){
                            if(v.candidateNonce == 0){
                                totalVotos.get(0).setTotal(v.numberVotes);
                            }
                            else{
                                for (Nonce nonce : nonces) {
                                    Boolean exist = false;
                                    List <Candidato> cl = totalVotos.stream().filter(s -> nonce.getCandidate_id()== s.getId()).collect(Collectors.toList());
                                    if(cl.isEmpty()){
                                        totalVotos.add(new Candidato(nonce.getCandidate_id(),0,nonce.getNome()));
                                    }
                                    //verificar se existe algum que nao preencha este caso e adicionar ao candidato 0 
                                    if(v.candidateNonce == nonce.getNonce_id() && v.sender.equals(nonce.getPk()) && nonce.getEleicao_id() == eleicao_id){
                                        int flag = 0;
                                        for (Candidato c : totalVotos ) {
                                            if(c.getId()==nonce.getCandidate_id()){
                                                c.setTotal(v.numberVotes);
                                                flag = 1;
                                            }
                                        }
                                    }      
                                }
                            }
                        }
                        else{
                            continue;
                        }
                    }
        }
            
            
            
            return totalVotos;
            
        }
	public int verificaVotoDoCandidato(ArrayList<Nonce> array) {
                         if(array.isEmpty()){
                             return -1;
                         }                         
                         
		PublicKey pk = array.get(0).getPk();
		int eleicao = array.get(0).getEleicao_id();
		for (Block item : blockchain) {
			for (Vote v : item.getData()) {

				if (v.sender.equals(pk) && v.id_eleicao == eleicao) {

					if (v.candidateNonce == 0) {
						return 0;
					}
					for (Nonce n : array) {

						if (n.getNonce_id() == v.candidateNonce) {
							return n.getCandidate_id();
						}
					}
				}
			}
		}
		return -1;
	}

		
	public  Block lastBlock()
	{
		return blockchain.get(blockchain.size()-1);	
	}
		
		
	/*
	public static void main(String[] args) throws Exception 
	{
            
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		//Create wallets
		VotersWallet walletA = new VotersWallet(1);
		VotersWallet walletB= new VotersWallet(1);
		VotersWallet walletC = new VotersWallet(1);
		VotersWallet walletD= new VotersWallet(1);
		VotersWallet voteBase = new VotersWallet(1);
                
                ArrayList<Nonce> n = new ArrayList<Nonce>();
                
		
		//create genesis Vote, which sends 1 vote to all wallets:
		genesisVote = new Vote(voteBase.publicKey, 000000, 1, 0);
		genesisVote.generateSignature(voteBase.privateKey);
		genesisVote.voteId = "0";
		
		//Create and minning genesis block
		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addVote(genesisVote);
		addBlock(genesis);
		*/
		
		/*	
		//testes
		long candidateX = StringUtils.generateNonce();
		System.out.println(candidateX);
		long candidateY= StringUtils.generateNonce();
		System.out.println(candidateY);
		//System.out.println(StringUtils.getStringFromKey(walletA.publicKey));
                
                ArrayList <Vote> nonMinedVotes = new ArrayList<Vote>();

		Block block1 = new Block(genesis.getHash());
		System.out.println("\nWalletA is Attempting to vote on candidate X...");
                
                if(blockchainHadVoted(walletA.publicKey,1) || nonMinedBlockhadVoted(walletA.publicKey, nonMinedVotes,1)){
			System.out.println("TRUE");
			 System.out.println("votante A  ja votou");
		}
		else{
			System.out.println("Pode Votar");
			block1.addVote(walletA.sendVote(candidateX,walletA.n_votes,1 ));
                        nonMinedVotes.add(walletA.sendVote(candidateX,walletA.n_votes,1 ));
                        n.add(new Nonce(candidateX, walletA.publicKey, 0,1));
		}
                
                System.out.println("\nWalletA is Attempting to vote on candidate AGAIN...");
                
                if(blockchainHadVoted(walletA.publicKey,1) || nonMinedBlockhadVoted(walletA.publicKey, nonMinedVotes,1)){
			System.out.println("TRUE");
			 System.out.println("votante A  ja votou");
		}
		else{
			System.out.println("Pode Votar");
			block1.addVote(walletA.sendVote(candidateX,walletA.n_votes,1 ));
                        nonMinedVotes.add(walletA.sendVote(candidateX,walletA.n_votes,1 ));
		}
                
                System.out.println("\nWalletB is Attempting to vote on candidate X...");
		 if(blockchainHadVoted(walletB.publicKey,2) || nonMinedBlockhadVoted(walletB.publicKey, nonMinedVotes,2)){
			 System.out.println("TRUE");
			 System.out.println("votante B ja votou");
		}
		else{
			 System.out.println("Pode Votar");
			 block1.addVote(walletB.sendVote(candidateX,walletB.n_votes,2 ));
                         nonMinedVotes.add(walletB.sendVote(candidateX,walletB.n_votes,2 ));
                         n.add(new Nonce(candidateX, walletB.publicKey, 0,2));
		 }
                 
                 addBlock(block1);
                 nonMinedVotes.clear();
		
		Block block2 = new Block(block1.getHash());
                System.out.println("\nWalletC is Attempting to vote on candidate Y...");
                if(blockchainHadVoted(walletC.publicKey,2) || nonMinedBlockhadVoted(walletC.publicKey, nonMinedVotes,2)){
			System.out.println("TRUE");
			System.out.println("Votante C ja votou");
		}
		else{
			System.out.println("Pode Votar");
			block2.addVote(walletC.sendVote(candidateY,walletC.n_votes, 2));
                        nonMinedVotes.add(walletC.sendVote(candidateY,walletC.n_votes, 2));
                        n.add(new Nonce(candidateY, walletC.publicKey, 1, 2));
		}
                System.out.println("\nWalletC is Attempting to vote on candidate Y AGAIN");
                if(blockchainHadVoted(walletC.publicKey,1) || nonMinedBlockhadVoted(walletC.publicKey, nonMinedVotes,1)){
			System.out.println("TRUE");
			System.out.println("Votante C ja votou");
		}
		else{
			System.out.println("Pode Votar");
			block2.addVote(walletC.sendVote(candidateY,walletC.n_votes, 1));
                        nonMinedVotes.add(walletC.sendVote(candidateY,walletC.n_votes, 1));
                        n.add(new Nonce(candidateY, walletC.publicKey, 1,1));
		}
                
                System.out.println("\nWalletD is Attempting to vote on candidate Y...");
		if(blockchainHadVoted(walletD.publicKey,2) || nonMinedBlockhadVoted(walletD.publicKey, nonMinedVotes,2)){
			System.out.println("TRUE");
			System.out.println("Votante D ja votou");
		}
		else{
			System.out.println("Pode Votar");
			block2.addVote(walletD.sendVote(candidateY,walletD.n_votes, 2 ));
                        nonMinedVotes.add(walletD.sendVote(candidateY,walletD.n_votes, 2 ));
                        n.add(new Nonce(candidateY, walletD.publicKey, 1,2));
		 }
                addBlock(block2);
                nonMinedVotes.clear();
		
		System.out.println("\n");
		System.out.println("\n");
		//block1.ImprimeBlock(1);
		//block2.ImprimeBlock(2);
		
		System.out.println(isChainValid());
                
                //int[] x = contaVotos(1,2,n);
                ArrayList<Candidato> x = contaVotos(2,2,n);
                System.out.println(""+x.toString());
                
		Block lastBlock = lastBlock();
		lastBlock.ImprimeBlock(99);
		
				
		
	}
	 */
}
