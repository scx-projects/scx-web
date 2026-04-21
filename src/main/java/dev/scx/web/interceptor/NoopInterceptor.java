package dev.scx.web.interceptor;

/// NoopInterceptor
///
/// @author scx567888
/// @version 0.0.1
public final class NoopInterceptor implements Interceptor {

    public static final NoopInterceptor NOOP_INTERCEPTOR = new NoopInterceptor();

    /// 私有构造函数 保证单例.
    private NoopInterceptor() {

    }

}
