package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.exception.Result;
import com.example.dto.TaskBatchDTO;
import com.example.dto.TaskDTO;
import com.example.entity.DeadLetterMessage;
import com.example.entity.QueueMonitorHistory;
import com.example.entity.Task;
import com.example.mapper.TaskMapper;
import com.example.service.TaskService;
import com.example.vo.TaskVO;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController("/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @PostMapping("/page")
    public Result<Page<Task>> page(@RequestBody TaskDTO task) {
        return Result.success(taskService.page(task));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody TaskDTO task) {
        taskService.add(task);
        return Result.success(null);
    }

    @PostMapping("/start")
    public Result<Void> start(@RequestBody TaskDTO task) {
        taskService.start(task);
        return Result.success(null);
    }

    @PostMapping("/start/batch")
    public Result<Void> startBatch(@RequestBody TaskBatchDTO dto) {
        taskService.startBatch(dto);
        return Result.success(null);
    }

    @PostMapping("/detail")
    public Result<TaskVO> startBatch(@RequestBody TaskDTO dto) {
        return Result.success(taskService.detail(dto));
    }

    @PostMapping("/dead/page")
    public Result<Page<DeadLetterMessage>> deadPage(@RequestBody TaskDTO dto) {
        return Result.success(taskService.deadPage(dto));
    }

    @PostMapping("/retry")
    public Result<Void> retry(@RequestBody TaskDTO dto) {
        taskService.retry(dto);
        return Result.success(null);
    }

    @GetMapping("/metrics")
    public Result<List<QueueMonitorHistory>> metrics() {
        List<String> queues = Arrays.asList("taskQueue", "bakQueue");
        return Result.success(taskService.getQueuesMonitor(queues, "/"));
    }

}
