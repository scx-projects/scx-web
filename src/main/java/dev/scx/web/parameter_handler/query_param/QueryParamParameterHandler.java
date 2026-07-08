package dev.scx.web.parameter_handler.query_param;

import dev.scx.object.NodeToObjectException;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.ScxSerialize;
import dev.scx.serialize.SerializeConfig;
import dev.scx.web.ScxWeb;
import dev.scx.web.annotation.QueryParam;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;
import dev.scx.web.parameter_handler.exception.RequiredParamMissingException;

import static dev.scx.node.NullNode.NULL;

/// QueryParamParameterHandler
///
/// @author scx567888
public final class QueryParamParameterHandler implements ParameterHandler {

    private final QueryParam queryParam;
    private final ParameterInfo parameter;
    private final String paramName;

    public QueryParamParameterHandler(QueryParam queryParam, ParameterInfo parameter) {
        this.queryParam = queryParam;
        this.parameter = parameter;
        this.paramName = initParamName(queryParam, parameter);
    }

    private static String initParamName(QueryParam queryParam, ParameterInfo parameter) {
        var tempValue = parameter.name();
        if (queryParam.value().length > 0) {
            tempValue = queryParam.value()[0];
        }
        return tempValue;
    }

    @Override
    public Object handle(RequestInfo requestInfo, ScxWeb scxWeb) throws Exception {
        var node = requestInfo.query().get(paramName);
        // 这里我们暂时将其看作 NullNode.
        if (node == null) {
            node = NULL;
        }
        Object value;
        // 这里因为 query 本质上是 多值 map 这里额外开启 单值数组兼容.
        try {
            value = ScxSerialize.nodeToObject(node, parameter.parameterType(), SerializeConfig.copyOf(scxWeb.serializeConfig()).singleValueArrayCompatibility(true));
        } catch (NodeToObjectException e) {
            // 转换为 参数转换错误.
            throw new ParamConvertException("类型转换失败: @QueryParam " + paramName + " -> " + parameter.parameterType(), e);
        }
        if (value == null && queryParam.required()) {
            throw new RequiredParamMissingException("缺少必填参数: @QueryParam " + paramName);
        }
        return value;
    }

}
