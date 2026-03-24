package com.conference.seating.service.algorithm;

import com.conference.seating.service.algorithm.impl.DepartmentGroupingAlgorithm;
import com.conference.seating.service.algorithm.impl.PriorityBasedAlgorithm;
import com.conference.seating.service.algorithm.impl.VipOptimizationAlgorithm;

/**
 * 排座算法工厂
 * 根据算法类型名称创建对应的算法实例
 */
public class AlgorithmFactory {

    /**
     * 创建排座算法实例
     * @param type 算法类型：priority / vip_optimization / department_grouping
     * @return 对应的算法实例，默认返回优先级算法
     */
    public static SeatingAlgorithm createAlgorithm(String type) {
        if (type == null) {
            return new PriorityBasedAlgorithm();
        }
        return switch (type.toLowerCase()) {
            case "vip", "vip_optimization" -> new VipOptimizationAlgorithm();
            case "department", "department_grouping" -> new DepartmentGroupingAlgorithm();
            default -> new PriorityBasedAlgorithm();
        };
    }
}
