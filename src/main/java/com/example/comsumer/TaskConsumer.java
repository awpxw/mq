package com.example.comsumer;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.entity.DeadLetterMessage;
import com.example.entity.MessageIdempotent;
import com.example.entity.Task;
import com.example.entity.TaskExecutionLog;
import com.example.enums.DeathReason;
import com.example.enums.TaskStatus;
import com.example.enums.TaskType;
import com.example.mapper.DeadLetterMessageMapper;
import com.example.utils.IdempotentUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

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
    public void handleTaskQueue(Message message) {
        MessageProperties props = message.getMessageProperties();
        //幂等校验
        synchronized (lock) {
            if (idempotentUtils.check(message)) {
                return;
            }
        }
        try {
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
        Long taskId = props.getHeader("taskId");
        DeadLetterMessage deadLetterMessage = ChainWrappers.lambdaQueryChain(DeadLetterMessage.class)
                .eq(DeadLetterMessage::getTaskId, taskId)
                .one();
        //更新任务
        updateTask(taskId, TaskStatus.FAILED.getCode());
        //更新记录
        updateLog(taskId, TaskStatus.SUCCESS.getCode());
        //死亡原因
        Task task = ChainWrappers.lambdaQueryChain(Task.class)
                .eq(Task::getTaskId, taskId)
                .one();
        boolean isDelay = task.getDelayMs() > 0L;
        //是否已死亡
        boolean exist = ChainWrappers.lambdaQueryChain(DeadLetterMessage.class).eq(DeadLetterMessage::getTaskId, taskId).count() > 0;
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
                    .retryCount(0)
                    .build();
            //存储死信
            deadLetterMessageMapper.insert(dMsg);
        } else {
            //重试次数+1
            ChainWrappers.lambdaUpdateChain(DeadLetterMessage.class)
                    .eq(DeadLetterMessage::getTaskId, deadLetterMessage.getTaskId())
                    .set(DeadLetterMessage::getDeathCount, deadLetterMessage.getDeathCount() + 1)
                    .set(DeadLetterMessage::getDeathTime, deadLetterMessage.getDeathTime())
                    .set(DeadLetterMessage::getRetryCount, deadLetterMessage.getRetryCount() + 1)
                    .update();
        }
    }

    private void retryTaskAndLog(MessageProperties props) {
        Long taskId = props.getHeader("taskId");
        //更新任务
        updateTask(taskId, TaskStatus.RETRY.getCode());
        //记录日志
        updateLog(taskId, TaskStatus.RETRY.getCode());
    }

    private void successTaskAndLog(MessageProperties props) {
        Long taskId = props.getHeader("taskId");
        //更新任务
        updateTask(taskId, TaskStatus.SUCCESS.getCode());
        //记录日志
        updateLog(taskId, TaskStatus.SUCCESS.getCode());
        //更新幂等1小时过期
        ChainWrappers.lambdaUpdateChain(MessageIdempotent.class).eq(MessageIdempotent::getTaskId, taskId).set(MessageIdempotent::getStatus, TaskStatus.SUCCESS.getCode()).set(MessageIdempotent::getProcessTime, LocalDateTime.now()).set(MessageIdempotent::getExpireTime, LocalDateTime.now().plusHours(1)).update();
    }

    private void updateTask(Long taskId, Integer success) {
        Task task = ChainWrappers.lambdaQueryChain(Task.class).eq(Task::getTaskId, taskId).one();
        ChainWrappers.lambdaUpdateChain(Task.class).eq(Task::getId, task.getId()).set(Task::getStatus, success).set(Task::getDurationMs, Duration.between(LocalDateTime.now(), task.getStartTime()).get(MILLIS)).set(Task::getFinishTime, LocalDateTime.now()).update();
    }

    private void updateLog(Long taskId, Integer status) {
        TaskExecutionLog log = ChainWrappers.lambdaQueryChain(TaskExecutionLog.class).eq(TaskExecutionLog::getTaskId, taskId).one();
        ChainWrappers.lambdaUpdateChain(TaskExecutionLog.class).eq(TaskExecutionLog::getTaskId, taskId).set(TaskExecutionLog::getStatus, status).set(TaskExecutionLog::getEndTime, LocalDateTime.now()).set(TaskExecutionLog::getDurationMs, Duration.between(LocalDateTime.now(), log.getEndTime()).get(MILLIS)).update();
    }

}
