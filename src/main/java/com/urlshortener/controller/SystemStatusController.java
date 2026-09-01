package com.urlshortener.controller;

import com.urlshortener.dto.RabbitMqStatusDto;
import com.urlshortener.dto.RedisStatusDto;
import com.urlshortener.dto.SystemStatusResponse;
import com.urlshortener.service.SystemMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/system")
@Tag(name = "Sistem ve Altyapı İzleme (Redis & RabbitMQ)", description = "Redis önbellek sunucusu durumu ve RabbitMQ kuyruk mesaj sayısı kontrol servisleri")
public class SystemStatusController {

    private final SystemMonitoringService systemMonitoringService;

    public SystemStatusController(SystemMonitoringService systemMonitoringService) {
        this.systemMonitoringService = systemMonitoringService;
    }

    @GetMapping("/status")
    @Operation(summary = "Genel Sistem ve Altyapı Durumu", description = "Redis ve RabbitMQ altyapı bileşenlerinin canlı durumunu ve RabbitMQ kuyruğundaki bekleyen mesaj sayısını döner.")
    @ApiResponse(responseCode = "200", description = "Sistem telemetri verileri getirildi")
    public ResponseEntity<SystemStatusResponse> getSystemStatus() {
        SystemStatusResponse status = systemMonitoringService.getSystemStatus();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/redis")
    @Operation(summary = "Redis Önbellek Durumu", description = "Redis bağlantı durumu, ping gecikmesi, önbellekteki anahtar sayısı ve bellek kullanımını döner.")
    @ApiResponse(responseCode = "200", description = "Redis durum bilgileri getirildi")
    public ResponseEntity<RedisStatusDto> getRedisStatus() {
        RedisStatusDto redisStatus = systemMonitoringService.getRedisStatus();
        return ResponseEntity.ok(redisStatus);
    }

    @GetMapping("/rabbitmq")
    @Operation(summary = "RabbitMQ ve Kuyruk Mesaj Durumu", description = "RabbitMQ bağlantı durumu ve url.click.queue kuyruğunda bekleyen anlık mesaj sayısını döner.")
    @ApiResponse(responseCode = "200", description = "RabbitMQ kuyruk durumu getirildi")
    public ResponseEntity<RabbitMqStatusDto> getRabbitMqStatus() {
        RabbitMqStatusDto rabbitMqStatus = systemMonitoringService.getRabbitMqStatus();
        return ResponseEntity.ok(rabbitMqStatus);
    }

    @PostMapping("/redis/flush-cache")
    @Operation(summary = "Redis URL Önbelleğini Temizle", description = "Redis önbelleğinde biriken kısa link kayıtlarını (short_url:*) temizler.")
    @ApiResponse(responseCode = "200", description = "Önbellek başarıyla temizlendi")
    public ResponseEntity<Map<String, Object>> flushRedisCache() {
        long deletedCount = systemMonitoringService.clearRedisCache();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("deletedKeysCount", deletedCount);
        response.put("message", deletedCount + " adet önbellek anahtarı temizlendi.");
        return ResponseEntity.ok(response);
    }
}
