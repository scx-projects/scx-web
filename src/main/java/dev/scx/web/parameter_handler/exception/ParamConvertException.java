package dev.scx.web.parameter_handler.exception;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

/// 参数转换异常
///
/// @author scx567888
public final class ParamConvertException extends Exception implements ScxHttpException {

    public ParamConvertException(String message) {
        super(message);
    }

    public ParamConvertException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
