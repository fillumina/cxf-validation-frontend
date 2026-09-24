package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.JavaType;
import org.junit.jupiter.api.Test;

/**
 * The import of {@code @Valid} belongs to the interface that carries the annotation, not to every
 * interface of a run.
 *
 * <p>The trigger is the option that annotates only one side together with an operation that has
 * nothing on that side: with {@code in} and no parameter there is nothing to annotate, and an
 * import nothing uses is the kind of detail a reader notices and a compiler does not.
 */
class ProcessorForJavaInterfaceTest {

    @Test
    void anInterfaceWithNothingToAnnotateIsWrittenWithoutTheImport() throws Exception {
        JavaInterface javaInterface = new JavaInterface();
        javaInterface.addMethod(method("ping"));

        new ProcessorForJavaInterface(options("request")).process(javaInterface);

        assertFalse(javaInterface.getImports().hasNext(),
                "nothing was annotated, so the interface needs no import");
    }

    @Test
    void anInterfaceWithAnIncomingParameterCarriesTheImport() throws Exception {
        JavaInterface javaInterface = new JavaInterface();
        javaInterface.addMethod(methodWithIncomingParameter("getWeather"));

        new ProcessorForJavaInterface(options("request")).process(javaInterface);

        assertTrue(javaInterface.getImports().hasNext(),
                "the parameter carries the annotation, so the import is needed");
    }

    /** The same method, with the other option: now the method itself carries the annotation. */
    @Test
    void aMethodAnnotatedOnTheReturnCarriesTheImport() throws Exception {
        JavaInterface javaInterface = new JavaInterface();
        javaInterface.addMethod(method("ping"));

        new ProcessorForJavaInterface(options("response")).process(javaInterface);

        assertTrue(javaInterface.getImports().hasNext(),
                "the method carries the annotation, so the import is needed");
    }

    private static JavaMethod methodWithIncomingParameter(String name) {
        JavaMethod method = method(name);
        JavaParameter parameter = new JavaParameter("city", "String", "");
        parameter.setStyle(JavaType.Style.IN);
        method.addParameter(parameter);
        return method;
    }

    private static JavaMethod method(String name) {
        JavaMethod method = new JavaMethod();
        method.setName(name);
        return method;
    }

    private static ServiceValidationOptions options(String policy) throws Exception {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();
        builder.parseArgument(ServiceValidationOptions.PREFIX
                + ServiceValidationOptions.OPTION_NAME + "=" + policy);
        return builder.build();
    }
}
