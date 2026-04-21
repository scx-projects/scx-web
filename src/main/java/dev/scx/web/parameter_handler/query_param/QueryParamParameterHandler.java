package dev.scx.web.parameter_handler.query_param;

import dev.scx.object.NodeToObjectException;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.NodeToObjectOptions;
import dev.scx.serialize.ScxSerialize;
import dev.scx.web.annotation.QueryParam;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;
import dev.scx.web.parameter_handler.exception.RequiredParamEmptyException;

import static dev.scx.node.NullNode.NULL;

/// QueryParamParameterHandler
///
/// @author scx567888
/// @version 0.0.1
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
    public Object handle(RequestInfo requestInfo) throws Exception {
        var node = requestInfo.query().get(paramName);
        // 这里我们暂时将其看作 NullNode.
        if (node == null) {
            node = NULL;
        }
        Object value;
        // 这里因为 query 本质上是 多值 map 这里额外开启 单值数组兼容.
        try {
            value = ScxSerialize.nodeToObject(node, parameter.parameterType(), new NodeToObjectOptions().singleValueArrayCompatibility(true));
        } catch (NodeToObjectException e) {
            // 转换为 参数转换错误.
            throw new ParamConvertException("参数类型转换异常 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@QueryParam] , 参数类型 [" + parameter.parameterType() + "] , 详细错误信息 : " + e.getMessage());
        }
        if (value == null && queryParam.required()) {
            throw new RequiredParamEmptyException("必填参数不能为空 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@QueryParam] , 参数类型 [" + parameter.parameterType() + "]");
        }
        return value;
    }

}
