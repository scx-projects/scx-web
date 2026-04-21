package dev.scx.web.test;

import dev.scx.http.exception.ForbiddenException;
import dev.scx.web.annotation.Route;
import dev.scx.web.annotation.Routes;

import java.util.Map;

@Routes
public class HelloWorldController {

    @Route("hello")
    public Object hello() {
        return Map.of("name", "scx567888😁");
    }

    @Route("no-perm")
    public Object noPerm() {
        throw new ForbiddenException();
    }

}
