package strategy;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EcoFriendlyRouteStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        if (routes == null || routes.isEmpty()) {
            throw new NoSuitableRouteException("Route list is empty or null.");
        }

        Map<String, String> rejectedRoutes = new HashMap<>();
        Route bestRoute = null;
        double minEcoScore = Double.MAX_VALUE;

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

            double ecoScore = route.carbonEmission() * 0.7 + route.distanceKm() * 0.3;
            if (ecoScore < minEcoScore) {
                minEcoScore = ecoScore;
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("No route satisfies hard constraints for Eco Strategy.");
        }

        String explanation = String.format("Selected %s with lowest CO2 emission of %.2f kg.", bestRoute.id(), bestRoute.carbonEmission());
        return new RouteDecision(bestRoute, getStrategyName(), explanation, rejectedRoutes);
    }

    @Override
    public String getStrategyName() {
        return "Eco-Friendly Route Strategy";
    }
}