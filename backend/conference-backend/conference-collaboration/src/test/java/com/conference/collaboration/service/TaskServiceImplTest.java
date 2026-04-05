package com.conference.collaboration.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.conference.collaboration.entity.TaskAssignee;
import com.conference.collaboration.entity.TaskInfo;
import com.conference.collaboration.entity.TaskLog;
import com.conference.collaboration.mapper.TaskAssigneeMapper;
import com.conference.collaboration.mapper.TaskInfoMapper;
import com.conference.collaboration.mapper.TaskLogMapper;
import com.conference.collaboration.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TaskServiceImpl 单元测试
 * 覆盖：提交验证(M6+M7)、催办(S4)、取消通知、Haversine距离计算
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("任务服务实现测试")
class TaskServiceImplTest {

    @Mock
    private TaskInfoMapper taskMapper;

    @Mock
    private TaskAssigneeMapper assigneeMapper;

    @Mock
    private TaskLogMapper logMapper;

    @Mock
    private TaskNotificationService notificationService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskInfo sampleTask;
    private TaskAssignee sampleAssignee;

    @BeforeEach
    void setUp() {
        sampleTask = new TaskInfo();
        sampleTask.setId(1001L);
        sampleTask.setTaskName("场地巡检");
        sampleTask.setStatus("pending");
        sampleTask.setDeadline(LocalDateTime.of(2026, 4, 10, 18, 0));
        sampleTask.setCreatorId(100L);

        sampleAssignee = new TaskAssignee();
        sampleAssignee.setId(1L);
        sampleAssignee.setTaskId(1001L);
        sampleAssignee.setUserId(201L);
        sampleAssignee.setUserName("张三");
        sampleAssignee.setStatus("pending");
    }

    // ==================== Haversine距离测试（M6） ====================

    @Nested
    @DisplayName("M6: Haversine距离计算")
    class HaversineTests {

        private double invokeHaversine(double lat1, double lon1, double lat2, double lon2) throws Exception {
            Method method = TaskServiceImpl.class.getDeclaredMethod("haversineDistance",
                    double.class, double.class, double.class, double.class);
            method.setAccessible(true);
            return (double) method.invoke(taskService, lat1, lon1, lat2, lon2);
        }

        @Test
        @DisplayName("同一点距离应为0")
        void samePoint_shouldReturnZero() throws Exception {
            double distance = invokeHaversine(30.0, 120.0, 30.0, 120.0);
            assertEquals(0.0, distance, 0.001);
        }

        @Test
        @DisplayName("北京到上海约1068km")
        void beijing_shanghai_shouldBeAbout1068km() throws Exception {
            // 北京: 39.9042, 116.4074; 上海: 31.2304, 121.4737
            double distance = invokeHaversine(39.9042, 116.4074, 31.2304, 121.4737);
            // 实际距离约1068km，允许±5km误差
            assertTrue(distance > 1063000 && distance < 1073000,
                    "北京到上海距离应约1068km, 实际: " + (distance / 1000) + "km");
        }

        @Test
        @DisplayName("相距约111m的两点")
        void nearbyPoints_shouldBeAbout111m() throws Exception {
            // 纬度差约0.001度 ≈ 111米
            double distance = invokeHaversine(30.0, 120.0, 30.001, 120.0);
            assertTrue(distance > 100 && distance < 120,
                    "距离应约111m, 实际: " + distance + "m");
        }
    }

    // ==================== 提交验证测试（M6+M7） ====================

    @Nested
    @DisplayName("M6+M7: 提交验证")
    class SubmitValidationTests {

        @Test
        @DisplayName("M7: 文字长度不足应拒绝提交")
        void submit_textTooShort_shouldThrow() {
            // Arrange
            sampleTask.setCompletionMethod("text_image");
            sampleTask.setConfig("{\"minTextLength\":20, \"minImageCount\":0}");
            when(taskMapper.selectById(1001L)).thenReturn(sampleTask);
            when(assigneeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAssignee);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    taskService.submitTask(1001L, 201L, "太短", null, null));
            assertTrue(ex.getMessage().contains("20"));
        }

        @Test
        @DisplayName("M7: 图片数量不足应拒绝提交")
        void submit_imagesTooFew_shouldThrow() {
            // Arrange
            sampleTask.setCompletionMethod("text_image");
            sampleTask.setConfig("{\"minTextLength\":0, \"minImageCount\":3}");
            when(taskMapper.selectById(1001L)).thenReturn(sampleTask);
            when(assigneeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAssignee);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    taskService.submitTask(1001L, 201L, "内容足够了足够了足够了", "[\"img1.jpg\"]", null));
            assertTrue(ex.getMessage().contains("3"));
        }

        @Test
        @DisplayName("M6: 位置超出距离范围应拒绝提交")
        void submit_locationTooFar_shouldThrow() {
            // Arrange
            sampleTask.setCompletionMethod("location");
            sampleTask.setConfig("{\"targetLocation\":{\"lat\":30.0,\"lng\":120.0},\"maxDistance\":100}");
            when(taskMapper.selectById(1001L)).thenReturn(sampleTask);
            when(assigneeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAssignee);

            // 用户位置距离目标约1111m
            String farLocation = "{\"lat\":30.01,\"lng\":120.0}";

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    taskService.submitTask(1001L, 201L, "签到", null, farLocation));
            assertTrue(ex.getMessage().contains("超出范围"));
        }

        @Test
        @DisplayName("M6: 位置在范围内应通过提交")
        void submit_locationInRange_shouldPass() {
            // Arrange
            sampleTask.setCompletionMethod("location");
            sampleTask.setConfig("{\"targetLocation\":{\"lat\":30.0,\"lng\":120.0},\"maxDistance\":200}");
            when(taskMapper.selectById(1001L)).thenReturn(sampleTask);
            when(assigneeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAssignee);
            when(assigneeMapper.updateById(any())).thenReturn(1);
            when(logMapper.insert(any())).thenReturn(1);

            // 模拟updateTaskProgress需要的查询
            when(assigneeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleAssignee));

            // 用户位置距离目标约55m（纬度差0.0005 ≈ 55m）
            String nearLocation = "{\"lat\":30.0005,\"lng\":120.0}";

            // Act & Assert - should not throw
            assertDoesNotThrow(() ->
                    taskService.submitTask(1001L, 201L, "签到", null, nearLocation));
        }

        @Test
        @DisplayName("M7: 无config应直接通过")
        void submit_noConfig_shouldPass() {
            // Arrange
            sampleTask.setConfig(null);
            sampleTask.setCompletionMethod("text_image");
            when(taskMapper.selectById(1001L)).thenReturn(sampleTask);
            when(assigneeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleAssignee);
            when(assigneeMapper.updateById(any())).thenReturn(1);
            when(logMapper.insert(any())).thenReturn(1);
            when(assigneeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleAssignee));

            // Act & Assert
            assertDoesNotThrow(() ->
                    taskService.submitTask(1001L, 201L, "内容", null, null));
        }

        @Test
        @DisplayName("非执行人提交应拒绝")
        void submit_nonAssignee_shouldThrow() {
            // Arrange
            when(assigneeMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    taskService.submitTask(1001L, 999L, "内容", null, null));
            assertTrue(ex.getMessage().contains("执行人"));
        }
    }

    // ==================== 催办通知测试（S4） ====================

    @Nested
    @DisplayName("S4: 催办通知")
    class UrgeTests {

        @Test
        @DisplayName("催办应通知未完成执行人")
        void urge_shouldNotifyPendingAssignees() {
            // Arrange
            sampleTask.setStatus("in_progress");
            when(taskMapper.selectById(1001L)).thenReturn(sampleTask);

            TaskAssignee completed = new TaskAssignee();
            completed.setUserId(202L);
            completed.setStatus("completed");

            TaskAssignee pending = new TaskAssignee();
            pending.setUserId(201L);
            pending.setStatus("pending");

            when(assigneeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(completed, pending));
            when(logMapper.insert(any())).thenReturn(1);

            // Act
            taskService.urgeTask(1001L, 100L, "管理员");

            // Assert - 只催办未完成的
            verify(notificationService).notifyTaskUrge(eq(sampleTask), argThat(list ->
                    list.size() == 1 && list.get(0).getUserId().equals(201L)));
        }

        @Test
        @DisplayName("催办不存在的任务应抛异常")
        void urge_nonExistentTask_shouldThrow() {
            when(taskMapper.selectById(999L)).thenReturn(null);

            assertThrows(RuntimeException.class, () ->
                    taskService.urgeTask(999L, 100L, "管理员"));
        }
    }

    // ==================== 取消通知测试 ====================

    @Nested
    @DisplayName("任务取消")
    class CancelTests {

        @Test
        @DisplayName("取消应通知所有执行人")
        void cancel_shouldNotifyAllAssignees() {
            // Arrange
            when(taskMapper.selectById(1001L)).thenReturn(sampleTask);
            when(taskMapper.updateById(any())).thenReturn(1);
            when(logMapper.insert(any())).thenReturn(1);
            when(assigneeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(sampleAssignee));

            // Act
            taskService.cancelTask(1001L, 100L, "管理员", "会议延期");

            // Assert
            assertEquals("cancelled", sampleTask.getStatus());
            verify(notificationService).notifyTaskCancelled(eq(sampleTask), anyList(), eq("会议延期"));
        }
    }
}
