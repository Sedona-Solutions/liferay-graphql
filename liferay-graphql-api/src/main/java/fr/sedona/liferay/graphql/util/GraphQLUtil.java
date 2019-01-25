package fr.sedona.liferay.graphql.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.dynamic.data.mapping.form.builder.context.DDMFormContextDeserializer;
import com.liferay.dynamic.data.mapping.form.builder.context.DDMFormContextDeserializerRequest;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.util.DDM;
import com.liferay.message.boards.kernel.model.MBMessage;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Component(
        immediate = true,
        service = GraphQLUtil.class
)
public class GraphQLUtil {
    private static final Log LOGGER = LogFactoryUtil.getLog(GraphQLUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ClassNameLocalService classNameLocalService;
    private UserLocalService userLocalService;
    private DDM ddm;
    private Map<Long, User> defaultUserCache = new HashMap<>();

    @Reference(unbind = "-")
    public void setClassNameLocalService(ClassNameLocalService classNameLocalService) {
        this.classNameLocalService = classNameLocalService;
    }

    @Reference(unbind = "-")
    public void setUserLocalService(UserLocalService userLocalService) {
        this.userLocalService = userLocalService;
    }

    @Reference(unbind = "-")
    protected void setDDM(DDM ddm) {
        this.ddm = ddm;
    }

    @Reference(target = "(dynamic.data.mapping.form.builder.context.deserializer.type=form)")
    protected DDMFormContextDeserializer<DDMForm> ddmFormBuilderContextToDDMForm;

    @Reference(target = "(dynamic.data.mapping.form.builder.context.deserializer.type=formLayout)")
    protected DDMFormContextDeserializer<DDMFormLayout> ddmFormBuilderContextToDDMFormLayout;

    public String getStringArg(DataFetchingEnvironment environment, String argumentName) {
        return getStringArg(environment, argumentName, "");
    }

    public String getStringArg(DataFetchingEnvironment environment, String argumentName, String defaultValue) {
        String value = environment.getArgument(argumentName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public int getIntArg(DataFetchingEnvironment environment, String argumentName) {
        return getIntArg(environment, argumentName, 0);
    }

    public int getIntArg(DataFetchingEnvironment environment, String argumentName, int defaultValue) {
        Integer value = environment.getArgument(argumentName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public long getLongArg(DataFetchingEnvironment environment, String argumentName) {
        return getLongArg(environment, argumentName, 0);
    }

    public long getLongArg(DataFetchingEnvironment environment, String argumentName, long defaultValue) {
        Long value = environment.getArgument(argumentName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public double getDoubleArg(DataFetchingEnvironment environment, String argumentName) {
        return getDoubleArg(environment, argumentName, 0);
    }

    public double getDoubleArg(DataFetchingEnvironment environment, String argumentName, double defaultValue) {
        Double value = environment.getArgument(argumentName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public boolean getBooleanArg(DataFetchingEnvironment environment, String argumentName) {
        return getBooleanArg(environment, argumentName, false);
    }

    public boolean getBooleanArg(DataFetchingEnvironment environment, String argumentName, boolean defaultValue) {
        Boolean value = environment.getArgument(argumentName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public Map<Locale, String> getTranslatedArg(DataFetchingEnvironment environment, String argumentName) {
        Map<String, String> translatedMap = environment.getArgument(argumentName);
        Map<Locale, String> values = translatedMap.entrySet()
                .stream()
                .map(object -> new AbstractMap.SimpleEntry<>(LocaleUtil.fromLanguageId(object.getKey()), object.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (LOGGER.isDebugEnabled()) {
            values.keySet()
                    .forEach(locale -> LOGGER.info("Read Key: " + locale.getDisplayName() + " / Value: " + values.get(locale)));
        }
        return values;
    }

    public String[] getStringArrayArg(DataFetchingEnvironment environment, String argumentName) {
        String[] value = environment.getArgument(argumentName);
        if (value == null) {
            value = new String[0];
        }
        return value;
    }

    public Locale getLocaleArg(DataFetchingEnvironment environment, String argumentName) {
        return LocaleUtil.fromLanguageId(environment.getArgument(argumentName));
    }

    public Date getDateArg(DataFetchingEnvironment environment, String argumentName) {
        return (Date) environment.getArgument(argumentName);
    }

    public DDMForm getDDDMFormArg(DataFetchingEnvironment environment, String argumentName) {
        String jsonString = environment.getArgument(argumentName);
        LOGGER.debug("Reading DDMForm arg from " + jsonString);
        try {
            return ddmFormBuilderContextToDDMForm.deserialize(DDMFormContextDeserializerRequest.with(jsonString));
        } catch (Exception e) {
            throw new GraphQLException("Could not deserialize DDMForm", e);
        }
    }

    public DDMForm getDDDMFormForJournalArticleArg(DataFetchingEnvironment environment, String argumentName) {
        String jsonString = environment.getArgument(argumentName);
        LOGGER.debug("Reading DDMForm for JournalArticle arg from " + jsonString);
        try {
            return ddm.getDDMForm(jsonString);
        } catch (Exception e) {
            throw new GraphQLException("Coult not deserialize DDMForm for JournalArticle", e);
        }
    }

    public DDMFormLayout getDDDMFormLayoutArg(DataFetchingEnvironment environment, String argumentName) {
        String jsonString = environment.getArgument(argumentName);
        LOGGER.debug("Reading DDMFormLayout arg from " + jsonString);
        try {
            return ddmFormBuilderContextToDDMFormLayout.deserialize(DDMFormContextDeserializerRequest.with(jsonString));
        } catch (Exception e) {
            throw new GraphQLException("Could not deserialize DDMFormLayout", e);
        }
    }

    public DDMFormLayout getDefaultDDDMFormLayout(DDMForm ddmForm) {
        return ddm.getDefaultDDMFormLayout(ddmForm);
    }

    public long[] getLongArrayArg(DataFetchingEnvironment environment, String argumentName) {
        long[] value = environment.getArgument(argumentName);
        if (value == null) {
            value = new long[0];
        }
        return value;
    }

    public MBMessage getMBMessageArg(DataFetchingEnvironment environment, String argumentName) {
        String jsonString = environment.getArgument(argumentName);
        try {
            return OBJECT_MAPPER.readValue(jsonString, MBMessage.class);
        } catch (IOException e) {
            throw new GraphQLException("Could not deserialize MBMessage", e);
        }
    }

    public long getClassNameId(String className) {
        return classNameLocalService.getClassNameId(className);
    }

    public User getDefaultUser() {
        return getDefaultUser(PortalUtil.getDefaultCompanyId());
    }

    public User getDefaultUser(long companyId) {
        if (defaultUserCache.containsKey(companyId)) {
            return defaultUserCache.get(companyId);
        }

        try {
            User defaultUser = userLocalService.getDefaultUser(companyId);
            defaultUserCache.put(companyId, defaultUser);
            return defaultUser;
        } catch (Exception e) {
            throw new GraphQLException("Could not get default User for Company with id " + companyId, e);
        }
    }

    public long getDefaultUserId() {
        return getDefaultUser().getUserId();
    }

    public long getDefaultUserId(long companyId) {
        return getDefaultUser(companyId).getUserId();
    }

    public long getEntityIdFromSource(Object source, String methodName) throws Exception {
        try {
            Method getIdMethod = source.getClass().getMethod(methodName);
            return (long) getIdMethod.invoke(source);
        } catch (Exception e) {
            LOGGER.warn("Could not find method with name '" + methodName + "' in class " + source.getClass().getName());
            throw e;
        }
    }
}
