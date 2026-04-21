package dev.scx.web.parameter_handler.path_capture;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.annotation.PathCapture;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// PathCaptureParameterHandlerBuilder
///
/// @author scx567888
/// @version 0.0.1
public final class PathCaptureParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var pathCapture = parameter.findAnnotation(PathCapture.class);
        if (pathCapture == null) {
            return null;
        }
        return new PathCaptureParameterHandler(pathCapture, parameter);
    }

}
