package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.*;
import br.com.cpa.questionario.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/questionnaires")
public class QuestionnaireController {

    @Autowired private QuestionnaireRepository questionnaireRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerRepository answerRepository;
    @Autowired private UserRepository userRepository;

    // ====== LISTAGEM ======
    @GetMapping
    public String listQuestionnaires(Model model) {
        model.addAttribute("questionnaires", questionnaireRepository.findAll());
        return "questionnaire/list";
    }

    @GetMapping("/available")
    public String availableQuestionnaires(Model model) {
        model.addAttribute("questionnaires",
                questionnaireRepository.findByStatus(StatusDisponibilidade.DISPONIVEL));
        return "questionnaire/available";
    }

    // ====== NOVO QUESTIONÁRIO ======
    @GetMapping("/new")
    public String newQuestionnaire(Model model) {
        model.addAttribute("questionnaire", new Questionnaire());
        return "questionnaire/edit";
    }

    @PostMapping("/save")
    public String saveQuestionnaire(@ModelAttribute Questionnaire questionnaire) {
        questionnaireRepository.save(questionnaire);
        return "redirect:/questionnaires";
    }

    // ====== VISUALIZAR QUESTIONÁRIO ======
    @GetMapping("/{id}")
    public String viewQuestionnaire(@PathVariable Long id, Model model) {
        Questionnaire q = questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));
        model.addAttribute("questionnaire", q);
        model.addAttribute("questions", questionRepository.findByQuestionnaireId(id));
        return "questionnaire/view";
    }

    // ====== EDITAR QUESTIONÁRIO ======
    @GetMapping("/{id}/edit")
    public String editQuestionnaire(@PathVariable Long id, Model model) {
        model.addAttribute("questionnaire", questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado")));
        return "questionnaire/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateQuestionnaire(@PathVariable Long id, @ModelAttribute Questionnaire questionnaire) {
        questionnaire.setId(id);
        questionnaireRepository.save(questionnaire);
        return "redirect:/questionnaires";
    }

    @PostMapping("/{id}/delete")
    public String deleteQuestionnaire(@PathVariable Long id) {
        questionnaireRepository.deleteById(id);
        return "redirect:/questionnaires";
    }

    // ====== CRUD DE QUESTÕES ======
    @GetMapping("/{id}/questions/new")
    public String newQuestion(@PathVariable Long id, Model model) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));
        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("question", new Question());
        return "questionnaire/add_question";
    }

    @PostMapping("/{id}/questions/new")
    public String createQuestion(@PathVariable Long id,
                                 @RequestParam String text,
                                 @RequestParam String type,
                                 @RequestParam(required = false) Integer score,
                                 Model model) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));

        Question q = new Question();
        q.setText(text);

        // Protege contra valores inválidos de enum
        QuestionType questionType;
        try {
            questionType = QuestionType.valueOf(type.toUpperCase(Locale.ROOT).trim());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Tipo de questão inválido: " + type);
            model.addAttribute("questionnaire", questionnaire);
            model.addAttribute("question", q);
            return "questionnaire/add_question";
        }

        q.setType(questionType);
        if (questionType == QuestionType.QUANTITATIVA && score != null) {
            q.setScore(score);
        } else {
            q.setScore(null);
        }

        q.setQuestionnaire(questionnaire);
        questionRepository.save(q);

        return "redirect:/questionnaires/" + id;
    }

    // ====== RESPOSTAS ======
    @GetMapping("/{id}/respond")
    public String respond(@PathVariable Long id, Model model) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));

        User current = getCurrentUserEntity();
        if (current == null) return "redirect:/login";

        // 🔹 Verifica se já respondeu
        boolean alreadyAnswered = answerRepository.existsByUserAndQuestion_Questionnaire(current, questionnaire);
        if (alreadyAnswered) {
            model.addAttribute("error", "Você já respondeu a este questionário.");
            model.addAttribute("questionnaire", questionnaire);
            model.addAttribute("questions", questionRepository.findByQuestionnaireId(id));
            return "questionnaire/respond";
        }

        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("questions", questionRepository.findByQuestionnaireId(id));
        return "questionnaire/respond";
    }

    @PostMapping("/{id}/respond")
    public String saveResponses(@PathVariable Long id, @RequestParam Map<String, String> params, Model model) {
        User current = getCurrentUserEntity();
        if (current == null) return "redirect:/login";

        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));

        // 🔹 Garante que não pode responder mais de uma vez
        boolean alreadyAnswered = answerRepository.existsByUserAndQuestion_Questionnaire(current, questionnaire);
        if (alreadyAnswered) {
            model.addAttribute("error", "Você já respondeu a este questionário.");
            model.addAttribute("questionnaire", questionnaire);
            model.addAttribute("questions", questionRepository.findByQuestionnaireId(id));
            return "questionnaire/respond";
        }

        Map<Long, String> responses = extractResponses(params);
        responses.forEach((qid, value) -> {
            Question question = questionRepository.findById(qid)
                    .orElseThrow(() -> new IllegalArgumentException("Questão não encontrada: " + qid));
            Answer a = new Answer();
            a.setQuestion(question);
            a.setResponse(value);
            a.setUser(current);
            answerRepository.save(a);
        });

        // 🔹 Redireciona com mensagem de sucesso
        model.addAttribute("success", "Respostas enviadas com sucesso!");
        return "redirect:/questionnaires/available";
    }

    // ====== VISUALIZAR RESPOSTAS ======
    @GetMapping("/{id}/answers")
    public String viewAnswers(@PathVariable Long id, Model model) {
        Questionnaire questionnaire = questionnaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Questionário não encontrado"));

        List<Question> questions = questionRepository.findByQuestionnaireId(id);
        List<Answer> allAnswers = answerRepository.findAll();

        Map<Long, List<Answer>> answersByQuestion = new HashMap<>();
        for (Answer a : allAnswers) {
            if (a.getQuestion() != null && a.getQuestion().getQuestionnaire().getId().equals(id)) {
                answersByQuestion.computeIfAbsent(a.getQuestion().getId(), k -> new ArrayList<>()).add(a);
            }
        }

        model.addAttribute("questionnaire", questionnaire);
        model.addAttribute("questions", questions);
        model.addAttribute("answersByQuestion", answersByQuestion);
        return "questionnaire/answers";
    }

    // ====== AUXILIARES ======
    private Map<Long, String> extractResponses(Map<String, String> params) {
        Map<Long, String> out = new HashMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("responses[")) {
                String idStr = k.substring("responses[".length(), k.length() - 1);
                try { out.put(Long.parseLong(idStr), v); } catch (Exception ignored) {}
            }
        });
        return out;
    }

    private User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return userRepository.findByUsername(auth.getName());
    }
}
