package dev.scx.web.parameter_handler.exception;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

/// 必填参数缺失异常
///
/// @author scx567888
public final class RequiredParamMissingException extends Exception implements ScxHttpException {

    public RequiredParamMissingException(String message) {
        super(message);
    }

    public RequiredParamMissingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
