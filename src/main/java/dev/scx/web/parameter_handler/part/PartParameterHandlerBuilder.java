package dev.scx.web.parameter_handler.part;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.annotation.Part;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// PartParameterHandlerBuilder
///
/// @author scx567888
/// @version 0.0.1
public final class PartParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var part = parameter.findAnnotation(Part.class);
        if (part == null) {
            return null;
        }
        return new PartParameterHandler(part, parameter);
    }

}
