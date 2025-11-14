package com.moshe.todolist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moshe.todolist.entity.Todo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TodoMapper extends BaseMapper<Todo> {
}
