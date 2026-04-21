package dev.scx.web.parameter_handler;

/// 参数处理器
///
/// @author scx567888
/// @version 0.0.1
public interface ParameterHandler {

    /// 将结果处理并返回
    ///
    /// @param requestInfo 包装后的 RoutingContext
    /// @return 处理后的结果
    Object handle(RequestInfo requestInfo) throws Exception;

}
