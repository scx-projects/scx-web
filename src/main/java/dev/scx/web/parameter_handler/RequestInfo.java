package dev.scx.web.parameter_handler;

import dev.scx.collection.multi_map.DefaultMultiMap;
import dev.scx.collection.multi_map.MultiMap;
import dev.scx.format.FormatToNodeException;
import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.media.multi_part.MultiPartPart;
import dev.scx.http.routing.RoutingContext;
import dev.scx.node.Node;
import dev.scx.node.ObjectNode;
import dev.scx.object.ObjectToNodeException;
import dev.scx.web.ScxWeb;
import dev.scx.web.parameter_handler.exception.BodyParseException;

import static dev.scx.http.media_type.MediaType.*;
import static dev.scx.serialize.ScxSerialize.*;

/// RequestInfo
///
/// @author scx567888
public final class RequestInfo {

    private final RoutingContext routingContext;
    private final ScxWeb scxWeb;
    private final ObjectNode pathCaptures;
    private final ObjectNode query;
    private BodyAndParts bodyAndParts;

    public RequestInfo(RoutingContext routingContext, ScxWeb scxWeb) {
        this.routingContext = routingContext;
        this.scxWeb = scxWeb;
        this.pathCaptures = initPathCaptures(this.routingContext, this.scxWeb);
        this.query = initQuery(this.routingContext, this.scxWeb);
        this.bodyAndParts = null;
    }

    private static ObjectNode initPathCaptures(RoutingContext routingContext, ScxWeb scxWeb) {
        return (ObjectNode) objectToNode(routingContext.pathMatch().namedCaptures(), scxWeb.serializeConfig());
    }

    private static ObjectNode initQuery(RoutingContext routingContext, ScxWeb scxWeb) {
        return (ObjectNode) objectToNode(routingContext.request().query().toMultiValueMap(), scxWeb.serializeConfig());
    }

    /// 这里不包装 asString / asFormParams / asMultiPart 抛出的异常.
    /// 这些异常属于 HTTP 请求体读取或底层协议解析阶段的错误,
    /// 如果底层已经抛出 ScxHttpException, 应保留其原始 statusCode.
    /// RequestInfo 只负责把 "已读取内容 -> Node" 这一层的解析失败包装成 BodyParseException.
    private static BodyAndParts initBodyAndParts(ScxHttpServerRequest request, ScxWeb scxWeb) throws BodyParseException {
        var contentType = request.contentType();
        // JSON 格式
        if (APPLICATION_JSON.equalsIgnoreParams(contentType)) {
            var bodyStr = request.asString();
            try {
                var body = fromJson(bodyStr, scxWeb.serializeConfig());
                return new BodyAndParts(body, null, BodySemantics.STRUCTURED);
            } catch (FormatToNodeException e) {
                throw new BodyParseException("请求体 JSON 解析失败", e);
            }
        }
        // XML 格式
        if (APPLICATION_XML.equalsIgnoreParams(contentType)) {
            var bodyStr = request.asString();
            try {
                var body = fromXml(bodyStr, scxWeb.serializeConfig());
                return new BodyAndParts(body, null, BodySemantics.STRUCTURED);
            } catch (FormatToNodeException e) {
                throw new BodyParseException("请求体 XML 解析失败", e);
            }
        }
        // x-www-form-urlencoded 格式
        if (APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreParams(contentType)) {
            var formParams = request.asFormParams();
            try {
                var body = objectToNode(formParams.toMultiValueMap(), scxWeb.serializeConfig());
                return new BodyAndParts(body, null, BodySemantics.FORM);
            } catch (ObjectToNodeException e) {
                throw new BodyParseException("请求体 Form 解析失败", e);
            }
        }
        // 多部分表单格式
        if (MULTIPART_FORM_DATA.equalsIgnoreParams(contentType)) {
            var formParts = new DefaultMultiMap<String, String>();
            var parts = new DefaultMultiMap<String, MultiPartPart>();

            // multipart 是顺序读取的, 而我们需要按名称随机访问 part,
            // 因此这里需要先将所有流式 MultiPartPart 缓存为内存式 MultiPartPart.
            //
            // 这里不落盘(临时文件缓存), 因为落盘会引入文件清理等生命周期概念.
            // 对于会对内存产生压力的大表单(如文件上传),
            // 用户应直接接收 ScxHttpServerRequest 等底层类型进行流式处理,
            // 这比落盘更直接(少了一次临时文件读写), 也更符合大文件场景.
            try (var multiPart = request.asMultiPart()) {
                for (var multiPartPart : multiPart) {

                    var name = multiPartPart.name();
                    var filename = multiPartPart.filename();

                    // 将流式的 MultiPartPart 转换成内存式的.
                    var cachePart = multiPartPart.cache();
                    parts.add(name, MultiPartPart.of().headers(multiPartPart.headers()).body(cachePart.asBytes()));

                    // 不带 filename 的 part 视为表单字段.
                    if (filename == null) {
                        formParts.add(name, cachePart.asString());
                    }

                }
            }
            try {
                var body = objectToNode(formParts.toMultiValueMap(), scxWeb.serializeConfig());
                return new BodyAndParts(body, parts, BodySemantics.FORM);
            } catch (ObjectToNodeException e) {
                throw new BodyParseException("请求体 Multipart 解析失败", e);
            }
        }
        // 处理不了
        return new BodyAndParts(null, null, BodySemantics.STRUCTURED);
    }

    public RoutingContext routingContext() {
        return routingContext;
    }

    public ObjectNode pathCaptures() {
        return pathCaptures;
    }

    public ObjectNode query() {
        return query;
    }

    /// 不存在 结构化 Body 返回 null
    public Node body() throws BodyParseException {
        if (bodyAndParts == null) {
            bodyAndParts = initBodyAndParts(routingContext.request(), scxWeb);
        }
        return bodyAndParts.body;
    }

    /// 不存在 parts 返回 null
    public MultiMap<String, MultiPartPart> parts() throws BodyParseException {
        if (bodyAndParts == null) {
            bodyAndParts = initBodyAndParts(routingContext.request(), scxWeb);
        }
        return bodyAndParts.parts;
    }

    public BodySemantics bodySemantics() throws BodyParseException {
        if (bodyAndParts == null) {
            bodyAndParts = initBodyAndParts(routingContext.request(), scxWeb);
        }
        return bodyAndParts.semantics;
    }

    public enum BodySemantics {
        STRUCTURED,
        FORM
    }

    private record BodyAndParts(Node body, MultiMap<String, MultiPartPart> parts, BodySemantics semantics) {

    }

}
