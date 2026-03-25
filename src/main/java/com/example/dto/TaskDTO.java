package com.example.dto;

import lombok.Data;

@Data
public class TaskDTO {

    private int pageNo;

    private int pageSize;

    private Long taskId;

    private String taskName;

    private String taskType;

    private String currentStage;

    private String taskData;

    private Integer maxRetry;

    private Long delayMs;

}
