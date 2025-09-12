package com.senai.avaluationsystem.repository;

import com.senai.avaluationsystem.model.Question;
import com.senai.avaluationsystem.model.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByType(QuestionType type);
    List<Question> findByRequired(Boolean required);
}