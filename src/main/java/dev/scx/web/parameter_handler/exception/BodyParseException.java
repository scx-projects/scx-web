package dev.scx.web.parameter_handler.exception;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

/// 请求体解析异常
///
/// 例如:
/// - JSON 语法错误
/// - XML 语法错误
/// - x-www-form-urlencoded 解析失败
/// - multipart/form-data 解析失败
public final class BodyParseException extends Exception implements ScxHttpException {

    public BodyParseException(String message) {
        super(message);
    }

    public BodyParseException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
