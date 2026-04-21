package dev.scx.web.return_value_handler;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.headers.accept.Accept;
import dev.scx.http.media_type.MediaType;
import dev.scx.web.ScxWeb;

import static dev.scx.serialize.ScxSerialize.toJson;
import static dev.scx.serialize.ScxSerialize.toXml;

/// 兜底 返回值处理器
///
/// @author scx567888
/// @version 0.0.1
public final class LastReturnValueHandler implements ReturnValueHandler {

    @Override
    public boolean canHandle(Object returnValue) {
        return true;
    }

    @Override
    public void handle(Object returnValue, ScxHttpServerRequest request, ScxWeb scxWeb) {
        MediaType contentType = MediaType.APPLICATION_JSON;
        Accept accept = request.accept();
        if (accept != null) {
            var negotiate = accept.negotiate(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
            if (negotiate != null) {
                contentType = negotiate;
            }
        }

        if (contentType == MediaType.APPLICATION_JSON) {
            request.response().contentType(contentType).send(toJson(returnValue, scxWeb.toJsonOptions()));
        } else {
            request.response().contentType(contentType).send(toXml(returnValue, scxWeb.toXmlOptions()));
        }
    }

}
