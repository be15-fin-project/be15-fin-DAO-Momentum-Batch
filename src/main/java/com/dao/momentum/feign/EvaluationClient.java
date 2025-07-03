package com.dao.momentum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(
        name = "evaluationClient",
        url = "${evaluation-service.url}",
        configuration = FeignClientConfig.class
)
public interface EvaluationClient {

    // 전체 평가 대상자 ID 리스트
    @GetMapping("/employees/ids")
    List<Long> getTargetEmployeeIds();

    // 하루 전 미제출자 ID 리스트
    @GetMapping("/evaluation/none-submit")
    List<Long> getUnsubmittedEmployeeIds();
}
