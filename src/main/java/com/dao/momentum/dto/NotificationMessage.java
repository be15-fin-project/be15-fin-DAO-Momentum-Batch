package com.dao.momentum.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NotificationMessage {
    private String content;           // 알림 내용
    private String type;              // 알림 타입: EVALUATION_START, EVALUATION_END 등
    private String url;               // 클릭 시 이동할 URL
    private Long receiverId;          // 수신자 ID
}
