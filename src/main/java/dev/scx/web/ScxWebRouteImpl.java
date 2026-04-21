package dev.scx.web;

import dev.scx.function.Function1Void;
import dev.scx.http.routing.RoutingContext;
import dev.scx.http.routing.method_matcher.MethodMatcher;
import dev.scx.http.routing.path_matcher.PathMatcher;
import dev.scx.http.routing.request_matcher.RequestMatcher;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.ParameterInfo;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;

import java.lang.reflect.InvocationTargetException;

import static dev.scx.web.ScxWeb.ROUTING_CONTEXT_SCOPED_VALUE;

/// ScxWebRouteImpl
///
/// @author scx567888
/// @version 0.0.1
final class ScxWebRouteImpl implements ScxWebRoute, Function1Void<RoutingContext, Throwable> {

    private final MethodInfo methodInfo;
    private final Object instance;
    private final ScxWeb scxWeb;
    private final RequestMatcher requestMatcher;
    private final PathMatcher pathMatcher;
    private final MethodMatcher methodMatcher;
    private final int priority;

    private final boolean isVoid;
    private final ParameterHandler[] parameterHandlers;

    ScxWebRouteImpl(MethodInfo methodInfo, Object instance, RequestMatcher requestMatcher, PathMatcher pathMatcher, MethodMatcher methodMatcher, int priority, ScxWeb scxWeb) {
        this.methodInfo = methodInfo;
        this.instance = instance;
        this.requestMatcher = requestMatcher;
        this.pathMatcher = pathMatcher;
        this.methodMatcher = methodMatcher;
        this.priority = priority;
        this.scxWeb = scxWeb;

        // 防止方法反射无法调用.
        this.methodInfo.setAccessible(true);
        // 是否无返回值.
        this.isVoid = this.methodInfo.returnType().rawClass() == void.class;
        // 初始化 参数处理器.
        this.parameterHandlers = buildParameterHandlers(this.scxWeb, this.methodInfo.parameters());
    }

    private static ParameterHandler[] buildParameterHandlers(ScxWeb scxWeb, ParameterInfo[] parameters) {
        var parameterHandlers = new ParameterHandler[parameters.length];
        for (int i = 0; i < parameters.length; i = i + 1) {
            parameterHandlers[i] = scxWeb.findParameterHandler(parameters[i]);
        }
        return parameterHandlers;
    }

    private static Object[] buildMethodParameters(ParameterHandler[] parameterHandlers, RoutingContext context) throws Exception {
        // 1, 获取 RequestInfo, 如果没有则创建 并 存储到 context.data 中. 这样同一个 RoutingContext 链上的 route 就可以共享 同一个 RequestInfo
        var requestInfo = (RequestInfo) context.data().computeIfAbsent("__" + context.hashCode() + "__", (_) -> new RequestInfo(context));
        // 2, 构建 方法参数.
        var methodParameter = new Object[parameterHandlers.length];

        for (int i = 0; i < methodParameter.length; i = i + 1) {
            methodParameter[i] = parameterHandlers[i].handle(requestInfo);
        }

        return methodParameter;
    }

    @Override
    public void apply(RoutingContext context) throws Throwable {
        ScopedValue.where(ROUTING_CONTEXT_SCOPED_VALUE, context).call(() -> {
            this.accept0(context);
            return null;
        });
    }

    private void accept0(RoutingContext context) throws Throwable {
        // 1, 执行前置处理器
        this.scxWeb.interceptor().preHandle(context, this);
        // 2, 根据 method 参数获取 invoke 时的参数
        var methodParameters = buildMethodParameters(parameterHandlers, context);
        // 3, 执行具体方法 (用来从请求中获取参数并执行反射调用方法以获取返回值)
        Object tempResult;
        try {
            tempResult = this.methodInfo.invoke(this.instance, methodParameters);
        } catch (InvocationTargetException e) {
            // 如果是反射调用时发生异常 则使用反射异常的内部异常
            throw e.getCause();
        }
        // 4, 执行后置处理器
        var finalResult = this.scxWeb.interceptor().postHandle(context, this, tempResult);
        // 5, 如果方法返回值不为 void 则调用返回值处理器
        if (!isVoid) {
            this.scxWeb.findReturnValueHandler(finalResult).handle(finalResult, context.request(), this.scxWeb);
        }
    }

    @Override
    public RequestMatcher requestMatcher() {
        return requestMatcher;
    }

    @Override
    public PathMatcher pathMatcher() {
        return pathMatcher;
    }

    @Override
    public MethodMatcher methodMatcher() {
        return methodMatcher;
    }

    @Override
    public Function1Void<RoutingContext, Throwable> handler() {
        return this;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public String toString() {
        return "ScxWebRouteImpl[" +
            "requestMatcher=" + requestMatcher +
            ", pathMatcher=" + pathMatcher +
            ", methodMatcher=" + methodMatcher +
            ", priority=" + priority +
            ", methodInfo=" + methodInfo +
            ']';
    }

}
