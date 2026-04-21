package dev.scx.web.parameter_handler.query_params;

import dev.scx.object.NodeToObjectException;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.NodeToObjectOptions;
import dev.scx.serialize.ScxSerialize;
import dev.scx.web.annotation.QueryParams;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;

/// QueryParamsParameterHandler
///
/// @author scx567888
/// @version 0.0.1
public final class QueryParamsParameterHandler implements ParameterHandler {

    private final QueryParams queryParams;
    private final ParameterInfo parameter;

    public QueryParamsParameterHandler(QueryParams queryParams, ParameterInfo parameter) {
        this.queryParams = queryParams;
        this.parameter = parameter;
    }

    @Override
    public Object handle(RequestInfo requestInfo) throws Exception {
        var query = requestInfo.query();
        // 这里因为 query 本质上是 多值 map 这里额外开启 单值数组兼容.
        try {
            return ScxSerialize.nodeToObject(query, parameter.parameterType(), new NodeToObjectOptions().singleValueArrayCompatibility(true));
        } catch (NodeToObjectException e) {
            // 转换为 参数转换错误.
            throw new ParamConvertException("参数类型转换异常 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@QueryParams] , 参数类型 [" + parameter.parameterType() + "] , 详细错误信息 : " + e.getMessage());
        }
    }

}
