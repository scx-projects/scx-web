package dev.scx.web.parameter_handler.part;

import dev.scx.http.media.multi_part.MultiPartPart;
import dev.scx.reflect.ParameterInfo;
import dev.scx.web.annotation.Part;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.RequestInfo;
import dev.scx.web.parameter_handler.exception.ParamConvertException;
import dev.scx.web.parameter_handler.exception.RequiredParamEmptyException;

/// PartParameterHandler
///
/// @author scx567888
/// @version 0.0.1
public final class PartParameterHandler implements ParameterHandler {

    private final Part part;
    private final ParameterInfo parameter;
    private final String partName;
    private final boolean isArray;

    public PartParameterHandler(Part part, ParameterInfo parameter) {
        this.part = part;
        this.parameter = parameter;
        this.partName = initPartName(part, parameter);
        this.isArray = initIsArray(parameter);
    }

    private static String initPartName(Part part, ParameterInfo parameter) {
        var tempValue = parameter.name();
        if (part.value().length > 0) {
            tempValue = part.value()[0];
        }
        return tempValue;
    }

    /// 此处会对参数类型进行校验.
    private static boolean initIsArray(ParameterInfo parameter) {
        var rawClass = parameter.parameterType().rawClass();
        if (rawClass == MultiPartPart.class) {
            return false;
        } else if (rawClass == MultiPartPart[].class) {
            return true;
        } else {
            throw new IllegalArgumentException("@Part 仅支持 MultiPartPart 或 MultiPartPart[] 参数类型, 当前参数 [" + parameter.name() + "] 的类型为 [" + parameter.parameterType() + "]");
        }
    }

    @Override
    public Object handle(RequestInfo requestInfo) throws ParamConvertException, RequiredParamEmptyException {
        var allParts = requestInfo.parts();
        // getAll 永远不会返回 null, 只可能是 [].
        var parts = allParts == null ?
            new MultiPartPart[0] :
            allParts.getAll(partName).toArray(MultiPartPart[]::new);

        if (isArray) {
            if (parts.length == 0 && part.required()) {
                throw new RequiredParamEmptyException("必填参数不能为空 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@Part] , 参数类型 [" + parameter.parameterType() + "]");
            }
            return parts;
        } else {
            if (parts.length == 0) {
                if (part.required()) {
                    throw new RequiredParamEmptyException("必填参数不能为空 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@Part] , 参数类型 [" + parameter.parameterType() + "]");
                }
                return null;
            } else if (parts.length == 1) {
                return parts[0];
            } else {
                // 多个 part
                throw new ParamConvertException("参数类型转换异常 !!! 参数名称 [" + parameter.name() + "] , 参数来源 [@Part] , 参数类型 [" + parameter.parameterType() + "] , 存在多个对应的 MultiPartPart");
            }
        }

    }

}
