package fr.sedona.liferay.graphql.maven.util;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class HandlebarsUtil {
    private static final String HANDLEBARS_BASE_PATH = "/";
    private static final String HANDLEBARS_EXTENSION = ".hbs";
    private static final String CHARSET_UTF8 = "UTF-8";

    public static Template getTemplate(String fileName) throws IOException {
        TemplateLoader loader = new ClassPathTemplateLoader();
        loader.setPrefix(HANDLEBARS_BASE_PATH);
        loader.setSuffix(HANDLEBARS_EXTENSION);
        Handlebars handlebars = new Handlebars(loader);
        return handlebars.compile(fileName);
    }

    public static void generateFromTemplate(String templateFileName,
                                            Context handlebarsCtx,
                                            String outputFilePath,
                                            boolean overwriteIfFileExists,
                                            Log log)
            throws MojoExecutionException {
        try {
            File outputFile = new File(outputFilePath);
            if (outputFile.exists() && outputFile.isFile() && !overwriteIfFileExists) {
                log.warn("File already exists and file overwriting is set to false");
                return;
            }

            Template resolversTemplate = HandlebarsUtil.getTemplate(templateFileName);
            FileUtils.writeStringToFile(
                    outputFile,
                    resolversTemplate.apply(handlebarsCtx),
                    Charset.forName(CHARSET_UTF8));
        } catch (Exception e) {
            throw new MojoExecutionException("Could not process handlebars template for " + templateFileName, e);
        }
    }

    private HandlebarsUtil() {
        // Do nothing
    }
}
