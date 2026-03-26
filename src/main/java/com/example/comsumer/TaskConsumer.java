package com.example.comsumer;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.entity.DeadLetterMessage;
import com.example.entity.MessageIdempotent;
import com.example.entity.Task;
import com.example.entity.TaskExecutionLog;
import com.example.enums.*;
import com.example.mapper.DeadLetterMessageMapper;
import com.example.utils.IdempotentUtils;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.temporal.ChronoUnit.MILLIS;


@Component
public class TaskConsumer {

    @Resource
    private IdempotentUtils idempotentUtils;

    @Resource
    private DeadLetterMessageMapper deadLetterMessageMapper;

    private static final Object lock = new Object();

    @RabbitHandler
    @RabbitListener(queues = "taskQueue")
    public void handleTaskQueue(Message message, Channel channel) throws IOException {
        MessageProperties props = message.getMessageProperties();
        //幂等校验
        synchronized (lock) {
            if (idempotentUtils.check(message)) {
                return;
            }
        }
        try {
            //todo:处理task任务
            successTaskAndLog(props);
        } catch (Exception e) {
            retryTaskAndLog(props);
            throw e;
        }
    }

    @RabbitHandler
    @RabbitListener(queues = "taskDeadQueue")
    public void handleTaskDeadQueue(Message message) {
        MessageProperties props = message.getMessageProperties();
        DeadLetterMessage deadLetterMessage = ChainWrappers.lambdaQueryChain(DeadLetterMessage.class)
                .eq(DeadLetterMessage::getTaskId, props.getHeader("taskId"))
                .one();
        Task task = ChainWrappers.lambdaQueryChain(Task.class)
                .eq(Task::getTaskId, deadLetterMessage.getTaskId())
                .one();
        boolean isDelay = task.getDelayMs() > 0L;
        boolean exist = ChainWrappers.lambdaQueryChain(DeadLetterMessage.class)
                .eq(DeadLetterMessage::getTaskId, props.getHeader("taskId"))
                .count() > 0;
        if (!exist) {
            //刚进入死信
            DeadLetterMessage dMsg = DeadLetterMessage.builder()
                    .messageId(props.getMessageId())
                    .taskId(props.getMessageId())
                    .taskType(props.getType())
                    .originalQueue(TaskType.fromCode(Integer.parseInt(props.getType())) + "Queue")
                    .originalExchange(TaskType.fromCode(Integer.parseInt(props.getType())) + "Exchange")
                    .originalRoutingKey(TaskType.fromCode(Integer.parseInt(props.getType())))
                    .messageBody(new String(message.getBody()))
                    .deathCount(1)
                    .deathReason(isDelay ? DeathReason.EXPIRED.getCode() : DeathReason.REJECTED.getCode())
                    .deathTime(LocalDateTime.now())
                    .retryStatus(RetryStatus.UN_RETRIED.getCode())
                    .retryCount(0)
                    .build();
            deadLetterMessageMapper.insert(dMsg);
        } else {
            //重试进入死信
            ChainWrappers.lambdaUpdateChain(DeadLetterMessage.class)
                    .eq(DeadLetterMessage::getTaskId, deadLetterMessage.getTaskId())
                    .set(DeadLetterMessage::getDeathReason, deadLetterMessage.getTaskId())
                    .set(DeadLetterMessage::getDeathCount, deadLetterMessage.getDeathCount() + 1)
                    .set(DeadLetterMessage::getDeathTime, deadLetterMessage.getDeathTime())
                    .set(DeadLetterMessage::getRetryStatus, RetryStatus.RETRY_FAILED.getCode())
                    .set(DeadLetterMessage::getRetryCount, deadLetterMessage.getRetryCount() + 1)
                    .update();
        }
    }

    private void retryTaskAndLog(MessageProperties props) {
        //更新任务
        Task task = ChainWrappers.lambdaQueryChain(Task.class).
                eq(Task::getTaskId, props.getHeader("taskId"))
                .one();
        ChainWrappers.lambdaUpdateChain(Task.class)
                .eq(Task::getId, task.getId())
                .set(Task::getCurrentStage, ExecutionStage.RETRYING.getCode())
                .set(Task::getStatus, TaskStatus.PROCESSING.getCode())
                .update();
        //记录日志
        TaskExecutionLog log = ChainWrappers.lambdaQueryChain(TaskExecutionLog.class)
                .eq(TaskExecutionLog::getTaskId, task.getId())
                .one();
        ChainWrappers.lambdaUpdateChain(TaskExecutionLog.class)
                .eq(TaskExecutionLog::getTaskId, log.getTaskId())
                .set(TaskExecutionLog::getExecutionNo, log.getExecutionNo() + 1)
                .set(TaskExecutionLog::getStatus, TaskStatus.PROCESSING.getCode())
                .update();
    }

    private void successTaskAndLog(MessageProperties props) {
        //更新任务
        Task task = ChainWrappers.lambdaQueryChain(Task.class).
                eq(Task::getTaskId, props.getHeader("taskId"))
                .one();
        ChainWrappers.lambdaUpdateChain(Task.class)
                .eq(Task::getId, task.getId())
                .set(Task::getCurrentStage, ExecutionStage.SUCCESS.getCode())
                .set(Task::getStatus, TaskStatus.SUCCESS.getCode())
                .set(Task::getDurationMs, Duration.between(LocalDateTime.now(), task.getStartTime()).get(MILLIS))
                .set(Task::getFinishTime, LocalDateTime.now())
                .update();
        //更新幂等1小时过期
        ChainWrappers.lambdaUpdateChain(MessageIdempotent.class)
                .eq(MessageIdempotent::getMessageId, task.getMessageId())
                .eq(MessageIdempotent::getTaskId, task.getId())
                .set(MessageIdempotent::getStatus, TaskStatus.SUCCESS.getCode())
                .set(MessageIdempotent::getProcessTime, LocalDateTime.now())
                .set(MessageIdempotent::getExpireTime, LocalDateTime.now().plusHours(1))
                .update();
        //记录日志
        TaskExecutionLog log = ChainWrappers.lambdaQueryChain(TaskExecutionLog.class)
                .eq(TaskExecutionLog::getTaskId, task.getId())
                .one();
        ChainWrappers.lambdaUpdateChain(TaskExecutionLog.class)
                .eq(TaskExecutionLog::getTaskId, task.getId())
                .eq(TaskExecutionLog::getExecutionNo, log.getExecutionNo() + 1)
                .set(TaskExecutionLog::getStatus, TaskStatus.SUCCESS.getCode())
                .set(TaskExecutionLog::getEndTime, LocalDateTime.now())
                .set(TaskExecutionLog::getDurationMs, Duration.between(LocalDateTime.now(), log.getEndTime()).get(MILLIS))
                .update();
    }

}
