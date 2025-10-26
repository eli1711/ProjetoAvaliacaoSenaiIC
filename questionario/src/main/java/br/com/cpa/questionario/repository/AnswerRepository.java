package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    boolean existsByUserAndQuestion_Questionnaire(User user, Questionnaire questionnaire);
}
