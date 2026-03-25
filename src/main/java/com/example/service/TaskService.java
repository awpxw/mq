package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.dto.TaskDTO;
import com.example.entity.Task;
import com.example.entity.TaskExecutionLog;
import com.example.enums.ExecutionStage;
import com.example.enums.ExecutionStatus;
import com.example.enums.TaskType;
import com.example.map.CommonMapper;
import com.example.mapper.TaskExecutionLogMapper;
import com.example.mapper.TaskMapper;
import com.example.utils.MsgUtils;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Service
public class TaskService {

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private TaskExecutionLogMapper taskExecutionLogMapper;


    public Page<Task> page(TaskDTO task) {
        return taskMapper.selectPage(Page.of(task.getPageNo(), task.getPageSize()), null);
    }

    public void add(TaskDTO dto) {
        Task task = CommonMapper.INSTANCE.toEntity(dto);
        taskMapper.insert(task);
    }

    public void start(TaskDTO dto) {
        Long taskId = dto.getTaskId();
        Task entity = taskMapper.selectById(taskId);
        String taskType = entity.getTaskType();
        //创建消息
        Message msg = MsgUtils.createMsg(taskType, dto.getTaskData());
        String msgId = msg.getMessageProperties().getMessageId();
        //是否延迟
        boolean isDelay = dto.getDelayMs() != null && dto.getDelayMs() > 0;
        Integer currentStage;
        LocalDateTime executeTime = null;
        if (isDelay) {
            currentStage = ExecutionStage.PENDING.getCode();
            executeTime = LocalDateTime.now().plusSeconds(dto.getDelayMs());
        } else {
            currentStage = ExecutionStage.RUNNING.getCode();
        }
        //更新任务
        ChainWrappers.lambdaUpdateChain(Task.class)
                .eq(Task::getId, taskId)
                .set(Task::getMessageId, msgId)
                .set(Task::getCurrentStage, currentStage)
                .set(Objects.nonNull(executeTime), Task::getExecuteTime, taskType)
                .set(Task::getQueueName, TaskType.fromCode(Integer.parseInt(taskType)))
                .set(Task::getExchangeName, TaskType.fromCode(Integer.parseInt(taskType)) + "Exchange")
                .set(Task::getRoutingKey, taskType)
                .update();
        //生成执行日志
        TaskExecutionLog log = TaskExecutionLog.builder()
                .taskId(taskId)
                .executionNo(0)
                .status(ExecutionStatus.START.getCode())
                .startTime(LocalDateTime.now())
                .inputData(dto.getTaskData())
                .queueName(TaskType.fromCode(Integer.parseInt(taskType)))
                .build();
        taskExecutionLogMapper.insert(log);
    }


}
