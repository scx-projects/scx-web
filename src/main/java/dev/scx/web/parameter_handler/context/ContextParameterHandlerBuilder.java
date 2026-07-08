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
import dev.scx.websocket.x.ScxWebSocketServerHandshakeRequest;
import dev.scx.websocket.x.ScxWebSocketServerHandshakeResponse;

/// 类型为 上下文 的参数处理器
///
/// @author scx567888
public final class ContextParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var rawClass = parameter.parameterType().rawClass();
        if (rawClass == RoutingContext.class) {
            return (requestInfo, _) -> requestInfo.routingContext();
        }
        if (rawClass == ScxHttpServerRequest.class) {
            return (requestInfo, _) -> requestInfo.routingContext().request();
        }
        if (rawClass == ScxHttpServerResponse.class) {
            return (requestInfo, _) -> requestInfo.routingContext().request().response();
        }
        if (rawClass == ScxWebSocketServerHandshakeRequest.class) {
            return (requestInfo, _) -> {
                var request = requestInfo.routingContext().request();
                if (request instanceof ScxWebSocketServerHandshakeRequest) {
                    return request;
                } else {
                    return null;
                }
            };
        }
        if (rawClass == ScxWebSocketServerHandshakeResponse.class) {
            return (requestInfo, _) -> {
                var response = requestInfo.routingContext().request().response();
                if (response instanceof ScxWebSocketServerHandshakeResponse) {
                    return response;
                } else {
                    return null;
                }
            };
        }
        if (rawClass == ScxHttpHeaders.class) {
            return (requestInfo, _) -> requestInfo.routingContext().request().headers();
        }
        if (rawClass == ByteInput.class) {
            return (requestInfo, _) -> requestInfo.routingContext().request().body();
        }
        if (rawClass == Cookies.class) {
            return (requestInfo, _) -> requestInfo.routingContext().request().cookies();
        }
        return null;
    }

}
