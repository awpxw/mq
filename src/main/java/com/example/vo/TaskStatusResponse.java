package com.example.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskStatusResponse {

    private String taskId;

    private Integer status;

    private String statusDesc;

    private String currentStage;

    private Integer retryCount;

    private Integer maxRetry;

    private Integer progress;

    private Integer queueDepth;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    private String errorMsg;

}