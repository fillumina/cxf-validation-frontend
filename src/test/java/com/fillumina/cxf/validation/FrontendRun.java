package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.apache.cxf.tools.common.CommandInterfaceUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJava;

/**
 * One run of CXF's wsdl2java in this JVM, with this project's frontend, over the test WSDL.
 *
 * <p>Driving the tooling directly is what the old line did too: it needs no Maven build and no
 * lifecycle, and it is the same code path a build takes, since the frontend is looked up from
 * {@code META-INF/tools-plugin.xml} on the classpath either way.
 */
final class FrontendRun {

    private static final Path WSDL = Path.of("src", "test", "resources", "hello.wsdl");
    private static final String INTERFACE = "com/example/weather/WeatherServicePortType.java";

    private final Path output;

    private FrontendRun(Path output) {
        this.output = output;
    }

    /**
     * @param policy the value of the {@code generateServiceValidationAnnotations} option, that is
     *     {@code in}, {@code out}, {@code inOut} or {@code none}
     */
    static FrontendRun of(String policy) throws Exception {
        Path output = Path.of("target", "generated-test-sources", "wsdl2java-" + policy);
        deleteRecursively(output);
        Files.createDirectories(output);

        ToolContext context = new ToolContext();
        context.put(ToolConstants.CFG_XJC_ARGS, new String[] {
            ServiceValidationOptions.PREFIX + ServiceValidationOptions.OPTION_NAME + "=" + policy });
        context.put(ToolConstants.CFG_OUTPUTDIR, output.toString());

        CommandInterfaceUtils.commandCommonMain();
        new WSDLToJava(new String[] {
            "-frontend", ValidSEIGenerator.FRONTEND_NAME,
            WSDL.toAbsolutePath().toString() }).run(context);

        return new FrontendRun(output);
    }

    /** @return the generated service endpoint interface, as it was written. */
    String generatedInterface() throws Exception {
        Path file = output.resolve(INTERFACE);
        assertTrue(Files.exists(file), "expected the interface at " + file);
        return Files.readString(file);
    }

    private static void deleteRecursively(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> files = Files.walk(directory)) {
            for (Path path : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
