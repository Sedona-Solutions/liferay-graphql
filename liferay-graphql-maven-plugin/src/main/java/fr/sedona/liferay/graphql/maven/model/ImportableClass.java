package fr.sedona.liferay.graphql.maven.model;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.context.FieldValueResolver;
import fr.sedona.liferay.graphql.maven.util.Constants;
import fr.sedona.liferay.graphql.maven.util.HandlebarsUtil;
import lombok.Data;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@Data
public class ImportableClass {
    private Log log;
    private boolean overwriteIfFileExists;
    private String fqClassName;
    private String fqModelClassName;
    private Class objectClass;
    private String fqServiceName;
    private Class serviceClass;
    private String className;
    private String classNameLower;
    private String classNameUpper;
    private String classNamePlural;
    private String classNamePluralLower;
    private String serviceName;
    private String outputResolversDir;
    private String outputResolversImplDir;
    private String outputResolversImplTestDir;
    private String outputBatchLoaderDir;
    private String resolversPackagePath;
    private String batchLoaderPackagePath;
    private String getAllMethodName;
    private Method getAllMethod;
    private String getAllMethodLambda;
    private String getOneMethodName;
    private boolean getOneByLongId;
    private String getOneParamName;
    private Method getOneMethod;
    private String getOneMethodLambda;
    private String createMethodName;
    private Method createMethod;
    private String createMethodLambda;
    private String updateMethodName;
    private Method updateMethod;
    private String updateMethodLambda;
    private String deleteMethodName;
    private Method deleteMethod;
    private String deleteMethodLambda;
    private String testAttributeDeclaration;
    private String testUtilAttributeMocks;
    private String testAddMethodArgumentsMock;
    private String testAddMethodExpectedResultAttributes;
    private String testAddMethodArgsMock;
    private String testAddMethodArgsMockAny;
    private String testUpdateMethodArgumentsMock;
    private String testUpdateMethodExpectedResultAttributes;
    private String testUpdateMethodArgsMock;
    private String testUpdateMethodArgsMockAny;

    public ImportableClass(Log log,
                           String fqClassName,
                           String fqServiceName,
                           Properties properties,
                           String outputResolversDir,
                           String outputResolversImplDir,
                           String outputResolversImplTestDir,
                           String outputBatchLoaderDir)
            throws ClassNotFoundException {
        this.log = log;
        this.fqClassName = fqClassName;
        this.fqServiceName = fqServiceName;
        this.outputResolversDir = outputResolversDir;
        this.outputResolversImplDir = outputResolversImplDir;
        this.outputResolversImplTestDir = outputResolversImplTestDir;
        this.outputBatchLoaderDir = outputBatchLoaderDir;

        prepareInfo();
        prepareLookupInfo(properties);
        introspectAndPrepare();
        prepareTestClass();
    }

    private void prepareInfo() {
        int lastSepIndex = fqClassName.lastIndexOf(".");
        className = fqClassName.substring(lastSepIndex + 1);
        classNameLower = className.toLowerCase();
        classNameUpper = className.toUpperCase();

        if (className.endsWith("y")) {
            classNamePlural = className.substring(0, className.length() - 1) + "ies";
        } else if (className.endsWith("s")) {
            classNamePlural = className + "es";
        } else {
            classNamePlural = className + "s";
        }
        classNamePluralLower = classNamePlural.toLowerCase();

        serviceName = classNameLower + "LocalService";

        int index = outputResolversDir.indexOf("java/");
        resolversPackagePath = outputResolversDir.substring(index + 5);
        resolversPackagePath = resolversPackagePath.replace("/", ".");

        index = outputBatchLoaderDir.indexOf("java/");
        batchLoaderPackagePath = outputBatchLoaderDir.substring(index + 5);
        batchLoaderPackagePath = batchLoaderPackagePath.replace("/", ".");
    }

    private void prepareLookupInfo(Properties properties) {
        overwriteIfFileExists = Boolean.parseBoolean(
                properties.getProperty(
                        Constants.PROPS_LIFERAY_OVERWRITE_PREFIX + fqClassName,
                        Boolean.FALSE.toString()));

        fqModelClassName = properties.getProperty(
                Constants.PROPS_LIFERAY_MODEL_PREFIX + fqClassName);
        if (fqModelClassName == null || fqModelClassName.isEmpty()) {
            fqModelClassName = fqClassName;
        }

        getAllMethodName = properties.getProperty(
                Constants.PROPS_LIFERAY_METHOD_READ_ALL_PREFIX + fqClassName,
                Constants.METHOD_GET + classNamePlural);
        getOneMethodName = properties.getProperty(
                Constants.PROPS_LIFERAY_METHOD_READ_PREFIX + fqClassName,
                Constants.METHOD_GET + className);
        createMethodName = properties.getProperty(
                Constants.PROPS_LIFERAY_METHOD_CREATE_PREFIX + fqClassName,
                Constants.METHOD_ADD + className);
        updateMethodName = properties.getProperty(
                Constants.PROPS_LIFERAY_METHOD_UPDATE_PREFIX + fqClassName,
                Constants.METHOD_UPDATE + className);
        deleteMethodName = properties.getProperty(
                Constants.PROPS_LIFERAY_METHOD_DELETE_PREFIX + fqClassName,
                Constants.METHOD_DELETE + className);
    }

    private void introspectAndPrepare() throws ClassNotFoundException {
        objectClass = Class.forName(fqModelClassName);
        serviceClass = Class.forName(fqServiceName);
        introspectGetAllMethod(serviceClass);
        introspectGetOneMethod(serviceClass);
        introspectCreateMethod(serviceClass);
        introspectUpdateMethod(serviceClass);
        introspectDeleteMethod(serviceClass);
    }

    private void introspectGetAllMethod(Class serviceClass) {
        log.info("Introspecting getAll method with signature: java.util.List " + getAllMethodName + "(int, int)");
        Predicate<Method> predicate = method -> method.getName().startsWith(getAllMethodName)
                && method.getReturnType() == List.class
                && method.getParameterCount() == 2
                && method.getParameterTypes()[0] == int.class
                && method.getParameterTypes()[1] == int.class;
        List<Method> potentialMatches = introspectMethod(serviceClass, predicate);
        if (potentialMatches.isEmpty()) {
            getAllMethodLambda = "            // No method found!!!\n";
            return;
        }

        log.debug("  -> Using 1st potential match");
        getAllMethod = potentialMatches.get(0);
        generateGetAllMethodLambda();
    }

    private void generateGetAllMethodLambda() {
        StringBuilder sb = new StringBuilder();
        sb.append("            int start = util.getIntArg(environment, \"start\", 0);\n");
        sb.append("            int end = util.getIntArg(environment, \"end\", 10);\n");
        sb.append("\n");
        sb.append("            return ");
        sb.append(serviceName);
        sb.append(".");
        sb.append(getAllMethod.getName());
        sb.append("(start, end);");
        getAllMethodLambda = sb.toString();
    }

    private void introspectGetOneMethod(Class serviceClass) {
        log.info("Introspecting getOne method with signature " + fqClassName + " " + getOneMethodName + "(long/String)");
        Predicate<Method> predicate = method -> method.getName().startsWith(getOneMethodName)
                && method.getReturnType().getName().equals(fqClassName)
                && method.getParameterCount() == 1;
        List<Method> potentialMatches = introspectMethod(serviceClass, predicate);
        if (potentialMatches.isEmpty()) {
            getOneMethodLambda = "            // No method found!!!\n";
            return;
        }

        log.debug("  -> Using 1st potential match");
        getOneMethod = potentialMatches.get(0);
        generateGetOneMethodLambda();
    }

    private void generateGetOneMethodLambda() {
        getOneByLongId = true;
        if (getOneMethod.getParameterTypes()[0] == String.class) {
            getOneByLongId = false;
        }

        if (getOneByLongId) {
            getOneParamName = classNameLower + "Id";
        } else {
            getOneParamName = getOneMethod.getParameters()[0].getName();
        }
        StringBuilder sb = new StringBuilder();
        if (getOneByLongId) {
            sb.append("            long ");
        } else {
            sb.append("            String ");
        }
        sb.append(getOneParamName);
        if (getOneByLongId) {
            sb.append(" = util.getLongArg(environment, \"");
            sb.append(getOneParamName);
            sb.append("\");\n");
        } else {
            sb.append(" = util.getStringArg(environment, \"");
            sb.append(getOneParamName);
            sb.append("\");\n");
        }

        sb.append("\n");
        sb.append("            return ");
        sb.append(serviceName);
        sb.append(".");
        sb.append(getOneMethod.getName());
        sb.append("(");
        sb.append(getOneParamName);
        sb.append(");");
        getOneMethodLambda = sb.toString();
    }

    private void introspectCreateMethod(Class serviceClass) {
        log.info("Introspecting create method with signature: " + fqClassName + " " + createMethodName + "(...)");
        Predicate<Method> predicate = method -> method.getName().startsWith(createMethodName)
                && method.getReturnType().getName().equals(fqClassName)
                && method.getParameterCount() > 1;
        List<Method> potentialMatches = introspectMethod(serviceClass, predicate);
        if (potentialMatches.isEmpty()) {
            createMethodLambda = "            // No method found!!!\n";
            return;
        }

        log.debug("  -> Using potential match with the highest parameter count");
        for (Method potentialMatch : potentialMatches) {
            if (potentialMatch.getAnnotation(Deprecated.class) != null) {
                // CASE: Method is deprecated
                continue;
            }

            if (createMethod == null || createMethod.getParameterCount() < potentialMatch.getParameterCount()) {
                createMethod = potentialMatch;
            }
        }
        generateCreateMethodLambda();
    }

    private void generateCreateMethodLambda() {
        StringBuilder sb = new StringBuilder();
        generateArguments(createMethod, sb);
        sb.append("\n");
        sb.append("            return ");
        sb.append(serviceName);
        sb.append(".");
        sb.append(createMethod.getName());
        sb.append("(");
        for (int i = 0; i < createMethod.getParameters().length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(createMethod.getParameters()[i].getName());
        }
        sb.append(");");
        createMethodLambda = sb.toString();
    }

    private void introspectUpdateMethod(Class serviceClass) {
        log.info("Introspecting update method with signature: " + fqClassName + " " + updateMethodName + "(long, ...)");
        Predicate<Method> predicate = method -> method.getName().startsWith(updateMethodName)
                && method.getReturnType().getName().equals(fqClassName)
                && method.getParameterCount() > 2
                && method.getParameterTypes()[0] == long.class;
        List<Method> potentialMatches = introspectMethod(serviceClass, predicate);
        if (potentialMatches.isEmpty()) {
            updateMethodLambda = "            // No method found!!!\n";
            return;
        }

        log.debug("  -> Using potential match with the highest parameter count");
        for (Method potentialMatch : potentialMatches) {
            if (potentialMatch.getAnnotation(Deprecated.class) != null) {
                // CASE: Method is deprecated
                continue;
            }

            if (updateMethod == null || updateMethod.getParameterCount() < potentialMatch.getParameterCount()) {
                updateMethod = potentialMatch;
            }
        }
        generateUpdateMethodLambda();
    }

    private void generateUpdateMethodLambda() {
        StringBuilder sb = new StringBuilder();
        generateArguments(updateMethod, sb);
        sb.append("\n");
        sb.append("            return ");
        sb.append(serviceName);
        sb.append(".");
        sb.append(updateMethod.getName());
        sb.append("(");
        for (int i = 0; i < updateMethod.getParameters().length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(updateMethod.getParameters()[i].getName());
        }
        sb.append(");");
        updateMethodLambda = sb.toString();
    }

    private void generateArguments(Method method, StringBuilder sb) {
        for (Parameter parameter : method.getParameters()) {
            sb.append("            ");
            sb.append(parameter.getType().getSimpleName());
            sb.append(" ");
            sb.append(parameter.getName());
            sb.append(" = ");

            if (parameter.getType() == boolean.class) {
                sb.append("util.getBooleanArg(environment, \"");
                sb.append(parameter.getName());
                sb.append("\");");
            } else if (parameter.getType() == int.class) {
                sb.append("util.getIntArg(environment, \"");
                sb.append(parameter.getName());
                sb.append("\");");
            } else if (parameter.getType() == long.class) {
                sb.append("util.getLongArg(environment, \"");
                sb.append(parameter.getName());
                sb.append("\");");
            } else if (parameter.getType() == double.class) {
                sb.append("util.getDoubleArg(environment, \"");
                sb.append(parameter.getName());
                sb.append("\");");
            } else if (parameter.getType() == String.class) {
                sb.append("util.getStringArg(environment, \"");
                sb.append(parameter.getName());
                sb.append("\");");
            } else {
                sb.append("environment.getArgument(\"");
                sb.append(parameter.getName());
                sb.append("\");");
            }
            sb.append("\n");
        }
    }

    private void introspectDeleteMethod(Class serviceClass) {
        log.info("Introspecting delete method with signature: " + fqClassName + " " + deleteMethodName + "(long)");
        Predicate<Method> predicate = method -> method.getName().startsWith(deleteMethodName)
                && method.getReturnType().getName().equals(fqClassName)
                && method.getParameterCount() == 1
                && method.getParameterTypes()[0] == long.class;
        List<Method> potentialMatches = introspectMethod(serviceClass, predicate);
        if (potentialMatches.isEmpty()) {
            deleteMethodLambda = "            // No method found!!!\n";
            return;
        }

        log.debug("  -> Using 1st potential match");
        deleteMethod = potentialMatches.get(0);
        generateDeleteMethodLambda();
    }

    private void generateDeleteMethodLambda() {
        String paramIdName = classNameLower + "Id";
        StringBuilder sb = new StringBuilder();
        sb.append("            long ");
        sb.append(paramIdName);
        sb.append(" = util.getLongArg(environment, \"");
        sb.append(paramIdName);
        sb.append("\");\n");
        sb.append("\n");
        sb.append("            return ");
        sb.append(serviceName);
        sb.append(".");
        sb.append(deleteMethod.getName());
        sb.append("(");
        sb.append(paramIdName);
        sb.append(");");
        deleteMethodLambda = sb.toString();
    }

    private List<Method> introspectMethod(Class serviceClass, Predicate<Method> predicate) {
        List<Method> potentialMatches = Arrays.stream(serviceClass.getMethods())
                .filter(predicate)
                .collect(Collectors.toList());
        log.info("  -> found " + potentialMatches.size() + " potential matches");
        if (log.isDebugEnabled()) {
            for (int i = 0; i < potentialMatches.size(); i++) {
                log.debug("  - Potential match " + i);
                log.debug("    * Name: " + potentialMatches.get(i).getName());
                for (int j = 0; j < potentialMatches.get(i).getParameters().length; j++) {
                    Parameter param = potentialMatches.get(i).getParameters()[j];
                    log.debug("    * Parameter " + j + ": " + param.getName() + " of type " + param.getType().getName());
                }
                log.debug("    * Return type: " + potentialMatches.get(i).getReturnType().getName());
            }
        }
        return potentialMatches;
    }

    private void prepareTestClass() {
        if (createMethod == null && updateMethod == null) {
            return;
        }

        prepareTestAttributeDeclaration();
        prepareTestUtilAttributeMocks();
        prepareTestAddMethod();
        prepareTestUpdateMethod();
    }

    private void prepareTestAttributeDeclaration() {
        // FIXME: Change this logic, when Liferay will be, one day, compiled with parameter names
        Method usedMethod = determineMethodToBeUsed();

        StringBuilder sb = new StringBuilder();
        Arrays.stream(usedMethod.getParameters()).forEach(parameter -> {
            sb.append("    private static final ");
            sb.append(parameter.getType().getSimpleName());
            sb.append(" ");
            sb.append(parameter.getName().toUpperCase());
            sb.append(" = null;\n");
        });
        testAttributeDeclaration = sb.toString();
    }

    private Method determineMethodToBeUsed() {
        Method selectedMethod = createMethod;
        if (updateMethod != null && (selectedMethod == null || selectedMethod.getParameterCount() < updateMethod.getParameterCount())) {
            selectedMethod = updateMethod;
        }
        return selectedMethod;
    }

    private void prepareTestUtilAttributeMocks() {
        // FIXME: Change this logic, when Liferay will be, one day, compiled with parameter names
        Method usedMethod = determineMethodToBeUsed();

        StringBuilder sb = new StringBuilder();
        Arrays.stream(usedMethod.getParameters()).forEach(parameter -> {
            sb.append("            when(graphQLUtil.get");
            sb.append(parameter.getType().getSimpleName());
            sb.append("Arg(eq(environment), eq(\"");
            sb.append(parameter.getName());
            sb.append("\")))\n");
            sb.append("                    .thenReturn(");
            sb.append(parameter.getName().toUpperCase());
            sb.append(");\n");
        });
        testUtilAttributeMocks = sb.toString();
    }

    private void prepareTestAddMethod() {
        if (createMethod == null) {
            return;
        }

        testAddMethodArgumentsMock = prepareTestMethodArgumentsMock(createMethod);
        testAddMethodExpectedResultAttributes = prepareTestMethodExpectedResultAttributes(createMethod);
        testAddMethodArgsMock = prepareTestMethodArgsMock(createMethod);
        testAddMethodArgsMockAny = prepareTestMethodArgsMockAny(createMethod);
    }

    private void prepareTestUpdateMethod() {
        if (updateMethod == null) {
            return;
        }

        testUpdateMethodArgumentsMock = prepareTestMethodArgumentsMock(updateMethod);
        testUpdateMethodExpectedResultAttributes = prepareTestMethodExpectedResultAttributes(updateMethod);
        testUpdateMethodArgsMock = prepareTestMethodArgsMock(updateMethod);
        testUpdateMethodArgsMockAny = prepareTestMethodArgsMockAny(updateMethod);
    }

    private String prepareTestMethodExpectedResultAttributes(Method method) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(method.getParameters()).forEach(parameter -> {
            sb.append("        expectedResult.set");
            sb.append(parameter.getName());
            sb.append("(");
            sb.append(parameter.getName().toUpperCase());
            sb.append(");\n");
        });
        return sb.toString();
    }

    private String prepareTestMethodArgumentsMock(Method method) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(method.getParameters()).forEach(parameter -> {
            sb.append("        arguments.put(\"");
            sb.append(parameter.getName());
            sb.append("\", ");
            sb.append(parameter.getName().toUpperCase());
            sb.append(");\n");
        });
        return sb.toString();
    }

    private String prepareTestMethodArgsMock(Method method) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Parameter parameter = method.getParameters()[i];
            sb.append("eq(");
            sb.append(parameter.getName().toUpperCase());
            sb.append(")");
        }
        return sb.toString();
    }

    private String prepareTestMethodArgsMockAny(Method method) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            Parameter parameter = method.getParameters()[i];
            sb.append("any");
            sb.append(parameter.getType().getSimpleName());
            sb.append("()");
        }
        return sb.toString();
    }

    public void generateSource() throws MojoExecutionException {
        log.info("Generating sources for class " + fqClassName);

        Context handlebarsCtx = Context.newBuilder(this)
                .resolver(FieldValueResolver.INSTANCE)
                .build();

        log.info("Generating source file from template: Resolvers.java");
        HandlebarsUtil.generateFromTemplate("Resolvers.java",
                handlebarsCtx,
                outputResolversDir + "/" + className + "Resolvers.java",
                overwriteIfFileExists,
                log);
        log.info("Generating source file from template: ResolversImpl.java");
        HandlebarsUtil.generateFromTemplate("ResolversImpl.java",
                handlebarsCtx,
                outputResolversImplDir + "/" + className + "ResolversImpl.java",
                overwriteIfFileExists,
                log);
        log.info("Generating source file from template: ResolversImplTest.java");
        HandlebarsUtil.generateFromTemplate("ResolversImplTest.java",
                handlebarsCtx,
                outputResolversImplTestDir + "/" + className + "ResolversImplTest.java",
                overwriteIfFileExists,
                log);
        log.info("Generating source file from template: BatchLoader.java");
        HandlebarsUtil.generateFromTemplate("BatchLoader.java",
                handlebarsCtx,
                outputBatchLoaderDir + "/" + className + "BatchLoader.java",
                overwriteIfFileExists,
                log);
    }
}
