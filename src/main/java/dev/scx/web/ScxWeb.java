package dev.scx.web;

import dev.scx.http.routing.RoutingContext;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.ToJsonOptions;
import dev.scx.serialize.ToXmlOptions;
import dev.scx.web.interceptor.Interceptor;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;
import dev.scx.web.parameter_handler.body.BodyParameterHandlerBuilder;
import dev.scx.web.parameter_handler.body_field.BodyFieldParameterHandlerBuilder;
import dev.scx.web.parameter_handler.context.ContextParameterHandlerBuilder;
import dev.scx.web.parameter_handler.last.LastParameterHandlerBuilder;
import dev.scx.web.parameter_handler.part.PartParameterHandlerBuilder;
import dev.scx.web.parameter_handler.path_capture.PathCaptureParameterHandlerBuilder;
import dev.scx.web.parameter_handler.query_param.QueryParamParameterHandlerBuilder;
import dev.scx.web.parameter_handler.query_params.QueryParamsParameterHandlerBuilder;
import dev.scx.web.return_value_handler.*;

import java.util.ArrayList;
import java.util.List;

import static dev.scx.web.interceptor.NoopInterceptor.NOOP_INTERCEPTOR;

/// ScxWeb
///
/// @author scx567888
/// @version 0.0.1
public final class ScxWeb {

    /// 路由上下文 THREAD_LOCAL
    static final ScopedValue<RoutingContext> ROUTING_CONTEXT_SCOPED_VALUE = ScopedValue.newInstance();

    private final List<ParameterHandlerBuilder> parameterHandlerBuilders;
    private final LastParameterHandlerBuilder lastParameterHandlerBuilder;
    private final List<ReturnValueHandler> returnValueHandlers;
    private final LastReturnValueHandler lastReturnValueHandler;
    private Interceptor interceptor;
    private ToJsonOptions toJsonOptions;
    private ToXmlOptions toXmlOptions;

    public ScxWeb() {
        this.parameterHandlerBuilders = new ArrayList<>();
        this.lastParameterHandlerBuilder = new LastParameterHandlerBuilder();
        this.returnValueHandlers = new ArrayList<>();
        this.lastReturnValueHandler = new LastReturnValueHandler();
        this.interceptor = NOOP_INTERCEPTOR;
        this.toJsonOptions = new ToJsonOptions();
        this.toXmlOptions = new ToXmlOptions();
        // 注册默认的返回值处理器
        addReturnValueHandler(new NullReturnValueHandler());
        addReturnValueHandler(new StringReturnValueHandler());
        addReturnValueHandler(new WebResultReturnValueHandler());
        // 注册默认的参数处理器
        addParameterHandlerBuilder(new ContextParameterHandlerBuilder());
        addParameterHandlerBuilder(new BodyParameterHandlerBuilder());
        addParameterHandlerBuilder(new BodyFieldParameterHandlerBuilder());
        addParameterHandlerBuilder(new QueryParamParameterHandlerBuilder());
        addParameterHandlerBuilder(new QueryParamsParameterHandlerBuilder());
        addParameterHandlerBuilder(new PathCaptureParameterHandlerBuilder());
        addParameterHandlerBuilder(new PartParameterHandlerBuilder());
    }

    /// 获取当前线程的 RoutingContext (只限在 scx mapping 注解的方法及其调用链上)
    ///
    /// @return 当前线程的 RoutingContext
    public static RoutingContext routingContext() {
        return ROUTING_CONTEXT_SCOPED_VALUE.get();
    }

    /// routes
    public List<ScxWebRoute> routes(Object... objects) {
        return new RouteCompiler(this).compile(objects);
    }

    // *************************** getter/setter *****************************

    public ScxWeb addParameterHandlerBuilder(ParameterHandlerBuilder handlerBuilder) {
        parameterHandlerBuilders.add(handlerBuilder);
        return this;
    }

    public ScxWeb addParameterHandlerBuilder(int index, ParameterHandlerBuilder handlerBuilder) {
        parameterHandlerBuilders.add(index, handlerBuilder);
        return this;
    }

    public ScxWeb addReturnValueHandler(ReturnValueHandler returnValueHandler) {
        returnValueHandlers.add(returnValueHandler);
        return this;
    }

    public ScxWeb addReturnValueHandler(int index, ReturnValueHandler returnValueHandler) {
        returnValueHandlers.add(index, returnValueHandler);
        return this;
    }

    public Interceptor interceptor() {
        return interceptor;
    }

    public ScxWeb interceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public ToJsonOptions toJsonOptions() {
        return toJsonOptions;
    }

    public ScxWeb toJsonOptions(ToJsonOptions toJsonOptions) {
        this.toJsonOptions = toJsonOptions;
        return this;
    }

    public ToXmlOptions toXmlOptions() {
        return toXmlOptions;
    }

    public ScxWeb toXmlOptions(ToXmlOptions toXmlOptions) {
        this.toXmlOptions = toXmlOptions;
        return this;
    }

    // *********************** 内部方法 *************************

    ParameterHandler findParameterHandler(ParameterInfo parameter) {
        for (var builder : parameterHandlerBuilders) {
            var parameterHandler = builder.tryBuild(parameter);
            if (parameterHandler != null) {
                return parameterHandler;
            }
        }
        return lastParameterHandlerBuilder.tryBuild(parameter);
    }

    ReturnValueHandler findReturnValueHandler(Object result) {
        for (var handler : returnValueHandlers) {
            if (handler.canHandle(result)) {
                return handler;
            }
        }
        return lastReturnValueHandler;
    }

}
