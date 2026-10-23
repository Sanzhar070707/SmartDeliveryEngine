import exception.NoSuitableRouteException;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import planner.RoutePlanner;
import planner.StrategySelector;
import strategy.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoutePlannerTest {

    private List<Route> routes;
    private Location locA;
    private Location locB;

    @BeforeEach
    void setUp() {
        locA = new Location("Astana Hub", 51.09, 71.41);
        locB = new Location("Airport", 51.02, 71.46);

        routes = List.of(
                new Route("R1", locA, locB, 10.0, 15, 3000, 2.0, true, 0),
                new Route("R2", locA, locB, 25.0, 45, 1200, 4.0, true, 0),
                new Route("R3", locA, locB, 8.0, 20, 2000, 0.3, true, 0), // Экологичный и короткий маршрут
                new Route("R4", locA, locB, 18.0, 25, 2500, 1.5, true, 1),
                new Route("R-Blocked", locA, locB, 5.0, 5, 500, 0.1, false, 0)
        );
    }

    @Test
    void testFastestRouteStrategy() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.URGENT, 5000, 60, false);
        RouteDecision decision = planner.plan(routes, req);
        assertEquals("R1", decision.selectedRoute().id());
    }

    @Test
    void testCheapestRouteStrategy() {
        RoutePlanner planner = new RoutePlanner(new CheapestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.ECONOMY, 5000, 60, false);
        RouteDecision decision = planner.plan(routes, req);
        assertEquals("R2", decision.selectedRoute().id());
    }

    @Test
    void testEcoFriendlyRouteStrategy() {
        RoutePlanner planner = new RoutePlanner(new EcoFriendlyRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.ECO, 5000, 60, true);
        RouteDecision decision = planner.plan(routes, req);
        assertEquals("R3", decision.selectedRoute().id());
    }

    @Test
    void testBalancedRouteStrategy() {
        RoutePlanner planner = new RoutePlanner(new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2));
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.STANDARD, 5000, 60, false);
        RouteDecision decision = planner.plan(routes, req);
        assertNotNull(decision.selectedRoute());
    }

    @Test
    void testPremiumRouteStrategy() {
        RoutePlanner planner = new RoutePlanner(new PremiumRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.PREMIUM, 10000, 60, false);
        RouteDecision decision = planner.plan(routes, req);
        assertNotNull(decision.selectedRoute());
    }

    @Test
    void testInvalidBalancedWeights() {
        assertThrows(IllegalArgumentException.class, () -> new BalancedRouteStrategy(0.5, 0.5, 0.5, 0.5));
    }

    @Test
    void testRuntimeStrategySwitching() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.STANDARD, 5000, 60, false);

        assertEquals("R1", planner.plan(routes, req).selectedRoute().id());

        planner.setStrategy(new CheapestRouteStrategy());
        assertEquals("R2", planner.plan(routes, req).selectedRoute().id());

        planner.setStrategy(new EcoFriendlyRouteStrategy());
        assertEquals("R3", planner.plan(routes, req).selectedRoute().id());
    }

    @Test
    void testAutoStrategySelectorUrgent() {
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.URGENT, 5000, 60, false);
        RouteStrategy strategy = StrategySelector.selectStrategy(req);
        assertTrue(strategy instanceof FastestRouteStrategy);
    }

    @Test
    void testAutoStrategySelectorEco() {
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.ECO, 5000, 60, true);
        RouteStrategy strategy = StrategySelector.selectStrategy(req);
        assertTrue(strategy instanceof EcoFriendlyRouteStrategy);
    }

    @Test
    void testHardConstraintBudgetExceeded() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.URGENT, 500, 60, false);
        assertThrows(NoSuitableRouteException.class, () -> planner.plan(routes, req));
    }

    @Test
    void testHardConstraintDeadlineExceeded() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.URGENT, 5000, 5, false);
        assertThrows(NoSuitableRouteException.class, () -> planner.plan(routes, req));
    }

    @Test
    void testRouteUnavailableFilteredOut() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.URGENT, 5000, 60, false);
        RouteDecision decision = planner.plan(routes, req);
        assertNotEquals("R-Blocked", decision.selectedRoute().id());
    }

    @Test
    void testNoValidRouteEmptyList() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.URGENT, 5000, 60, false);
        assertThrows(NoSuitableRouteException.class, () -> planner.plan(List.of(), req));
    }

    @Test
    void testStrategyComparisonMode() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        DeliveryRequest req = new DeliveryRequest(DeliveryPriority.STANDARD, 5000, 60, false);

        List<RouteStrategy> strategies = List.of(
                new FastestRouteStrategy(),
                new CheapestRouteStrategy(),
                new EcoFriendlyRouteStrategy(),
                new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2)
        );

        for (RouteStrategy strat : strategies) {
            planner.setStrategy(strat);
            RouteDecision decision = planner.plan(routes, req);
            assertNotNull(decision.selectedRoute());
            assertNotNull(decision.strategyName());
        }
    }

    @Test
    void testNullRequestHandling() {
        RoutePlanner planner = new RoutePlanner(new FastestRouteStrategy());
        RouteDecision decision = planner.plan(routes, null);
        assertNotNull(decision.selectedRoute());
    }
}