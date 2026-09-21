package com.fillumina.cxf.validation;

import org.apache.cxf.tools.common.model.JAnnotation;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaParameter;

/**
 * Annotates one method of the interface: its return value, and the parameters it takes and returns,
 * each according to the option in use.
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

    void process(JavaMethod javaMethod) {
        if (options.isValidOut()) {
            log("adding the annotation to the return value of " + javaMethod.getSignature());
            javaMethod.addAnnotation(VALID_RETURN, valid);
        }
        javaMethod.getParameters().forEach(this::process);
    }

    private void process(JavaParameter javaParameter) {
        if (options.isValidIn() && (javaParameter.isIN() || javaParameter.isINOUT())) {
            log("adding the annotation to the incoming " + javaParameter.getName());
            javaParameter.addAnnotation(VALID_PARAM, valid);
        }
        if (options.isValidOut() && (javaParameter.isOUT() || javaParameter.isINOUT())) {
            log("adding the annotation to the outgoing " + javaParameter.getName());
            javaParameter.addAnnotation(VALID_RETURN, valid);
        }
    }

    private void log(String message) {
        if (options.isVerbose()) {
            System.out.println("[" + ValidSEIGenerator.FRONTEND_NAME + "] " + message);
        }
    }
}
