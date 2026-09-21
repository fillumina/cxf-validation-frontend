package com.fillumina.cxf.validation;

import org.apache.cxf.tools.common.model.JavaModel;

/** Walks the interfaces of the model of a WSDL and annotates each of their methods. */
class ProcessorForJavaModel {

    private final ProcessorForJavaInterface processorForJavaInterface;

    ProcessorForJavaModel(ServiceValidationOptions options) {
        this.processorForJavaInterface = new ProcessorForJavaInterface(options);
    }

    void process(JavaModel javaModel) {
        javaModel.getInterfaces().values().forEach(processorForJavaInterface::process);
    }
}
