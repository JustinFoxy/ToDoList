package com.moshe.todolist.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;   //来自 MyBatis-Plus
import lombok.Data; //来自 Lombok注解

import java.time.LocalDateTime;

@Data   //来自 Lombok注解，用来自动生成：所有 getter / setter 、toString()、 equals()、 hashCode()
@TableName("todo")  //来自 MyBatis-Plus ，让这个实体类对应数据库里的todo表
public class Todo {


    @TableId(type = IdType.AUTO)    //告诉 MyBatis-Plus：这个字段是主键（PRIMARY KEY）并且数据库是自增（AUTO_INCREMENT）
    private Integer id;

    private String content;

    private Integer completed;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    //外健
    @TableField("user_id")
    private Integer userId;

    // ⭐ 新增：父任务 ID（可以为 null）
    @TableField("parent_id")
    private Integer parentId;
}