package com.dao.momentum.service;

import com.dao.momentum.dto.NotificationMessage;
import com.dao.momentum.entity.EvaluationNotificationReservation;
import com.dao.momentum.entity.IsSent;
import com.dao.momentum.feign.EvaluationClient;
import com.dao.momentum.producer.NotificationKafkaProducer;
import com.dao.momentum.repository.EvaluationNotificationReservationRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationBatchService {

    private final NotificationKafkaProducer producer;
    private final EvaluationNotificationReservationRepository reservationRepository;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final EvaluationClient evaluationClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /**
     * 매일 10시 실행되는 예약된 평가 알림 발송
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendEvaluationNotifications() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        log.info("[알림 배치 시작] 조회 대상 기간: {} ~ {}", startOfDay, endOfDay);

        List<EvaluationNotificationReservation> reservations =
                reservationRepository.findAllByIsSentAndScheduledDateBetween(IsSent.N, startOfDay, endOfDay);

        log.info("[예약 조회 결과] 총 {}건", reservations.size());

        for (EvaluationNotificationReservation reservation : reservations) {
            executor.submit(() -> {
                try {
                    String type = reservation.getType();
                    String content = buildContent(type, reservation);

                    if (content == null) return;

                    sendToAllTargets(type, content);

                    reservation.markAsSent();
                    reservationRepository.save(reservation);
                    log.info("[예약 상태 갱신] 예약 ID: {} -> 발송 완료", reservation.getId());

                } catch (Exception e) {
                    log.error("[알림 전송 실패] 예약 ID: {}", reservation.getId(), e);
                }
            });
        }
    }

    /**
     * DB에 예약된 알림 발송
     */
    private void sendToAllTargets(String type, String content) {
        List<Long> receiverIds = getReceiverIds(type);

        if (receiverIds.isEmpty()) {
            log.info("[즉시 발송] 알림 대상 없음, type={}", type);
            return;
        }

        for (Long receiverId : receiverIds) {
            NotificationMessage message = NotificationMessage.builder()
                    .type(type)
                    .content(content)
                    .url("/performance/evaluation/my/list")
                    .receiverId(receiverId)
                    .build();

            producer.send(receiverId.toString(), message);
            log.info("[알림 전송 성공] 수신자 ID: {}, type={}", receiverId, type);
        }
    }

    /**
     * 컨트롤러에서 전달된 메시지 객체 기반 즉시 발송
     */
    public void sendToAllTargets(NotificationMessage templateMessage) {
        String type = templateMessage.getType();

        if (!"EVALUATION_START".equals(type)) {
            log.warn("[즉시 발송] 잘못된 타입 요청: {}", type);
            throw new IllegalArgumentException("지원되지 않는 알림 타입입니다: " + type + " -> (EVALUATION_START)");
        }

        List<Long> receiverIds = getReceiverIds(type);

        if (receiverIds.isEmpty()) {
            log.info("[즉시 발송] 알림 대상 없음, type={}", type);
            return;
        }

        LocalDate today = LocalDate.now();
        String start = today.format(DATE_FORMATTER);
        String end = today.plusDays(6).format(DATE_FORMATTER);

        String content = String.format(
                "[평가 시작 안내]\n평가 기간이 시작되었습니다.(%s ~ %s)\n해당 평가에 참여해주세요.",
                start, end
        );

        for (Long receiverId : receiverIds) {
            executor.submit(() -> {
                try {
                    NotificationMessage message = NotificationMessage.builder()
                            .type(type)
                            .content(content)
                            .url(templateMessage.getUrl())
                            .receiverId(receiverId)
                            .build();

                    producer.send(receiverId.toString(), message);
                    log.info("[즉시 발송] 수신자 ID: {}, type={}", receiverId, type);
                } catch (Exception e) {
                    log.error("[즉시 발송 실패] 수신자 ID: {}, type={}", receiverId, type, e);
                }
            });
        }
    }


    /**
     * 평가 타입에 따른 알림 대상 조회
     */
    private List<Long> getReceiverIds(String type) {
        return switch (type) {
            case "EVALUATION_START" -> evaluationClient.getTargetEmployeeIds();
            case "EVALUATION_END" -> evaluationClient.getUnsubmittedEmployeeIds();
            default -> throw new IllegalArgumentException("지원되지 않는 알림 타입: " + type);
        };
    }

    /**
     * 알림 타입에 따른 메시지 본문 생성
     */
    private String buildContent(String type, EvaluationNotificationReservation reservation) {
        if ("EVALUATION_START".equals(type)) {
            String start = reservation.getStartDate().format(DATE_FORMATTER);
            String end = reservation.getEndDate().format(DATE_FORMATTER);
            return String.format("[평가 시작 안내]\n평가 기간이 시작되었습니다.(%s ~ %s)\n해당 평가에 참여해주세요.", start, end);
        } else if ("EVALUATION_END".equals(type)) {
            String end = reservation.getEndDate().format(DATE_FORMATTER);
            return String.format("[평가 마감 임박]\n평가 기간이 오늘 종료됩니다.(%s)\n지금 참여해주세요.", end);
        } else {
            log.warn("[알림 타입 무시] 지원되지 않는 type: {}", type);
            return null;
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        log.info("[배치 서버 종료] ExecutorService shutdown 시작");
        executor.shutdown();
        log.info("[배치 서버 종료] ExecutorService 정상 종료");
    }
}

