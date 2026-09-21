package com.fillumina.cxf.validation;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import java.io.IOException;
import org.xml.sax.ErrorHandler;

/**
 * Carries the options of the {@code bean-validation} frontend, and does nothing else.
 *
 * <p>An option of a CXF frontend has to travel among the XJC arguments, because that is the array
 * CXF fills and hands to the frontend. XJC, in turn, refuses an argument that no XJC plugin
 * consumes: it stops with {@code unrecognized parameter} and prints its usage. This plugin exists
 * only to consume them, and its option name, {@code -XCxfValidationFrontendOptions}, says as much.
 *
 * <p>It deliberately answers to a name of its own rather than to the name of the plugin that
 * annotates the generated classes. Two plugins with one option name are a trap: XJC activates the
 * first one it finds for the bare option and returns at the first one that consumes an argument, so
 * the other is left out, silently and according to the order of the classpath.
 *
 * <p>The frontend itself reads the options in the CXF stage, from the arguments CXF passes it, so
 * nothing here parses them.
 *
 * <p>The README has a section on the arrangement, "How an option reaches a CXF frontend, and the
 * XJC plugin that comes with it", including the two rules that keep it from going wrong.
 */
public class FrontendOptionsPlugin extends Plugin {

    @Override
    public String getOptionName() {
        return ServiceValidationOptions.OPTION_PREFIX_NAME;
    }

    @Override
    public String getUsage() {
        return "  -" + ServiceValidationOptions.OPTION_PREFIX_NAME
                + ":" + ServiceValidationOptions.OPTION_NAME + "=in|out|inOut|none\n"
                + "      :  read by the cxf-validation-frontend frontend, which writes @Valid on the"
                + " service endpoint interface it generates. This plugin only accepts the option:"
                + " XJC refuses an argument no plugin consumes, and a frontend's options travel"
                + " among the XJC arguments\n";
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
