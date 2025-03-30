package com.escola.model;

import java.util.Date;

public class Avaliacao {
    private int id;
    private int alunoId;
    private int satisfacao;
    private int satisfacaoInfra;
    private String comentario;
    private Date dataAvaliacao;

    // Getters e Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getAlunoId() {
        return alunoId;
    }
    public void setAlunoId(int alunoId) {
        this.alunoId = alunoId;
    }
    public int getSatisfacao() {
        return satisfacao;
    }
    public void setSatisfacao(int satisfacao) {
        this.satisfacao = satisfacao;
    }
    public int getSatisfacaoInfra() {
        return satisfacaoInfra;
    }
    public void setSatisfacaoInfra(int satisfacaoInfra) {
        this.satisfacaoInfra = satisfacaoInfra;
    }
    public String getComentario() {
        return comentario;
    }
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    public Date getDataAvaliacao() {
        return dataAvaliacao;
    }
    public void setDataAvaliacao(Date dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }
}