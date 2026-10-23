package strategy;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalancedRouteStrategy implements RouteStrategy {

    private final double timeWeight;
    private final double costWeight;
    private final double distanceWeight;
    private final double emissionWeight;

    public BalancedRouteStrategy(double timeWeight, double costWeight, double distanceWeight, double emissionWeight) {
        double sum = timeWeight + costWeight + distanceWeight + emissionWeight;
        if (Math.abs(sum - 1.0) > 0.001) {
            throw new IllegalArgumentException("Sum of weights must equal 1.0. Current sum: " + sum);
        }
        this.timeWeight = timeWeight;
        this.costWeight = costWeight;
        this.distanceWeight = distanceWeight;
        this.emissionWeight = emissionWeight;
    }

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        if (routes == null || routes.isEmpty()) {
            throw new NoSuitableRouteException("Route list is empty or null.");
        }

        Map<String, String> rejectedRoutes = new HashMap<>();
        Route bestRoute = null;
        double minScore = Double.MAX_VALUE;

        for (Route route : routes) {
            if (!route.isAvailable()) {
                rejectedRoutes.put(route.id(), "Route is unavailable.");
                continue;
            }
            if (request != null && route.cost() > request.maxBudget()) {
                rejectedRoutes.put(route.id(), "Cost exceeds budget.");
                continue;
            }
            if (request != null && request.maxDeliveryMinutes() > 0 && route.getTotalMinutes() > request.maxDeliveryMinutes()) {
                rejectedRoutes.put(route.id(), "Time exceeds deadline.");
                continue;
            }

            double score = (route.getTotalMinutes() * timeWeight)
                    + (route.cost() * costWeight)
                    + (route.distanceKm() * distanceWeight)
                    + (route.carbonEmission() * emissionWeight);

            if (score < minScore) {
                minScore = score;
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("No route satisfies hard constraints for Balanced Strategy.");
        }

        String explanation = String.format("Selected %s with balanced score of %.2f.", bestRoute.id(), minScore);
        return new RouteDecision(bestRoute, getStrategyName(), explanation, rejectedRoutes);
    }

    @Override
    public String getStrategyName() {
        return "Balanced Route Strategy";
    }
}