好的！以下是**分布式任务调度平台**的完整 15 个 REST API 接口设计，每个接口都明确标注了要演示的 RabbitMQ 技术点。

---

# 分布式任务调度平台 - REST API 接口文档

## 基础信息
- **Base URL**: `http://localhost:8080/api`
- **响应格式**: JSON

---

## 接口列表（共15个）

| 序号 | 接口 | 方法 | 功能 | 演示技术点 |
|------|------|------|------|-----------|
| 1 | `/tasks` | POST | 提交普通任务 | Publisher Confirm、消息持久化 |
| 2 | `/tasks/delayed` | POST | 提交延迟任务 | 延迟队列插件、x-delayed-message |
| 3 | `/tasks/priority` | POST | 提交优先级任务 | 优先级队列、消息排序 |
| 4 | `/tasks/batch` | POST | 批量提交任务 | 批量发送、性能测试 |
| 5 | `/tasks/{taskId}` | GET | 查询任务详情 | 幂等性验证、状态追踪 |
| 6 | `/tasks/{taskId}/status` | GET | 查询任务状态 | 消息确认机制 |
| 7 | `/tasks/{taskId}/retry` | POST | 手动重试任务 | 死信队列、手动重发 |
| 8 | `/tasks/{taskId}/cancel` | DELETE | 取消待执行任务 | 消息确认、队列删除 |
| 9 | `/dlq/messages` | GET | 查询死信队列消息 | 死信队列可视化 |
| 10 | `/dlq/messages/{messageId}` | GET | 查询死信消息详情 | 死信原因分析 |
| 11 | `/dlq/retry/{messageId}` | POST | 单条死信重发 | 手动重发、消息路由 |
| 12 | `/dlq/retry/batch` | POST | 批量重发死信 | 批量操作、幂等性 |
| 13 | `/dlq/messages/{messageId}` | DELETE | 删除死信消息 | 死信清理 |
| 14 | `/monitor/queues` | GET | 查询队列监控数据 | Actuator、队列深度 |
| 15 | `/monitor/metrics` | GET | 查询系统指标 | Micrometer、可观测性 |

---

## 接口 1：提交普通任务

**功能**：提交一个异步任务到消息队列，演示生产者可靠性

**URL**：`POST /api/tasks`

**请求体**：
```json
{
    "taskType": "EMAIL",           // 任务类型：EMAIL/SMS/CALC/REPORT
    "priority": 5,                 // 优先级 1-10，默认5
    "taskData": {
        "to": "user@example.com",
        "subject": "测试邮件",
        "content": "这是一封测试邮件"
    },
    "maxRetry": 3,                 // 最大重试次数
    "timeout": 30000               // 超时时间（毫秒）
}
```

**响应**：
```json
{
    "code": 200,
    "message": "任务提交成功",
    "data": {
        "taskId": "TASK_1700000000001",
        "messageId": "MSG_1700000000001",
        "status": "PENDING",
        "queueName": "queue.email",
        "estimatedTime": "2026-03-25 15:30:00"
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| Publisher Confirm | `rabbitTemplate.setConfirmCallback()` 确认消息到达 Broker |
| 消息持久化 | `MessageDeliveryMode.PERSISTENT` |
| 消息ID生成 | `UUID.randomUUID().toString()` 全局唯一 |
| 路由策略 | 根据 taskType 路由到不同队列 |

---

## 接口 2：提交延迟任务

**功能**：提交一个延迟执行的任务，演示延迟队列插件

**URL**：`POST /api/tasks/delayed`

**请求体**：
```json
{
    "taskType": "SMS",
    "taskData": {
        "phone": "13800138000",
        "message": "验证码：123456"
    },
    "delayMs": 60000,              // 延迟60秒执行
    "maxRetry": 3
}
```

**响应**：
```json
{
    "code": 200,
    "message": "延迟任务提交成功",
    "data": {
        "taskId": "TASK_1700000000002",
        "delayUntil": "2026-03-25 15:31:00",
        "delayMs": 60000
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 延迟交换机 | `rabbitmq_delayed_message_exchange` 插件 |
| 延迟头设置 | `setHeader("x-delay", delayMs)` |
| 延迟队列 | 专门配置 `delay.exchange` 和 `delay.queue` |

---

## 接口 3：提交优先级任务

**功能**：提交带优先级的任务，高优先级任务优先被消费

**URL**：`POST /api/tasks/priority`

**请求体**：
```json
{
    "taskType": "CALC",
    "priority": 10,                // 最高优先级
    "taskData": {
        "calculation": "report.generate",
        "params": {"date": "2026-03-25"}
    }
}
```

**响应**：
```json
{
    "code": 200,
    "message": "优先级任务提交成功",
    "data": {
        "taskId": "TASK_1700000000003",
        "priority": 10,
        "queueName": "queue.calc.priority"
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 优先级队列 | `x-max-priority=10` 队列参数 |
| 消息优先级 | `setPriority(priority)` |
| 队列隔离 | 高优先级任务走独立队列 |

---

## 接口 4：批量提交任务

**功能**：一次提交多个任务，演示批量发送和性能

**URL**：`POST /api/tasks/batch`

**请求体**：
```json
{
    "tasks": [
        {
            "taskType": "EMAIL",
            "taskData": {"to": "user1@example.com", "subject": "通知1"}
        },
        {
            "taskType": "EMAIL",
            "taskData": {"to": "user2@example.com", "subject": "通知2"}
        },
        {
            "taskType": "SMS",
            "taskData": {"phone": "13800138001", "message": "验证码"}
        }
    ],
    "batchId": "BATCH_20260325_001"
}
```

**响应**：
```json
{
    "code": 200,
    "message": "批量提交成功",
    "data": {
        "batchId": "BATCH_20260325_001",
        "total": 3,
        "successCount": 3,
        "failedCount": 0,
        "taskIds": ["TASK_001", "TASK_002", "TASK_003"]
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 批量发送 | `rabbitTemplate.invoke()` 批量确认 |
| 事务支持 | `channel.txSelect()` 事务边界 |
| 性能测试 | 记录发送耗时、TPS |

---

## 接口 5：查询任务详情

**功能**：查询任务的完整信息，包括执行历史和结果

**URL**：`GET /api/tasks/{taskId}`

**响应**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": "TASK_1700000000001",
        "taskType": "EMAIL",
        "status": "SUCCESS",
        "priority": 5,
        "taskData": {"to": "user@example.com", "subject": "测试邮件"},
        "result": {
            "success": true,
            "message": "邮件发送成功",
            "sendTime": "2026-03-25 15:30:05"
        },
        "retryCount": 0,
        "createTime": "2026-03-25 15:30:00",
        "startTime": "2026-03-25 15:30:01",
        "finishTime": "2026-03-25 15:30:05",
        "duration": 4000
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 幂等性验证 | Redis 存储已处理的消息ID |
| 状态追踪 | 内存/Redis 存储任务状态 |
| 链路追踪 | traceId 贯穿整个流程 |

---

## 接口 6：查询任务状态

**功能**：快速查询任务当前状态，轻量级接口

**URL**：`GET /api/tasks/{taskId}/status`

**响应**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "taskId": "TASK_1700000000001",
        "status": "RUNNING",           // PENDING/RUNNING/SUCCESS/FAILED
        "currentStage": "CONSUMING",
        "retryCount": 1,
        "progress": 50,                // 执行进度百分比
        "queueDepth": 5,               // 队列中等待的任务数
        "consumerTag": "consumer_001"
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 消息确认机制 | 通过 ACK/NACK 状态推断 |
| 消费者状态 | 从 RabbitMQ API 获取 |

---

## 接口 7：手动重试任务

**功能**：手动重试失败的任务，演示死信队列重发

**URL**：`POST /api/tasks/{taskId}/retry`

**请求体**（可选）：
```json
{
    "resetRetryCount": true,           // 是否重置重试次数
    "newPriority": 8                   // 新优先级（可选）
}
```

**响应**：
```json
{
    "code": 200,
    "message": "任务已重新发送",
    "data": {
        "oldTaskId": "TASK_1700000000001",
        "newTaskId": "TASK_1700000000001_RETRY",
        "newMessageId": "MSG_1700000001000",
        "status": "RETRY_SENT"
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 死信队列 | 从 DLQ 读取消息重新发布 |
| 手动重发 | 重新发送到原队列 |
| 重试次数管理 | 更新 Redis 中的重试计数 |

---

## 接口 8：取消待执行任务

**功能**：取消尚未被消费的任务

**URL**：`DELETE /api/tasks/{taskId}`

**响应**：
```json
{
    "code": 200,
    "message": "任务已取消",
    "data": {
        "taskId": "TASK_1700000000001",
        "cancelTime": "2026-03-25 15:35:00",
        "originalStatus": "PENDING"
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 消息确认 | 如果消息在队列中，需要从队列移除 |
| 队列管理 | 使用 RabbitMQ HTTP API 删除消息 |

---

## 接口 9：查询死信队列消息

**功能**：查看所有进入死信队列的消息

**URL**：`GET /api/dlq/messages?page=1&size=20`

**响应**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "total": 15,
        "page": 1,
        "size": 20,
        "list": [
            {
                "messageId": "MSG_1700000000001",
                "taskId": "TASK_1700000000001",
                "taskType": "EMAIL",
                "failedStage": "CONSUMING",
                "errorMsg": "邮件服务器连接超时",
                "retryCount": 3,
                "originalQueue": "queue.email",
                "deadTime": "2026-03-25 15:32:00",
                "messageSize": 1024
            }
        ]
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 死信队列可视化 | 从 DLQ 获取消息列表 |
| 失败原因分析 | 从消息头提取 x-death 信息 |

---

## 接口 10：查询死信消息详情

**功能**：查看单条死信消息的完整信息

**URL**：`GET /api/dlq/messages/{messageId}`

**响应**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "messageId": "MSG_1700000000001",
        "taskId": "TASK_1700000000001",
        "fullMessage": {
            "taskType": "EMAIL",
            "taskData": {"to": "user@example.com", "subject": "测试"},
            "maxRetry": 3
        },
        "deathInfo": {
            "exchange": "dlx.exchange",
            "routingKey": "dlq.email",
            "reason": "rejected",
            "time": "2026-03-25 15:32:00",
            "count": 1
        },
        "stackTrace": "java.net.ConnectException: Connection refused..."
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| x-death 头解析 | 提取消息的死亡原因和路径 |
| 完整消息还原 | 反序列化原始消息体 |

---

## 接口 11：单条死信重发

**功能**：将一条死信消息重新发送到原队列

**URL**：`POST /api/dlq/retry/{messageId}`

**请求体**（可选）：
```json
{
    "modifyData": {
        "maxRetry": 5,
        "priority": 9
    }
}
```

**响应**：
```json
{
    "code": 200,
    "message": "死信重发成功",
    "data": {
        "oldMessageId": "MSG_1700000000001",
        "newMessageId": "MSG_1700000001001",
        "targetQueue": "queue.email",
        "status": "RETRY_SENT"
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 手动重发 | 从 DLQ 消费后重新发布 |
| 消息修改 | 可选修改消息内容再重发 |
| 幂等性保证 | 新消息生成新 messageId |

---

## 接口 12：批量重发死信

**功能**：批量重发符合条件的死信消息

**URL**：`POST /api/dlq/retry/batch`

**请求体**：
```json
{
    "taskType": "EMAIL",               // 可选，按类型过滤
    "failedBefore": "2026-03-25 00:00:00",  // 可选，失败时间之前
    "maxCount": 50,                    // 最多重发数量
    "resetRetryCount": true
}
```

**响应**：
```json
{
    "code": 200,
    "message": "批量重发完成",
    "data": {
        "totalInDLQ": 15,
        "retriedCount": 15,
        "successCount": 14,
        "failedCount": 1,
        "failedMessages": ["MSG_1700000000005"]
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 批量操作 | 循环处理死信消息 |
| 事务控制 | 确保批量操作的原子性 |

---

## 接口 13：删除死信消息

**功能**：删除死信队列中的无效消息

**URL**：`DELETE /api/dlq/messages/{messageId}`

**响应**：
```json
{
    "code": 200,
    "message": "死信消息已删除",
    "data": {
        "messageId": "MSG_1700000000001",
        "deleted": true
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| 消息确认 | 从 DLQ 消费后 ACK 删除 |
| 运维能力 | 清理无效消息 |

---

## 接口 14：查询队列监控数据

**功能**：获取所有队列的实时监控数据

**URL**：`GET /api/monitor/queues`

**响应**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "queues": [
            {
                "name": "queue.email",
                "messagesReady": 25,           // 待消费消息数
                "messagesUnacknowledged": 3,    // 未确认消息数
                "consumers": 2,                // 消费者数量
                "messagesTotal": 28,            // 总消息数
                "messageStats": {
                    "publishRate": 5.2,        // 发布速率（条/秒）
                    "deliverRate": 4.8         // 消费速率（条/秒）
                }
            },
            {
                "name": "dlq.email",
                "messagesReady": 15,
                "messagesUnacknowledged": 0,
                "consumers": 1
            }
        ],
        "totalMessages": 43,
        "totalDLQMessages": 15
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| Actuator 集成 | 暴露 RabbitMQ 指标 |
| RabbitMQ HTTP API | 调用管理接口获取数据 |

---

## 接口 15：查询系统指标

**功能**：获取系统级性能指标

**URL**：`GET /api/monitor/metrics`

**响应**：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "tasks": {
            "totalSubmitted": 1250,
            "totalSuccess": 1180,
            "totalFailed": 45,
            "totalRetried": 30,
            "successRate": 94.4
        },
        "performance": {
            "avgProcessTime": 3200,        // 平均处理耗时（毫秒）
            "p95ProcessTime": 8500,        // P95处理耗时
            "p99ProcessTime": 15000
        },
        "rabbitmq": {
            "channelCount": 5,
            "connectionCount": 2,
            "confirmRate": 100.0,          // 确认率
            "returnRate": 0.5              // 退回率
        },
        "system": {
            "uptime": 86400000,             // 运行时间（毫秒）
            "memoryUsed": 256,              // 内存使用（MB）
            "threadCount": 32
        }
    }
}
```

**演示的技术点**：
| 技术点 | 实现方式 |
|--------|---------|
| Micrometer | 集成 Prometheus 指标 |
| 可观测性 | 提供完整的系统监控数据 |

---

## 统一错误响应

当接口发生错误时，返回格式如下：

```json
{
    "code": 400,
    "message": "任务不存在",
    "data": null,
    "timestamp": 1711353600000,
    "errors": [
        {
            "field": "taskId",
            "message": "任务ID不能为空"
        }
    ]
}
```

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 409 | 任务状态冲突 |
| 500 | 服务器内部错误 |

---

**这 15 个接口完整覆盖了 RabbitMQ 的所有核心能力演示，需要我继续提供每个接口的具体实现代码吗？**