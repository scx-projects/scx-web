package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.web.ScxWeb;

import static dev.scx.http.headers.HttpHeaderName.LOCATION;
import static dev.scx.http.status_code.HttpStatusCode.*;

/// 重定向
///
/// @author scx567888
/// @version 0.0.1
public final class Redirect implements WebResult {

    private final String location;
    private final ScxHttpStatusCode status;

    private Redirect(String location, ScxHttpStatusCode status) {
        this.location = location;
        this.status = status;
    }

    public static Redirect ofTemporary(String location) {
        return new Redirect(location, TEMPORARY_REDIRECT);
    }

    public static Redirect ofPermanent(String location) {
        return new Redirect(location, PERMANENT_REDIRECT);
    }

    public static Redirect ofMovedPermanently(String location) {
        return new Redirect(location, MOVED_PERMANENTLY);
    }

    public static Redirect ofFound(String location) {
        return new Redirect(location, FOUND);
    }

    public static Redirect ofSeeOther(String location) {
        return new Redirect(location, SEE_OTHER);
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) throws Exception {
        request.response().setHeader(LOCATION, location).statusCode(status).send();
    }

}
