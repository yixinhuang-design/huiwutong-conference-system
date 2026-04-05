package com.conference.collaboration.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务评论实体
 * @author AI Executive
 * @date 2026-04-01
 */
@Data
@TableName("task_comment")
public class TaskComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;
    private Long userId;
    private String userName;
    private String content;
    private Long parentId;          // 父评论ID，用于回复
    private String attachments;     // JSON

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
