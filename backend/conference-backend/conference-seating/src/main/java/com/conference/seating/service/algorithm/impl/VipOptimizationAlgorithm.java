package com.conference.seating.service.algorithm.impl;

import com.conference.seating.entity.SeatingSeat;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.service.algorithm.SeatingAlgorithm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * VIP优化排座算法
 * VIP人员分配到前排中心区域，支持VIP专区隔离
 */
public class VipOptimizationAlgorithm implements SeatingAlgorithm {

    @Override
    public Map<Long, Long> execute(List<SeatingAttendee> attendees, List<SeatingSeat> seats) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (attendees == null || seats == null || attendees.isEmpty() || seats.isEmpty()) {
            return result;
        }

        // 分离VIP和普通参会者
        List<SeatingAttendee> vipList = attendees.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsVip()))
                .collect(Collectors.toList());
        List<SeatingAttendee> normalList = attendees.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIsVip()))
                .collect(Collectors.toList());

        // 座位按排号升序，同排按列号排序（中心位置优先）
        List<SeatingSeat> sortedSeats = new ArrayList<>(seats);
        sortedSeats.sort((a, b) -> {
            int rowA = a.getRowNum() != null ? a.getRowNum() : Integer.MAX_VALUE;
            int rowB = b.getRowNum() != null ? b.getRowNum() : Integer.MAX_VALUE;
            if (rowA != rowB) return Integer.compare(rowA, rowB);
            int colA = a.getColumnNum() != null ? a.getColumnNum() : 0;
            int colB = b.getColumnNum() != null ? b.getColumnNum() : 0;
            return Integer.compare(colA, colB);
        });

        // VIP先分配前排座位
        Iterator<SeatingSeat> seatIt = sortedSeats.iterator();
        Set<Long> usedSeats = new HashSet<>();

        for (SeatingAttendee vip : vipList) {
            if (!seatIt.hasNext()) break;
            SeatingSeat seat = seatIt.next();
            result.put(vip.getId(), seat.getId());
            usedSeats.add(seat.getId());
        }

        // 普通人员分配剩余座位
        for (SeatingAttendee normal : normalList) {
            if (!seatIt.hasNext()) break;
            SeatingSeat seat = seatIt.next();
            result.put(normal.getId(), seat.getId());
        }

        return result;
    }

    @Override
    public String getAlgorithmName() {
        return "vip_optimization";
    }

    @Override
    public String getDescription() {
        return "VIP优化排座：VIP人员优先分配前排中心座位";
    }
}
