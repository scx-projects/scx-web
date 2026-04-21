package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.headers.content_disposition.ContentDisposition;
import dev.scx.http.media_type.FileFormat;
import dev.scx.http.media_type.MediaType;
import dev.scx.web.ScxWeb;

import java.io.File;
import java.io.InputStream;

/// 原始文件 但不需要下载的 比如 pdf 之类
///
/// @author scx567888
/// @version 0.0.1
public final class Inline extends Binary {

    private final FileFormat fileFormat;

    private Inline(InputStream inputStream, FileFormat fileFormat) {
        super(inputStream);
        this.fileFormat = fileFormat;
    }

    private Inline(File file, FileFormat fileFormat) {
        super(file);
        this.fileFormat = fileFormat;
    }

    private Inline(byte[] bytes, FileFormat fileFormat) {
        super(bytes);
        this.fileFormat = fileFormat;
    }

    public static Inline of(InputStream inputStream, FileFormat fileFormat) {
        return new Inline(inputStream, fileFormat);
    }

    public static Inline of(byte[] bytes, FileFormat fileFormat) {
        return new Inline(bytes, fileFormat);
    }

    public static Inline of(File file, FileFormat fileFormat) {
        return new Inline(file, fileFormat);
    }

    public static Inline of(File file) {
        return new Inline(file, FileFormat.findByFileName(file.getName()));
    }

    public static ContentDisposition getInlineContentDisposition() {
        return ContentDisposition.of("inline");
    }

    public static MediaType getMediaTypeByFileFormat(FileFormat fileFormat) {
        return fileFormat != null ? fileFormat.mediaType() : MediaType.APPLICATION_OCTET_STREAM;
    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        var mediaType = getMediaTypeByFileFormat(fileFormat);
        var contentDisposition = getInlineContentDisposition();
        var response = request.response();
        response.contentType(mediaType);
        response.contentDisposition(contentDisposition);
        super.apply(request, scxWeb);
    }

}
