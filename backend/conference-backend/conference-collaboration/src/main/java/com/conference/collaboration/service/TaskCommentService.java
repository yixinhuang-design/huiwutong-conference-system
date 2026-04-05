package com.conference.collaboration.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.collaboration.entity.TaskComment;
import com.conference.collaboration.mapper.TaskCommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 任务评论服务
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentMapper commentMapper;

    /**
     * 添加评论
     */
    @Transactional
    public TaskComment addComment(TaskComment comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setDeleted(0);
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        commentMapper.insert(comment);
        log.info("评论添加成功: taskId={}, userId={}", comment.getTaskId(), comment.getUserId());
        return comment;
    }

    /**
     * 获取任务评论列表（分页）
     */
    public Page<TaskComment> listComments(Long taskId, int page, int size) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId)
               .eq(TaskComment::getDeleted, 0)
               .orderByDesc(TaskComment::getCreateTime);
        return commentMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取任务所有评论（含回复层级结构）
     */
    public List<Map<String, Object>> getCommentTree(Long taskId) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId)
               .eq(TaskComment::getDeleted, 0)
               .orderByAsc(TaskComment::getCreateTime);
        List<TaskComment> allComments = commentMapper.selectList(wrapper);

        // 构建评论树
        Map<Long, Map<String, Object>> commentMap = new LinkedHashMap<>();
        List<Map<String, Object>> rootComments = new ArrayList<>();

        for (TaskComment comment : allComments) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("comment", comment);
            node.put("replies", new ArrayList<>());
            commentMap.put(comment.getId(), node);
        }

        for (TaskComment comment : allComments) {
            Map<String, Object> node = commentMap.get(comment.getId());
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                rootComments.add(node);
            } else {
                Map<String, Object> parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> replies = (List<Map<String, Object>>) parent.get("replies");
                    replies.add(node);
                } else {
                    rootComments.add(node);
                }
            }
        }

        return rootComments;
    }

    /**
     * 删除评论（逻辑删除）
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        TaskComment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }
        commentMapper.deleteById(commentId);
        log.info("评论已删除: commentId={}", commentId);
    }

    /**
     * 获取评论数
     */
    public long countByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskComment::getTaskId, taskId)
               .eq(TaskComment::getDeleted, 0);
        return commentMapper.selectCount(wrapper);
    }
}
