package dev.scx.web.parameter_handler;

import dev.scx.reflect.ParameterInfo;

/// 参数处理器构建器
///
/// @author scx567888
/// @version 0.0.1
public interface ParameterHandlerBuilder {

    /// 判断是否可以处理这个参数 返回 null 不支持
    ///
    /// @param parameter 参数实例
    /// @return 参数处理器 (为 null 表示不支持)
    ParameterHandler tryBuild(ParameterInfo parameter);

}
