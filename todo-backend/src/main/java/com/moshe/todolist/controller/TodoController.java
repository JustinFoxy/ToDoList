package com.moshe.todolist.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moshe.todolist.entity.Todo;
import com.moshe.todolist.mapper.TodoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.server.ResponseStatusException;

@RestController
@CrossOrigin
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoMapper mapper;

    public TodoController(TodoMapper mapper) {
        this.mapper = mapper;
    }

    // 查询用户所有任务（父 + 子）
    @GetMapping
    public List<Todo> list(@RequestParam(required = false) Integer userId) {
        QueryWrapper<Todo> qw = new QueryWrapper<>();

        // 如果传了 userId，就只查这个用户的
        if (userId != null) {
            qw.eq("user_id", userId);
        }
        return mapper.selectList(qw);
    }


    // 查询某个用户的顶级任务（parent_id IS NULL）
    @GetMapping("/roots")
    public List<Todo> listRoot(@RequestParam Integer userId) {
        QueryWrapper<Todo> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
                .isNull("parent_id");
        return mapper.selectList(qw);
    }

    // 查询某个父任务的所有子任务
    @GetMapping("/children")
    public List<Todo> listChildren(@RequestParam Integer parentId) {
        QueryWrapper<Todo> qw = new QueryWrapper<>();
        qw.eq("parent_id", parentId);
        return mapper.selectList(qw);
    }


    // 新增任务（可以是父任务，也可以是子任务）
    @PostMapping
    public Todo addTodo(@RequestBody Todo todo) {

        // 防止前端没传这些字段，我们手动设置
        todo.setCompleted(0);
        todo.setCreateTime(LocalDateTime.now());
        todo.setUpdateTime(LocalDateTime.now());
        mapper.insert(todo);
        return todo;
    }

    // 删除任务（有外键 + ON DELETE CASCADE，会把子任务一起删）
    @DeleteMapping("/{id}")
    public String deleteTodo(@PathVariable Integer id) {
        int rows = mapper.deleteById(id);  // MyBatis-Plus 自带的方法

        if (rows > 0) {
            // 控制台会看到：<== Updates: 1
            return "删除成功";
        } else {
            // 控制台会看到：<== Updates: 0
            return "要删除的 Todo 不存在";
        }
    }


    // 4. 修改任务内容 & 状态
    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Integer id, @RequestBody Todo body) {

        Todo dbTodo = mapper.selectById(id);
        if (dbTodo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo 不存在: " + id);
        }

        dbTodo.setContent(body.getContent());
        dbTodo.setCompleted(body.getCompleted());
        dbTodo.setUpdateTime(LocalDateTime.now());

        mapper.updateById(dbTodo);
        return dbTodo;
    }
}