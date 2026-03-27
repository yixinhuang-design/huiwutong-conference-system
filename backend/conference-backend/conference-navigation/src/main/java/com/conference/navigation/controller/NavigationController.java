package com.conference.navigation.controller;

import com.conference.common.result.Result;
import com.conference.common.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 位置导航 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/navigation")
public class NavigationController {
    
    /**
     * 路径规划
     */
    @GetMapping("/route")
    public Result<Map<String, Object>> getRoute(
            @RequestParam Double startLat,
            @RequestParam Double startLng,
            @RequestParam Double endLat,
            @RequestParam Double endLng) {
        
        Long tenantId = TenantContextHolder.getTenantId();
        log.info("[租户{}] 规划路线: ({},{}) -> ({},{})", tenantId, startLat, startLng, endLat, endLng);
        
        Map<String, Object> route = new HashMap<>();
        route.put("tenantId", tenantId);
        route.put("distance", 500.0);
        route.put("duration", 600);
        route.put("steps", 10);
        return Result.ok(route);
    }
    
    /**
     * 获取AR导航数据
     */
    @GetMapping("/ar-data")
    public Result<Map<String, Object>> getArData(@RequestParam Long conferenceId) {
        Map<String, Object> arData = new HashMap<>();
        arData.put("arSupported", true);
        arData.put("mapUrl", "http://...");
        return Result.ok(arData);
    }
    
    /**
     * 室内定位
     */
    @PostMapping("/locate")
    public Result<Map<String, Object>> getIndoorLocation(
            @RequestParam Long conferenceId,
            @RequestParam String beaconSignal) {
        
        Map<String, Object> location = new HashMap<>();
        location.put("latitude", 39.9042);
        location.put("longitude", 116.4074);
        location.put("accuracy", 2.5);
        return Result.ok(location);
    }
}
