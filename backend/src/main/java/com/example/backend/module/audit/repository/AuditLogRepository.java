package com.example.backend.module.audit.repository;

import com.example.backend.module.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByEntityTypeOrderByDateActionDesc(String entityType);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByDateActionDesc(String entityType, Long entityId);

    @Query("SELECT a FROM AuditLog a WHERE a.dateAction BETWEEN :debut AND :fin ORDER BY a.dateAction DESC")
    List<AuditLog> findByPeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}