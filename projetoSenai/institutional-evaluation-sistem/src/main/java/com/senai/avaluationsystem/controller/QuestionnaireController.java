package com.senai.avaluationsystem.controller;

import com.senai.avaluationsystem.dto.AnswerListRequest;
import com.senai.avaluationsystem.dto.AnswerRequest;
import com.senai.avaluationsystem.dto.QuestionResponse;
import com.senai.avaluationsystem.service.AnswerService;
import com.senai.avaluationsystem.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/questionnaire")
public class QuestionnaireController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AnswerService answerService;

    /**
     * Exibe a página do questionário
     */
    @GetMapping
    public String showQuestionnaire(Model model) {
        List<QuestionResponse> questions = questionService.getAllQuestions();

        // Prepara uma lista de AnswerRequest já com os questionId
        AnswerListRequest answerListRequest = new AnswerListRequest();
        List<AnswerRequest> answers = new ArrayList<>();
        for (QuestionResponse q : questions) {
            AnswerRequest ar = new AnswerRequest();
            ar.setQuestionId(q.getId());
            answers.add(ar);
        }
        answerListRequest.setAnswers(answers);

        model.addAttribute("questions", questions);
        model.addAttribute("answerListRequest", answerListRequest);

        return "questionnaire"; // templates/questionnaire.html
    }

    /**
     * Salva as respostas enviadas
     */
    @PostMapping("/submit")
    public String submitAnswers(
            @ModelAttribute("answerListRequest") @Valid AnswerListRequest answerListRequest,
            Model model) {
        try {
            if (answerListRequest.getAnswers() != null && !answerListRequest.getAnswers().isEmpty()) {
                answerListRequest.getAnswers()
                        .forEach(answerService::createAnswer);
            }

            model.addAttribute("message", "Respostas enviadas com sucesso!");
            return "success"; // templates/success.html
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao salvar respostas: " + e.getMessage());
            return "questionnaire";
        }
    }
}
