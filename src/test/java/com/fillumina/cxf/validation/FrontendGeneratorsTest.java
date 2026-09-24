package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The generators CXF's own frontend declares actually run under this one, which is what the single
 * {@code bean-validation} frontend is for: one option generates the interface and validates it.
 *
 * <p>{@code FrontendDescriptorTest} checks the list in {@code META-INF/tools-plugin.xml}; this
 * checks that the generators on that list run. The service class is the cheapest proof: it comes
 * from CXF's {@code ServiceGenerator}, and the old line's minimal frontend did not produce it.
 */
class FrontendGeneratorsTest {

    private static final String SEI = "com/example/weather/WeatherServicePortType.java";
    private static final String SERVICE = "com/example/weather/WeatherService.java";
    private static final String IMPL = "com/example/weather/WeatherServicePortImpl.java";
    private static final String CLIENT =
            "com/example/weather/WeatherServicePortType_WeatherServicePort_Client.java";
    private static final String SERVER =
            "com/example/weather/WeatherServicePortType_WeatherServicePort_Server.java";

    @Test
    void theServiceGeneratorRunsBesideTheValidatedInterface() throws Exception {
        FrontendRun run = FrontendRun.of("both");

        assertTrue(run.hasFile(SEI), "the interface the frontend validates");
        assertTrue(run.hasFile(SERVICE), "the service class CXF's own generator writes");
        assertTrue(run.generatedInterface().contains("@Valid"), "the interface is annotated");
    }

    @Test
    void theClientServerAndImplGeneratorsRunWhenTheyAreAsked() throws Exception {
        FrontendRun run = FrontendRun.of("both", "-client", "-server", "-impl");

        assertTrue(run.hasFile(IMPL), "the implementation");
        assertTrue(run.hasFile(CLIENT), "the client stub");
        assertTrue(run.hasFile(SERVER), "the server stub");
    }
}
