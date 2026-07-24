package br.com.cpa.questionario.repository;

import br.com.cpa.questionario.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop200ByOrderByDataHoraDesc();

    List<AuditLog> findTop200ByInstituicaoIdOrderByDataHoraDesc(Long instituicaoId);
}
