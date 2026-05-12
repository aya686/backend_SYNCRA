package tn.esprit.pifirst.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calcule la distance entre deux points géographiques en kilomètres
     * Formule de Haversine
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calcule la vitesse de déplacement en km/h
     */
    public double calculateSpeed(double distanceKm, double hoursDiff) {
        if (hoursDiff <= 0 || distanceKm <= 0) return 0;
        return distanceKm / hoursDiff;
    }
}