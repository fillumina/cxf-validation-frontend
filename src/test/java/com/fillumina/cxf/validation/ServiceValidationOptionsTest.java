package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.tools.xjc.BadCommandLineException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
        assertFalse(builder.parseArgument("-XCxfValidationFrontendOptionsOther:unknown=none"));
        assertFalse(builder.parseArgument("-extension"));

        ServiceValidationOptions options = builder.build();
        assertTrue(options.isValidIn(), "the defaults must survive an argument of another plugin");
        assertTrue(options.isValidOut());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        // the names a reader understands
        "request,  true,  false",
        "response, false, true",
        "both,     true,  true",
        "none,     false, false",
        // the names the WSDL gives the same two sides, whatever the case
        "in,    true,  false",
        "out,   false, true",
        "inOut, true,  true",
        "IN,    true,  false",
        "Out,   false, true",
        "INOUT, true,  true",
    })
    void thePolicyIsRead(String policy, boolean validIn, boolean validOut) throws Exception {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();

        assertTrue(builder.parseArgument(ServiceValidationOptions.PREFIX
                + ServiceValidationOptions.OPTION_NAME + "=" + policy));

        ServiceValidationOptions options = builder.build();
        assertEquals(validIn, options.isValidIn());
        assertEquals(validOut, options.isValidOut());
    }

    @ParameterizedTest
    @CsvSource({
        "generateAnnotatons=none, generateAnnotatons",
        "unknown=none, unknown",
        "verboseTypo=true, verboseTypo",
        "':', option",
        "'', option"
    })
    void anUnknownNameIsRefused(String suffix, String badName) {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();
        String argument = "-" + ServiceValidationOptions.OPTION_PREFIX_NAME
                + (suffix.isEmpty() ? "" : suffix.startsWith(":") ? suffix : ":" + suffix);
        Exception thrown = assertThrows(Exception.class, () -> builder.parseArgument(argument));
        assertTrue(thrown.getMessage().contains(badName), thrown.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "true", "TRUE" })
    void aVerboseSwitchIsOnWhenItNamesOnOrSaysNothing(String value) throws Exception {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();
        assertTrue(builder.parseArgument("-XCxfValidationFrontendOptions:verbose"
                + (value.isEmpty() ? "" : "=" + value)));
        assertTrue(builder.build().isVerbose());
    }

    @ParameterizedTest
    @ValueSource(strings = { "false", "FALSE" })
    void aVerboseSwitchIsOffWhenItNamesOff(String value) throws Exception {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();
        assertTrue(builder.parseArgument("-XCxfValidationFrontendOptions:verbose=" + value));
        assertFalse(builder.build().isVerbose());
    }

    @ParameterizedTest
    @ValueSource(strings = { "typo", "1", "yes", "no" })
    void aVerboseSwitchWithAValueThatNamesNoStateIsRefused(String value) {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();
        Exception thrown = assertThrows(BadCommandLineException.class,
                () -> builder.parseArgument("-XCxfValidationFrontendOptions:verbose=" + value));
        assertTrue(thrown.getMessage().contains("verbose"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains(value), thrown.getMessage());
    }

    @Test
    void anUnknownPolicyIsRefused() {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();

        Exception thrown = assertThrows(Exception.class, () -> builder.parseArgument(
                ServiceValidationOptions.PREFIX + ServiceValidationOptions.OPTION_NAME + "=sideways"));

        // the message says what is accepted, so the reader does not have to look it up
        assertTrue(thrown.getMessage().contains("request"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("both"), thrown.getMessage());
    }
}
