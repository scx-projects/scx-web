package dev.scx.web.parameter_handler.last;

import dev.scx.reflect.ParameterInfo;
import dev.scx.web.parameter_handler.ParameterHandler;
import dev.scx.web.parameter_handler.ParameterHandlerBuilder;

/// 最后的参数处理器构建器.
///
/// 这个 builder 不负责真正构建参数处理器, 而是作为参数解析链的最后一道失败边界.
/// 如果参数没有被前面的上下文参数处理器或显式来源注解处理, 就在这里直接报错.
///
/// ## 设计原则 : 参数来源必须显式
///
/// scx-web 不根据参数名, 参数类型, 请求内容, path template, query string 或 body 结构
/// 自动推断普通业务参数的来源.
///
/// 例如下面这些推断看起来都很方便:
///
/// ```java
/// public User get(String id) { ... }          // 自动从 path 里找 id?
/// public List<User> list(int page) { ... }   // 自动从 query 里找 page?
/// public User create(User user) { ... }      // 自动从 body 里读 user?
/// ```
///
/// 但这些规则并不稳定:
///
/// ```
/// String 可能来自 path, query, header, cookie, body field 或 multipart.
/// int 可能来自 path capture, query param, body field, 也可能只是业务默认值.
/// 对象类型可能来自 JSON body, form body, multipart part, 也可能是上下文对象.
/// 参数名可能因为编译参数, 混淆, 重构而不可用或发生变化.
/// 同一个名字可能同时出现在 path, query 和 body 中.
/// ```
///
/// 一旦允许自动推断, 参数绑定就会从 "当前参数显式声明来源"
/// 变成 "框架根据一组优先级猜测来源".
/// 这会引入新的心智模型:
///
/// ```
/// path 和 query 同名时谁优先?
/// query 和 body field 同名时谁优先?
/// String 默认来自哪里?
/// 对象默认来自 body 吗?
/// GET 请求能不能自动读 body?
/// multipart 下对象参数如何推断?
/// 参数改名是否会改变接口行为?
/// ```
///
/// 这些问题没有一个对所有场景都自然正确的答案.
/// 因此 scx-web 选择不做自动推断.
///
/// 除少数明确的上下文类型参数外, 普通业务参数必须通过注解显式声明来源:
///
/// ```
/// @PathCapture
/// @QueryParam
/// @QueryParams
/// @Body
/// @BodyField
/// @Part
/// ```
///
/// 这样可以保证参数绑定来源局部可见, 行为稳定, 重构安全, 也更容易 debug.
/// 所以不要把这里改成根据类型或参数名自动兜底推断来源.
///
/// @author scx567888
public final class LastParameterHandlerBuilder implements ParameterHandlerBuilder {

    @Override
    public ParameterHandler tryBuild(ParameterInfo parameter) {
        throw new IllegalArgumentException("无法确定参数来源: 参数 [" + parameter.name() + "] , 类型 [" + parameter.parameterType() + "].");
    }

}
