package dev.scx.web.parameter_handler.context;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.ScxHttpServerResponse;
import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.headers.cookie.Cookies;
import dev.scx.http.routing.RoutingContext;
import dev.scx.io.ByteInput;
import dev.scx.reflect.ParameterInfo;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.websocket.x.ScxServerWebSocketHandshakeRequest;
import dev.scx.websocket.x.ScxServerWebSocketHandshakeResponse;

/// 类型为 上下文 的参数处理器
///
/// @author scx567888
/// @version 0.0.1
public final class ContextParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var rawClass = parameter.parameterType().rawClass();
        if (rawClass == RoutingContext.class) {
            return RequestInfo::routingContext;
        }
        if (rawClass == ScxHttpServerRequest.class) {
            return (requestInfo) -> requestInfo.routingContext().request();
        }
        if (rawClass == ScxHttpServerResponse.class) {
            return (requestInfo) -> requestInfo.routingContext().request().response();
        }
        if (rawClass == ScxServerWebSocketHandshakeRequest.class) {
            return (requestInfo) -> {
                var request = requestInfo.routingContext().request();
                if (request instanceof ScxServerWebSocketHandshakeRequest) {
                    return request;
                } else {
                    return null;
                }
            };
        }
        if (rawClass == ScxServerWebSocketHandshakeResponse.class) {
            return (requestInfo) -> {
                var response = requestInfo.routingContext().request().response();
                if (response instanceof ScxServerWebSocketHandshakeResponse) {
                    return response;
                } else {
                    return null;
                }
            };
        }
        if (rawClass == ScxHttpHeaders.class) {
            return (requestInfo) -> requestInfo.routingContext().request().headers();
        }
        if (rawClass == ByteInput.class) {
            return (requestInfo) -> requestInfo.routingContext().request().body();
        }
        if (rawClass == Cookies.class) {
            return (requestInfo) -> requestInfo.routingContext().request().cookies();
        }
        return null;
    }

}
