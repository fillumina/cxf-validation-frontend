package com.fillumina.cxf.validation;

import com.sun.tools.xjc.BadCommandLineException;
import java.util.Arrays;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.generators.SEIGenerator;
import org.apache.cxf.tools.wsdlto.frontend.jaxws.processor.WSDLToJavaProcessor;

/**
 * Adds the {@code @Valid} annotation to the methods and the parameters of the service endpoint
 * interface CXF generates from a WSDL.
 *
 * <p>The frontend is switched on with {@code -frontend bean-validation}, which also runs CXF's own
 * generators: this class extends the generator CXF uses for the interface and only adds the
 * annotations to the model before handing it back.
 *
 * <p>Which of the method, the incoming parameters and the outgoing parameters are annotated is the
 * {@code generateAnnotations} option, read from the XJC arguments CXF passes
 * through.
 *
 * @author Vojtech Krasa
 * @author Francesco Illuminati
 */
public class ValidSEIGenerator extends SEIGenerator {

    static final String FRONTEND_NAME = "bean-validation";

    @Override
    public String getName() {
        return FRONTEND_NAME;
    }

    @Override
    public void generate(ToolContext context) throws ToolException {
        ServiceValidationOptions options = parseArguments(context);

        Map<QName, JavaModel> models =
                CastUtils.cast((Map<?, ?>) context.get(WSDLToJavaProcessor.MODEL_MAP));
        if (models != null) {
            ProcessorForJavaModel processor = new ProcessorForJavaModel(options);
            models.values().forEach(processor::process);
        }

        super.generate(context);
    }

    /**
     * Reads the option out of the XJC arguments, which CXF passes to the frontend unchanged.
     */
    private ServiceValidationOptions parseArguments(ToolContext context) throws ToolException {
        ServiceValidationOptions.Builder builder = ServiceValidationOptions.builder();

        String[] xjcArguments = (String[]) context.get(ToolConstants.CFG_XJC_ARGS);
        if (xjcArguments != null) {
            for (String argument : Arrays.asList(xjcArguments)) {
                try {
                    builder.parseArgument(argument);
                } catch (BadCommandLineException ex) {
                    throw new ToolException("cannot read the arguments of the frontend, they are: "
                            + Arrays.toString(xjcArguments), ex);
                }
            }
        }

        ServiceValidationOptions options = builder.build();
        options.logActualOptions();
        return options;
    }
}
