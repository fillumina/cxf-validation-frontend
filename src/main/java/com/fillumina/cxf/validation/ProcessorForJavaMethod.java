package com.fillumina.cxf.validation;

import org.apache.cxf.tools.common.model.JAnnotation;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;

/**
 * Annotates one method of the interface: its non-void return value and its input/output
 * parameters according to the option in use. An INOUT holder is annotated only once.
 */
class ProcessorForJavaMethod {

    /** The keys the annotations are filed under in the model of the method. */
    private static final String VALID_PARAM = "VALID_PARAM";
    private static final String VALID_RETURN = "VALID_RETURN";

    private final ServiceValidationOptions options;
    private final JAnnotation valid;

    ProcessorForJavaMethod(ServiceValidationOptions options) {
        this.options = options;
        this.valid = new JAnnotation(jakarta.validation.Valid.class);
    }

    /**
     * @return true when at least one annotation was written into this method, which is what decides
     *     whether the interface needs the import
     */
    boolean process(JavaMethod javaMethod) {
        boolean written = false;
        if (options.isValidOut() && !"void".equals(javaMethod.getReturnValue())) {
            log("adding the annotation to the return value of " + javaMethod.getSignature());
            javaMethod.addAnnotation(VALID_RETURN, valid);
            written = true;
        }
        for (JavaParameter javaParameter : javaMethod.getParameters()) {
            written |= process(javaParameter);
        }
        return written;
    }

    private boolean process(JavaParameter javaParameter) {
        boolean incoming = options.isValidIn() && (javaParameter.isIN() || javaParameter.isINOUT());
        boolean outgoing = options.isValidOut() && (javaParameter.isOUT() || javaParameter.isINOUT());
        if (incoming || outgoing) {
            log("adding the annotation to " + javaParameter.getName());
            javaParameter.addAnnotation(VALID_PARAM, valid);
            return true;
        }
        return false;
    }

    private void log(String message) {
        if (options.isVerbose()) {
            System.out.println("[" + ValidSEIGenerator.FRONTEND_NAME + "] " + message);
        }
    }
}
