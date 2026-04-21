package dev.scx.web.parameter_handler.path_capture;

import dev.scx.object.NodeToObjectException;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.ScxSerialize;
import dev.scx.web.annotation.PathCapture;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;

import static dev.scx.node.NullNode.NULL;

/// PathCaptureParameterHandler
///
/// @author scx567888
/// @version 0.0.1
public final class PathCaptureParameterHandler implements ParameterHandler {

    private final PathCapture pathCapture;
    private final ParameterInfo parameter;
    private final String captureName;

    public PathCaptureParameterHandler(PathCapture pathCapture, ParameterInfo parameter) {
        this.pathCapture = pathCapture;
        this.parameter = parameter;
        this.captureName = initCaptureName(pathCapture, parameter);
    }

    private static String initCaptureName(PathCapture pathCapture, ParameterInfo parameter) {
        var tempValue = parameter.name();
        if (pathCapture.value().length > 0) {
            tempValue = pathCapture.value()[0];
        }
        return tempValue;
    }

    @Override
    public Object handle(RequestInfo requestInfo) throws Exception {
        var node = requestInfo.pathCaptures().get(captureName);
        // 这里我们暂时将其看作 NullNode.
        if (node == null) {
            node = NULL;
        }
        try {
            return ScxSerialize.nodeToObject(node, parameter.parameterType());
        } catch (NodeToObjectException e) {
            // 转换为 参数转换错误.
            throw new ParamConvertException("参数类型转换异常 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@PathCapture] , 参数类型 [" + parameter.parameterType() + "] , 详细错误信息 : " + e.getMessage());
        }

    }

}
