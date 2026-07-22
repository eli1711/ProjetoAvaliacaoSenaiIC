package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.Questionnaire;
import br.com.cpa.questionario.model.StatusDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    List<Questionnaire> findByStatus(StatusDisponibilidade status);

    List<Questionnaire> findByInstituicaoId(Long instituicaoId);

    List<Questionnaire> findByStatusAndInstituicaoId(StatusDisponibilidade status, Long instituicaoId);

    long countByQuestionarioBaseId(Long questionarioBaseId);

    @Query("select q from Questionnaire q where q.bloqueado = false or q.bloqueado is null")
    List<Questionnaire> findModelosEditaveis();

    @Query("""
            select q from Questionnaire q
            where q.status = :status
              and (q.bloqueado = false or q.bloqueado is null)
            """)
    List<Questionnaire> findModelosEditaveisByStatus(StatusDisponibilidade status);

    @Query("""
            select q from Questionnaire q
            where q.instituicao.id = :instituicaoId
              and (q.bloqueado = false or q.bloqueado is null)
            """)
    List<Questionnaire> findModelosEditaveisByInstituicaoId(Long instituicaoId);

    @Query("""
            select q from Questionnaire q
            where q.status = :status
              and q.instituicao.id = :instituicaoId
              and (q.bloqueado = false or q.bloqueado is null)
            """)
    List<Questionnaire> findModelosEditaveisByStatusAndInstituicaoId(StatusDisponibilidade status, Long instituicaoId);
}
