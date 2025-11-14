package com.moshe.todolist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moshe.todolist.entity.User;
import com.moshe.todolist.mapper.UserMapper;
import com.moshe.todolist.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // 注册
    @Override
    public User register(String name, String password) {
        // 1. 检查用户名是否已存在
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("name", name);
        User exist = userMapper.selectOne(qw);
        if (exist != null) {
            // 400 Bad Request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户名已存在");
        }

        // 2. 真的去创建用户
        User user = new User();
        user.setName(name);
        user.setPassword(password); // 先用明文，后面可以再改成加密
        user.setRegisteredTime(LocalDateTime.now());

        userMapper.insert(user);

        // 出于安全考虑，不把密码返回给前端
        user.setPassword(null);
        return user;
    }

    // 登录
    @Override
    public User login(String name, String password) {
        // 1. 根据用户名查
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("name", name);
        User user = userMapper.selectOne(qw);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户不存在");
        }

        // 2. 比较密码（现在先明文比较）
        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "密码错误");
        }

        // 登录成功，同样不把密码返回
        user.setPassword(null);
        return user;
    }
}