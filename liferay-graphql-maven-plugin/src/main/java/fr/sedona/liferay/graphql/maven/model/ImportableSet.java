package fr.sedona.liferay.graphql.maven.model;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.context.FieldValueResolver;
import fr.sedona.liferay.graphql.maven.util.Constants;
import fr.sedona.liferay.graphql.maven.util.HandlebarsUtil;
import lombok.Data;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.beans.Introspector;
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
        Collections.sort(this.classes, Comparator.comparing(ImportableClass::getFqClassName));
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
        StringBuilder referencesSb = new StringBuilder();
        StringBuilder registerLoaderSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                referencesSb.append("\n");
                registerLoaderSb.append("\n");
            }

            // Reference
            referencesSb.append("    @Reference\n");
            referencesSb.append("    private ");
            referencesSb.append(clazz.getClassName());
            referencesSb.append("BatchLoader ");
            referencesSb.append(clazz.getClassNameLower());
            referencesSb.append("BatchLoader;\n");

            // Register loader
            registerLoaderSb.append("        register(");
            registerLoaderSb.append(clazz.getClassName());
            registerLoaderSb.append("BatchLoader.KEY, ");
            registerLoaderSb.append(clazz.getClassNameLower());
            registerLoaderSb.append("BatchLoader);\n");
        }
        registryReferences = referencesSb.toString();
        registryRegisterLoaders = registerLoaderSb.toString();
    }

    private void prepareEngineInfo() {
        StringBuilder importsSb = new StringBuilder();
        StringBuilder referencesSb = new StringBuilder();
        StringBuilder queriesSb = new StringBuilder();
        StringBuilder mutationsSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                referencesSb.append("\n");
                queriesSb.append("\n");
                mutationsSb.append("\n");
            }

            // Imports
            importsSb.append("import ");
            importsSb.append(clazz.getResolversPackagePath());
            importsSb.append(".");
            importsSb.append(clazz.getClassName());
            importsSb.append("Resolvers;\n");

            // References
            String resolversName = clazz.getClassNameLower() + "Resolvers";
            referencesSb.append("    @Reference\n");
            referencesSb.append("    private ");
            referencesSb.append(clazz.getClassName());
            referencesSb.append("Resolvers ");
            referencesSb.append(resolversName);
            referencesSb.append(";\n");

            // Queries
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

            // Mutations
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
        engineImports = importsSb.toString();
        engineReferences = referencesSb.toString();
        engineQueries = queriesSb.toString();
        engineMutations = mutationsSb.toString();
    }

    private void prepareSchemaInfo() {
        StringBuilder queriesSb = new StringBuilder();
        StringBuilder mutationsSb = new StringBuilder();
        StringBuilder typesSb = new StringBuilder();
        for (int i = 0; i < classes.size(); i++) {
            ImportableClass clazz = classes.get(i);

            if (i != 0) {
                queriesSb.append("\n");
                mutationsSb.append("\n");
                typesSb.append("\n");
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
        schemaQueries = queriesSb.toString();
        schemaMutations = mutationsSb.toString();
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
            if (i == 0 && (param.getType() == long.class || param.getType() == Long.class)) {
                sb.append("ID");
            } else if (param.getType() == long.class || param.getType() == Long.class) {
                sb.append("Long");
            } else if (param.getType() == double.class || param.getType() == Double.class) {
                sb.append("Float");
            } else if (param.getType() == int.class || param.getType() == Integer.class) {
                sb.append("Int");
            } else if (param.getType() == boolean.class || param.getType() == Boolean.class) {
                sb.append("Boolean");
            } else if (param.getType() == String.class) {
                sb.append("String");
            } else if (param.getType() == long[].class || param.getType() == Long[].class) {
                sb.append("[Long]");
            } else if (param.getType() == double[].class || param.getType() == Double[].class) {
                sb.append("[Float]");
            } else if (param.getType() == int[].class || param.getType() == Integer[].class) {
                sb.append("[Int]");
            } else if (param.getType() == boolean[].class || param.getType() == Boolean[].class) {
                sb.append("[Boolean]");
            } else if (param.getType() == String[].class) {
                sb.append("[String]");
            } else {
                sb.append(param.getType().getSimpleName());
            }
            if (i + 1 < method.getParameterCount()) {
                sb.append(",");
            }
            sb.append("\n");
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
                    if (attributeName.equalsIgnoreCase(objectClass.getSimpleName() + "Id")) {
                        sb.append("ID");
                    } else if (method.getReturnType() == long.class || method.getReturnType() == Long.class) {
                        sb.append("Long");
                    } else if (method.getReturnType() == double.class || method.getReturnType() == Double.class) {
                        sb.append("Float");
                    } else if (method.getReturnType() == int.class || method.getReturnType() == Integer.class) {
                        sb.append("Int");
                    } else if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                        sb.append("Boolean");
                    } else if (method.getReturnType() == String.class) {
                        sb.append("String");
                    } else if (method.getReturnType() == long[].class || method.getReturnType() == Long[].class) {
                        sb.append("[Long]");
                    } else if (method.getReturnType() == double[].class || method.getReturnType() == Double[].class) {
                        sb.append("[Float]");
                    } else if (method.getReturnType() == int[].class || method.getReturnType() == Integer[].class) {
                        sb.append("[Int]");
                    } else if (method.getReturnType() == boolean[].class || method.getReturnType() == Boolean[].class) {
                        sb.append("[Boolean]");
                    } else if (method.getReturnType() == String[].class) {
                        sb.append("[String]");
                    } else {
                        sb.append(method.getReturnType().getSimpleName());
                    }
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
        return Introspector.decapitalize(attributeName);
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
