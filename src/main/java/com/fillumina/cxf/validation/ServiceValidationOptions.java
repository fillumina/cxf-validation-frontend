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
 * them, which is silent and depends on the classpath order. Only this one option is read here, and
 * the frontend deliberately does not depend on that plugin.
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
     * Whether {@code @Valid} is written on the method itself and on the parameters the service
     * returns.
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
         * Reads one argument, and does nothing when it is not one of this frontend's. An argument
         * of another plugin has to be left alone: XJC returns at the first plugin that consumes an
         * argument, so a plugin that claims too much keeps the other's options from being read.
         *
         * @param argument one argument of the command line, as it was written
         * @return true when the argument belonged to this frontend
         * @throws BadCommandLineException when the argument is this frontend's but its value is not
         *     one of the accepted ones
         */
        public boolean parseArgument(String argument) throws BadCommandLineException {
            if (!argument.startsWith(PREFIX)) {
                return false;
            }
            String option = argument.substring(PREFIX.length());
            int equals = option.indexOf('=');
            String name = equals < 0 ? option : option.substring(0, equals);
            String value = equals < 0 ? "" : option.substring(equals + 1).trim();

            if ("verbose".equals(name)) {
                verbose = !"false".equalsIgnoreCase(value);
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
            }
            return true;
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
