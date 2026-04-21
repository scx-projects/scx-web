package dev.scx.web.return_value_handler;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;
import dev.scx.web.result.WebResult;

/// WebResult 处理器
///
/// @author scx567888
/// @version 0.0.1
public final class WebResultReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean canHandle(Object returnValue) {
        return returnValue instanceof WebResult;
    }

    @Override
    public void handle(Object returnValue, ScxHttpServerRequest request, ScxWeb scxWeb) throws Exception {
        if (!(returnValue instanceof WebResult webResult)) {
            throw new IllegalArgumentException("参数不是 WebResult 类型 !!! " + returnValue.getClass());
        }
        webResult.apply(request, scxWeb);
    }

}
