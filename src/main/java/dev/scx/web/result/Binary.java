package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.headers.content_disposition.ContentDisposition;
import dev.scx.http.media_type.FileFormat;
import dev.scx.http.media_type.MediaType;
import dev.scx.http.media_type.ScxMediaType;
import dev.scx.http.routing.x.static_files.StaticFilesSupport;
import dev.scx.http.sender.ScxHttpSender.BodyWriter;
import dev.scx.web.ScxWeb;

import java.io.File;
import java.io.InputStream;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

/// 二进制响应结果.
///
/// 用于向响应体写入二进制内容, 内容来源可以是 `byte[]`, [File], [InputStream] 或 [BodyWriter].
///
/// `of()` 表示裸发送, 不主动设置 `Content-Type` 或 `Content-Disposition`;
/// `download()` 表示以附件形式发送, 例如文件下载.
/// `inline()` 表示以内联形式发送, 例如在浏览器中预览 PDF 或图片.
///
/// @author scx567888
public final class Binary implements WebResult {

    private final Object source;
    private ScxMediaType contentType;
    private ContentDisposition contentDisposition;

    private Binary(byte[] bytes) {
        this.source = bytes;
    }

    private Binary(File file) {
        this.source = file;
    }

    private Binary(InputStream inputStream) {
        this.source = inputStream;
    }

    private Binary(BodyWriter bodyWriter) {
        this.source = bodyWriter;
    }

    // **************** 裸二进制响应工厂 *******************

    public static Binary of(byte[] bytes) {
        return new Binary(bytes);
    }

    public static Binary of(File file) {
        return new Binary(file);
    }

    public static Binary of(InputStream inputStream) {
        return new Binary(inputStream);
    }

    public static Binary of(BodyWriter bodyWriter) {
        return new Binary(bodyWriter);
    }

    // **************** download 发送工厂 *******************

    public static Binary download(byte[] bytes, String downloadName) {
        return new Binary(bytes).contentType(getMediaTypeByFileName(downloadName)).contentDisposition(getDownloadContentDisposition(downloadName));
    }

    public static Binary download(File file, String downloadName) {
        return new Binary(file).contentType(getMediaTypeByFileName(downloadName)).contentDisposition(getDownloadContentDisposition(downloadName));
    }

    public static Binary download(File file) {
        var downloadName = file.getName();
        return new Binary(file).contentType(getMediaTypeByFileName(downloadName)).contentDisposition(getDownloadContentDisposition(downloadName));
    }

    public static Binary download(InputStream inputStream, String downloadName) {
        return new Binary(inputStream).contentType(getMediaTypeByFileName(downloadName)).contentDisposition(getDownloadContentDisposition(downloadName));
    }

    public static Binary download(BodyWriter bodyWriter, String downloadName) {
        return new Binary(bodyWriter).contentType(getMediaTypeByFileName(downloadName)).contentDisposition(getDownloadContentDisposition(downloadName));
    }

    // **************** inline 发送工厂 *******************

    public static Binary inline(byte[] bytes, FileFormat fileFormat) {
        return new Binary(bytes).contentType(getMediaTypeByFileFormat(fileFormat)).contentDisposition(getInlineContentDisposition());
    }

    public static Binary inline(File file, FileFormat fileFormat) {
        return new Binary(file).contentType(getMediaTypeByFileFormat(fileFormat)).contentDisposition(getInlineContentDisposition());
    }

    public static Binary inline(File file) {
        var fileFormat = FileFormat.findByFileName(file.getName());
        return new Binary(file).contentType(getMediaTypeByFileFormat(fileFormat)).contentDisposition(getInlineContentDisposition());
    }

    public static Binary inline(InputStream inputStream, FileFormat fileFormat) {
        return new Binary(inputStream).contentType(getMediaTypeByFileFormat(fileFormat)).contentDisposition(getInlineContentDisposition());
    }

    public static Binary inline(BodyWriter bodyWriter, FileFormat fileFormat) {
        return new Binary(bodyWriter).contentType(getMediaTypeByFileFormat(fileFormat)).contentDisposition(getInlineContentDisposition());
    }

    // **************** 辅助方法 *******************

    /// URLEncoder.encode 针对 ' ' (空格) 会编码为 '+' , 而这里我们需要的是编码为 %20
    ///
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

    public static ContentDisposition getInlineContentDisposition() {
        return ContentDisposition.of("inline");
    }

    public static MediaType getMediaTypeByFileFormat(FileFormat fileFormat) {
        return fileFormat != null ? fileFormat.mediaType() : MediaType.APPLICATION_OCTET_STREAM;
    }

    public Binary contentType(ScxMediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    public Binary contentDisposition(ContentDisposition contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        var response = request.response();
        if (contentType != null) {
            response.contentType(contentType);
        }
        if (contentDisposition != null) {
            response.contentDisposition(contentDisposition);
        }
        switch (source) {
            case byte[] bytes -> response.send(bytes);
            case File file -> StaticFilesSupport.sendFile(file, request);
            case InputStream inputStream -> response.send(inputStream);
            case BodyWriter bodyWriter -> response.send(bodyWriter);
            default -> throw new IllegalStateException("Unexpected value: " + source.getClass());
        }
    }

}
