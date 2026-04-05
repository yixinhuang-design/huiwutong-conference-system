package com.conference.collaboration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.conference.collaboration.entity.TaskComment;
import com.conference.collaboration.service.TaskCommentService;
import com.conference.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务评论 Controller
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@RestController
@RequestMapping("/api/task-comment")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService commentService;

    /**
     * 添加评论
     */
    @PostMapping("/add")
    public Result<TaskComment> addComment(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody TaskComment comment) {
        // 优先使用网关注入的用户ID
        if (headerUserId != null) {
            comment.setUserId(headerUserId);
        }
        if (comment.getUserId() == null) {
            return Result.fail("用户ID不能为空");
        }
        if (comment.getTaskId() == null) {
            return Result.fail("任务ID不能为空");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.fail("评论内容不能为空");
        }
        TaskComment created = commentService.addComment(comment);
        return Result.ok("评论成功", created);
    }

    /**
     * 获取评论列表（分页）
     */
    @GetMapping("/list/{taskId}")
    public Result<Page<TaskComment>> listComments(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TaskComment> result = commentService.listComments(taskId, page, size);
        return Result.ok(result);
    }

    /**
     * 获取评论树（含回复）
     */
    @GetMapping("/tree/{taskId}")
    public Result<List<Map<String, Object>>> getCommentTree(@PathVariable Long taskId) {
        List<Map<String, Object>> tree = commentService.getCommentTree(taskId);
        return Result.ok(tree);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public Result<String> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) Long userId) {
        Long effectiveUserId = headerUserId != null ? headerUserId : userId;
        if (effectiveUserId == null) {
            return Result.fail("用户ID不能为空");
        }
        commentService.deleteComment(commentId, effectiveUserId);
        return Result.ok("删除成功");
    }

    /**
     * 获取评论数
     */
    @GetMapping("/count/{taskId}")
    public Result<Long> getCommentCount(@PathVariable Long taskId) {
        long count = commentService.countByTaskId(taskId);
        return Result.ok(count);
    }
}
