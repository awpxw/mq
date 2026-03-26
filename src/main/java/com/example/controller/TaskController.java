package com.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.common.exception.Result;
import com.example.dto.TaskBatchDTO;
import com.example.dto.TaskDTO;
import com.example.entity.Task;
import com.example.mapper.TaskMapper;
import com.example.service.TaskService;
import com.example.vo.TaskVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @Resource
    private TaskMapper taskMapper;

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

    @PostMapping("/retry")
    public Result<Void> retry(@RequestBody TaskDTO dto) {
        return Result.success(taskService.retry(dto));
    }



}
