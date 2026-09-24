package com.fillumina.cxf.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** CXF's real source for a one-way void operation and a shared INOUT holder. */
class FrontendEdgeCasesTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "request,  true,  true",
        "response, true,  false",
        "both,     true,  true",
        "none,     false, false"
    })
    void inoutIsAnnotatedOnceAndVoidHasNoReturnConstraint(String policy,
            boolean holderIsValid, boolean voidInputIsValid) throws Exception {
        String source = FrontendRun.of(policy).generatedInterface();
        String update = method(source, "updateCity(");
        String notify = method(source, "notifyCity(");

        assertTrue(update.contains("WebParam.Mode.INOUT"), source);
        assertEquals(holderIsValid ? 1 : 0, count(update, "@Valid"), update);
        assertFalse(beforeSignature(source, "updateCity(").contains("@Valid"), source);
        assertTrue(source.contains("public void notifyCity("), source);
        assertEquals(voidInputIsValid ? 1 : 0, count(notify, "@Valid"), notify);
        assertFalse(beforeSignature(source, "notifyCity(").contains("@Valid"), source);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "request,  false, true",
        "response, true,  false",
        "both,     true,  true",
        "none,     false, false"
    })
    void outOnlyHoldersHaveExactlyOneAnnotationEach(String policy,
            boolean outputIsValid, boolean inputIsValid) throws Exception {
        String source = FrontendRun.of(policy).generatedInterface();
        String lookup = method(source, "lookupCity(");
        String input = lookup.substring(0,
                lookup.indexOf("java.lang.String lookupCity,"));
        String weather = lookup.substring(lookup.indexOf("java.lang.String lookupCity,"),
                lookup.indexOf("Holder<java.lang.String> weather,"));
        String notice = lookup.substring(lookup.indexOf("Holder<java.lang.String> weather,"),
                lookup.indexOf("Holder<java.lang.String> notice"));

        assertTrue(weather.contains("WebParam.Mode.OUT"), lookup);
        assertTrue(notice.contains("WebParam.Mode.OUT"), lookup);
        assertEquals(inputIsValid ? 1 : 0, count(input, "@Valid"), lookup);
        assertEquals(outputIsValid ? 1 : 0, count(weather, "@Valid"), lookup);
        assertEquals(outputIsValid ? 1 : 0, count(notice, "@Valid"), lookup);
        assertFalse(beforeSignature(source, "lookupCity(").contains("@Valid"), source);
    }

    private static String method(String source, String name) {
        int start = source.indexOf(name);
        assertTrue(start >= 0, source);
        return source.substring(start, source.indexOf(");", start));
    }

    private static String beforeSignature(String source, String name) {
        int start = source.indexOf(name);
        assertTrue(start >= 0, source);
        return source.substring(source.lastIndexOf("@WebMethod", start), start);
    }

    private static int count(String text, String token) {
        return (text.length() - text.replace(token, "").length()) / token.length();
    }
}
