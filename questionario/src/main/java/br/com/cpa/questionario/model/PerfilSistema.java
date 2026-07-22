package br.com.cpa.questionario.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum PerfilSistema {
    SUPER_ADMIN("ROLE_SUPER_ADMIN", EnumSet.allOf(Permissao.class)),

    ADMIN_INSTITUICAO("ROLE_ADMIN", EnumSet.of(
            Permissao.GERENCIAR_INSTITUICOES,
            Permissao.GERENCIAR_USUARIOS,
            Permissao.GERENCIAR_TURMAS_CURSOS,
            Permissao.CRIAR_QUESTIONARIO,
            Permissao.EDITAR_QUESTIONARIO,
            Permissao.PUBLICAR_AVALIACAO,
            Permissao.ENCERRAR_AVALIACAO,
            Permissao.VISUALIZAR_RESULTADOS,
            Permissao.EXPORTAR_RELATORIOS,
            Permissao.GERENCIAR_PLANOS_ACAO
    )),

    GESTOR_CPA("ROLE_GESTOR", EnumSet.of(
            Permissao.CRIAR_QUESTIONARIO,
            Permissao.EDITAR_QUESTIONARIO,
            Permissao.PUBLICAR_AVALIACAO,
            Permissao.ENCERRAR_AVALIACAO,
            Permissao.VISUALIZAR_RESULTADOS,
            Permissao.EXPORTAR_RELATORIOS,
            Permissao.GERENCIAR_PLANOS_ACAO
    )),

    COORDENADOR("ROLE_COORDENADOR", EnumSet.of(
            Permissao.VISUALIZAR_RESULTADOS,
            Permissao.EXPORTAR_RELATORIOS
    )),

    PROFESSOR("ROLE_PROFESSOR", EnumSet.of(
            Permissao.VISUALIZAR_RESULTADOS
    )),

    COLABORADOR("ROLE_COLABORADOR", EnumSet.noneOf(Permissao.class)),

    ALUNO("ROLE_ALUNO", EnumSet.of(
            Permissao.RESPONDER_AVALIACAO
    )),

    AVALIADOR_EXTERNO("ROLE_AVALIADOR_EXTERNO", EnumSet.of(
            Permissao.VISUALIZAR_RESULTADOS
    )),

    USUARIO("ROLE_USER", EnumSet.noneOf(Permissao.class));

    private final String authority;
    private final Set<Permissao> permissoes;

    PerfilSistema(String authority, Set<Permissao> permissoes) {
        this.authority = authority;
        this.permissoes = Collections.unmodifiableSet(permissoes);
    }

    public String getAuthority() {
        return authority;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public static Set<Permissao> permissoesPorAuthority(String authority) {
        return Arrays.stream(values())
                .filter(perfil -> perfil.authority.equals(authority))
                .findFirst()
                .map(PerfilSistema::getPermissoes)
                .orElseGet(() -> EnumSet.noneOf(Permissao.class));
    }
}
