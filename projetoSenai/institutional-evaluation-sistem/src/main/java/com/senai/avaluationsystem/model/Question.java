package com.senai.avaluationsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, length = 500)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType type;

    @Column(name = "min_scale")
    private Integer minScale;

    @Column(name = "max_scale")
    private Integer maxScale;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_type")
    private ResponseType responseType;

    @Column(name = "is_required", nullable = false)
    private Boolean required = false;
    

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Integer getMinScale() {
        return minScale;
    }

    public void setMinScale(Integer minScale) {
        this.minScale = minScale;
    }

    public Integer getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(Integer maxScale) {
        this.maxScale = maxScale;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
