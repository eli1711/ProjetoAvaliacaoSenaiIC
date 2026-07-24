package br.com.cpa.questionario.service;

import br.com.cpa.questionario.model.Aluno;
import br.com.cpa.questionario.model.AvaliacaoAplicada;
import br.com.cpa.questionario.model.StatusAluno;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AvaliacaoEmailService {

    private static final Logger log = LoggerFactory.getLogger(AvaliacaoEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean envioHabilitado;
    private final String baseUrl;

    public AvaliacaoEmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                                 @Value("${app.email.convites.enabled:false}") boolean envioHabilitado,
                                 @Value("${app.email.base-url:http://localhost:8080}") String baseUrl) {
        this.mailSenderProvider = mailSenderProvider;
        this.envioHabilitado = envioHabilitado;
        this.baseUrl = baseUrl;
    }

    public int enviarConvites(AvaliacaoAplicada avaliacaoAplicada) {
        if (avaliacaoAplicada.getTurma() == null
                || avaliacaoAplicada.getTurma().getAlunos() == null) {
            return 0;
        }

        if (!envioHabilitado) {
            log.info("Envio de convites CPA desativado. Avaliacao id={}", avaliacaoAplicada.getId());
            return 0;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new IllegalStateException("Envio de e-mail habilitado, mas JavaMailSender nao esta configurado.");
        }

        int enviados = 0;
        for (Aluno aluno : avaliacaoAplicada.getTurma().getAlunos()) {
            if (aluno.getUser() == null || aluno.getUser().getStatus() != StatusAluno.ATIVO) {
                continue;
            }
            mailSender.send(montarMensagem(aluno, avaliacaoAplicada));
            enviados++;
        }
        return enviados;
    }

    private SimpleMailMessage montarMensagem(Aluno aluno, AvaliacaoAplicada avaliacaoAplicada) {
        String link = baseUrl.replaceAll("/+$", "")
                + "/avaliacoes/" + avaliacaoAplicada.getId() + "/responder";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(aluno.getEmail());
        message.setSubject("Avaliacao CPA - " + avaliacaoAplicada.getQuestionario().getName());
        message.setText("Ola " + aluno.getNome() + ",\n\n"
                + "Voce possui uma avaliacao CPA disponivel. "
                + "Acesse o link abaixo usando seu login institucional:\n"
                + link + "\n\n"
                + "Sua participacao ajuda a melhorar a instituicao.");
        return message;
    }
}
