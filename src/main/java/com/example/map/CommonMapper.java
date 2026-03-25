package com.example.map;

import com.example.dto.TaskDTO;
import com.example.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CommonMapper {

    CommonMapper INSTANCE = Mappers.getMapper(CommonMapper.class);

    Task toEntity(TaskDTO dto);

}