package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByUserUsername(String username);

    Optional<Aluno> findByRa(String ra);

    Optional<Aluno> findByRaAndInstituicaoId(String ra, Long instituicaoId);

    List<Aluno> findByInstituicaoId(Long instituicaoId);
}
