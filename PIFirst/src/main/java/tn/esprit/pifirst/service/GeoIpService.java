package tn.esprit.pifirst.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class GeoIpService {

    private static final String API_URL = "http://ip-api.com/json/";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Récupère les informations géographiques à partir d'une adresse IP
     * @param ip Adresse IP à géolocaliser
     * @return GeoInfo contenant pays, ville, coordonnées, FAI, ou null si erreur
     */
    public GeoInfo getGeoInfo(String ip) {
        // Ignorer les IP locales
        // Ignorer les IP locales
        if (ip.startsWith("127.0.0.1") || ip.startsWith("0:0:0:0") ||
                ip.startsWith("localhost") || ip.startsWith("::1")) {
            // Simuler un pays par défaut (France)
            GeoInfo fakeInfo = new GeoInfo();
            fakeInfo.setCountry("France");
            fakeInfo.setCountryCode("FR");
            fakeInfo.setCity("Paris");
            fakeInfo.setLat(48.8566);
            fakeInfo.setLon(2.3522);
            return fakeInfo;
        }
        try {
            URL url = new URL(API_URL + ip);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "MS4-Platform/1.0");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ Erreur API géolocalisation, code: " + responseCode);
                return null;
            }

            Scanner scanner = new Scanner(conn.getInputStream());
            String response = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            scanner.close();

            JsonNode json = mapper.readTree(response);

            if ("success".equals(json.get("status").asText())) {
                GeoInfo info = new GeoInfo();
                info.setCountry(json.get("country").asText());
                info.setCountryCode(json.get("countryCode").asText());
                info.setCity(json.get("city").asText());
                info.setLat(json.get("lat").asDouble());
                info.setLon(json.get("lon").asDouble());
                info.setIsp(json.get("isp").asText());
                info.setRegion(json.get("regionName").asText());
                info.setZip(json.get("zip").asText());

                System.out.println("✅ Géolocalisation réussie pour " + ip + " : " + info.getCity() + ", " + info.getCountry());
                return info;
            } else {
                System.err.println("❌ Échec géolocalisation pour " + ip + " : " + json.get("message").asText());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur géolocalisation IP " + ip + " : " + e.getMessage());
        }
        return null;
    }

    /**
     * Classe contenant les informations de géolocalisation
     */
    public static class GeoInfo {
        private String country;
        private String countryCode;
        private String city;
        private String region;
        private String zip;
        private double lat;
        private double lon;
        private String isp;

        // Constructeur par défaut
        public GeoInfo() {}

        // Getters et Setters
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public String getZip() { return zip; }
        public void setZip(String zip) { this.zip = zip; }

        public double getLat() { return lat; }
        public void setLat(double lat) { this.lat = lat; }

        public double getLon() { return lon; }
        public void setLon(double lon) { this.lon = lon; }

        public String getIsp() { return isp; }
        public void setIsp(String isp) { this.isp = isp; }

        @Override
        public String toString() {
            return "GeoInfo{" +
                    "country='" + country + '\'' +
                    ", city='" + city + '\'' +
                    ", lat=" + lat +
                    ", lon=" + lon +
                    ", isp='" + isp + '\'' +
                    '}';
        }
    }
}