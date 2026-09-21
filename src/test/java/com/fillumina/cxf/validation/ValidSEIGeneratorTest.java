package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Where {@code @Valid} lands, for each value of the option that decides it.
 *
 * <p>The old line had one test class per value and per annotation library; there is one library
 * here, so the four values are four cases of one test.
 */
class ValidSEIGeneratorTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "in,    false, true",
        "out,   true,  false",
        "inOut, true,  true",
        "none,  false, false",
    })
    void theAnnotationIsWrittenWhereTheOptionSays(String policy, boolean onTheMethod,
            boolean onTheParameter) throws Exception {
        String generated = FrontendRun.of(policy).generatedInterface();

        assertEquals(onTheMethod, theMethodCarriesIt(generated), generated);
        assertEquals(onTheParameter, theParameterCarriesIt(generated), generated);
        assertEquals(onTheMethod || onTheParameter,
                generated.contains("import jakarta.validation.Valid;"), generated);
    }

    /** The return value is annotated above the signature, with the other method annotations. */
    private static boolean theMethodCarriesIt(String generated) {
        int signature = signatureOf(generated);
        return generated.substring(generated.lastIndexOf("@WebMethod", signature), signature)
                .contains("@Valid");
    }

    /** The parameter is annotated in the list that follows the signature. */
    private static boolean theParameterCarriesIt(String generated) {
        int signature = signatureOf(generated);
        return generated.substring(signature, generated.indexOf(");", signature))
                .contains("@Valid");
    }

    private static int signatureOf(String generated) {
        int signature = generated.indexOf("getWeather(");
        if (signature < 0) {
            throw new AssertionError("no getWeather in the generated interface: " + generated);
        }
        return signature;
    }
}
