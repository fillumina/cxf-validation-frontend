package com.fillumina.cxf.validation;

import jakarta.validation.Valid;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaMethod;

/** Annotates the methods of one generated interface, which needs the import of the annotation. */
class ProcessorForJavaInterface {

    private final ProcessorForJavaMethod processorForJavaMethod;

    ProcessorForJavaInterface(ServiceValidationOptions options) {
        this.processorForJavaMethod = new ProcessorForJavaMethod(options);
    }

    void process(JavaInterface javaInterface) {
        boolean written = false;
        for (JavaMethod method : javaInterface.getMethods()) {
            written |= processorForJavaMethod.process(method);
        }
        // the import belongs to the interface that carries the annotation, and an interface where
        // nothing was written needs none: deciding this from the options would put an import into
        // every interface of a run, whether or not it carries anything
        if (written) {
            javaInterface.addImport(Valid.class.getCanonicalName());
        }
    }
}
