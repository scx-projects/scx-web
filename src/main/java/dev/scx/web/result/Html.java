package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.media_type.ScxMediaType;
import dev.scx.web.ScxWeb;

import static dev.scx.http.media_type.MediaType.TEXT_HTML;
import static java.nio.charset.StandardCharsets.UTF_8;

/// Html
///
/// @author scx567888
/// @version 0.0.1
public final class Html implements WebResult {

    private final String html;

    private Html(String html) {
        this.html = html;
    }

    public static Html of(String html) {
        return new Html(html);
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) throws Exception {
        request.response()
            .contentType(ScxMediaType.of(TEXT_HTML).charset(UTF_8))
            .send(html);
    }

}
