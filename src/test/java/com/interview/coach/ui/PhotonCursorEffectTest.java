package com.interview.coach.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PhotonCursorEffectTest {

    private static final Path STATIC_DIR = Path.of("src/main/resources/static");

    @Test
    void pageProvidesAccessibleNonBlockingPhotonCursorEffect() throws IOException {
        String html = Files.readString(STATIC_DIR.resolve("index.html"));
        String css = Files.readString(STATIC_DIR.resolve("styles.css"));
        String js = Files.readString(STATIC_DIR.resolve("app.js"));

        assertThat(html).contains("id=\"photonCanvas\"");
        assertThat(css).contains(".photon-canvas");
        assertThat(css).contains("pointer-events: none");
        assertThat(css).contains("@media (prefers-reduced-motion: reduce)");
        assertThat(js).contains("function initPhotonCursorEffect");
        assertThat(js).contains("requestAnimationFrame");
        assertThat(js).contains("prefers-reduced-motion: reduce");
    }
}
