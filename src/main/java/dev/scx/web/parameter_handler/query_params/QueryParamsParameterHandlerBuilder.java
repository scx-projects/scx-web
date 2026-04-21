package dev.scx.web.parameter_handler.query_params;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.annotation.QueryParams;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// QueryParamsParameterHandlerBuilder
///
/// @author scx567888
/// @version 0.0.1
public final class QueryParamsParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        var queryParams = parameter.findAnnotation(QueryParams.class);
        if (queryParams == null) {
            return null;
        }
        return new QueryParamsParameterHandler(queryParams, parameter);
    }

}
