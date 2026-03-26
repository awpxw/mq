package com.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskBatchDTO {

    private List<TaskDTO> tasks;

}
