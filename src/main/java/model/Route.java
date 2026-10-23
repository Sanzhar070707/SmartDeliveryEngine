package model;

public record Route(
        String id,
        Location origin,
        Location destination,
        double distanceKm,
        int estimatedMinutes,
        double cost,
        double carbonEmission,
        boolean isAvailable,
        int trafficPenaltyMinutes
) {
    public Route(String id, Location origin, Location destination, double distanceKm, int estimatedMinutes, double cost, double carbonEmission) {
        this(id, origin, destination, distanceKm, estimatedMinutes, cost, carbonEmission, true, 0);
    }

    public int getTotalMinutes() {
        return estimatedMinutes + trafficPenaltyMinutes;
    }
}