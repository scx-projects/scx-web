package dev.scx.web.test;

import dev.scx.http.exception.ForbiddenException;
import dev.scx.http.media_type.FileFormat;
import dev.scx.http.routing.RoutingContext;
import dev.scx.web.annotation.PathCapture;
import dev.scx.web.annotation.QueryParam;
import dev.scx.web.annotation.Route;
import dev.scx.web.annotation.Routes;
import dev.scx.web.result.Binary;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import static dev.scx.web.result.Noop.NOOP;

@Routes
public class HelloWorldController {

    @Route("/:path")
    public Object name(@PathCapture String path, RoutingContext context) throws Throwable {
        if (path.contains("scx")) {
            return path;
        }
        context.next();
        return NOOP;
    }

    @Route("/info")
    public Object name(@QueryParam(required = false) String id) {
        return "id -> " + id;
    }

    @Route("/hello")
    public Object hello(@QueryParam(required = false) LocalDateTime time) {
        if (time == null) {
            time = LocalDateTime.now();
        }
        return Map.of("name", "scx567888😁", "time", time);
    }

    @Route("/download")
    public Object download() {
        return Binary.download("下载的内容".repeat(100).getBytes(StandardCharsets.UTF_8), "文件名🧨😁 + ? + 😍  .txt");
    }

    @Route("/inline")
    public Object inline() {
        // 测试复杂的预览写出
        return Binary.inline(byteOutput -> {
            try (byteOutput) {
                for (int i = 0; i < 100; i = i + 1) {
                    byteOutput.write((i + "").repeat(100).getBytes());
                    Thread.sleep(50);
                }
            }
        }, FileFormat.TXT);
    }

    @Route("/no-perm")
    public Object noPerm() {
        throw new ForbiddenException();
    }

}
