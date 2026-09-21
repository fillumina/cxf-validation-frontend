package com.fillumina.cxf.validation;

import jakarta.validation.Valid;
import org.apache.cxf.tools.common.model.JavaInterface;

/** Annotates the methods of one generated interface, which needs the import of the annotation. */
class ProcessorForJavaInterface {

    private final ServiceValidationOptions options;
    private final ProcessorForJavaMethod processorForJavaMethod;

    ProcessorForJavaInterface(ServiceValidationOptions options) {
        this.options = options;
        this.processorForJavaMethod = new ProcessorForJavaMethod(options);
    }

    void process(JavaInterface javaInterface) {
        // with nothing to write, the interface keeps the import out as well
        if (options.writesSomething()) {
            javaInterface.addImport(Valid.class.getCanonicalName());
        }
        javaInterface.getMethods().forEach(processorForJavaMethod::process);
    }
}
