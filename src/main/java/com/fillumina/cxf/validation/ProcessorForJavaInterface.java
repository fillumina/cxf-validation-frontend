package com.fillumina.cxf.validation;

import jakarta.validation.Valid;
import org.apache.cxf.tools.common.model.JavaInterface;

/** Annotates the methods of one generated interface, which needs the import of the annotation. */
class ProcessorForJavaInterface {

    private final ProcessorForJavaMethod processorForJavaMethod;

    ProcessorForJavaInterface(ServiceValidationOptions options) {
        this.processorForJavaMethod = new ProcessorForJavaMethod(options);
    }

    void process(JavaInterface javaInterface) {
        javaInterface.addImport(Valid.class.getCanonicalName());
        javaInterface.getMethods().forEach(processorForJavaMethod::process);
    }
}
