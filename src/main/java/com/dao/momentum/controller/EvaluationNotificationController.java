package com.dao.momentum.controller;

import com.dao.momentum.dto.NotificationMessage;
import com.dao.momentum.service.NotificationBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/evaluation-notifications")
public class EvaluationNotificationController {

    private final NotificationBatchService batchService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEvaluationNotificationImmediately(
            @RequestBody NotificationMessage message
    ) {
        batchService.sendToAllTargets(message);
        return ResponseEntity.ok("평가 알림이 즉시 전송되었습니다.");
    }
}