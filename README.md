项目规划
类别,具体接口/组件,主要作用,提升的能力点
配置类,RabbitConfig,定义 Exchange、Queue、Binding、DLQ、延迟交换机,交换机队列设计、死信机制、延迟消息
生产者,RabbitTemplate + OrderProducer,发送消息 + Publisher Confirm,生产者可靠性、生产者确认机制
消费者,@RabbitListener + AbstractRabbitConsumer,手动 ACK、拒绝消息、幂等处理,手动确认、死信队列、幂等性
事件定义,BaseEvent + OrderCreatedEvent 等,统一消息体结构,消息标准化、可追踪性
控制器,OrderController,REST 接口触发发送消息,快速测试与演示
监控相关,Spring Boot Actuator + Micrometer,暴露 RabbitMQ 指标,可观测性、运维能力
辅助工具,StringRedisTemplate,消息幂等判断,分布式幂等处理
管理界面,RabbitMQ Management (15672端口),可视化查看队列、消息、DLQ,运维调试能力

生产者可靠性
掌握 Publisher Confirms + Returns
消息持久化设置
发送失败回调处理

消费者可靠性（最重要）
手动 ACK / NACK
拒绝消息进入死信队列（DLQ）
配置 x-dead-letter-exchange 和 x-dead-letter-routing-key

重试机制
Spring AMQP 内置重试 + 指数退避
失败后不重入原队列，直接走 DLQ

消息幂等性
使用 messageId + Redis setIfAbsent 实现 exactly-once 处理

延迟消息处理
使用 rabbitmq_delayed_message_exchange 插件
设置 x-delay header

消息标准化与追踪
统一 BaseEvent（带 messageId、traceId）
在 MessageProperties 中传递 traceId

合理队列与交换机设计
Topic Exchange + 路由键规划
主队列与死信队列分离

监控与运维基础
Actuator 暴露指标
RabbitMQ 管理界面使用
手动重发 DLQ 消息的能力

项目结构

rabbitmq-shop-demo/
├── pom.xml
├── src/main/java/com/example/rabbitmqshop/
│   ├── RabbitmqShopApplication.java
│   ├── config/RabbitConfig.java          # 交换机、队列、DLQ、延迟交换机
│   ├── event/OrderCreatedEvent.java      # 事件基类 + 具体事件
│   ├── producer/OrderProducer.java       # 发送消息 + Publisher Confirm
│   ├── consumer/OrderConsumer.java       # 手动 ACK + 幂等 + DLQ 处理
│   ├── controller/WebController.java     # 页面控制器（发送消息、查看DLQ）
│   ├── service/DlqService.java           # DLQ 重发逻辑（可选进阶）
│   └── dto/OrderDto.java                 # 页面表单 DTO
├── src/main/resources/
│   ├── application.yml                   # 配置
│   ├── templates/                        # Thymeleaf 页面
│   │   ├── index.html                    # 首页：发送订单表单 + 发送记录
│   │   └── dlq.html                      # DLQ 管理页面（简单列表 + 重发按钮）
│   └── static/css/style.css              # 简单样式
├── docker-compose.yml                    # 一键启动 RabbitMQ（带 management）
└── README.md


业务流程

首页：任务提交表单
├── 选择任务类型（邮件/短信/报表/备份）
├── 设置优先级（滑动条）
├── 设置延迟时间
├── 填写任务参数（JSON）
└── 提交按钮

任务列表页
├── 进行中的任务
├── 已完成的任务
├── 失败的任务
└── 实时刷新状态

DLQ 管理页
├── 查看失败任务列表
├── 查看失败原因
├── 单个重试 / 批量重试
└── 删除死信消息

监控面板
├── 队列深度图表
├── 消费速率
├── 任务成功率
└── 平均处理耗时