package com.conference.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 归档统计概览响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveStatsResponse implements Serializable {

    /** 课件资料数量 */
    private Integer courseware;

    /** 学员互动内容数量 */
    private Integer interaction;

    /** 业务数据类型数量 */
    private Integer businessData;

    /** 消息总条数 */
    private Integer messages;

    /** 是否允许学员上传 */
    private Boolean allowStudentUpload;

    /** 是否已打包下载 */
    private Boolean isPacked;
}
