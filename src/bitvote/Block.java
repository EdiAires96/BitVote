/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitvote;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;


public class Block implements Serializable
{
	private String hash;
	private String previousHash;
	private ArrayList<Vote> votes = new ArrayList<Vote>(); 
	private String merkleRoot;
	private long timeStamp;
	private long nonce; //number for proof of work
    
    public Block(String previousHash){
        
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }
    
    public String calculateHash() {
        String calculatedHash = HashUtils.hashFuncSHA256(previousHash+Long.toString(timeStamp)+Long.toString(nonce)+merkleRoot);
        return calculatedHash;
    } 

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public ArrayList<Vote> getData() {
        return votes;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getNonce() {
        return nonce;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
	
	public String getMerkleRoot() 
	{
		return merkleRoot;
	}

	public void setMerkleRoot(String merkleRoot) {
		this.merkleRoot = merkleRoot;
	}

/*
    public void setData(String data) {
        this.data = data;
    }
*/
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    @Override
    public String toString() {
        return "Block{" + "hash=" + hash + ", previousHash=" + previousHash + ", votes=" + votes.toString() + ", timeStamp=" + timeStamp + ", nonce=" + nonce + '}';
    }

	public static String convertDate(long t)
	{
		Timestamp time = new Timestamp(t);
		return time.toString();

	}
	
	public void ImprimeBlock(int blockid) 
	{
		String s = "----->> Block ID = " + blockid + "<----- \n";
		s = s + "Hash: " + hash + "\n";
		s = s + " PreviousHash: " + previousHash+ "\n";
		s = s + "Nonce: " + nonce+ "\n";
		s = s + "Timestamp: " + convertDate(timeStamp)+ "\n";
	
		s = s + "\n----------------------Votos---------------------------\n";
		for (Vote v : votes) {
			s = s + "Candidato: " + v.candidateNonce + "\nVotante: " + StringUtils.getStringFromKey(v.sender) + "\nNúmero de votos: " + v.numberVotes + "\nAssinatura: "
					+ StringUtils.getStringFromSignature(v.signature) + "\nVoteID: " + v.voteId + "\n";
			s = s + "------------------------------------------------------\n";
		}
		System.out.println(s);
	}
	
	
    
    public void mineBlock(int difficulty){
	merkleRoot  = StringUtils.getMerkleRoot(votes);
        nonce = 0;
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(true){
            hash = calculateHash();
            if(hash.substring(0,difficulty).equals(target))
                break;
            nonce++;
        }
        System.out.println("Block Mined!!! : " + hash);
    }
	
public boolean addVote(Vote vote) throws Exception
{
	//process vote and check if valid, unless block is genesis block then ignore.
        if(vote == null)
            return false;
        if( !( "0".equals( previousHash) ) ) 
	{
		if( (vote.processVote() != true) )
		{
			System.out.println("Vote Failed to process. Discarded.");
			return false;
		}
        }
        votes.add(vote);
        System.out.println("Vote adicionado ao bloco");
        return true;
    }
    
}
