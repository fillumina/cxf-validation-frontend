package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;

/** The generated interface must compile and expose valid metadata to a real provider. */
class GeneratedInterfaceValidationTest {

    @Test
    void generatedBothCompilesAndTheProviderAcceptsItsMethods() throws Exception {
        FrontendRun run = FrontendRun.of("both");
        Path classes = Path.of("target", "generated-test-classes", "validation");
        Files.createDirectories(classes);
        var compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "a JDK is needed to compile the generated source");
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        try (Stream<Path> files = Files.walk(run.generatedSources());
                StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, null, null)) {
            List<Path> sources = files.filter(path -> path.toString().endsWith(".java")).toList();
            var units = manager.getJavaFileObjectsFromPaths(sources);
            boolean compiled = compiler.getTask(null, manager, diagnostics,
                    List.of("-proc:none", "-classpath", System.getProperty("java.class.path"),
                            "-d", classes.toString()), null, units).call();
            assertTrue(compiled, diagnostics.getDiagnostics().toString());
        }

        try (URLClassLoader loader = new URLClassLoader(new URL[] {classes.toUri().toURL()},
                getClass().getClassLoader());
                ValidatorFactory factory = Validation.byDefaultProvider().configure()
                        .messageInterpolator(new ParameterMessageInterpolator())
                        .buildValidatorFactory()) {
            Class<?> service = loader.loadClass("com.example.weather.WeatherServicePortType");
            Class<?> holder = loader.loadClass("jakarta.xml.ws.Holder");
            var descriptor = factory.getValidator().getConstraintsForClass(service);
            var weather = descriptor.getConstraintsForMethod("getWeather", String.class);
            assertNotNull(weather);
            assertTrue(weather.getReturnValueDescriptor().isCascaded());
            var lookup = descriptor.getConstraintsForMethod("lookupCity", String.class, holder, holder);
            assertNotNull(lookup);
            assertTrue(lookup.getParameterDescriptors().get(0).isCascaded());
            assertTrue(lookup.getParameterDescriptors().get(1).isCascaded());
            assertTrue(lookup.getParameterDescriptors().get(2).isCascaded());
            assertFalse(lookup.getReturnValueDescriptor().isCascaded());
            var update = descriptor.getConstraintsForMethod("updateCity", holder);
            var notify = descriptor.getConstraintsForMethod("notifyCity", String.class);
            assertNotNull(update);
            assertTrue(update.getParameterDescriptors().getFirst().isCascaded());
            assertFalse(update.getReturnValueDescriptor().isCascaded());
            assertNotNull(notify);
            assertTrue(notify.getParameterDescriptors().getFirst().isCascaded());
            assertFalse(notify.getReturnValueDescriptor().isCascaded());
        }
    }
}
