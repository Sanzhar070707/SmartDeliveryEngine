package strategy;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PremiumRouteStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        if (routes == null || routes.isEmpty()) {
            throw new NoSuitableRouteException("Route list is empty or null.");
        }

        Map<String, String> rejectedRoutes = new HashMap<>();
        Route bestRoute = null;
        int minTime = Integer.MAX_VALUE;

        for (Route route : routes) {
            if (!route.isAvailable()) {
                rejectedRoutes.put(route.id(), "Route is unavailable.");
                continue;
            }
            if (route.trafficPenaltyMinutes() > 5) {
                rejectedRoutes.put(route.id(), "Traffic penalty too high.");
                continue;
            }

            if (route.getTotalMinutes() < minTime) {
                minTime = route.getTotalMinutes();
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("No premium route found.");
        }

        String explanation = String.format("Selected VIP premium route %s.", bestRoute.id());
        return new RouteDecision(bestRoute, getStrategyName(), explanation, rejectedRoutes);
    }

    @Override
    public String getStrategyName() {
        return "Premium Route Strategy";
    }
}