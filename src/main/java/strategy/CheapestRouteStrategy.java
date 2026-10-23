package strategy;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheapestRouteStrategy implements RouteStrategy {

    @Override
    public RouteDecision selectRoute(List<Route> routes, DeliveryRequest request) {
        if (routes == null || routes.isEmpty()) {
            throw new NoSuitableRouteException("Route list is empty or null.");
        }

        Map<String, String> rejectedRoutes = new HashMap<>();
        Route bestRoute = null;
        double minCost = Double.MAX_VALUE;

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

            if (route.cost() < minCost) {
                minCost = route.cost();
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("No route satisfies hard constraints for Cheapest Strategy.");
        }

        String explanation = String.format("Selected %s with lowest cost of %.2f.", bestRoute.id(), bestRoute.cost());
        return new RouteDecision(bestRoute, getStrategyName(), explanation, rejectedRoutes);
    }

    @Override
    public String getStrategyName() {
        return "Cheapest Route Strategy";
    }
}