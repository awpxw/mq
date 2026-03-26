package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.dto.TaskBatchDTO;
import com.example.dto.TaskDTO;
import com.example.entity.DeadLetterMessage;
import com.example.entity.QueueMonitorHistory;
import com.example.entity.Task;
import com.example.entity.TaskExecutionLog;
import com.example.enums.TaskStatus;
import com.example.enums.TaskType;
import com.example.map.CommonMapper;
import com.example.mapper.DeadLetterMessageMapper;
import com.example.mapper.TaskExecutionLogMapper;
import com.example.mapper.TaskMapper;
import com.example.utils.MsgUtils;
import com.example.vo.TaskVO;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.MessageStats;
import com.rabbitmq.http.client.domain.QueueInfo;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private TaskMapper taskMapper;

    @Resource
    private TaskExecutionLogMapper taskExecutionLogMapper;

    @Resource
    private DeadLetterMessageMapper deadLetterMessageMapper;

    @Resource
    private Client rabbitManagementClient;


    public Page<Task> page(TaskDTO task) {
        return taskMapper.selectPage(Page.of(task.getPageNo(), task.getPageSize()), null);
    }

    public void add(TaskDTO dto) {
        Task task = CommonMapper.INSTANCE.toEntity(dto);
        taskMapper.insert(task);
    }

    @Transactional
    public void start(TaskDTO dto) {
        Long taskId = dto.getTaskId();
        Task entity = taskMapper.selectById(taskId);
        String taskType = entity.getTaskType();
        //创建消息
        Message msg = MsgUtils.createMsg(taskId, taskType, dto.getTaskData());
        String msgId = msg.getMessageProperties().getMessageId();
        //是否延迟
        boolean isDelay = dto.getDelayMs() != null && dto.getDelayMs() > 0;
        Integer status;
        LocalDateTime executeTime = null;
        if (isDelay) {
            status = TaskStatus.PENDING.getCode();
            executeTime = LocalDateTime.now().plusSeconds(dto.getDelayMs());
        } else {
            status = TaskStatus.RUNNING.getCode();
        }
        //更新任务
        ChainWrappers.lambdaUpdateChain(Task.class)
                .eq(Task::getId, taskId)
                .set(Task::getMessageId, msgId)
                .set(Task::getStatus, status)
                .set(Objects.nonNull(executeTime), Task::getExecuteTime, taskType)
                .set(Task::getQueueName, TaskType.fromCode(Integer.parseInt(taskType)))
                .set(Task::getExchangeName, TaskType.fromCode(Integer.parseInt(taskType)) + "Exchange")
                .set(Task::getRoutingKey, taskType)
                .update();
        //生成执行日志
        TaskExecutionLog log = TaskExecutionLog.builder()
                .taskId(taskId)
                .status(status)
                .startTime(LocalDateTime.now())
                .inputData(dto.getTaskData())
                .queueName(TaskType.fromCode(Integer.parseInt(taskType)))
                .build();
        taskExecutionLogMapper.insert(log);
        //发送消息
        rabbitTemplate.convertAndSend(TaskType.fromCode(Integer.parseInt(taskType)) + "Exchange", taskType);
    }

    @Transactional
    public void startBatch(TaskBatchDTO dto) {
        for (TaskDTO task : dto.getTasks()) {
            start(task);
        }
    }

    public TaskVO detail(TaskDTO dto) {
        Long taskId = dto.getTaskId();
        Task one = ChainWrappers.lambdaQueryChain(Task.class)
                .eq(Task::getId, taskId)
                .one();
        return CommonMapper.INSTANCE.toVO(one);
    }

    public void retry(TaskDTO dto) {
        Long taskId = dto.getTaskId();
        DeadLetterMessage dMsg = ChainWrappers.lambdaQueryChain(DeadLetterMessage.class)
                .eq(DeadLetterMessage::getTaskId, taskId)
                .one();
        //创建消息
        Message msg = MsgUtils.createMsgFromDead(dMsg);
        //发送消息
        rabbitTemplate.convertAndSend(TaskType.fromCode(Integer.parseInt(dMsg.getTaskType())) + "Exchange", dMsg.getTaskType(), msg);
    }

    public Page<DeadLetterMessage> deadPage(TaskDTO dto) {
        return deadLetterMessageMapper.selectPage(Page.of(dto.getPageNo(), dto.getPageSize()), null);
    }

    public List<QueueMonitorHistory> getQueuesMonitor(List<String> queueNames, String vhost) {
        return queueNames.stream()
                .map(queue -> getQueueMonitor(vhost, queue))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public QueueMonitorHistory getQueueMonitor(String vhost, String queueName) {
        try {
            QueueInfo queueInfo = rabbitManagementClient.getQueue(vhost, queueName);
            if (queueInfo == null) {
                return null;
            }
            QueueMonitorHistory history = new QueueMonitorHistory();
            history.setQueueName(queueInfo.getName());
            history.setVhost(queueInfo.getVhost());
            history.setMessagesReady((int) queueInfo.getMessagesReady());
            history.setMessagesUnacknowledged((int) queueInfo.getMessagesUnacknowledged());
            history.setMessagesTotal((int) queueInfo.getTotalMessages());
            history.setConsumers((int) queueInfo.getConsumerCount());
            history.setMemoryUsed(queueInfo.getMemoryUsed());

            // 速率字段（带判空）
            MessageStats stats = queueInfo.getMessageStats();
            if (stats != null) {
                if (stats.getBasicPublishDetails() != null) {
                    history.setPublishRate(BigDecimal.valueOf(stats.getBasicPublishDetails().getRate()));
                }
                if (stats.getBasicDeliverDetails() != null) {
                    history.setDeliverRate(BigDecimal.valueOf(stats.getBasicDeliverDetails().getRate()));
                }
                if (stats.getAckDetails() != null) {
                    history.setAckRate(BigDecimal.valueOf(stats.getAckDetails().getRate()));
                }
            }
            return history;
        } catch (Exception e) {
            return null;
        }
    }

}
