package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.Aluno;
import br.com.cpa.questionario.model.AvaliacaoAplicada;
import br.com.cpa.questionario.model.RespostaAluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespostaAlunoRepository extends JpaRepository<RespostaAluno, Long> {

    boolean existsByAlunoAndAvaliacaoAplicada(Aluno aluno, AvaliacaoAplicada avaliacaoAplicada);
    void deleteByAvaliacaoAplicada(AvaliacaoAplicada avaliacaoAplicada);
}
