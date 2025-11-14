package com.moshe.todolist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moshe.todolist.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 先不用写额外方法，BaseMapper 已经有常用 CRUD 了
}