package com.conference.collaboration.service;

import com.conference.collaboration.entity.TaskComment;
import com.conference.collaboration.mapper.TaskCommentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TaskCommentService 单元测试
 * 验证评论模块的CRUD功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("任务评论服务测试")
class TaskCommentServiceTest {

    @Mock
    private TaskCommentMapper commentMapper;

    @InjectMocks
    private TaskCommentService commentService;

    private TaskComment sampleComment;

    @BeforeEach
    void setUp() {
        sampleComment = new TaskComment();
        sampleComment.setTaskId(1001L);
        sampleComment.setUserId(201L);
        sampleComment.setUserName("张三");
        sampleComment.setContent("任务进展顺利");
        sampleComment.setParentId(0L);
    }

    @Test
    @DisplayName("M4: 添加评论 - 应设置创建时间并插入")
    void addComment_shouldSetCreateTimeAndInsert() {
        // Arrange
        when(commentMapper.insert(any(TaskComment.class))).thenReturn(1);

        // Act
        TaskComment result = commentService.addComment(sampleComment);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCreateTime());
        verify(commentMapper).insert(sampleComment);
    }

    @Test
    @DisplayName("M4: 删除评论 - 只能删除自己的评论")
    void deleteComment_shouldOnlyDeleteOwnComment() {
        // Arrange
        sampleComment.setId(10L);
        sampleComment.setUserId(201L);
        when(commentMapper.selectById(10L)).thenReturn(sampleComment);

        // Act
        commentService.deleteComment(10L, 201L);

        // Assert
        verify(commentMapper).deleteById(10L);
    }

    @Test
    @DisplayName("M4: 删除别人评论 - 应抛出异常")
    void deleteComment_otherUser_shouldThrowException() {
        // Arrange
        sampleComment.setId(10L);
        sampleComment.setUserId(201L);
        when(commentMapper.selectById(10L)).thenReturn(sampleComment);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            commentService.deleteComment(10L, 999L);
        });

        verify(commentMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("M4: 评论计数 - 应正确返回数量")
    void countByTaskId_shouldReturnCorrectCount() {
        // Arrange
        when(commentMapper.selectCount(any())).thenReturn(5L);

        // Act
        long count = commentService.countByTaskId(1001L);

        // Assert
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("M4: 删除不存在的评论 - 应抛出异常")
    void deleteComment_notExist_shouldThrowException() {
        // Arrange
        when(commentMapper.selectById(99L)).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            commentService.deleteComment(99L, 201L);
        });
    }
}
