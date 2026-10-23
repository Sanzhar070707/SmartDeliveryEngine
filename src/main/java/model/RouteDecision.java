package model;

import java.util.Map;

public record RouteDecision(
        Route selectedRoute,
        String strategyName,
        String explanation,
        Map<String, String> rejectedRoutesWithReasons
) {}