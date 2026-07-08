package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.media_type.ScxMediaType;
import dev.scx.serialize.SerializeConfig;
import dev.scx.web.ScxWeb;

import static dev.scx.http.media_type.MediaType.APPLICATION_JSON;
import static dev.scx.serialize.ScxSerialize.toJson;
import static java.nio.charset.StandardCharsets.UTF_8;

/// Json 格式的返回值
///
/// @author scx567888
public final class Json implements WebResult {

    private final Object data;
    private final SerializeConfig serializeConfig;

    private Json(Object data, SerializeConfig serializeConfig) {
        this.data = data;
        this.serializeConfig = serializeConfig;
    }

    public static Json of(Object data) {
        return new Json(data, null);
    }

    public static Json of(Object data, SerializeConfig serializeConfig) {
        return new Json(data, serializeConfig);
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        request.response()
            .contentType(ScxMediaType.of(APPLICATION_JSON).charset(UTF_8))
            .send(toJson(data, serializeConfig != null ? serializeConfig : scxWeb.serializeConfig()));
    }

}
