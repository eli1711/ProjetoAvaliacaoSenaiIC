package br.com.cpa.questionario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ActiveProfiles;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AnaliseAvaliacoesTemplateTest {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Test
    void renderizaTelaDeAnaliseComModeloMinimo() {
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IWebExchange exchange = application.buildExchange(request, response);

        String html = templateEngine.process("analise/avaliacoes",
                new WebContext(exchange, Locale.forLanguageTag("pt-BR"), modeloMinimo()));

        assertThat(html).contains("Analise das avaliacoes");
    }

    private Map<String, Object> modeloMinimo() {
        Map<String, Object> model = new HashMap<>();
        model.put("turmas", List.of());
        model.put("anos", List.of(2026));
        model.put("avaliacoes", List.of());
        model.put("avaliacoesFiltradas", List.of());
        model.put("selectedTurmaId", null);
        model.put("selectedAno", null);
        model.put("selectedAvaliacaoId", null);
        model.put("totalEnvios", 0L);
        model.put("totalRespostas", 0L);
        model.put("mediaQuantitativa", null);
        model.put("classificacaoMedia", "Sem dados");
        model.put("qtdAvaliacoesConsideradas", 0L);
        model.put("resultadosRestritos", true);
        model.put("minimoRespostasGrupo", 5);
        model.put("mensagemResultadosRestritos", "Resultados ocultados para proteger os participantes.");
        model.put("relatorioIndividualPermitido", false);
        model.put("distribuicaoPorPergunta", Collections.emptyMap());
        model.put("totalRespostasPergunta", Collections.emptyMap());
        model.put("mediaPergunta", Collections.emptyMap());
        model.put("somaNotasAvaliacao", Collections.emptyMap());
        model.put("mediaNotasAvaliacao", Collections.emptyMap());
        model.put("respostasQualitativasPorPergunta", Collections.emptyMap());
        model.put("analisePorAluno", List.of());
        model.put("enviosDisponiveis", List.of());
        model.put("selectedRespostaAlunoId", null);
        model.put("relatorioAluno", List.of());
        model.put("mediaAluno", null);
        model.put("mediaAlunoComZero", null);
        return model;
    }
}
