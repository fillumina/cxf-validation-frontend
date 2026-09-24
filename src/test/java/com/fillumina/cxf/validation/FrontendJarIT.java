package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The frontend, run out of the jar a build would put on the classpath of the cxf-codegen-plugin.
 *
 * <p>The ordinary suite runs against {@code target/classes}, where a resource missing from the jar
 * cannot be noticed: the descriptor, or the service file that lets XJC accept the option, could be
 * dropped by the packaging and every test would still pass. This one starts a JVM whose classpath
 * is the jar and the CXF tooling, with no classes directory on it, and generates the interface
 * there.
 *
 * <p>It runs in the integration test phase, which is why {@code mvn test} leaves it out: it needs
 * the jar, and {@code package} builds that. Use {@code mvn verify}, {@code mvn install} or
 * {@code mvn deploy}, and CI, which runs {@code verify}.
 */
class FrontendJarIT {

    private static final String WSDL = "src/test/resources/hello.wsdl";
    private static final String INTERFACE = "com/example/weather/WeatherServicePortType.java";

    @Test
    void theFrontendGeneratesFromTheJar() throws Exception {
        Path jar = Path.of(required("frontend.jar"));
        assertTrue(Files.exists(jar), "package builds the jar before this test: " + jar);
        String tooling = Files.readString(Path.of(required("tooling.classpath.file"))).trim();
        Path output = Files.createTempDirectory("frontend-from-the-jar");

        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", jar + File.pathSeparator + tooling,
                "org.apache.cxf.tools.wsdlto.WSDLToJava",
                "-frontend", ValidSEIGenerator.FRONTEND_NAME,
                "-xjc-" + ServiceValidationOptions.OPTION_PREFIX_NAME + ":"
                        + ServiceValidationOptions.OPTION_NAME + "=both",
                "-d", output.toString(),
                Path.of(WSDL).toAbsolutePath().toString()));

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String log = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), () -> "wsdl2java did not run out of the jar: " + log);

        String generated = Files.readString(output.resolve(INTERFACE));
        assertTrue(generated.contains("import jakarta.validation.Valid;"), generated);
        assertTrue(generated.contains("@Valid"), generated);
    }

    private static String required(String property) {
        String value = System.getProperty(property);
        if (value == null) {
            throw new AssertionError("the build must pass -D" + property);
        }
        return value;
    }
}
