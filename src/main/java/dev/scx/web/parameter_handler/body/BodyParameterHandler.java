package dev.scx.web.parameter_handler.body;

import dev.scx.object.NodeToObjectException;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.NodeToObjectOptions;
import dev.scx.serialize.ScxSerialize;
import dev.scx.web.annotation.Body;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;

import static dev.scx.node.NullNode.NULL;

/// BodyParameterHandler
///
/// @author scx567888
/// @version 0.0.1
public final class BodyParameterHandler implements ParameterHandler {

    private final Body body;
    private final ParameterInfo parameter;

    public BodyParameterHandler(Body body, ParameterInfo parameter) {
        this.body = body;
        this.parameter = parameter;
    }

    @Override
    public Object handle(RequestInfo requestInfo) throws Exception {
        var body = requestInfo.body();
        var bodySemantics = requestInfo.bodySemantics();

        // 这里我们暂时将其看作 NullNode.
        if (body == null) {
            body = NULL;
        }
        try {
            // 如果 body 来自 结构化数据 我们采用严格转换, 如果来自 表单类型数据 我们开启 单值数组兼容
            var options = switch (bodySemantics) {
                case STRUCTURED -> new NodeToObjectOptions();
                case FORM -> new NodeToObjectOptions().singleValueArrayCompatibility(true);
            };
            return ScxSerialize.nodeToObject(body, parameter.parameterType(), options);
        } catch (NodeToObjectException e) {
            // 转换为 参数转换错误.
            throw new ParamConvertException("参数类型转换异常 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@Body] , 参数类型 [" + parameter.parameterType() + "] , 详细错误信息 : " + e.getMessage());
        }
    }

}
