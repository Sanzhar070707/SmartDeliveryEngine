package planner;

import model.DeliveryPriority;
import model.DeliveryRequest;
import strategy.*;

public class StrategySelector {

    public static RouteStrategy selectStrategy(DeliveryRequest request) {
        if (request == null || request.priority() == null) {
            return new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);
        }

        if (request.ecoPreferred() || request.priority() == DeliveryPriority.ECO) {
            return new EcoFriendlyRouteStrategy();
        }

        return switch (request.priority()) {
            case URGENT -> new FastestRouteStrategy();
            case ECONOMY -> new CheapestRouteStrategy();
            case PREMIUM -> new PremiumRouteStrategy();
            default -> new BalancedRouteStrategy(0.4, 0.3, 0.1, 0.2);
        };
    }
}