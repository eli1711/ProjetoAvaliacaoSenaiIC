package br.com.cpa.questionario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instituicao")
public class Instituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "identificador_institucional", nullable = false, unique = true)
    private String identificadorInstitucional;

    private String cnpj;

    @Column(columnDefinition = "TEXT")
    private String endereco;

    @Column(columnDefinition = "TEXT")
    private String contatos;

    private String responsavelInstitucional;

    private boolean ativo = true;

    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String configuracoes;

    private String periodoLetivoAtual;

    @Column(columnDefinition = "TEXT")
    private String dadosRelatorio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIdentificadorInstitucional() {
        return identificadorInstitucional;
    }

    public void setIdentificadorInstitucional(String identificadorInstitucional) {
        this.identificadorInstitucional = identificadorInstitucional;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getContatos() {
        return contatos;
    }

    public void setContatos(String contatos) {
        this.contatos = contatos;
    }

    public String getResponsavelInstitucional() {
        return responsavelInstitucional;
    }

    public void setResponsavelInstitucional(String responsavelInstitucional) {
        this.responsavelInstitucional = responsavelInstitucional;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getConfiguracoes() {
        return configuracoes;
    }

    public void setConfiguracoes(String configuracoes) {
        this.configuracoes = configuracoes;
    }

    public String getPeriodoLetivoAtual() {
        return periodoLetivoAtual;
    }

    public void setPeriodoLetivoAtual(String periodoLetivoAtual) {
        this.periodoLetivoAtual = periodoLetivoAtual;
    }

    public String getDadosRelatorio() {
        return dadosRelatorio;
    }

    public void setDadosRelatorio(String dadosRelatorio) {
        this.dadosRelatorio = dadosRelatorio;
    }
}
