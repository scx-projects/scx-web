package dev.scx.web;

import dev.scx.http.routing.Route;
import dev.scx.reflect.MethodInfo;

/// ScxWebRoute
///
/// @author scx567888
public sealed interface ScxWebRoute extends Route permits ScxWebRouteImpl {

    int priority();

    /// 返回用于编译此路由的处理方法信息.
    ///
    /// ScxWeb 只会编译路由类中直接声明的方法,
    /// 因此 `methodInfo().declaringClass()` 就是声明该路由方法的类,
    /// 无需单独暴露 classInfo.
    MethodInfo methodInfo();

}
