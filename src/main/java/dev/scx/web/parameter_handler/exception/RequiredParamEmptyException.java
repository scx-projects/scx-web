package dev.scx.web.parameter_handler.exception;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

/// 必须参数缺失异常
///
/// @author scx567888
/// @version 0.0.1
public final class RequiredParamEmptyException extends Exception implements ScxHttpException {

    public RequiredParamEmptyException(String message) {
        super(message);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
