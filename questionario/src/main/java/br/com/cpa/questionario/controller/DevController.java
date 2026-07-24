package br.com.cpa.questionario.controller;

import br.com.cpa.questionario.model.Instituicao;
import br.com.cpa.questionario.repository.InstituicaoRepository;
import br.com.cpa.questionario.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dev")
public class DevController {

    private final InstituicaoRepository instituicaoRepository;
    private final UserRepository userRepository;

    public DevController(InstituicaoRepository instituicaoRepository,
                         UserRepository userRepository) {
        this.instituicaoRepository = instituicaoRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(Model model) {
        long totalInstituicoes = instituicaoRepository.count();
        long instituicoesAtivas = instituicaoRepository.findAll().stream()
                .filter(Instituicao::isAtivo)
                .count();

        model.addAttribute("totalInstituicoes", totalInstituicoes);
        model.addAttribute("instituicoesAtivas", instituicoesAtivas);
        model.addAttribute("totalUsuarios", userRepository.count());
        return "dev/index";
    }
}
