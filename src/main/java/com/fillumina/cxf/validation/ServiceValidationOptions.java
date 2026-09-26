package com.fillumina.cxf.validation;

import com.sun.tools.xjc.BadCommandLineException;

/**
 * The options of the {@code bean-validation} frontend.
 *
 * <p>They travel among the XJC arguments, under a name of their own, and are accepted there by
 * {@link FrontendOptionsPlugin}:
 *
 * <pre>
 * -xjc-XCxfValidationFrontendOptions:generateAnnotations=both
 * </pre>
 *
 * <p>The name is this project's. It is not the name of the plugin that annotates the generated
 * classes: an option that two plugins answer to makes XJC activate and consult only the first of
 * them, which is silent and depends on the classpath order. Unknown names under this prefix
 * fail rather than silently leaving the default policy in effect. Only this policy and verbose are
 * read here; the frontend deliberately does not depend on that plugin.
 */
public final class ServiceValidationOptions {

    /** The name of the option among the XJC arguments, without its leading dash. */
    public static final String OPTION_PREFIX_NAME = "XCxfValidationFrontendOptions";

    /** What an option of this frontend starts with, as it is written on the command line. */
    public static final String PREFIX = "-" + OPTION_PREFIX_NAME + ":";

    /** The option that says where {@code @Valid} is written on a method. */
    public static final String OPTION_NAME = "generateAnnotations";

    private final boolean validIn;
    private final boolean validOut;
    private final boolean verbose;

    private ServiceValidationOptions(boolean validIn, boolean validOut, boolean verbose) {
        this.validIn = validIn;
        this.validOut = validOut;
        this.verbose = verbose;
    }

    /**
     * Whether {@code @Valid} is written on the parameters the service takes.
     *
     * @return true when it is
     */
    public boolean isValidIn() {
        return validIn;
    }

    /**
     * Whether {@code @Valid} is written on non-void method returns and on outgoing parameters,
     * including INOUT holders. An INOUT holder gets one annotation even when both sides are selected.
     *
     * @return true when it is
     */
    public boolean isValidOut() {
        return validOut;
    }

    /**
     * Whether the frontend reports which annotation it wrote where.
     *
     * @return true when it does
     */
    public boolean isVerbose() {
        return verbose;
    }

    void logActualOptions() {
        if (verbose) {
            System.out.println("[" + ValidSEIGenerator.FRONTEND_NAME + "] "
                    + OPTION_NAME + ": " + value());
        }
    }

    private String value() {
        if (validIn && validOut) {
            return "both";
        }
        return validIn ? "request" : validOut ? "response" : "none";
    }

    /**
     * The way to the options of the frontend.
     *
     * @return a builder, whose defaults are the ones the frontend starts with
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Collects the options as the arguments are read one by one. */
    public static final class Builder {

        private boolean validIn = true;
        private boolean validOut = true;
        private boolean verbose = false;

        private Builder() {
        }

        /**
         * Reads one argument, rejecting unknown names and values under this frontend's prefix,
         * and doing nothing when the argument is not one of this frontend's. An argument of
         * another plugin has to be left alone: XJC returns at the first plugin that consumes an
         * argument, so a plugin that claims too much keeps the other's options from being read.
         *
         * @param argument one argument of the command line, as it was written
         * @return true when the argument belonged to this frontend
         * @throws BadCommandLineException when the argument is this frontend's but its name or
         *     value is not accepted
         */
        public boolean parseArgument(String argument) throws BadCommandLineException {
            if (!argument.equals("-" + OPTION_PREFIX_NAME) && !argument.startsWith(PREFIX)) {
                return false;
            }
            if (argument.equals("-" + OPTION_PREFIX_NAME)) {
                throw new BadCommandLineException("missing option name after " + PREFIX
                        + " (expected " + OPTION_NAME + " or verbose)");
            }
            String option = argument.substring(PREFIX.length());
            int equals = option.indexOf('=');
            String name = equals < 0 ? option : option.substring(0, equals);
            String value = equals < 0 ? "" : option.substring(equals + 1).trim();

            if ("verbose".equals(name)) {
                verbose = readVerbose(value);
            } else if (OPTION_NAME.equals(name)) {
                // request and response are what the value means to a reader; in and out are what
                // the WSDL calls the two messages, and what WebParam.Mode calls them in the
                // interface this frontend annotates, so both spellings are accepted
                switch (value.toLowerCase()) {
                    case "request", "in" -> {
                        validIn = true;
                        validOut = false;
                    }
                    case "response", "out" -> {
                        validIn = false;
                        validOut = true;
                    }
                    case "both", "inout" -> {
                        validIn = true;
                        validOut = true;
                    }
                    case "none" -> {
                        validIn = false;
                        validOut = false;
                    }
                    default -> throw new BadCommandLineException("option " + OPTION_NAME
                            + " accepts request, response, both or none, or the in, out and inout of"
                            + " the WSDL, and not '" + value + "'");
                }
            } else {
                throw new BadCommandLineException("unknown frontend option '" + name
                        + "' in " + argument + " (expected " + OPTION_NAME + " or verbose)");
            }
            return true;
        }

        /**
         * Reads the value of {@code verbose}. Written on its own it means on, and `true` and
         * `false`, in any case, are the two values that name a state. Anything else is refused
         * like any other unknown value, rather than being read as on because it is not
         * `false`.
         *
         * @param value what followed the equals sign, empty when the option was written bare
         * @return whether verbose is on
         * @throws BadCommandLineException when the value names no state
         */
        private static boolean readVerbose(String value) throws BadCommandLineException {
            if (value.isEmpty() || "true".equalsIgnoreCase(value)) {
                return true;
            }
            if ("false".equalsIgnoreCase(value)) {
                return false;
            }
            throw new BadCommandLineException("option verbose accepts true or false, either on its"
                    + " own or as its value, and not '" + value + "'");
        }

        /**
         * The end of the collection.
         *
         * @return the options, ready to be read by the frontend
         */
        public ServiceValidationOptions build() {
            return new ServiceValidationOptions(validIn, validOut, verbose);
        }
    }
}
