package dev.scx.web;

import dev.scx.http.routing.Route;

/// ScxWebRoute
///
/// @author scx567888
/// @version 0.0.1
public sealed interface ScxWebRoute extends Route permits ScxWebRouteImpl {

    int priority();

}
