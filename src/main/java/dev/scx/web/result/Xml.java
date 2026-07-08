package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.media_type.ScxMediaType;
import dev.scx.serialize.SerializeConfig;
import dev.scx.web.ScxWeb;

import static dev.scx.http.media_type.MediaType.APPLICATION_XML;
import static dev.scx.serialize.ScxSerialize.toXml;
import static java.nio.charset.StandardCharsets.UTF_8;

/// Xml 格式的返回值
///
/// @author scx567888
public final class Xml implements WebResult {

    private final Object data;
    private final SerializeConfig serializeConfig;

    private Xml(Object data, SerializeConfig serializeConfig) {
        this.data = data;
        this.serializeConfig = serializeConfig;
    }

    public static Xml of(Object data) {
        return new Xml(data, null);
    }

    public static Xml of(Object data, SerializeConfig serializeConfig) {
        return new Xml(data, serializeConfig);
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        request.response()
            .contentType(ScxMediaType.of(APPLICATION_XML).charset(UTF_8))
            .send(toXml(data, serializeConfig != null ? serializeConfig : scxWeb.serializeConfig()));
    }

}
