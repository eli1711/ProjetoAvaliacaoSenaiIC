package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.Aluno;
import br.com.cpa.questionario.model.AvaliacaoAplicada;
import br.com.cpa.questionario.model.ParticipacaoAvaliacao;
import br.com.cpa.questionario.model.StatusResposta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipacaoAvaliacaoRepository extends JpaRepository<ParticipacaoAvaliacao, Long> {

    Optional<ParticipacaoAvaliacao> findByAlunoAndAvaliacaoAplicada(Aluno aluno, AvaliacaoAplicada avaliacaoAplicada);

    boolean existsByAlunoAndAvaliacaoAplicadaAndStatus(
            Aluno aluno,
            AvaliacaoAplicada avaliacaoAplicada,
            StatusResposta status
    );
}
