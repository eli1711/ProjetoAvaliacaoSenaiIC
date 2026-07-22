package br.com.cpa.questionario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "participacao_avaliacao",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_participacao_aluno_avaliacao",
                columnNames = {"aluno_id", "avaliacao_aplicada_id"}
        )
)
public class ParticipacaoAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "avaliacao_aplicada_id")
    private AvaliacaoAplicada avaliacaoAplicada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusResposta status = StatusResposta.PENDENTE;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataConclusao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public AvaliacaoAplicada getAvaliacaoAplicada() {
        return avaliacaoAplicada;
    }

    public void setAvaliacaoAplicada(AvaliacaoAplicada avaliacaoAplicada) {
        this.avaliacaoAplicada = avaliacaoAplicada;
    }

    public StatusResposta getStatus() {
        return status;
    }

    public void setStatus(StatusResposta status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(LocalDateTime dataConclusao) {
        this.dataConclusao = dataConclusao;
    }
}
