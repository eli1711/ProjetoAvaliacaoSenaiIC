package com.senai.avaluationsystem.repository;

import com.senai.avaluationsystem.model.Answer;
import com.senai.avaluationsystem.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByQuestion(Question question);
}
