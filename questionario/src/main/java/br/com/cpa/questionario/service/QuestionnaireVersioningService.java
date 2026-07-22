package br.com.cpa.questionario.service;

import br.com.cpa.questionario.model.Question;
import br.com.cpa.questionario.model.Questionnaire;
import br.com.cpa.questionario.model.StatusDisponibilidade;
import br.com.cpa.questionario.repository.QuestionRepository;
import br.com.cpa.questionario.repository.QuestionnaireRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionnaireVersioningService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;

    public QuestionnaireVersioningService(QuestionnaireRepository questionnaireRepository,
                                          QuestionRepository questionRepository) {
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public Questionnaire criarVersaoPublicada(Questionnaire selecionado) {
        Questionnaire modeloBase = obterModeloBase(selecionado);
        List<Question> perguntas = questionRepository.findByQuestionnaireId(modeloBase.getId());
        if (perguntas.isEmpty()) {
            throw new IllegalStateException("Nao e possivel publicar uma avaliacao sem perguntas.");
        }

        long proximaVersao = questionnaireRepository.countByQuestionarioBaseId(modeloBase.getId()) + 1;

        Questionnaire versao = new Questionnaire();
        versao.setName(modeloBase.getName());
        versao.setDescription(modeloBase.getDescription());
        versao.setSemester(modeloBase.getSemester());
        versao.setYear(modeloBase.getYear());
        versao.setStatus(StatusDisponibilidade.DISPONIVEL);
        versao.setInstituicao(modeloBase.getInstituicao());
        versao.setQuestionarioBaseId(modeloBase.getId());
        versao.setVersao((int) proximaVersao);
        versao.setBloqueado(true);

        Questionnaire versaoSalva = questionnaireRepository.save(versao);
        for (Question pergunta : perguntas) {
            Question copia = copiarPergunta(pergunta);
            copia.setQuestionnaire(versaoSalva);
            questionRepository.save(copia);
        }

        return versaoSalva;
    }

    private Questionnaire obterModeloBase(Questionnaire selecionado) {
        if (selecionado.isBloqueado() && selecionado.getQuestionarioBaseId() != null) {
            return questionnaireRepository.findById(selecionado.getQuestionarioBaseId())
                    .orElseThrow(() -> new IllegalStateException("Modelo base do questionario publicado nao encontrado."));
        }
        return selecionado;
    }

    private Question copiarPergunta(Question origem) {
        Question copia = new Question();
        copia.setText(origem.getText());
        copia.setType(origem.getType());
        copia.setScore(origem.getScore());
        copia.setOption1Label(origem.getOption1Label());
        copia.setOption2Label(origem.getOption2Label());
        copia.setOption3Label(origem.getOption3Label());
        copia.setOption4Label(origem.getOption4Label());
        copia.setOption5Label(origem.getOption5Label());
        copia.setItemAvaliacao(origem.getItemAvaliacao());
        copia.setGrauImportanciaModelo(origem.getGrauImportanciaModelo());
        return copia;
    }
}
