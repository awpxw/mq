package com.example.enums;

/**
 * 执行阶段枚举
 * 定义任务执行过程中的各个状态
 */
public enum ExecutionStage {
    
    /** 待执行：任务已创建，等待调度执行 */
    PENDING("待执行", 0),
    
    /** 执行中：任务正在执行过程中 */
    RUNNING("执行中", 1),
    
    /** 重试中：任务执行失败，正在重试 */
    RETRYING("重试中", 2),
    
    /** 执行失败：任务最终执行失败（重试次数用尽） */
    FAILED("执行失败", 3),

    /** 执行成功 */
    SUCCESS("执行成功", 4);
    
    /** 阶段描述 */
    private final String description;
    
    /** 阶段编码/优先级 */
    private final Integer code;
    
    ExecutionStage(String description, Integer code) {
        this.description = description;
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    /**
     * 根据编码获取枚举
     */
    public static ExecutionStage fromCode(Integer code) {
        for (ExecutionStage stage : values()) {
            if (stage.code.equals(code)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("无效的执行阶段编码: " + code);
    }
    
    /**
     * 根据描述获取枚举
     */
    public static ExecutionStage fromDescription(String description) {
        for (ExecutionStage stage : values()) {
            if (stage.description.equals(description)) {
                return stage;
            }
        }
        throw new IllegalArgumentException("无效的执行阶段描述: " + description);
    }
    
    /**
     * 判断是否为最终状态（不可再变化的状态）
     */
    public boolean isFinal() {
        return this == FAILED;
    }
    
    /**
     * 判断是否为可执行状态
     */
    public boolean isExecutable() {
        return this == PENDING || this == RETRYING;
    }
    
    /**
     * 判断是否在执行中
     */
    public boolean isRunning() {
        return this == RUNNING;
    }
}