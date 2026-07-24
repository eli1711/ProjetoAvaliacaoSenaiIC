package br.com.cpa.questionario.service;

import br.com.cpa.questionario.exception.ResultadoRestritoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ResultadoPrivacidadeService {

    private final int minimoRespostasGrupo;
    private final boolean permiteRelatorioIndividual;
    private final double limiteMuitoInsatisfeito;
    private final double limiteInsatisfeito;
    private final double limiteNeutro;
    private final double limiteSatisfeito;

    public ResultadoPrivacidadeService(
            @Value("${app.privacidade.minimo-respostas-grupo:5}") int minimoRespostasGrupo,
            @Value("${app.privacidade.permite-relatorio-individual:false}") boolean permiteRelatorioIndividual,
            @Value("${app.resultados.limite-muito-insatisfeito:1.5}") double limiteMuitoInsatisfeito,
            @Value("${app.resultados.limite-insatisfeito:2.5}") double limiteInsatisfeito,
            @Value("${app.resultados.limite-neutro:3.0}") double limiteNeutro,
            @Value("${app.resultados.limite-satisfeito:3.5}") double limiteSatisfeito) {
        this.minimoRespostasGrupo = minimoRespostasGrupo;
        this.permiteRelatorioIndividual = permiteRelatorioIndividual;
        this.limiteMuitoInsatisfeito = limiteMuitoInsatisfeito;
        this.limiteInsatisfeito = limiteInsatisfeito;
        this.limiteNeutro = limiteNeutro;
        this.limiteSatisfeito = limiteSatisfeito;
    }

    public boolean podeExibirResultadosAgregados(long totalEnvios) {
        return totalEnvios >= minimoRespostasGrupo;
    }

    public void validarExportacaoPermitida(long totalEnvios) {
        if (!podeExibirResultadosAgregados(totalEnvios)) {
            throw new ResultadoRestritoException(
                    mensagemResultadosRestritos(totalEnvios),
                    totalEnvios,
                    minimoRespostasGrupo);
        }
    }

    public boolean podeExibirRelatorioIndividual() {
        return permiteRelatorioIndividual;
    }

    public int getMinimoRespostasGrupo() {
        return minimoRespostasGrupo;
    }

    public String mensagemResultadosRestritos(long totalEnvios) {
        return "Resultados ocultados para proteger a privacidade dos participantes. "
                + "O grupo selecionado possui " + totalEnvios
                + " envio(s), abaixo do minimo configurado de " + minimoRespostasGrupo + ".";
    }

    public String classificarMedia(Double media) {
        if (media == null) {
            return "Sem dados";
        }
        if (media <= limiteMuitoInsatisfeito) {
            return "Muito insatisfeito";
        }
        if (media <= limiteInsatisfeito) {
            return "Insatisfeito";
        }
        if (media <= limiteNeutro) {
            return "Neutro";
        }
        if (media <= limiteSatisfeito) {
            return "Satisfeito";
        }
        return "Muito satisfeito";
    }

    public int classificarMediaComoNivel(Double media) {
        if (media == null) {
            return 0;
        }
        if (media <= limiteMuitoInsatisfeito) {
            return 1;
        }
        if (media <= limiteInsatisfeito) {
            return 2;
        }
        if (media <= limiteSatisfeito) {
            return 3;
        }
        return 4;
    }
}
