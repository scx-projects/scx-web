package dev.scx.web.return_value_handler;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;

/// 返回值处理器
///
/// @author scx567888
/// @version 0.0.1
public interface ReturnValueHandler {

    /// 是否能够处理
    boolean canHandle(Object returnValue);

    /// 处理结果
    void handle(Object returnValue, ScxHttpServerRequest request, ScxWeb scxWeb) throws Exception;

}
