package com.synchub.ms6.controller;

import com.synchub.ms6.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ms6/api/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Test simple: récupérer la météo d'une ville
     * GET /api/weather/test?city=Tunis
     */
    @GetMapping("/test")
    public ResponseEntity<?> testWeather(@RequestParam(defaultValue = "Tunis") String city) {
        log.info("Testing weather API for city: {}", city);
        
        WeatherService.WeatherData weather = weatherService.getCurrentWeather(city);
        
        if (weather == null) {
            return ResponseEntity.badRequest().body("Impossible de récupérer la météo pour: " + city);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("temperature", weather.getTemperature() + "°C");
        response.put("condition", weather.getCondition());
        response.put("description", weather.getDescription());
        response.put("humidity", weather.getHumidity() + "%");
        response.put("windSpeed", weather.getWindSpeed() + " m/s");
        response.put("icon", weather.getIcon());
        response.put("deliveryGood", weatherService.isDeliveryConditionsGood(city));
        response.put("advice", weatherService.getDeliveryAdvice(city));

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier si les conditions sont favorables pour la livraison
     * GET /api/weather/delivery-check?city=Paris
     */
    @GetMapping("/delivery-check")
    public ResponseEntity<?> checkDeliveryConditions(@RequestParam String city) {
        boolean isGood = weatherService.isDeliveryConditionsGood(city);
        String advice = weatherService.getDeliveryAdvice(city);
        
        Map<String, Object> response = new HashMap<>();
        response.put("city", city);
        response.put("deliveryPossible", isGood);
        response.put("message", advice);
        
        if (!isGood) {
            response.put("warning", "Conditions météo défavorables - livraison risquée");
        }
        
        return ResponseEntity.ok(response);
    }
}
