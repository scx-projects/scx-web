package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.routing.x.static_files.StaticFilesSupport;
import dev.scx.web.ScxWeb;

import java.io.File;
import java.io.InputStream;

/// 基本写入程序 可以直接向相应体中写入数据
///
/// @author scx567888
/// @version 0.0.1
public class Binary implements WebResult {

    protected final Object bin;

    protected Binary(InputStream inputStream) {
        this.bin = inputStream;
    }

    protected Binary(File file) {
        this.bin = file;
    }

    protected Binary(byte[] bytes) {
        this.bin = bytes;
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        var response = request.response();
        switch (bin) {
            case byte[] bytes -> response.send(bytes);
            case File file -> StaticFilesSupport.sendFile(file, request);
            case InputStream inputStream -> response.send(inputStream);
            default -> throw new IllegalStateException("Unexpected value: " + bin.getClass());
        }
    }

}
