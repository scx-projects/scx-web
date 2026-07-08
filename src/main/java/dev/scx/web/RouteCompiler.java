package dev.scx.web;

import dev.scx.http.method.HttpMethod;
import dev.scx.http.routing.method_matcher.AnyMethodMatcher;
import dev.scx.http.routing.method_matcher.MethodMatcher;
import dev.scx.http.routing.method_matcher.MultiMethodMatcher;
import dev.scx.http.routing.path_matcher.AnyPathMatcher;
import dev.scx.http.routing.path_matcher.PathMatcher;
import dev.scx.http.routing.path_matcher.TemplatePathMatcher;
import dev.scx.http.routing.request_matcher.RequestMatcher;
import dev.scx.reflect.ArrayTypeInfo;
import dev.scx.reflect.ClassInfo;
import dev.scx.reflect.MethodInfo;
import dev.scx.reflect.PrimitiveTypeInfo;
import dev.scx.web.annotation.Route;
import dev.scx.web.annotation.Routes;
import dev.scx.websocket.x.ScxWebSocketServerHandshakeRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static dev.scx.reflect.AccessModifier.PUBLIC;
import static dev.scx.reflect.ClassKind.ANNOTATION;
import static dev.scx.reflect.ClassKind.ENUM;
import static dev.scx.reflect.ScxReflect.typeOf;

/// 路由编译器.
///
/// 注意: 这里拒绝的不是某个具体实现方案, 而是整个 "路由继承" 概念.
///
/// 父类 route inheritance 看起来像是 Java 继承的自然延伸,
/// 但 HTTP endpoint 并不是单个方法注解, 而是一组开放的行为元数据.
/// 这组元数据没有通用的继承代数.
/// 因此, route inheritance 无法作为一个简单、正交、可预测的基础能力加入 scx-web.
///
/// ## 设计原则 : 路由只属于声明它的类
///
/// scx-web 使用 "局部路由声明" 模型.
/// 也就是说, 只有注册类自身直接声明的 [@Route][dev.scx.web.annotation.Route]
/// 方法才会被编译成 HTTP 路由.
///
/// 继承来的方法即使带有 `@Route`, 也不会因为子类被注册而自动成为子类的路由.
/// 父类仍然可以用于共享普通 Java 逻辑, 例如 helper 方法, service 字段, 通用返回封装,
/// 权限检查方法, 分页方法等. 但是 scx-web 不把 Java 继承解释为 HTTP API 继承.
///
/// ## 这不是技术限制, 而是有意的设计取舍
///
/// 从 Java 语言角度看, 子类确实 "拥有" 父类的 public/protected 方法.
/// 例如 `new Child().foo()` 可以调用继承自 `Parent` 的 `foo()`.
/// 但是 HTTP endpoint 是对外暴露的 API 边界,
/// 它不应该简单等同于 "这个对象能调用哪些方法".
///
/// scx-web 在这里选择的语义是:
///
/// ```
/// 可调用的 Java 方法 != 对外暴露的 HTTP endpoint
/// ```
///
/// 一个 controller 暴露什么 HTTP API, 应该由这个 controller 自己显式声明.
/// 这样读代码时只需要看当前 controller, 就能知道它的 HTTP 暴露面.
///
/// ## 为什么不扫描父类上的 @Route?
///
/// 表面上看, 扫描父类上的 `@Route` 可以减少一些重复代码,
/// 尤其是 CRUD 风格的 BaseController:
///
/// ```java
/// abstract class BaseCrudController<T> {
///
///     @Route("list")
///     public List<T> list() {
///         ...
///     }
///
///     @Route("get/:id")
///     public T get(@PathCapture("id") String id) {
///         ...
///     }
///
/// }
/// ```
///
/// ```java
/// @Routes("/users")
/// class UserController extends BaseCrudController<User> {
///
/// }
/// ```
///
/// 这确实很方便. 但是一旦支持这种行为, 父类就不再只是普通的实现复用,
/// 而会变成 HTTP API 的传播源.
/// 父类新增一个 `@Route` 方法, 可能意味着所有子类都同时新增一个对外 endpoint:
///
/// ```java
/// abstract class BaseCrudController<T> {
///
///     @Route("export")
///     public Binary export() {
///         ...
///     }
///
/// }
/// ```
///
/// ```java
/// @Routes("/api/user")
/// class UserController extends BaseCrudController<User> {
///
/// }
/// ```
///
/// ```java
/// @Routes("/api/order")
/// class OrderController extends BaseCrudController<Order> {
///
/// }
/// ```
///
/// 如果继承路由是默认行为, 上面的改动可能同时暴露:
///
/// ```
/// /api/user/export
/// /api/order/export
/// ```
///
/// 这会让一次普通的父类修改变成多个 controller 的对外 API 变更.
/// 对 Web 框架来说, "少暴露" 通常比 "意外多暴露" 更安全.
///
/// ## 重写方法会让问题变得更复杂
///
/// 一旦支持继承路由, 就必须定义子类重写父类路由方法时的语义:
///
/// ```java
/// class BaseController {
///
///     @Route("hello")
///     public String hello() {
///         return "base";
///     }
///
/// }
/// ```
///
/// ```java
/// @Routes("/api")
/// class ApiController extends BaseController {
///
///     @Override
///     public String hello() {
///         return "child";
///     }
///
/// }
/// ```
///
/// 这里 `/api/hello` 到底是否存在?
///
/// 可能的规则有两种, 但都有明显代价.
///
/// ### 方案一 : 子类重写但不写 @Route, 仍然继承父类路由
///
/// 这个模型把父类上的 `@Route` 视为一种 HTTP contract.
/// 子类重写方法只是替换实现, 不改变 HTTP 契约.
///
/// 这个模型对 BaseCrudController 很友好,
/// 但是马上会遇到 "如何隐藏继承来的路由" 的问题:
///
/// ```java
/// abstract class BaseCrudController<T> {
///
///     @Route("delete/:id")
///     public void delete(@PathCapture("id") String id) {
///         ...
///     }
///
/// }
/// ```
///
/// ```java
/// @Routes("/api/user")
/// class UserController extends BaseCrudController<User> {
///     // User 不希望暴露 delete, 应该怎么取消?
/// }
/// ```
///
/// 如果重写但不写 `@Route` 仍然继承父类路由,
/// 那就必须引入新的抑制机制, 例如 `@NoRoute`, `@IgnoreRoute`,
/// 或 `@Route(enabled = false)`.
/// 这会让框架多出一套 "路由取消" 语义.
///
/// ### 方案二 : 子类重写但不写 @Route, 视为取消父类路由
///
/// 这个模型不需要 `@NoRoute`.
/// 子类重写后, 是否暴露完全看子类方法自己有没有 `@Route`.
///
/// 但是这会削弱 BaseController 的核心价值.
/// 用户只是想改实现时, 也必须重复写 `@Route` 以及所有参数绑定注解:
///
/// ```java
/// abstract class BaseCrudController<T> {
///
///     @Route("get/:id")
///     public T get(@PathCapture("id") String id) {
///         ...
///     }
///
/// }
/// ```
///
/// ```java
/// @Routes("/api/user")
/// class UserController extends BaseCrudController<User> {
///
///     @Override
///     @Route("get/:id")
///     public User get(@PathCapture("id") String id) {
///         ...
///     }
///
/// }
/// ```
///
/// 这样既没有完全的所见即所得, 也没有真正复用 HTTP endpoint 声明.
/// 它属于两边都不够干净的折中.
///
/// ## 参数绑定注解会进一步扩大复杂度
///
/// 路由继承不只是 `@Route` 一个注解的问题.
/// 方法参数上的绑定注解也必须有继承规则:
///
/// ```java
/// abstract class BaseController {
///
///     @Route("get/:id")
///     public Object get(@PathCapture("id") String id) {
///         ...
///     }
///
/// }
/// ```
///
/// ```java
/// @Routes("/users")
/// class UserController extends BaseController {
///
///     @Override
///     public Object get(String userId) {
///         ...
///     }
///
/// }
/// ```
///
/// 如果 `@Route` 被继承, 那 `@PathCapture("id")` 是否也继承?
/// 如果继承, 是按参数名继承, 还是按参数位置继承?
/// 子类参数名从 `id` 改成 `userId` 后, 绑定规则是否仍然来自父类?
/// 如果子类方法注解改成 `@Route("get/:uid")`, 命名捕获规则怎么规定?
///
/// 这不是简单地 "多继承几个注解" 就能解决的问题.
/// 如果选择按参数位置继承, 那只是人为选定了一种规则,
/// 并不代表这个规则天然正确.
/// 它只在某些场景下看起来合理,
/// 但一旦子类改变路径模板, 改变参数含义, 调整参数顺序,
/// 或引入新的绑定注解, 这个规则就会立刻暴露出非正交性.
///
/// 换句话说, 不存在一个对所有方法签名变化都自然成立的参数注解继承规则.
/// 所谓 "完整继承", 只是把某一组具体规则固定下来,
/// 而不是从概念上真正自洽.
///
/// ## 权限, 校验, 文档等元数据会让继承规则继续外溢
///
/// 实际项目中, endpoint 上通常不只有路由和参数绑定,
/// 还会有权限, 认证, 校验, 限流, 缓存, 审计, 文档等元数据:
///
/// ```java
/// abstract class BaseController {
///
///     @Route("delete/:id")
///     @RequireRole("admin")
///     public void delete(@PathCapture("id") String id) {
///         ...
///     }
///
/// }
/// ```
///
/// ```java
/// @Routes("/users")
/// class UserController extends BaseController {
///
///     @Override
///     public void delete(String userId) {
///         ...
///     }
///
/// }
/// ```
///
/// 这里 `@RequireRole("admin")` 是否继承?
///
/// 如果继承, 那么 `UserController.delete()` 明面上没有权限注解,
/// 但实际仍然需要 admin, 这会降低局部可读性.
///
/// 如果不继承, 那么父类原本受保护的 endpoint 在子类重写后可能失去保护,
/// 这对安全语义非常危险.
///
/// 如果子类又声明了自己的权限:
///
/// ```java
/// @Override
/// @RequireRole("owner")
/// public void delete(String userId) {
///     ...
/// }
/// ```
///
/// 那么父类的 `admin` 和子类的 `owner` 是覆盖关系, 合并关系,
/// AND 关系, 还是 OR 关系?
///
/// 更重要的是, scx-web 不可能知道所有用户自定义注解的语义.
/// 对 `@RequireRole` 而言, 合并可能意味着 AND 或 OR.
/// 对 `@RateLimit` 而言, 合并可能意味着取更严格值, 覆盖, 或叠加.
/// 对 `@Cache` 而言, 合并可能意味着完全不同的缓存键.
/// 对 `@Audit` 而言, 合并可能意味着记录一次还是记录多次.
/// 对 `@Transactional` 而言, 合并可能涉及传播行为.
/// 这些注解都可能参与 endpoint 行为,
/// 但它们的语义不是 scx-web 能统一推导的.
///
/// 因此, endpoint metadata inheritance 不是一个封闭系统.
/// 框架可以为自己内置的少数注解硬编码规则,
/// 但无法为所有可能影响 endpoint 的注解提供一个通用、正交、可组合的继承模型.
///
/// 这些问题说明: 支持父类路由继承后,
/// 复杂度会从 `@Route` 扩散到整个 endpoint metadata 系统.
/// 这不是一个孤立的小功能.
///
/// ## 为什么不引入 @RouteTemplate / @NoRoute / @InheritRoutes?
///
/// 表面上可以通过更多注解和规则来补这个模型:
///
/// ```
/// @RouteTemplate
/// @InheritRoutes
/// @NoRoute
/// endpoint contract inheritance
/// parameter metadata inheritance
/// security metadata inheritance
/// contract override
/// contract suppression
/// ```
///
/// 但是这些注解和规则并没有真正解决概念问题, 只是把问题显式化了.
///
/// 一旦引入 route inheritance, 就必须回答:
///
/// ```
/// 哪些 metadata 属于 endpoint contract?
/// 哪些 metadata 可以继承?
/// 哪些 metadata 必须覆盖?
/// 哪些 metadata 可以合并?
/// 合并时是 AND, OR, append, replace, min, max, 还是用户自定义?
/// 子类如何局部修改 contract?
/// 子类如何删除 contract 的一部分?
/// 删除一个 route 是否也删除权限, 文档, 校验, 拦截器等 metadata?
/// ```
///
/// 这些问题没有一个普适答案.
/// 因为 endpoint metadata 是开放集合, 不是封闭集合.
/// 除了 scx-web 自己定义的 `@Route`, `@PathCapture`, `@Body` 等注解之外,
/// 用户还可能有自己的权限, 校验, 文档, 审计, 缓存, 限流, 事务, feature flag 等注解.
/// 这些注解的继承语义无法由 scx-web 自动推导.
///
/// 所以所谓 "完整 route inheritance" 并不是一个可以被自然补全的正交功能.
/// 它必然变成一组人为规定的特殊规则.
/// 规则越多, 用户越需要学习.
/// 规则越少, 行为越容易在安全性, 可读性, debug 上出问题.
///
/// 因此 scx-web 不选择这条路.
///
/// scx-web 在这里选择更小, 更直接, 更容易 debug 的模型:
///
/// ```
/// 当前类
/// 当前方法
/// 当前注解
/// 当前路由
/// ```
///
/// 这会带来一些 controller 外壳代码重复,
/// 但这些重复通常发生在 HTTP 边界层.
/// 边界层的显式重复可以换来更清楚的 API 暴露面.
/// 业务逻辑仍然可以通过 service, repository, use case, helper, composition,
/// protected 方法等方式复用.
///
/// ## 最终规则
///
/// RouteCompiler 必须保持以下规则:
///
/// 1. 只扫描注册类自身直接声明的方法.
/// 2. 不扫描父类继承来的 @Route 方法.
/// 3. 不把 Java 继承解释为 HTTP API 继承.
/// 4. 父类可以共享实现, 但不能隐式传播 endpoint.
/// 5. 想暴露 HTTP endpoint, 必须在当前 controller 类中显式声明 @Route.
///
/// 这个选择是为了保证:
///
/// ```
/// 路由来源清楚
/// API 暴露面局部可见
/// debug 路径短
/// 不需要 override 继承规则
/// 不需要参数注解继承规则
/// 不需要权限注解继承规则
/// 不需要 @NoRoute 之类的抑制机制
/// 父类改动不会意外扩大子类 HTTP API
/// ```
///
/// 因此, 不要把这里的 declared-method 扫描改成 inherited-method 扫描.
///
/// 这不是一个简单的 "支持更多方法" 的改动.
/// 它会把 scx-web 从:
///
/// ```
/// 局部路由声明模型
/// ```
///
/// 改成:
///
/// ```
/// endpoint contract inheritance 模型
/// ```
///
/// 后者不是前者的自然扩展, 而是另一套完全不同的框架语义.
/// 它要求为 route, 参数绑定, 权限, 校验, 文档, 拦截器以及用户自定义 metadata
/// 定义继承, 覆盖, 合并, 删除规则.
///
/// 这些规则不存在通用且正交的答案.
/// 所以这里不是 "暂时不支持继承",
/// 而是 "按设计不把继承作为路由发现机制".
///
/// 同理, RouteCompiler 也不把 static 方法作为路由发现机制.
///
/// static @Route 的问题不是技术上能不能调用,
/// 而是它表示的是 "类级函数", 不是 "当前 controller 实例的行为".
///
/// 支持 static @Route 不是放宽一个校验,
/// 而是引入另一种路由来源:
///
/// ```
/// 类级函数 -> HTTP endpoint
/// ```
///
/// 这会改变 scx-web 的路由模型, 不应混入当前的实例路由扫描.
///
/// @author scx567888
final class RouteCompiler {

    private static final Comparator<ScxWebRoute> PRIORITY_COMPARATOR = Comparator.comparing(ScxWebRoute::priority);

    private static final Comparator<ScxWebRoute> HAS_WILDCARD_COMPARATOR = Comparator.comparing(r -> {
        var p = r.pathMatcher();
        if (p instanceof AnyPathMatcher anyPathMatcher) {
            return 2;
        } else if (p instanceof TemplatePathMatcher templatePathMatcher) {
            return templatePathMatcher.hasWildcard() ? 1 : 0;
        }
        return 0;
    });

    private static final Comparator<ScxWebRoute> PARAM_COUNT_COMPARATOR = Comparator.comparing(r -> {
        var p = r.pathMatcher();
        if (p instanceof AnyPathMatcher anyPathMatcher) {
            return Integer.MAX_VALUE;
        } else if (p instanceof TemplatePathMatcher templatePathMatcher) {
            return templatePathMatcher.paramCount();
        }
        return 0;
    });

    private static final Comparator<ScxWebRoute> METHOD_SPECIFICITY_COMPARATOR = Comparator.comparingInt(r -> {
        var m = r.methodMatcher();
        if (m instanceof AnyMethodMatcher anyMethodMatcher) {
            return Integer.MAX_VALUE;
        } else if (m instanceof MultiMethodMatcher multiMethodMatcher) {
            return multiMethodMatcher.methods().size();
        }
        return 0;
    });

    private final ScxWeb scxWeb;

    public RouteCompiler(ScxWeb scxWeb) {
        this.scxWeb = scxWeb;
    }

    private static List<RouteClass> collectRouteClasses(Object... objects) {
        var result = new ArrayList<RouteClass>();
        for (var object : objects) {
            if (object == null) {
                continue;
            }
            var typeInfo = typeOf(object.getClass());
            if (typeInfo instanceof PrimitiveTypeInfo) {
                throw new IllegalArgumentException("原始类型不能作为路由注册类实例 : " + object.getClass());
            }
            if (typeInfo instanceof ArrayTypeInfo) {
                throw new IllegalArgumentException("数组不能作为路由注册类实例 : " + object.getClass());
            }

            var classInfo = (ClassInfo) typeInfo;

            if (classInfo.classKind() == ENUM) {
                throw new IllegalArgumentException("枚举类型不能作为路由注册类 : " + classInfo);
            }
            if (classInfo.classKind() == ANNOTATION) {
                throw new IllegalArgumentException("注解类型不能作为路由注册类 : " + classInfo);
            }
            var routes = classInfo.findAnnotation(Routes.class);
            if (routes == null) {
                throw new IllegalArgumentException("类未标注 @Routes，不能作为路由注册类 : " + classInfo);
            }
            result.add(new RouteClass(object, classInfo, routes));
        }
        return result;
    }

    private static List<RouteMethod> collectRouteMethods(List<RouteClass> routeClasses) {
        var result = new ArrayList<RouteMethod>();
        for (var routeClass : routeClasses) {
            // 只扫描当前类显式声明的方法
            var methods = routeClass.classInfo.methods();
            for (var method : methods) {
                var route = method.findAnnotation(Route.class);
                // 没有注解跳过
                if (route == null) {
                    continue;
                }
                if (method.accessModifier() != PUBLIC) {
                    throw new IllegalArgumentException("路由方法必须是 public : " + method.declaringClass().name() + "#" + method.signature());
                }
                // 这里我们限制 static 方法
                if (method.isStatic()) {
                    throw new IllegalArgumentException("路由方法不能是 static : " + method.declaringClass().name() + "#" + method.signature());
                }
                result.add(new RouteMethod(routeClass.object, method, routeClass.routes, route));
            }
        }
        return result;
    }

    private static List<ScxWebRoute> compileRoutes(List<RouteMethod> routeMethods, ScxWeb scxWeb) {
        var result = new ArrayList<ScxWebRoute>();
        for (var routeMethod : routeMethods) {
            var methodInfo = routeMethod.methodInfo;
            var instance = routeMethod.object;
            try {
                var requestMatcher = buildRequestMatcher(routeMethod.route);
                var pathMatcher = buildPathMatcher(routeMethod.routes, routeMethod.route);
                var methodMatcher = buildMethodMatcher(routeMethod.route);
                var priority = routeMethod.route.priority();

                var scxWebRoute = new ScxWebRouteImpl(methodInfo, instance, requestMatcher, pathMatcher, methodMatcher, priority, scxWeb);
                result.add(scxWebRoute);
            } catch (Exception e) {
                throw new IllegalArgumentException("路由编译失败: " + methodInfo.declaringClass().name() + "#" + methodInfo.signature(), e);
            }
        }
        return result;
    }

    private static RequestMatcher buildRequestMatcher(Route route) {
        if (route.kind() == Route.RouteKind.WEBSOCKET_UPGRADE) {
            return RequestMatcher.typeIs(ScxWebSocketServerHandshakeRequest.class);
        } else {
            return RequestMatcher.typeNot(ScxWebSocketServerHandshakeRequest.class);
        }
    }

    /// 根据类级 [Routes] 和方法级 [Route] 构建路径匹配器.
    ///
    /// 注意: 这里不做路径归一化.
    ///
    /// 这里不会自动补 `/`、删除 `/`、合并 `//`.
    /// 这不是遗漏, 而是 RouteCompiler 的设计取舍.
    ///
    /// 对 RouteCompiler 来说, 用户写下的字符串就是 route template 本身.
    /// [Routes] 表示模板字符串前缀, 不是目录路径前缀.
    ///
    /// 具体规则:
    ///
    /// 1. [Routes#value()] 作为类级模板前缀;
    /// 2. [Route#value()] 作为方法级模板;
    /// 3. 默认情况下, 最终模板 = 类级模板前缀 + 方法级模板;
    /// 4. 如果 [Route#absolute()] 为 true, 则忽略类级模板前缀;
    /// 5. 如果类级模板和方法级模板都没有声明, 则表示不限制路径, 返回 [PathMatcher#any()].
    ///
    /// 示例:
    ///
    /// - `@Routes("/api")` + `@Route("/user")` -> `/api/user`
    /// - `@Routes("/get_by")` + `@Route("_id")` -> `/get_by_id`
    /// - `@Routes("/api/")` + `@Route("/order")` -> `/api//order`
    /// - `@Routes("/api")` + `@Route(value = "/user", absolute = true)` -> `/user`
    ///
    /// `/api/order` 和 `/api//order` 是两个不同的模板声明.
    /// 如果在这里把 `/api//order` 归一化成 `/api/order`, 就等于我们在替用户做决定.
    ///
    /// 这会带来以下问题:
    ///
    /// 1. 用户将无法显式声明 `/api//order`;
    /// 2. 原本不同的 route template 可能在编译后变成同一个 matcher, 从而制造源码层面不直观的隐式路由冲突.
    /// 3. 用户将无法仅通过注解字面值直接推断最终模板, 从而增加理解成本和心智负担.
    ///
    /// 因此, 本方法只负责按字面拼接:
    ///
    ///     fullPath = pathPrefix + path
    ///
    /// 然后交给 [PathMatcher#ofTemplate(String)].
    /// 模板字符串内部如何匹配, 只由 [PathMatcher] 负责解释.
    private static PathMatcher buildPathMatcher(Routes routes, Route route) {
        var pathPrefix = routes.value().length > 0 ? routes.value()[0] : null;

        var path = route.value().length > 0 ? route.value()[0] : null;

        // absolute route 不参与类级前缀拼接.
        if (route.absolute()) {
            pathPrefix = null;
        }

        // 没有任何显式路径模板时, 表示不限制路径.
        if (pathPrefix == null && path == null) {
            return PathMatcher.any();
        }

        var fullPath = "";
        if (pathPrefix != null) {
            fullPath += pathPrefix;
        }
        if (path != null) {
            fullPath += path;
        }

        return PathMatcher.ofTemplate(fullPath);
    }

    private static MethodMatcher buildMethodMatcher(Route route) {
        // WebSocket 升级请求我们不限制 Method, 因为上层会自行拦截.
        if (route.kind() == Route.RouteKind.WEBSOCKET_UPGRADE) {
            return MethodMatcher.any();
        }
        // 去重.
        var methods = Arrays.stream(route.methods()).distinct().toArray(HttpMethod[]::new);
        return methods.length == 0 ? MethodMatcher.any() : MethodMatcher.of(methods);
    }

    /// 排序 规则如下
    ///
    /// 1 若注解上标识了 priority 则按照注解上的 priority 进行排序 如下
    /// `0 > 5 > 13 > 199`
    ///
    /// 2 如果根据路径是否为精确路径 进行排序 如下
    /// `/api/user > /api/*`
    ///
    /// 3 根据路径参数数量进行排序 (越少越靠前) 如下
    /// `/api/user/list > /api/user/:m > /api/:u/:m/`
    ///
    /// 4 根据方法来, (越具体越靠前).
    /// `{GET} > {POST, GET} > ANY`.
    private static List<ScxWebRoute> sortRoutes(List<ScxWebRoute> routes) {
        return routes.stream().sorted(
            PRIORITY_COMPARATOR
                .thenComparing(HAS_WILDCARD_COMPARATOR)
                .thenComparing(PARAM_COUNT_COMPARATOR)
                .thenComparing(METHOD_SPECIFICITY_COMPARATOR)
        ).toList();
    }

    public List<ScxWebRoute> compile(Object... objects) {
        // 1, 收集路由类.
        var routeClasses = collectRouteClasses(objects);
        // 2, 收集路由方法.
        var routeMethods = collectRouteMethods(routeClasses);
        // 3, 编译为 ScxWebRoute.
        var routes = compileRoutes(routeMethods, scxWeb);
        // 4, 排序.
        return sortRoutes(routes);
    }

    private record RouteClass(Object object, ClassInfo classInfo, Routes routes) {

    }

    private record RouteMethod(Object object, MethodInfo methodInfo, Routes routes, Route route) {

    }

}
