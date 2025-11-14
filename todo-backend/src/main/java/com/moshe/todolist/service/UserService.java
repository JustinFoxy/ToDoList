package com.moshe.todolist.service;

import com.moshe.todolist.entity.User;

public interface UserService {

    // 注册
    User register(String name, String password);

    // 登录
    User login(String name, String password);
}