/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bitvote;

import java.io.Serializable;
import java.security.PublicKey;

/**
 *
 * @author paulo
 */
public class Nonce implements Serializable{
    private long nonce_id;
    private PublicKey pk;
    private int candidate_id;
    private String nome;
    private int eleicao_id;

    public Nonce(long nonce_id, PublicKey pk, int candidate_id, int eleicao_id, String nome) {
        this.nonce_id = nonce_id;
        this.pk = pk;
        this.candidate_id = candidate_id;
        this.eleicao_id = eleicao_id;
    }
    public Nonce(long nonce_id, int candidate_id, String nome){
        this.nonce_id = nonce_id;
        this.candidate_id = candidate_id;
        this.nome=nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getEleicao_id() {
        return eleicao_id;
    }

    public void setEleicao_id(int eleicao_id) {
        this.eleicao_id = eleicao_id;
    }

    public long getNonce_id() {
        return nonce_id;
    }

    public PublicKey getPk() {
        return pk;
    }

    public int getCandidate_id() {
        return candidate_id;
    }

    public void setNonce_id(long nonce_id) {
        this.nonce_id = nonce_id;
    }

    public void setPk(PublicKey pk) {
        this.pk = pk;
    }

    public void setCandidate_id(int candidate_id) {
        this.candidate_id = candidate_id;
    }

    @Override
    public String toString() {
        return "Nonce{" + "nonce_id=" + nonce_id + ", pk=" + pk + ", candidate_id=" + candidate_id + '}';
    }
    
    
    
}
