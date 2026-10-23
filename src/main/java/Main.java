import model.*;
import planner.RoutePlanner;
import planner.StrategySelector;
import strategy.*;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Location origin = new Location("Astana Hub", 51.09, 71.41);
        Location dest = new Location("Airport", 51.02, 71.46);

        List<Route> routes = List.of(
                new Route("Route A (Express)", origin, dest, 18.0, 20, 5000, 4.5, true, 2),
                new Route("Route B (Cheap)", origin, dest, 22.0, 40, 1800, 3.2, true, 0),
                new Route("Route C (Eco)", origin, dest, 19.5, 30, 2500, 1.2, true, 0),
                new Route("Route D (Standard)", origin, dest, 20.0, 28, 2800, 2.5, true, 1),
                new Route("Route E (Blocked)", origin, dest, 15.0, 15, 6000, 5.0, false, 0),
                new Route("Route F (VIP)", origin, dest, 17.5, 18, 7000, 3.8, true, 0)
        );

        System.out.println("=== SMART DELIVERY & ROUTE OPTIMIZATION ENGINE ===");

        DeliveryRequest request = new DeliveryRequest(DeliveryPriority.URGENT, 10000, 25, false);

        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        System.out.println("\n[1] Fastest Strategy: " + planner.plan(routes, request).explanation());

        planner.setStrategy(new CheapestRouteStrategy());
        System.out.println("[2] Cheapest Strategy: " + planner.plan(routes, request).explanation());

        // Автоматический выбор
        RouteStrategy autoStrategy = StrategySelector.selectStrategy(request);
        planner.setStrategy(autoStrategy);
        System.out.println("[3] Auto-selected (" + autoStrategy.getStrategyName() + "): " + planner.plan(routes, request).explanation());
    }
}