package dev.scx.web.return_value_handler;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;

/// String 类型处理器
///
/// @author scx567888
/// @version 0.0.1
public final class StringReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean canHandle(Object returnValue) {
        return returnValue instanceof String;
    }

    @Override
    public void handle(Object returnValue, ScxHttpServerRequest request, ScxWeb scxWeb) {
        if (!(returnValue instanceof String str)) {
            throw new IllegalArgumentException("参数不是 String 类型 !!! " + returnValue.getClass());
        }
        request.response().send(str);
    }

}
