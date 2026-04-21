package dev.scx.web.return_value_handler;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;

/// 空值处理器
///
/// @author scx567888
/// @version 0.0.1
public final class NullReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean canHandle(Object returnValue) {
        return returnValue == null;
    }

    @Override
    public void handle(Object returnValue, ScxHttpServerRequest request, ScxWeb scxWeb) {
        request.response().send();
    }

}
