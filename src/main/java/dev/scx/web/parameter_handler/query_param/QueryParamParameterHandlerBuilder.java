package dev.scx.web.parameter_handler.query_param;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.annotation.QueryParam;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// QueryParamParameterHandlerBuilder
///
/// @author scx567888
/// @version 0.0.1
public final class QueryParamParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var queryParam = parameter.findAnnotation(QueryParam.class);
        if (queryParam == null) {
            return null;
        }
        return new QueryParamParameterHandler(queryParam, parameter);
    }

}
