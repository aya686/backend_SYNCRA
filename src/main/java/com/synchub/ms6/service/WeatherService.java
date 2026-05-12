package com.synchub.ms6.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@Slf4j
public class WeatherService {

    @Value("${openweather.api.key:}")
    private String apiKey;

    @Value("${openweather.enabled:false}")
    private boolean enabled;

    @Value("${openweather.mock:false}")
    private boolean mockMode; // Mode démo quand l'API n'est pas disponible

    private final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Récupère la météo actuelle pour une ville
     * @param city Nom de la ville (ex: "Tunis", "Paris")
     * @return WeatherData contenant température, conditions, etc.
     */
    public WeatherData getCurrentWeather(String city) {
        log.info("Weather service enabled: {}, API key present: {}", enabled, !apiKey.isEmpty());
        
        if (!enabled || apiKey.isEmpty()) {
            log.warn("Weather service is disabled or API key not configured");
            return null;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", "metric") // Celsius
                    .queryParam("lang", "fr") // Français
                    .toUriString();

            log.info("Fetching weather for city: {} from URL: {}", city, url.replace(apiKey, "***"));
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response == null) {
                log.error("OpenWeather API returned null response");
                return null;
            }
            
            if (response.containsKey("cod") && !response.get("cod").toString().equals("200")) {
                log.error("OpenWeather API error: {} - {}", response.get("cod"), response.get("message"));
                return null;
            }
            
            log.info("Weather API response: {}", response);
            return parseWeatherResponse(response);
        } catch (Exception e) {
            log.error("Failed to fetch weather for {}: {}", city, e.getMessage());
            
            // Mode démo : retourner des données simulées si l'API échoue
            if (mockMode) {
                log.info("Using MOCK weather data for {}", city);
                return generateMockWeatherData(city);
            }
            
            return null;
        }
    }

    /**
     * Génère des données météo simulées pour le développement
     */
    private WeatherData generateMockWeatherData(String city) {
        WeatherData data = new WeatherData();
        
        // Simuler des conditions différentes selon la ville
        String cityLower = city.toLowerCase();
        if (cityLower.contains("tunis") || cityLower.contains("sousse") || cityLower.contains("sfax")) {
            // Tunisie - Ensoleillé
            data.setTemperature(24.5);
            data.setCondition("Clear");
            data.setDescription("ciel dégagé");
            data.setHumidity(45);
            data.setWindSpeed(3.5);
            data.setIcon("01d");
        } else if (cityLower.contains("paris") || cityLower.contains("london")) {
            // Europe - Pluvieux
            data.setTemperature(12.0);
            data.setCondition("Rain");
            data.setDescription("pluie légère");
            data.setHumidity(78);
            data.setWindSpeed(5.2);
            data.setIcon("10d");
        } else {
            // Défaut - Partiellement nuageux
            data.setTemperature(20.0);
            data.setCondition("Clouds");
            data.setDescription("partiellement nuageux");
            data.setHumidity(60);
            data.setWindSpeed(4.0);
            data.setIcon("03d");
        }
        
        return data;
    }

    /**
     * Vérifie si les conditions météo sont favorables pour la livraison
     * @param city Nom de la ville
     * @return true si conditions favorables
     */
    public boolean isDeliveryConditionsGood(String city) {
        WeatherData weather = getCurrentWeather(city);
        if (weather == null) return true; // Par défaut, on assume OK

        // Conditions défavorables : pluie forte, neige, orage
        String condition = weather.getCondition().toLowerCase();
        return !(condition.contains("rain") || 
                 condition.contains("snow") || 
                 condition.contains("storm") ||
                 condition.contains("thunder") ||
                 weather.getWindSpeed() > 50); // Vent > 50 km/h
    }

    /**
     * Génère un message de conseil basé sur la météo
     */
    public String getDeliveryAdvice(String city) {
        WeatherData weather = getCurrentWeather(city);
        if (weather == null) return "Conditions météo non disponibles.";

        StringBuilder advice = new StringBuilder();
        advice.append("Météo à ").append(city).append(": ");
        advice.append(weather.getDescription()).append(", ");
        advice.append("Temp: ").append(weather.getTemperature()).append("°C. ");

        if (!isDeliveryConditionsGood(city)) {
            advice.append("⚠️ Conditions défavorables - risque de retard.");
        } else {
            advice.append("✅ Conditions favorables pour la livraison.");
        }

        return advice.toString();
    }

    @SuppressWarnings("unchecked")
    private WeatherData parseWeatherResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("main")) {
            return null;
        }

        WeatherData data = new WeatherData();
        
        Map<String, Object> main = (Map<String, Object>) response.get("main");
        data.setTemperature(((Number) main.get("temp")).doubleValue());
        data.setHumidity(((Number) main.get("humidity")).intValue());

        Map<String, Object> wind = (Map<String, Object>) response.get("wind");
        data.setWindSpeed(wind != null ? ((Number) wind.get("speed")).doubleValue() : 0);

        var weatherList = (java.util.List<Map<String, Object>>) response.get("weather");
        if (weatherList != null && !weatherList.isEmpty()) {
            Map<String, Object> weather = weatherList.get(0);
            data.setCondition((String) weather.get("main"));
            data.setDescription((String) weather.get("description"));
            data.setIcon((String) weather.get("icon"));
        }

        return data;
    }

    // Classe interne pour les données météo
    public static class WeatherData {
        private double temperature;
        private int humidity;
        private double windSpeed;
        private String condition; // Rain, Snow, Clear, etc.
        private String description; // Description en français
        private String icon; // Code icône OpenWeather

        // Getters et Setters
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        
        public int getHumidity() { return humidity; }
        public void setHumidity(int humidity) { this.humidity = humidity; }
        
        public double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }
        
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }
}
