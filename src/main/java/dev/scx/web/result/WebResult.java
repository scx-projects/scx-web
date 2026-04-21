package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;

/// WebResult
///
/// @author scx567888
/// @version 0.0.1
public interface WebResult {

    void apply(ScxHttpServerRequest request, ScxWeb scxWeb) throws Exception;

}
