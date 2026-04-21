package dev.scx.web.parameter_handler.body;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.annotation.Body;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// BodyParameterHandlerBuilder
///
/// @author scx567888
/// @version 0.0.1
public final class BodyParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var body = parameter.findAnnotation(Body.class);
        if (body == null) {
            return null;
        }
        return new BodyParameterHandler(body, parameter);
    }

}
