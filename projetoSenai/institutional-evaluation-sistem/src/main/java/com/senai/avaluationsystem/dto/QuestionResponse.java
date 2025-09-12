package com.senai.avaluationsystem.dto;

import com.senai.avaluationsystem.model.QuestionType;
import com.senai.avaluationsystem.model.ResponseType;

public class QuestionResponse {
    private Long id;
    private String text;
    private QuestionType type;
    private Integer minScale;
    private Integer maxScale;
    private ResponseType responseType;
    private Boolean required;
    
    // Construtores
    public QuestionResponse() {}
    
    public QuestionResponse(Long id, String text, QuestionType type, Boolean required) {
        this.id = id;
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