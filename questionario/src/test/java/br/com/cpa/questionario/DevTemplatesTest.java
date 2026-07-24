package br.com.cpa.questionario;

import br.com.cpa.questionario.model.Instituicao;
import br.com.cpa.questionario.model.StatusAluno;
import br.com.cpa.questionario.model.User;
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DevTemplatesTest {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Test
    void renderizaPainelDev() {
        Map<String, Object> model = new HashMap<>();
        model.put("totalInstituicoes", 2L);
        model.put("instituicoesAtivas", 1L);
        model.put("totalUsuarios", 5L);

        String html = render("dev/index", model);

        assertThat(html).contains("Painel do dev");
    }

    @Test
    void renderizaCadastroDeInstituicoes() {
        Instituicao instituicao = instituicao(1L);

        String lista = render("instituicao/list", Map.of("instituicoes", List.of(instituicao)));

        assertThat(lista).contains("/dev/instituicoes/new");
    }

    @Test
    void renderizaListagemDeUsuariosComModoDev() {
        Instituicao instituicao = instituicao(1L);
        User user = new User();
        user.setUsername("admin");
        user.setName("Administrador");
        user.setEmail("admin@teste.local");
        user.setRole("ROLE_ADMIN");
        user.setStatus(StatusAluno.ATIVO);
        user.setInstituicao(instituicao);

        Map<String, Object> model = new HashMap<>();
        model.put("users", List.of(user));
        model.put("superAdmin", true);

        String html = render("user/list", model);

        assertThat(html).contains("Instituicao Teste");
    }

    private String render(String template, Map<String, Object> model) {
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        IWebExchange exchange = application.buildExchange(request, response);

        return templateEngine.process(template,
                new WebContext(exchange, Locale.forLanguageTag("pt-BR"), model));
    }

    private Instituicao instituicao(Long id) {
        Instituicao instituicao = new Instituicao();
        instituicao.setId(id);
        instituicao.setNome("Instituicao Teste");
        instituicao.setIdentificadorInstitucional("TESTE");
        instituicao.setResponsavelInstitucional("Responsavel");
        instituicao.setPeriodoLetivoAtual("2026/1");
        return instituicao;
    }
}
