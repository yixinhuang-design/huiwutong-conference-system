package com.conference.collaboration.controller;

import com.conference.collaboration.service.TaskExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 任务导出 Controller
 * @author AI Executive
 * @date 2026-04-01
 */
@Slf4j
@RestController
@RequestMapping("/api/task-export")
@RequiredArgsConstructor
public class TaskExportController {

    private final TaskExportService exportService;

    /**
     * 导出征集表单数据为Excel
     */
    @GetMapping("/collection/{taskId}/excel")
    public ResponseEntity<byte[]> exportCollectionToExcel(@PathVariable Long taskId) {
        try {
            byte[] excelData = exportService.exportCollectionToExcel(taskId);

            String filename = "征集数据_" + taskId + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (IOException e) {
            log.error("导出征集表单失败: taskId={}", taskId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出任务完成报告为Excel
     */
    @GetMapping("/report/{taskId}/excel")
    public ResponseEntity<byte[]> exportTaskReportToExcel(@PathVariable Long taskId) {
        try {
            byte[] excelData = exportService.exportTaskReportToExcel(taskId);

            String filename = "任务报告_" + taskId + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (IOException e) {
            log.error("导出任务报告失败: taskId={}", taskId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
