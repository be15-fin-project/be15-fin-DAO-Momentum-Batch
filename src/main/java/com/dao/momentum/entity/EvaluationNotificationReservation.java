package com.dao.momentum.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EvaluationNotificationReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // EVALUATION_START, EVALUATION_END

    private LocalDateTime scheduledDate;  // 알림 발송 예정일

    private LocalDateTime startDate;      // 평가 시작일

    private LocalDateTime endDate;        // 평가 종료일

    @Enumerated(EnumType.STRING)
    private IsSent isSent;                // Y, N

    private LocalDateTime sentAt;         // 실제 발송 시각

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void markAsSent() {
        this.isSent = IsSent.Y;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}