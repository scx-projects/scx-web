package dev.scx.web.parameter_handler.body_field;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.annotation.BodyField;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// BodyFieldParameterHandlerBuilder
///
/// @author scx567888
/// @version 0.0.1
public final class BodyFieldParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var bodyField = parameter.findAnnotation(BodyField.class);
        if (bodyField == null) {
            return null;
        }
        return new BodyFieldParameterHandler(bodyField, parameter);
    }

}
