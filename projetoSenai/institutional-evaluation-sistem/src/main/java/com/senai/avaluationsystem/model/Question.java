package com.senai.avaluationsystem.model;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String text;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;
    
    private Integer minScale;
    
    private Integer maxScale;
    
    @Enumerated(EnumType.STRING)
    private ResponseType responseType;
    
    @Column(nullable = false)
    private Boolean required = false;
    
    // Construtores
    public Question() {}
    
    public Question(String text, QuestionType type, Boolean required) {
        this.text = text;
        this.type = type;
        this.required = required;
    }
    
    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    
    public Integer getMinScale() { return minScale; }
    public void setMinScale(Integer minScale) { this.minScale = minScale; }
    
    public Integer getMaxScale() { return maxScale; }
    public void setMaxScale(Integer maxScale) { this.maxScale = maxScale; }
    
    public ResponseType getResponseType() { return responseType; }
    public void setResponseType(ResponseType responseType) { this.responseType = responseType; }
    
    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }
}