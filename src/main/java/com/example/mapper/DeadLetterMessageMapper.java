package com.example.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.DeadLetterMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeadLetterMessageMapper extends BaseMapper<DeadLetterMessage> {

}