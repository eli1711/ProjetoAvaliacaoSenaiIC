package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.Questionnaire;
import br.com.cpa.questionario.model.StatusDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {

    // Consulta para buscar questionários por status
    List<Questionnaire> findByStatus(StatusDisponibilidade status);
}
