package com.conference.seating.service.algorithm.impl;

import com.conference.seating.entity.SeatingSeat;
import com.conference.seating.entity.SeatingAttendee;
import com.conference.seating.service.algorithm.SeatingAlgorithm;

import java.util.*;

/**
 * 优先级排座算法
 * VIP和预留人员优先分配前排/中心座位，普通人员按序分配
 */
public class PriorityBasedAlgorithm implements SeatingAlgorithm {

    @Override
    public Map<Long, Long> execute(List<SeatingAttendee> attendees, List<SeatingSeat> seats) {
        Map<Long, Long> result = new LinkedHashMap<>();
        if (attendees == null || seats == null || attendees.isEmpty() || seats.isEmpty()) {
            return result;
        }

        // 按优先级排序：VIP > 预留 > 普通
        List<SeatingAttendee> sorted = new ArrayList<>(attendees);
        sorted.sort((a, b) -> {
            int scoreA = (Boolean.TRUE.equals(a.getIsVip()) ? 2 : 0) + (Boolean.TRUE.equals(a.getIsReserved()) ? 1 : 0);
            int scoreB = (Boolean.TRUE.equals(b.getIsVip()) ? 2 : 0) + (Boolean.TRUE.equals(b.getIsReserved()) ? 1 : 0);
            return Integer.compare(scoreB, scoreA);
        });

        // 座位按排号升序（前排优先）
        List<SeatingSeat> availableSeats = new ArrayList<>(seats);
        availableSeats.sort(Comparator.comparingInt(s -> s.getRowNum() != null ? s.getRowNum() : Integer.MAX_VALUE));

        Iterator<SeatingSeat> seatIt = availableSeats.iterator();
        for (SeatingAttendee attendee : sorted) {
            if (!seatIt.hasNext()) break;
            SeatingSeat seat = seatIt.next();
            result.put(attendee.getId(), seat.getId());
        }

        return result;
    }

    @Override
    public String getAlgorithmName() {
        return "priority";
    }

    @Override
    public String getDescription() {
        return "优先级排座：VIP和预留人员优先分配前排座位";
    }
}
