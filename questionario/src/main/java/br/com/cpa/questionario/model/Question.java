package br.com.cpa.questionario.model;

import jakarta.persistence.*;

@Entity
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    private Integer score; // 0-5 para QUANTITATIVA

    // labels das alternativas (múltipla escolha)
    private String option1Label; // ex: Discordo totalmente
    private String option2Label; // ex: Discordo parcialmente
    private String option3Label; // ex: Concordo parcialmente
    private String option4Label; // ex: Concordo totalmente
    private String option5Label; // ex: Não sei opinar (opcional)

    @ManyToOne
    @JoinColumn(name = "questionnaire_id")
    private Questionnaire questionnaire;

    // getters / setters ...

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getOption1Label() { return option1Label; }
    public void setOption1Label(String option1Label) { this.option1Label = option1Label; }

    public String getOption2Label() { return option2Label; }
    public void setOption2Label(String option2Label) { this.option2Label = option2Label; }

    public String getOption3Label() { return option3Label; }
    public void setOption3Label(String option3Label) { this.option3Label = option3Label; }

    public String getOption4Label() { return option4Label; }
    public void setOption4Label(String option4Label) { this.option4Label = option4Label; }

    public String getOption5Label() { return option5Label; }
    public void setOption5Label(String option5Label) { this.option5Label = option5Label; }

    public Questionnaire getQuestionnaire() { return questionnaire; }
    public void setQuestionnaire(Questionnaire questionnaire) { this.questionnaire = questionnaire; }
}
