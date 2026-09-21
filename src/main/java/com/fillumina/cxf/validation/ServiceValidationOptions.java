package com.fillumina.cxf.validation;

import com.sun.tools.xjc.BadCommandLineException;

/**
 * The options of this frontend.
 *
 * <p>They are written among the XJC arguments, after the name of the plugin whose vocabulary they
 * belong to, so that a build that already passes the options of the annotation plugin passes these
 * the same way:
 *
 * <pre>
 * -xjc-XBeanValidationAnnotations:generateServiceValidationAnnotations=inOut
 * </pre>
 *
 * <p>Only this one option is read here. The frontend needs no other, and it deliberately does not
 * depend on the plugin that writes the annotations of the generated classes.
 */
public final class ServiceValidationOptions {

    /** The option name, which is the one the frontend answered to before it was split out. */
    static final String OPTION_NAME = "generateServiceValidationAnnotations";
    static final String PREFIX = "-XBeanValidationAnnotations:";

    private final boolean validIn;
    private final boolean validOut;
    private final boolean verbose;

    private ServiceValidationOptions(boolean validIn, boolean validOut, boolean verbose) {
        this.validIn = validIn;
        this.validOut = validOut;
        this.verbose = verbose;
    }

    public boolean isValidIn() {
        return validIn;
    }

    public boolean isValidOut() {
        return validOut;
    }

    public boolean isVerbose() {
        return verbose;
    }

    void logActualOptions() {
        if (verbose) {
            System.out.println("[" + ValidSEIGenerator.FRONTEND_NAME + "] "
                    + "generateServiceValidationAnnotations: " + value());
        }
    }

    private String value() {
        if (validIn && validOut) {
            return "inOut";
        }
        return validIn ? "in" : validOut ? "out" : "none";
    }

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
         * Reads one argument, and does nothing when it is not one of this frontend's.
         *
         * @return true when the argument belonged to this frontend
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
                switch (value.toLowerCase()) {
                    case "in" -> {
                        validIn = true;
                        validOut = false;
                    }
                    case "out" -> {
                        validIn = false;
                        validOut = true;
                    }
                    case "inout" -> {
                        validIn = true;
                        validOut = true;
                    }
                    case "none" -> {
                        validIn = false;
                        validOut = false;
                    }
                    default -> throw new BadCommandLineException("option " + OPTION_NAME
                            + " accepts in, out, inOut or none, and not '" + value + "'");
                }
            }
            return true;
        }

        public ServiceValidationOptions build() {
            return new ServiceValidationOptions(validIn, validOut, verbose);
        }
    }
}
