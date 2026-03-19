package com.conference.seating.service.algorithm;

import com.conference.seating.entity.SeatingSeat;
import com.conference.seating.entity.SeatingAttendee;
import java.util.List;
import java.util.Map;

/**
 * 智能分配算法接口
 * @author AI
 * @date 2026-03-07
 */
public interface SeatingAlgorithm {
    /**
     * 执行座位分配
     * @param attendees 参会人员列表
     * @param seats 座位列表
     * @return 分配结果：key为attendeeId, value为seatId
     */
    Map<Long, Long> execute(List<SeatingAttendee> attendees, List<SeatingSeat> seats);

    /**
     * 获取算法名称
     */
    String getAlgorithmName();

    /**
     * 获取算法描述
     */
    String getDescription();
}

