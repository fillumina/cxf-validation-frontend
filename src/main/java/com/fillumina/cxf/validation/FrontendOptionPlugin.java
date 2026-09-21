package com.fillumina.cxf.validation;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import java.io.IOException;
import org.xml.sax.ErrorHandler;

/**
 * Accepts the option of this frontend among the XJC arguments, so that XJC does not refuse it.
 *
 * <p>An argument is passed to XJC by prefixing it with {@code -xjc-}, and XJC rejects one that no
 * plugin consumes. The frontend itself runs later, in the CXF stage, and reads the option out of
 * the arguments it is given, so this class does nothing with it beyond acknowledging it.
 *
 * <p>It answers to the name of the annotation plugin on purpose: a build that carries both the
 * annotation plugin and this frontend passes one option, and XJC hands it to both.
 */
public class FrontendOptionPlugin extends Plugin {

    @Override
    public String getOptionName() {
        return "XBeanValidationAnnotations";
    }

    @Override
    public String getUsage() {
        return "  -XBeanValidationAnnotations:generateServiceValidationAnnotations=in|out|inOut|none"
                + "\n      :  read by the cxf-validation-frontend frontend, which adds @Valid to the"
                + " generated service endpoint interface\n";
    }

    @Override
    public int parseArgument(Options opt, String[] args, int index)
            throws BadCommandLineException, IOException {
        return args[index].startsWith("-" + getOptionName()) ? 1 : 0;
    }

    @Override
    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) {
        // the annotations of the generated classes are not this plugin's business
        return true;
    }
}
