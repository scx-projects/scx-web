package dev.scx.web.parameter_handler;

import dev.scx.collection.multi_map.DefaultMultiMap;
import dev.scx.collection.multi_map.MultiMap;
import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.media.multi_part.MultiPartPart;
import dev.scx.http.routing.RoutingContext;
import dev.scx.node.Node;
import dev.scx.node.ObjectNode;

import static dev.scx.http.media_type.MediaType.*;
import static dev.scx.serialize.ScxSerialize.*;

/// RequestInfo
///
/// @author scx567888
/// @version 0.0.1
public final class RequestInfo {

    private final RoutingContext routingContext;
    private final ObjectNode pathCaptures;
    private final ObjectNode query;
    private BodyAndParts bodyAndParts;

    public RequestInfo(RoutingContext routingContext) {
        this.routingContext = routingContext;
        this.pathCaptures = initPathCaptures(routingContext);
        this.query = initQuery(routingContext);
        this.bodyAndParts = null;
    }

    private static ObjectNode initPathCaptures(RoutingContext routingContext) {
        return (ObjectNode) objectToNode(routingContext.pathMatch().namedCaptures());
    }

    private static ObjectNode initQuery(RoutingContext routingContext) {
        return (ObjectNode) objectToNode(routingContext.request().query().toMultiValueMap());
    }

    private static BodyAndParts initBodyAndParts(ScxHttpServerRequest request) {
        var contentType = request.contentType();
        // JSON 格式
        if (APPLICATION_JSON.equalsIgnoreParams(contentType)) {
            var bodyStr = request.asString();
            var body = fromJson(bodyStr);
            return new BodyAndParts(body, null, BodySemantics.STRUCTURED);
        }
        // XML 格式
        if (APPLICATION_XML.equalsIgnoreParams(contentType)) {
            var bodyStr = request.asString();
            var body = fromXml(bodyStr);
            return new BodyAndParts(body, null, BodySemantics.STRUCTURED);
        }
        // x-www-form-urlencoded 格式
        if (APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreParams(contentType)) {
            var formParams = request.asFormParams();
            var body = objectToNode(formParams.toMultiValueMap());
            return new BodyAndParts(body, null, BodySemantics.FORM);
        }
        // 多部份表单格式
        if (MULTIPART_FORM_DATA.equalsIgnoreParams(contentType)) {
            var formParts = new DefaultMultiMap<String, String>();
            var parts = new DefaultMultiMap<String, MultiPartPart>();

            try (var multiPart = request.asMultiPart()) {
                for (var multiPartPart : multiPart) {

                    var name = multiPartPart.name();
                    var filename = multiPartPart.filename();

                    // 这里我们需要将流式的 MultiPartPart 转换成 内存式的.
                    var cachePart = multiPartPart.cache();
                    parts.add(name, MultiPartPart.of().headers(multiPartPart.headers()).body(cachePart.asBytes()));

                    // 不带 filename 的 part 视为表单字段.
                    if (filename == null) {
                        formParts.add(name, cachePart.asString());
                    }

                }
            }
            var body = objectToNode(formParts.toMultiValueMap());
            return new BodyAndParts(body, parts, BodySemantics.FORM);
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
    public Node body() {
        if (bodyAndParts == null) {
            bodyAndParts = initBodyAndParts(routingContext.request());
        }
        return bodyAndParts.body;
    }

    /// 不存在 parts 返回 null
    public MultiMap<String, MultiPartPart> parts() {
        if (bodyAndParts == null) {
            bodyAndParts = initBodyAndParts(routingContext.request());
        }
        return bodyAndParts.parts;
    }

    public BodySemantics bodySemantics() {
        if (bodyAndParts == null) {
            bodyAndParts = initBodyAndParts(routingContext.request());
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
