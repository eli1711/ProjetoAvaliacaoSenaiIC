package br.com.cpa.questionario.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plano_acao", indexes = {
        @Index(name = "idx_plano_acao_instituicao", columnList = "instituicao_id"),
        @Index(name = "idx_plano_acao_avaliacao", columnList = "avaliacao_aplicada_id"),
        @Index(name = "idx_plano_acao_status", columnList = "status"),
        @Index(name = "idx_plano_acao_prazo", columnList = "prazo")
})
public class PlanoAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @ManyToOne
    @JoinColumn(name = "avaliacao_aplicada_id")
    private AvaliacaoAplicada avaliacaoAplicada;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String problemaIdentificado;

    private String indicadorRelacionado;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String acaoProposta;

    @Column(nullable = false)
    private String responsavel;

    private String setorResponsavel;

    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadePlanoAcao prioridade = PrioridadePlanoAcao.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPlanoAcao status = StatusPlanoAcao.PLANEJADA;

    @Column(nullable = false)
    private Integer percentualConclusao = 0;

    @Column(columnDefinition = "TEXT")
    private String evidencias;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(columnDefinition = "TEXT")
    private String resultadoEsperado;

    @Column(columnDefinition = "TEXT")
    private String resultadoAlcancado;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private String criadoPor;
    private String atualizadoPor;

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
        normalizar();
    }

    @PreUpdate
    public void preUpdate() {
        atualizadoEm = LocalDateTime.now();
        normalizar();
    }

    public void normalizar() {
        if (prioridade == null) {
            prioridade = PrioridadePlanoAcao.MEDIA;
        }
        if (status == null) {
            status = StatusPlanoAcao.PLANEJADA;
        }
        setPercentualConclusao(percentualConclusao);
    }

    public boolean isAtrasada() {
        return prazo != null
                && prazo.isBefore(LocalDate.now())
                && status != StatusPlanoAcao.CONCLUIDA
                && status != StatusPlanoAcao.CANCELADA;
    }

    public StatusPlanoAcao getStatusEfetivo() {
        return isAtrasada() ? StatusPlanoAcao.ATRASADA : status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instituicao getInstituicao() { return instituicao; }
    public void setInstituicao(Instituicao instituicao) { this.instituicao = instituicao; }

    public AvaliacaoAplicada getAvaliacaoAplicada() { return avaliacaoAplicada; }
    public void setAvaliacaoAplicada(AvaliacaoAplicada avaliacaoAplicada) { this.avaliacaoAplicada = avaliacaoAplicada; }

    public String getProblemaIdentificado() { return problemaIdentificado; }
    public void setProblemaIdentificado(String problemaIdentificado) { this.problemaIdentificado = problemaIdentificado; }

    public String getIndicadorRelacionado() { return indicadorRelacionado; }
    public void setIndicadorRelacionado(String indicadorRelacionado) { this.indicadorRelacionado = indicadorRelacionado; }

    public String getAcaoProposta() { return acaoProposta; }
    public void setAcaoProposta(String acaoProposta) { this.acaoProposta = acaoProposta; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getSetorResponsavel() { return setorResponsavel; }
    public void setSetorResponsavel(String setorResponsavel) { this.setorResponsavel = setorResponsavel; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }

    public PrioridadePlanoAcao getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadePlanoAcao prioridade) { this.prioridade = prioridade; }

    public StatusPlanoAcao getStatus() { return status; }
    public void setStatus(StatusPlanoAcao status) { this.status = status; }

    public Integer getPercentualConclusao() { return percentualConclusao; }
    public void setPercentualConclusao(Integer percentualConclusao) {
        int valor = percentualConclusao == null ? 0 : percentualConclusao;
        this.percentualConclusao = Math.max(0, Math.min(100, valor));
    }

    public String getEvidencias() { return evidencias; }
    public void setEvidencias(String evidencias) { this.evidencias = evidencias; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getResultadoEsperado() { return resultadoEsperado; }
    public void setResultadoEsperado(String resultadoEsperado) { this.resultadoEsperado = resultadoEsperado; }

    public String getResultadoAlcancado() { return resultadoAlcancado; }
    public void setResultadoAlcancado(String resultadoAlcancado) { this.resultadoAlcancado = resultadoAlcancado; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getCriadoPor() { return criadoPor; }
    public void setCriadoPor(String criadoPor) { this.criadoPor = criadoPor; }

    public String getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(String atualizadoPor) { this.atualizadoPor = atualizadoPor; }
}
