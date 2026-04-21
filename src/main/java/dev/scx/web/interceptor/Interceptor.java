package dev.scx.web.interceptor;

import dev.scx.http.routing.RoutingContext;
import dev.scx.web.ScxWebRoute;

/// 拦截器
///
/// @author scx567888
/// @version 0.0.1
public interface Interceptor {

    /// 前置处理器 若要中断执行请在 handle 中抛出异常.
    default void preHandle(RoutingContext routingContext, ScxWebRoute scxWebRoute) throws Exception {

    }

    /// 注意 : 若处理器中的方法 返回值为 void (即无返回值) result 为 null.
    /// 后置处理器 路由处理器 执行完成之后 但是并没有将结果响应回客户端之前调用
    default Object postHandle(RoutingContext routingContext, ScxWebRoute scxWebRoute, Object result) throws Exception {
        return result;
    }

}
