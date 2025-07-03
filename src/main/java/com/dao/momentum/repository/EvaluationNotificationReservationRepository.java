package com.dao.momentum.repository;

import com.dao.momentum.entity.EvaluationNotificationReservation;
import com.dao.momentum.entity.IsSent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EvaluationNotificationReservationRepository extends JpaRepository<EvaluationNotificationReservation, Long> {
    List<EvaluationNotificationReservation> findAllByIsSentAndScheduledDateBetween(IsSent isSent, LocalDateTime start, LocalDateTime end);
}