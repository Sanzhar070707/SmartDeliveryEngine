package strategy;

import exception.NoSuitableRouteException;
import model.DeliveryRequest;
import model.Route;
import model.RouteDecision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastestRouteStrategy implements RouteStrategy {

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
            if (request != null && route.cost() > request.maxBudget()) {
                rejectedRoutes.put(route.id(), "Cost exceeds budget.");
                continue;
            }
            int totalTime = route.getTotalMinutes();
            if (request != null && request.maxDeliveryMinutes() > 0 && totalTime > request.maxDeliveryMinutes()) {
                rejectedRoutes.put(route.id(), "Time exceeds deadline.");
                continue;
            }

            if (totalTime < minTime) {
                minTime = totalTime;
                bestRoute = route;
            }
        }

        if (bestRoute == null) {
            throw new NoSuitableRouteException("No route satisfies hard constraints for Fastest Strategy.");
        }

        String explanation = String.format("Selected %s with minimum delivery time of %d mins.", bestRoute.id(), bestRoute.getTotalMinutes());
        return new RouteDecision(bestRoute, getStrategyName(), explanation, rejectedRoutes);
    }

    @Override
    public String getStrategyName() {
        return "Fastest Route Strategy";
    }
}