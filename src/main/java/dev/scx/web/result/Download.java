package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.headers.content_disposition.ContentDisposition;
import dev.scx.http.media_type.FileFormat;
import dev.scx.http.media_type.MediaType;
import dev.scx.web.ScxWeb;

import java.io.File;
import java.io.InputStream;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

/// 文件下载
///
/// @author scx567888
/// @version 0.0.1
public final class Download extends Binary {

    private final String downloadName;

    private Download(InputStream inputStream, String downloadName) {
        super(inputStream);
        this.downloadName = downloadName;
    }

    private Download(File file, String downloadName) {
        super(file);
        this.downloadName = downloadName;
    }

    private Download(byte[] bytes, String downloadName) {
        super(bytes);
        this.downloadName = downloadName;
    }

    public static Download of(InputStream inputStream, String downloadName) {
        return new Download(inputStream, downloadName);
    }

    public static Download of(byte[] bytes, String downloadName) {
        return new Download(bytes, downloadName);
    }

    public static Download of(File file, String downloadName) {
        return new Download(file, downloadName);
    }

    public static Download of(File file) {
        return new Download(file, file.getName());
    }

    /// URLEncoder.encode 针对 ' ' (空格) 会编码为 '+' , 而这里我们需要的是编码为 %20
    ///
    /// @param downloadName a [java.lang.String] object
    /// @return c
    /// @see <a href="https://www.rfc-editor.org/rfc/rfc6266.html">https://www.rfc-editor.org/rfc/rfc6266.html</a>
    public static ContentDisposition getDownloadContentDisposition(String downloadName) {
        var contentDisposition = ContentDisposition.of("attachment");
        contentDisposition.params().set("filename*", "utf-8''" + encode(downloadName, UTF_8).replace("+", "%20"));
        return contentDisposition;
    }

    public static MediaType getMediaTypeByFileName(String filename) {
        var fileFormat = FileFormat.findByFileName(filename);
        return fileFormat != null ? fileFormat.mediaType() : MediaType.APPLICATION_OCTET_STREAM;
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        var mediaType = getMediaTypeByFileName(downloadName);
        var contentDisposition = getDownloadContentDisposition(downloadName);
        var response = request.response();
        response.contentType(mediaType);
        response.contentDisposition(contentDisposition);
        super.apply(request, scxWeb);
    }

}
