package fr.sedona.liferay.graphql.maven;

import fr.sedona.liferay.graphql.maven.model.ImportableClass;
import fr.sedona.liferay.graphql.maven.model.ImportableSet;
import fr.sedona.liferay.graphql.maven.util.Constants;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Mojo(name = "generate-sources")
public class LiferayGraphQLMojo extends AbstractMojo {

    @Parameter
    private String outputResolversDir;

    @Parameter
    private String outputResolversImplDir;

    @Parameter
    private String outputBatchLoaderDir;

    @Parameter
    private String outputRegistryDir;

    @Parameter
    private String outputEngineImplDir;

    @Parameter
    private String outputSchemaDir;

    @Parameter
    private File importablePropertiesFile;

    @Parameter
    private Properties importableClasses;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isInvalidParameter(outputResolversDir)) {
            throw new MojoFailureException("No output directory is specified for Resolvers");
        }

        if (isInvalidParameter(outputResolversImplDir)) {
            throw new MojoFailureException("No output directory is specified for ResolversImpl");
        }

        if (isInvalidParameter(outputBatchLoaderDir)) {
            throw new MojoFailureException("No output directory is specified for BatchLoader");
        }

        if (isInvalidParameter(outputRegistryDir)) {
            throw new MojoFailureException("No output directory is specified for GraphQLRegistry");
        }

        if (isInvalidParameter(outputEngineImplDir)) {
            throw new MojoFailureException("No output directory is specified for GraphQLEngineImpl");
        }

        if (isInvalidParameter(outputSchemaDir)) {
            throw new MojoFailureException("No output directory is specified for GraphQL schema");
        }

        if (importablePropertiesFile == null && (importableClasses == null || importableClasses.isEmpty())) {
            throw new MojoFailureException("No importable classes specified");
        }

        if (importablePropertiesFile != null) {
            importableClasses = new Properties();
            try {
                importableClasses.load(new FileReader(importablePropertiesFile));
            } catch (IOException e) {
                throw new MojoFailureException("Could not load properties file:" + importablePropertiesFile.getAbsolutePath(), e);
            }
        }

        getLog().info("Generating sources for specified classes");
        List<ImportableClass> classesToProcess = new ArrayList<>();
        for (String importableClassNamePropsKey : importableClasses.stringPropertyNames()) {
            if (!importableClassNamePropsKey.startsWith(Constants.PROPS_LIFERAY_SERVICE_PREFIX)) {
                continue;
            }

            String importableClassName = importableClassNamePropsKey.substring(Constants.PROPS_LIFERAY_SERVICE_PREFIX.length());
            getLog().info("Generating sources for specified class " + importableClassName);
            String importableClassService = importableClasses.getProperty(importableClassNamePropsKey);
            ImportableClass importClass;
            try {
                importClass = new ImportableClass(getLog(),
                        importableClassName,
                        importableClassService,
                        importableClasses,
                        outputResolversDir,
                        outputResolversImplDir,
                        outputBatchLoaderDir);
            } catch (ClassNotFoundException e) {
                throw new MojoExecutionException("Could not introspect service or model class " + importableClassService, e);
            }
            classesToProcess.add(importClass);
            importClass.generateSource();
        }
        ImportableSet set = new ImportableSet(getLog(),
                classesToProcess,
                importableClasses,
                outputRegistryDir,
                outputEngineImplDir,
                outputSchemaDir);
        set.generateSource();
        getLog().info("Sources generated for all specified classes");
    }

    private boolean isInvalidParameter(String param) {
        return param == null || param.trim().length() == 0;
    }
}
