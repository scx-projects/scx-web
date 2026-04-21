package dev.scx.web.parameter_handler.last;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// LastParameterHandler
///
/// @author scx567888
/// @version 0.0.1
public final class LastParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        throw new IllegalArgumentException(
            "无法确定参数来源: 参数 [" + parameter.name() + "] , 类型 [" + parameter.parameterType() + "] . " +
                "除上下文类型参数外, 请显式声明来源注解: @PathCapture / @QueryParam / @QueryParams / @Body / @BodyField / @Part"
        );
    }

}
