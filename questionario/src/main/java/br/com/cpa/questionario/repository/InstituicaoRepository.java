package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {

    Optional<Instituicao> findByIdentificadorInstitucional(String identificadorInstitucional);
}
