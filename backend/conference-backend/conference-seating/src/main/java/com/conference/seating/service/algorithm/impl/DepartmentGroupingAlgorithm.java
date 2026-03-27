package com.conference.seating.service.algorithm.impl;

import com.conference.seating.entity.SeatingSeat;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.service.algorithm.SeatingAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门分组排座算法
 * 同部门人员安排在相邻座位，按部门分区就座
 */
public class DepartmentGroupingAlgorithm implements SeatingAlgorithm {

    @Override
    public Map<Long, Long> execute(List<SeatingAttendee> attendees, List<SeatingSeat> seats) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (attendees == null || seats == null || attendees.isEmpty() || seats.isEmpty()) {
            return result;
        }

        // 按部门分组
        Map<String, List<SeatingAttendee>> departmentGroups = attendees.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDepartment() != null ? a.getDepartment() : "未分组",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 座位按排号、列号排序
        List<SeatingSeat> sortedSeats = new ArrayList<>(seats);
        sortedSeats.sort((a, b) -> {
            int rowA = a.getRowNum() != null ? a.getRowNum() : Integer.MAX_VALUE;
            int rowB = b.getRowNum() != null ? b.getRowNum() : Integer.MAX_VALUE;
            if (rowA != rowB) return Integer.compare(rowA, rowB);
            int colA = a.getColumnNum() != null ? a.getColumnNum() : 0;
            int colB = b.getColumnNum() != null ? b.getColumnNum() : 0;
            return Integer.compare(colA, colB);
        });

        // 按部门顺序依次分配连续座位
        Iterator<SeatingSeat> seatIt = sortedSeats.iterator();
        for (Map.Entry<String, List<SeatingAttendee>> entry : departmentGroups.entrySet()) {
            for (SeatingAttendee attendee : entry.getValue()) {
                if (!seatIt.hasNext()) break;
                SeatingSeat seat = seatIt.next();
                result.put(attendee.getId(), seat.getId());
            }
        }

        return result;
    }

    @Override
    public String getAlgorithmName() {
        return "department_grouping";
    }

    @Override
    public String getDescription() {
        return "部门分组排座：同部门人员安排在相邻座位";
    }
}
