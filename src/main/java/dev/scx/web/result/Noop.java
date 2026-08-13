package dev.scx.web.result;

import dev.scx.http.ScxHttpServerRequest;
import dev.scx.web.ScxWeb;

/// Noop
///
/// @author scx567888
public final class Noop implements WebResult {

    public static final Noop NOOP = new Noop();

    private Noop() {

    }

    @Override
    public void apply(ScxHttpServerRequest request, ScxWeb scxWeb) {
        // NOOP
    }

}
