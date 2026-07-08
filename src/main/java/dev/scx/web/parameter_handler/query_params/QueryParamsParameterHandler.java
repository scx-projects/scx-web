package dev.scx.web.parameter_handler.query_params;

import dev.scx.object.NodeToObjectException;
import dev.scx.reflect.ParameterInfo;
import dev.scx.serialize.ScxSerialize;
import dev.scx.serialize.SerializeConfig;
import dev.scx.web.ScxWeb;
import dev.scx.web.annotation.QueryParams;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;

/// QueryParamsParameterHandler
///
/// @author scx567888
public final class QueryParamsParameterHandler implements ParameterHandler {

    private final QueryParams queryParams;
    private final ParameterInfo parameter;

    public QueryParamsParameterHandler(QueryParams queryParams, ParameterInfo parameter) {
        this.queryParams = queryParams;
        this.parameter = parameter;
    }

    @Override
    public Object handle(RequestInfo requestInfo, ScxWeb scxWeb) throws Exception {
        var query = requestInfo.query();
        // 这里因为 query 本质上是 多值 map 这里额外开启 单值数组兼容.
        try {
            return ScxSerialize.nodeToObject(query, parameter.parameterType(), SerializeConfig.copyOf(scxWeb.serializeConfig()).singleValueArrayCompatibility(true));
        } catch (NodeToObjectException e) {
            // 转换为 参数转换错误.
            throw new ParamConvertException("类型转换失败: @QueryParams -> " + parameter.parameterType(), e);
        }
    }

}
