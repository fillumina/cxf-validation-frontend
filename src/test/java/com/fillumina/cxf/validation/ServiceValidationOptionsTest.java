package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * What the frontend does with the arguments it is given.
 *
 * <p>The first case is the one that matters most: an argument of another plugin has to be left
 * alone. Swallowing one would keep that plugin from running, which XJC would not report.
 */
class ServiceValidationOptionsTest {

    @Test
    void anArgumentOfAnotherPluginIsLeftAlone() throws Exception {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();

        assertFalse(builder.parseArgument("-XBeanValidationAnnotations:generateNotNullAnnotations=true"));
        assertFalse(builder.parseArgument("-XReplacePrimitives"));
        assertFalse(builder.parseArgument("-extension"));

        ServiceValidationOptions options = builder.build();
        assertTrue(options.isValidIn(), "the defaults must survive an argument of another plugin");
        assertTrue(options.isValidOut());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "in,    true,  false",
        "out,   false, true",
        "inOut, true,  true",
        "none,  false, false",
    })
    void thePolicyIsRead(String policy, boolean validIn, boolean validOut) throws Exception {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();

        assertTrue(builder.parseArgument(ServiceValidationOptions.PREFIX
                + ServiceValidationOptions.OPTION_NAME + "=" + policy));

        ServiceValidationOptions options = builder.build();
        assertEquals(validIn, options.isValidIn());
        assertEquals(validOut, options.isValidOut());
    }

    @Test
    void anUnknownPolicyIsRefused() {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();

        assertThrows(Exception.class, () -> builder.parseArgument(
                ServiceValidationOptions.PREFIX + ServiceValidationOptions.OPTION_NAME + "=sideways"));
    }
}
