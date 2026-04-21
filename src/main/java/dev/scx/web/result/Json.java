package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.media_type.ScxMediaType;
import dev.scx.serialize.ToJsonOptions;
import dev.scx.web.ScxWeb;

import static dev.scx.http.media_type.MediaType.APPLICATION_JSON;
import static dev.scx.serialize.ScxSerialize.toJson;
import static java.nio.charset.StandardCharsets.UTF_8;

/// Json 格式的返回值
///
/// @author scx567888
/// @version 0.0.1
public final class Json implements WebResult {

    private final Object data;
    private final ToJsonOptions toJsonOptions;

    private Json(Object data, ToJsonOptions toJsonOptions) {
        this.data = data;
        this.toJsonOptions = toJsonOptions;
    }

    public static Json of(Object data) {
        return new Json(data, null);
    }

    public static Json of(Object data, ToJsonOptions toJsonOptions) {
        return new Json(data, toJsonOptions);
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        request.response()
            .contentType(ScxMediaType.of(APPLICATION_JSON).charset(UTF_8))
            .send(toJson(data, toJsonOptions != null ? toJsonOptions : scxWeb.toJsonOptions()));
    }

}
