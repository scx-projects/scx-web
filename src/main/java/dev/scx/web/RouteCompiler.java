package dev.scx.web;

import dev.scx.http.method.HttpMethod;
import dev.scx.http.routing.method_matcher.AnyMethodMatcher;
import dev.scx.http.routing.method_matcher.MethodMatcher;
import dev.scx.http.routing.method_matcher.MultiMethodMatcher;
import dev.scx.http.routing.path_matcher.AnyPathMatcher;
import dev.scx.http.routing.path_matcher.PathMatcher;
import dev.scx.http.routing.path_matcher.TemplatePathMatcher;
import dev.scx.http.routing.request_matcher.RequestMatcher;
import dev.scx.reflect.ArrayTypeInfo;
import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.PrimitiveTypeInfo;
import dev.scx.web.annotation.Route;
import dev.scx.web.annotation.Routes;
import dev.scx.websocket.x.ScxServerWebSocketHandshakeRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static dev.scx.http.method.HttpMethod.GET;
import static dev.scx.reflect.AccessModifier.PUBLIC;
import static dev.scx.reflect.ClassKind.ANNOTATION;
import static dev.scx.reflect.ClassKind.ENUM;
import static dev.scx.reflect.ScxReflect.typeOf;

/// RouteCompiler
///
/// @author scx567888
/// @version 0.0.1
final class RouteCompiler {

    private final ScxWeb scxWeb;

    public RouteCompiler(ScxWeb scxWeb) {
        this.scxWeb = scxWeb;
    }

    private static List<RouteClass> collectRouteClasses(Object... objects) {
        var result = new ArrayList<RouteClass>();
        for (var object : objects) {
            if (object == null) {
                continue;
            }
            var typeInfo = typeOf(object.getClass());
            if (typeInfo instanceof PrimitiveTypeInfo) {
                throw new IllegalArgumentException("原始类型不能作为路由注册类实例 : " + object.getClass());
            }
            if (typeInfo instanceof ArrayTypeInfo) {
                throw new IllegalArgumentException("数组不能作为路由注册类实例 : " + object.getClass());
            }

            var classInfo = (ClassInfo) typeInfo;

            if (classInfo.classKind() == ENUM) {
                throw new IllegalArgumentException("枚举类型不能作为路由注册类 : " + classInfo);
            }
            if (classInfo.classKind() == ANNOTATION) {
                throw new IllegalArgumentException("注解类型不能作为路由注册类 : " + classInfo);
            }
            var routes = classInfo.findAnnotation(Routes.class);
            if (routes == null) {
                throw new IllegalArgumentException("类未标注 @Routes，不能作为路由注册类 : " + classInfo);
            }
            result.add(new RouteClass(object, classInfo, routes));
        }
        return result;
    }

    private static List<RouteMethod> collectRouteMethods(List<RouteClass> routeClasses) {
        var result = new ArrayList<RouteMethod>();
        for (var routeClass : routeClasses) {
            // 只扫描当前类显式声明的方法
            var methods = routeClass.classInfo.methods();
            for (var method : methods) {
                var route = method.findAnnotation(Route.class);
                // 没有注解跳过
                if (route == null) {
                    continue;
                }
                if (method.accessModifier() != PUBLIC) {
                    throw new IllegalArgumentException("路由方法必须是 public : " + method);
                }
                if (method.isStatic()) {
                    throw new IllegalArgumentException("路由方法不能是 static : " + method);
                }
                result.add(new RouteMethod(routeClass.object, method, routeClass.routes, route));
            }
        }
        return result;
    }

    private static List<ScxWebRoute> compileRoutes(List<RouteMethod> routeMethods, ScxWeb scxWeb) {
        var result = new ArrayList<ScxWebRoute>();
        for (var routeMethod : routeMethods) {
            var methodInfo = routeMethod.methodInfo;
            var instance = routeMethod.object;

            var requestMatcher = buildRequestMatcher(routeMethod.route);
            var pathMatcher = buildPathMatcher(routeMethod.routes, routeMethod.route);
            var methodMatcher = buildMethodMatcher(routeMethod.route);
            var priority = routeMethod.route.priority();

            var scxWebRoute = new ScxWebRouteImpl(methodInfo, instance, requestMatcher, pathMatcher, methodMatcher, priority, scxWeb);
            result.add(scxWebRoute);
        }
        return result;
    }

    private static RequestMatcher buildRequestMatcher(Route route) {
        if (route.kind() == Route.RouteKind.WEBSOCKET_UPGRADE) {
            return RequestMatcher.typeIs(ScxServerWebSocketHandshakeRequest.class);
        } else {
            return RequestMatcher.typeNot(ScxServerWebSocketHandshakeRequest.class);
        }
    }

    private static PathMatcher buildPathMatcher(Routes routes, Route route) {
        String pathPrefix = "";
        if (routes.value().length > 0) {
            pathPrefix = routes.value()[0];
        }

        String path = "";
        if (route.value().length > 0) {
            path = route.value()[0];
        }
        // 绝对路径我们 将 pathPrefix 置空
        if (route.absolute()) {
            pathPrefix = "";
        }

        var normalizedParts = Arrays.stream((pathPrefix + "/" + path).split("/"))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();

        return normalizedParts.isEmpty() ?
            PathMatcher.any() :
            PathMatcher.ofTemplate("/" + String.join("/", normalizedParts));
    }

    private static MethodMatcher buildMethodMatcher(Route route) {
        // 去重.
        var methods = Arrays.stream(route.methods()).distinct().toArray(HttpMethod[]::new);
        // WebSocket 升级请求只允许 GET 或 空 限制.
        if (route.kind() == Route.RouteKind.WEBSOCKET_UPGRADE) {
            var ok = methods.length == 0 || (methods.length == 1 && methods[0] == GET);
            if (!ok) {
                throw new IllegalArgumentException("WEBSOCKET_UPGRADE route only allows explicit methods = {GET}, or leave methods empty");
            }
        }
        return methods.length == 0 ? MethodMatcher.any() : MethodMatcher.of(methods);
    }

    /// 排序 规则如下
    ///
    /// 1 若注解上标识了 priority 则按照注解上的 priority 进行排序 如下
    /// 0 > 5 > 13 > 199
    ///
    /// 2 如果根据路径是否为精确路径 进行排序 如下
    /// /api/user > /api/*
    ///
    /// 3 根据路径参数数量进行排序 (越少越靠前) 如下
    /// /api/user/list > /api/user/:m > /api/:u/:m/
    ///
    /// 4 根据方法来, (越具体越靠前).
    /// {GET} > {POST, GET} > ANY.
    private static List<ScxWebRoute> sortRoutes(List<ScxWebRoute> routes) {
        Comparator<ScxWebRoute> PRIORITY_COMPARATOR = Comparator.comparing(ScxWebRoute::priority);

        Comparator<ScxWebRoute> HAS_WILDCARD_COMPARATOR = Comparator.comparing(r -> {
            var p = r.pathMatcher();
            if (p instanceof AnyPathMatcher anyPathMatcher) {
                return 2;
            } else if (p instanceof TemplatePathMatcher templatePathMatcher) {
                return templatePathMatcher.hasWildcard() ? 1 : 0;
            }
            return 0;
        });

        Comparator<ScxWebRoute> PARAM_COUNT_COMPARATOR = Comparator.comparing(r -> {
            var p = r.pathMatcher();
            if (p instanceof AnyPathMatcher anyPathMatcher) {
                return Integer.MAX_VALUE;
            } else if (p instanceof TemplatePathMatcher templatePathMatcher) {
                return templatePathMatcher.paramCount();
            }
            return 0;
        });

        Comparator<ScxWebRoute> METHOD_SPECIFICITY_COMPARATOR = Comparator.comparingInt(r -> {
            var m = r.methodMatcher();
            if (m instanceof AnyMethodMatcher anyMethodMatcher) {
                return Integer.MAX_VALUE;
            } else if (m instanceof MultiMethodMatcher multiMethodMatcher) {
                return multiMethodMatcher.methods().size();
            }
            return 0;
        });

        return routes.stream().sorted(
            PRIORITY_COMPARATOR
                .thenComparing(HAS_WILDCARD_COMPARATOR)
                .thenComparing(PARAM_COUNT_COMPARATOR)
                .thenComparing(METHOD_SPECIFICITY_COMPARATOR)
        ).toList();
    }

    public List<ScxWebRoute> compile(Object... objects) {
        // 1, 收集路由类.
        var routeClasses = collectRouteClasses(objects);
        // 2, 收集路由方法.
        var routeMethods = collectRouteMethods(routeClasses);
        // 3, 编译为 ScxWebRoute.
        var routes = compileRoutes(routeMethods, scxWeb);
        // 4, 排序.
        return sortRoutes(routes);
    }

    private record RouteClass(Object object, ClassInfo classInfo, Routes routes) {

    }

    private record RouteMethod(Object object, MethodInfo methodInfo, Routes routes, Route route) {

    }

}
