package dev.scx.web.parameter_handler.body_field;

import dev.scx.node.Node;
import dev.scx.node.ObjectNode;
import dev.scx.object.NodeToObjectException;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.NodeToObjectOptions;
import dev.scx.serialize.ScxSerialize;
import dev.scx.web.annotation.BodyField;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;
import dev.scx.web.parameter_handler.exception.RequiredParamEmptyException;

import static dev.scx.node.NullNode.NULL;

/// BodyFieldParameterHandler
///
/// @author scx567888
/// @version 0.0.1
public final class BodyFieldParameterHandler implements ParameterHandler {

    private final BodyField bodyField;
    private final ParameterInfo parameter;
    private final String fieldName;

    public BodyFieldParameterHandler(BodyField bodyField, ParameterInfo parameter) {
        this.bodyField = bodyField;
        this.parameter = parameter;
        this.fieldName = initFieldName(bodyField, parameter);
    }

    private static String initFieldName(BodyField bodyField, ParameterInfo parameter) {
        var tempValue = parameter.name();
        if (bodyField.value().length > 0) {
            tempValue = bodyField.value()[0];
        }
        return tempValue;
    }

    @Override
    public Object handle(RequestInfo requestInfo) throws Exception {
        // 最终的 node
        Node node = null;

        var body = requestInfo.body();
        var bodySemantics = requestInfo.bodySemantics();

        //  只有能够成功从 ObjectNode 中取值 我们才赋值.
        if (body instanceof ObjectNode objectNode) {
            node = objectNode.get(fieldName);
        }

        // 这里我们暂时将其看作 NullNode.
        if (node == null) {
            node = NULL;
        }
        Object value;
        try {
            // 如果 body 来自 结构化数据 我们采用严格转换, 如果来自 表单类型数据 我们开启 单值数组兼容
            var options = switch (bodySemantics) {
                case STRUCTURED -> new NodeToObjectOptions();
                case FORM -> new NodeToObjectOptions().singleValueArrayCompatibility(true);
            };
            value = ScxSerialize.nodeToObject(node, parameter.parameterType(), options);
        } catch (NodeToObjectException e) {
            // 转换为 参数转换错误.
            throw new ParamConvertException("参数类型转换异常 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@BodyField] , 参数类型 [" + parameter.parameterType() + "] , 详细错误信息 : " + e.getMessage());
        }
        if (value == null && bodyField.required()) {
            throw new RequiredParamEmptyException("必填参数不能为空 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@BodyField] , 参数类型 [" + parameter.parameterType() + "]");
        }
        return value;
    }

}
