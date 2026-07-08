package dev.scx.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 类级路由注解.
///
/// 标记一个可注册的路由类, 并为其中的方法级 [Route] 提供模板前缀.
///
/// @author scx567888
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Routes {

    /// 类级路由模板前缀.
    ///
    /// 默认情况下, 该模板会与方法级 [Route] 模板按字面拼接.
    ///
    /// 语法参考 [dev.scx.http.routing.path_matcher.TemplatePathMatcher].
    ///
    /// 为空时, 表示当前类不声明类级路径模板前缀.
    /// 注意: 空值不表示根路径 `/`.
    String[] value() default {};

}
