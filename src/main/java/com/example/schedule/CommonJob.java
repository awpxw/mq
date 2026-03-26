package com.example.schedule;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.entity.Task;
import com.example.enums.TaskStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CommonJob {


    @Scheduled(fixedRate = 1000)
    public void startDelayTask() {

        ChainWrappers.lambdaUpdateChain(Task.class)
                .eq(Task::getStatus, TaskStatus.PENDING.getCode())
                .ne(Task::getDelayMs, null)
                .gt(Task::getExecuteTime, LocalDateTime.now())
                .set(Task::getStatus, TaskStatus.RUNNING.getCode())
                .set(Task::getStartTime, LocalDateTime.now())
                .update();

    }

}
