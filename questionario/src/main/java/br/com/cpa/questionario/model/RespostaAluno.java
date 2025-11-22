package br.com.cpa.questionario.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class RespostaAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AGORA referencia Aluno, não User
    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id") // FK para aluno.id
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "avaliacao_aplicada_id")
    private AvaliacaoAplicada avaliacaoAplicada;

    private LocalDateTime dataResposta;

    @Enumerated(EnumType.STRING)
    private StatusResposta status = StatusResposta.RESPONDIDO;

    @OneToMany(mappedBy = "respostaAluno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> respostas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Aluno getAluno() { return aluno; }
    public void setAluno(Aluno aluno) { this.aluno = aluno; }

    public AvaliacaoAplicada getAvaliacaoAplicada() { return avaliacaoAplicada; }
    public void setAvaliacaoAplicada(AvaliacaoAplicada avaliacaoAplicada) {
        this.avaliacaoAplicada = avaliacaoAplicada;
    }

    public LocalDateTime getDataResposta() { return dataResposta; }
    public void setDataResposta(LocalDateTime dataResposta) { this.dataResposta = dataResposta; }

    public StatusResposta getStatus() { return status; }
    public void setStatusResposta(StatusResposta status) { this.status = status; }

    public List<Answer> getRespostas() { return respostas; }
    public void setRespostas(List<Answer> respostas) { this.respostas = respostas; }
}
