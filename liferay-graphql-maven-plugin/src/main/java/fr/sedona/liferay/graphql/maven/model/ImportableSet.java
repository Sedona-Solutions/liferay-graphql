package fr.sedona.liferay.graphql.maven.model;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.context.FieldValueResolver;
import fr.sedona.liferay.graphql.maven.util.Constants;
import fr.sedona.liferay.graphql.maven.util.GraphQLType;
import fr.sedona.liferay.graphql.maven.util.HandlebarsUtil;
import lombok.Data;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Data
public class ImportableSet {
    private Log log;
    private boolean overwriteIfSchemaExists;
    private List<String> ignoredAttributes;
    private List<ImportableClass> classes;
    private String outputRegistryDir;
    private String outputEngineImplDir;
    private String outputSchemaDir;
    private String registryReferences;
    private String registryRegisterLoaders;
    private String engineImports;
    private String engineReferences;
    private String engineQueries;
    private String engineMutations;
    private String schemaQueries;
    private String schemaMutations;
    private String schemaTypes;

    public ImportableSet(Log log,
                         List<ImportableClass> classes,
                         Properties properties,
                         String outputRegistryDir,
                         String outputEngineImplDir,
                         String outputSchemaDir) {
        this.log = log;
        this.classes = classes;
        this.classes.sort(Comparator.comparing(ImportableClass::getFqClassName));
        this.outputRegistryDir = outputRegistryDir;
        this.outputEngineImplDir = outputEngineImplDir;
        this.outputSchemaDir = outputSchemaDir;

        prepareIgnoredAttributes(properties);
        prepareRegistryInfo();
        prepareEngineInfo();
        prepareSchemaInfo();
    }

    private void prepareIgnoredAttributes(Properties properties) {
        String values = properties.getProperty(Constants.PROPS_LIFERAY_MODEL_IGNORED_ATTRIBUTES);
        ignoredAttributes = Arrays.asList(values.split(","));
        log.info(ignoredAttributes.size() + " ignored attributes specified");

        overwriteIfSchemaExists = Boolean.parseBoolean(
                properties.getProperty(
                        Constants.PROPS_LIFERAY_OVERWRITE_SCHEMA_FILE,
                        Boolean.FALSE.toString()));
        if (overwriteIfSchemaExists) {
            log.info("Schema file will be overwritten even if the file exists");
        } else {
            log.info("Schema file will not be overwritten if the file exists");
        }
    }

    private void prepareRegistryInfo() {
        prepareRegistryReferences();
        prepareRegistryRegisterLoaders();
    }

    private void prepareRegistryReferences() {
        StringBuilder referencesSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                referencesSb.append("\n");
            }

            referencesSb.append("    @Reference\n");
            referencesSb.append("    private ");
            referencesSb.append(clazz.getClassName());
            referencesSb.append("BatchLoader ");
            referencesSb.append(clazz.getClassNameLower());
            referencesSb.append("BatchLoader;\n");
        }
        registryReferences = referencesSb.toString();
    }

    private void prepareRegistryRegisterLoaders() {
        StringBuilder registerLoaderSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                registerLoaderSb.append("\n");
            }

            registerLoaderSb.append("        register(");
            registerLoaderSb.append(clazz.getClassName());
            registerLoaderSb.append("BatchLoader.KEY, ");
            registerLoaderSb.append(clazz.getClassNameLower());
            registerLoaderSb.append("BatchLoader);\n");
        }
        registryRegisterLoaders = registerLoaderSb.toString();
    }

    private void prepareEngineInfo() {
        prepareEngineImports();
        prepareEngineReferences();
        prepareEngineQueries();
        prepareEngineMutations();
    }

    private void prepareEngineImports() {
        StringBuilder importsSb = new StringBuilder();
        for (ImportableClass clazz : classes) {
            importsSb.append("import ");
            importsSb.append(clazz.getResolversPackagePath());
            importsSb.append(".");
            importsSb.append(clazz.getClassName());
            importsSb.append("Resolvers;\n");
        }
        engineImports = importsSb.toString();
    }

    private void prepareEngineReferences() {
        StringBuilder referencesSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                referencesSb.append("\n");
            }

            String resolversName = clazz.getClassNameLower() + "Resolvers";
            referencesSb.append("    @Reference\n");
            referencesSb.append("    private ");
            referencesSb.append(clazz.getClassName());
            referencesSb.append("Resolvers ");
            referencesSb.append(resolversName);
            referencesSb.append(";\n");
        }
        engineReferences = referencesSb.toString();
    }

    private void prepareEngineQueries() {
        StringBuilder queriesSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                queriesSb.append("\n");
            }

            String resolversName = clazz.getClassNameLower() + "Resolvers";
            queriesSb.append("                        // START -- Query resolvers for class ");
            queriesSb.append(clazz.getFqClassName());
            queriesSb.append("\n");
            queriesSb.append("                        .dataFetcher(\"");
            queriesSb.append(clazz.getClassNamePluralLower());
            queriesSb.append("\", ");
            queriesSb.append(resolversName);
            queriesSb.append(".get");
            queriesSb.append(clazz.getClassNamePlural());
            queriesSb.append("DataFetcher())\n");
            queriesSb.append("                        .dataFetcher(\"");
            queriesSb.append(clazz.getClassNameLower());
            queriesSb.append("\", ");
            queriesSb.append(resolversName);
            queriesSb.append(".get");
            queriesSb.append(clazz.getClassName());
            queriesSb.append("DataFetcher())\n");
            queriesSb.append("                        // END -- Query resolvers for class ");
            queriesSb.append(clazz.getFqClassName());
            queriesSb.append("\n");
        }
        engineQueries = queriesSb.toString();
    }

    private void prepareEngineMutations() {
        StringBuilder mutationsSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                mutationsSb.append("\n");
            }

            String resolversName = clazz.getClassNameLower() + "Resolvers";
            mutationsSb.append("                        // START -- Mutation resolvers for class ");
            mutationsSb.append(clazz.getFqClassName());
            mutationsSb.append("\n");
            mutationsSb.append("                        .dataFetcher(\"create");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("\", ");
            mutationsSb.append(resolversName);
            mutationsSb.append(".create");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("DataFetcher())\n");
            mutationsSb.append("                        .dataFetcher(\"update");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("\", ");
            mutationsSb.append(resolversName);
            mutationsSb.append(".update");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("DataFetcher())\n");
            mutationsSb.append("                        .dataFetcher(\"delete");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("\", ");
            mutationsSb.append(resolversName);
            mutationsSb.append(".delete");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("DataFetcher())\n");
            mutationsSb.append("                        // END -- Mutation resolvers for class ");
            mutationsSb.append(clazz.getFqClassName());
            mutationsSb.append("\n");
        }
        engineMutations = mutationsSb.toString();
    }

    private void prepareSchemaInfo() {
        prepareSchemaQueries();
        prepareSchemaMutations();
        prepareSchemaTypes();
    }

    private void prepareSchemaQueries() {
        StringBuilder queriesSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                queriesSb.append("\n");
            }

            queriesSb.append("    # START -- Queries for class ");
            queriesSb.append(clazz.getFqClassName());
            queriesSb.append("\n");

            // Queries: get all
            queriesSb.append("    ");
            queriesSb.append(clazz.getClassNamePluralLower());
            queriesSb.append("(\n");
            queriesSb.append("        start: Int,\n");
            queriesSb.append("        end: Int\n");
            queriesSb.append("    ): [");
            queriesSb.append(clazz.getClassName());
            queriesSb.append("]\n");
            queriesSb.append("\n");

            // Queries: get one
            queriesSb.append("    ");
            queriesSb.append(clazz.getClassNameLower());
            queriesSb.append("(\n");
            queriesSb.append("        ");
            queriesSb.append(clazz.getGetOneParamName());
            if (clazz.isGetOneByLongId()) {
                queriesSb.append(": Long\n");
            } else {
                queriesSb.append(": String\n");
            }
            queriesSb.append("    ): ");
            queriesSb.append(clazz.getClassName());
            queriesSb.append("\n");

            queriesSb.append("    # END -- Queries for class ");
            queriesSb.append(clazz.getFqClassName());
            queriesSb.append("\n");
        }
        schemaQueries = queriesSb.toString();
    }

    private void prepareSchemaMutations() {
        StringBuilder mutationsSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                mutationsSb.append("\n");
            }

            mutationsSb.append("    # START -- Mutations for class ");
            mutationsSb.append(clazz.getFqClassName());
            mutationsSb.append("\n");

            // Mutations: create
            mutationsSb.append("    create");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("(\n");
            processMethodParameters(clazz.getCreateMethod(), mutationsSb);
            mutationsSb.append("    ): ");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("\n");
            mutationsSb.append("\n");

            // Mutations: update
            mutationsSb.append("    update");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("(\n");
            processMethodParameters(clazz.getUpdateMethod(), mutationsSb);
            mutationsSb.append("    ): ");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("\n");
            mutationsSb.append("\n");

            // Mutations: delete
            mutationsSb.append("    delete");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("(\n");
            mutationsSb.append("        ");
            mutationsSb.append(clazz.getClassNameLower());
            mutationsSb.append("Id: Long\n");
            mutationsSb.append("    ): ");
            mutationsSb.append(clazz.getClassName());
            mutationsSb.append("\n");

            mutationsSb.append("    # END -- Mutations for class ");
            mutationsSb.append(clazz.getFqClassName());
            mutationsSb.append("\n");
        }
        schemaMutations = mutationsSb.toString();
    }

    private void prepareSchemaTypes() {
        StringBuilder typesSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                typesSb.append("\n");
            }

            typesSb.append("# START -- Type for class ");
            typesSb.append(clazz.getFqClassName());
            typesSb.append("\n");

            // Types
            typesSb.append("type ");
            typesSb.append(clazz.getClassName());
            typesSb.append(" {\n");
            processTypeAttributes(clazz.getObjectClass(), typesSb);
            typesSb.append("}\n");

            typesSb.append("# END -- Type for class ");
            typesSb.append(clazz.getFqClassName());
            typesSb.append("\n");
        }
        schemaTypes = typesSb.toString();
    }

    private void processMethodParameters(Method method, StringBuilder sb) {
        if (method == null) {
            return;
        }

        for (int i = 0; i < method.getParameterCount(); i++) {
            Parameter param = method.getParameters()[i];
            sb.append("        ");
            sb.append(param.getName());
            sb.append(": ");
            sb.append(getType(param.getType()));
            if (i + 1 < method.getParameterCount()) {
                sb.append(",");
            }
            sb.append("\n");
        }
    }

    private String getType(Class clazz) {
        GraphQLType type = GraphQLType.fromClass(clazz);
        if (type != null) {
            return type.getSchemaType();
        } else {
            return clazz.getSimpleName();
        }
    }

    private void processTypeAttributes(Class objectClass, StringBuilder sb) {
        Set<String> processedAttributes = new HashSet<>();
        Arrays.stream(objectClass.getDeclaredMethods())
                .distinct()
                .filter(method -> (method.getName().startsWith(Constants.METHOD_GET)
                        || method.getName().startsWith(Constants.METHOD_IS))
                        && method.getParameterCount() == 0
                        && !ignoredAttributes.contains(getAttributeName(method)))
                .sorted(Comparator.comparing(Method::getName))
                .forEach(method -> {
                    String attributeName = getAttributeName(method);
                    if (processedAttributes.contains(attributeName)) {
                        return;
                    }

                    sb.append("    ");
                    sb.append(attributeName);
                    sb.append(": ");
                    sb.append(getType(method.getReturnType()));
                    sb.append("\n");
                    processedAttributes.add(attributeName);
                });
    }

    private String getAttributeName(Method method) {
        String methodName = method.getName();
        String attributeName;
        if (methodName.startsWith(Constants.METHOD_GET)) {
            attributeName = methodName.substring(Constants.METHOD_GET.length());
        } else if (methodName.startsWith(Constants.METHOD_IS)) {
            attributeName = methodName.substring(Constants.METHOD_IS.length());
        } else {
            log.warn("Method name does not start with 'get' or 'is': " + methodName);
            attributeName = "";
        }
        return attributeName.toLowerCase().substring(0, 1) + attributeName.substring(1);
    }

    public void generateSource() throws MojoExecutionException {
        log.info("Generating sources for whole set");

        Context handlebarsCtx = Context.newBuilder(this)
                .resolver(FieldValueResolver.INSTANCE)
                .build();

        log.info("Generating source file from template: GraphQLRegistry.java");
        HandlebarsUtil.generateFromTemplate("GraphQLRegistry.java",
                handlebarsCtx,
                outputRegistryDir + "/" + "GraphQLRegistry.java",
                overwriteIfSchemaExists,
                log);
        log.info("Generating source file from template: GraphQLEngineImpl.java");
        HandlebarsUtil.generateFromTemplate("GraphQLEngineImpl.java",
                handlebarsCtx,
                outputEngineImplDir + "/" + "GraphQLEngineImpl.java",
                overwriteIfSchemaExists,
                log);
        log.info("Generating source file from template: liferay-schema.graphql");
        HandlebarsUtil.generateFromTemplate("liferay-schema.graphql",
                handlebarsCtx,
                outputSchemaDir + "/" + "liferay-schema.graphql",
                overwriteIfSchemaExists,
                log);
    }
}
