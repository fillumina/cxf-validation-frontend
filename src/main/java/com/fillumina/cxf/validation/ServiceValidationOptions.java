package com.fillumina.cxf.validation;

import com.sun.tools.xjc.BadCommandLineException;

/**
 * The options of the {@code bean-validation} frontend.
 *
 * <p>They travel among the XJC arguments, under a name of their own, and are accepted there by
 * {@link FrontendOptionsPlugin}:
 *
 * <pre>
 * -xjc-XCxfValidationFrontendOptions:generateServiceValidationAnnotations=inOut
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

    /** The option that says which sides of a method carry the annotation. */
    public static final String OPTION_NAME = "generateServiceValidationAnnotations";

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

    /** @return whether any side carries the annotation, and the import is therefore needed. */
    public boolean writesSomething() {
        return validIn || validOut;
    }

    void logActualOptions() {
        if (verbose) {
            System.out.println("[" + ValidSEIGenerator.FRONTEND_NAME + "] "
                    + OPTION_NAME + ": " + value());
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
