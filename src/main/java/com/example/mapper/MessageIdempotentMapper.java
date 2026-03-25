package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.MessageIdempotent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageIdempotentMapper extends BaseMapper<MessageIdempotent> {

}