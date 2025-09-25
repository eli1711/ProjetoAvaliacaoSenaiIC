package com.senai.avaluationsystem.dto;

import com.senai.avaluationsystem.model.QuestionType;
import com.senai.avaluationsystem.model.ResponseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QuestionRequest {

    @NotBlank(message = "O texto da questão é obrigatório")
    @Size(min = 5, max = 500, message = "A questão deve ter entre 5 e 500 caracteres")
    private String text;

    private QuestionType type;

    private Integer minScale;

    private Integer maxScale;

    private ResponseType responseType;

    private Boolean required = false;

    // Getters e Setters
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
