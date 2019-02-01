package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.dynamic.data.mapping.exception.NoSuchTemplateException;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.DDMTemplateBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DDMTemplateResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionContextBuilder;
import graphql.execution.ExecutionId;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import org.dataloader.DataLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link DDMTemplateResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DDMTemplateResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long TEMPLATE_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final long CLASS_NAME_ID = 1L;
    private static final long CLASS_PK = 2L;
    private static final long RESOURCE_CLASS_NAME_ID = 3L;
    private static final String TEMPLATE_KEY = "TEMPLATE-KEY";
    private static final Map<Locale, String> NAME_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final String TYPE = "type";
    private static final String MODE = "mode";
    private static final String LANGUAGE = "language";
    private static final String SCRIPT = "script";
    private static final boolean CACHEABLE = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DDMTemplate> dataLoader;

    static {
        NAME_MAP = new HashMap<>();
        NAME_MAP.put(LocaleUtil.US, "Test title");
        NAME_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");
    }

    @InjectMocks
    DDMTemplateResolvers resolvers = new DDMTemplateResolversImpl();

    @Mock
    private DDMTemplateLocalService localService;

    @Mock
    private GraphQLUtil graphQLUtil;

    @Before
    public void setUp() {
        executionId = ExecutionId.from("execution-1");
        executionContext = ExecutionContextBuilder.newExecutionContextBuilder()
                .executionId(executionId)
                .build();

        dataLoader = mock(DataLoader.class);
        mockEnvironment = mock(DataFetchingEnvironment.class);
        doReturn(dataLoader)
                .when(mockEnvironment)
                .getDataLoader(DDMTemplateBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DDMTemplateResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("templateId")))
                    .thenReturn(TEMPLATE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("classNameId")))
                    .thenReturn(CLASS_NAME_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getLongArg(eq(environment), eq("resourceClassNameId")))
                    .thenReturn(RESOURCE_CLASS_NAME_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("templateKey")))
                    .thenReturn(TEMPLATE_KEY);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("nameMap")))
                    .thenReturn(NAME_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getStringArg(eq(environment), eq("type")))
                    .thenReturn(TYPE);
            when(graphQLUtil.getStringArg(eq(environment), eq("mode")))
                    .thenReturn(MODE);
            when(graphQLUtil.getStringArg(eq(environment), eq("language"), anyString()))
                    .thenReturn(LANGUAGE);
            when(graphQLUtil.getStringArg(eq(environment), eq("script")))
                    .thenReturn(SCRIPT);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("cacheable")))
                    .thenReturn(CACHEABLE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getStringArg(eq(environment), anyString(), anyString()))
                    .thenReturn("");
        }
    }

    private DataFetchingEnvironment getTestEnvironment(Map<String, Object> arguments) {
        return new DataFetchingEnvironmentImpl(
                null,
                arguments,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                executionId,
                null,
                null,
                executionContext);
    }

    @Test
    public void getDDMTemplatesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMTemplate> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMTemplate entity = mock(DDMTemplate.class);
                    entity.setTemplateId(value);
                    availableObjects.add(entity);
                });
        List<DDMTemplate> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMTemplates(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMTemplate> results = resolvers.getDDMTemplatesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMTemplatesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DDMTemplate> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMTemplate entity = mock(DDMTemplate.class);
                    entity.setTemplateId(value);
                    availableObjects.add(entity);
                });
        List<DDMTemplate> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMTemplates(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMTemplate> results = resolvers.getDDMTemplatesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMTemplatesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMTemplate> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMTemplate entity = mock(DDMTemplate.class);
                    entity.setTemplateId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMTemplates(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMTemplate> results = resolvers.getDDMTemplatesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMTemplatesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMTemplate> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMTemplates(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMTemplate> results = resolvers.getDDMTemplatesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMTemplateDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DDMTemplate expectedResult = mock(DDMTemplate.class);
        expectedResult.setTemplateId(TEMPLATE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("templateId"))
                .thenReturn(TEMPLATE_ID);
        when(dataLoader.load(TEMPLATE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DDMTemplate> asyncResult = resolvers.getDDMTemplateDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DDMTemplate result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDDMTemplateDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("templateId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DDMTemplate> asyncResult = resolvers.getDDMTemplateDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDDMTemplateDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("templateId"))
                .thenReturn(TEMPLATE_ID);
        when(dataLoader.load(TEMPLATE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DDMTemplate> asyncResult = resolvers.getDDMTemplateDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DDMTemplate result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createDDMTemplateDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("classNameId", CLASS_NAME_ID);
        arguments.put("classPK", CLASS_PK);
        arguments.put("resourceClassNameId", RESOURCE_CLASS_NAME_ID);
        arguments.put("templateKey", TEMPLATE_KEY);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("mode", MODE);
        arguments.put("language", LANGUAGE);
        arguments.put("script", SCRIPT);
        arguments.put("cacheable", CACHEABLE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMTemplate expectedResult = mock(DDMTemplate.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setClassNameId(CLASS_NAME_ID);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setResourceClassNameId(RESOURCE_CLASS_NAME_ID);
        expectedResult.setTemplateKey(TEMPLATE_KEY);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setMode(MODE);
        expectedResult.setLanguage(LANGUAGE);
        expectedResult.setScript(SCRIPT);
        expectedResult.setCacheable(CACHEABLE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addTemplate(eq(USER_ID), eq(GROUP_ID), eq(CLASS_NAME_ID), eq(CLASS_PK), eq(RESOURCE_CLASS_NAME_ID), eq(TEMPLATE_KEY), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MODE), eq(LANGUAGE), eq(SCRIPT), eq(CACHEABLE), anyBoolean(), anyString(), any(), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMTemplate result = resolvers.createDDMTemplateDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createDDMTemplateDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("classNameId", CLASS_NAME_ID);
        arguments.put("classPK", CLASS_PK);
        arguments.put("resourceClassNameId", RESOURCE_CLASS_NAME_ID);
        arguments.put("templateKey", TEMPLATE_KEY);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("mode", MODE);
        arguments.put("language", LANGUAGE);
        arguments.put("script", SCRIPT);
        arguments.put("cacheable", CACHEABLE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMTemplate expectedResult = mock(DDMTemplate.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setClassNameId(CLASS_NAME_ID);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setResourceClassNameId(RESOURCE_CLASS_NAME_ID);
        expectedResult.setTemplateKey(TEMPLATE_KEY);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setMode(MODE);
        expectedResult.setLanguage(LANGUAGE);
        expectedResult.setScript(SCRIPT);
        expectedResult.setCacheable(CACHEABLE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addTemplate(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(CLASS_NAME_ID), eq(CLASS_PK), eq(RESOURCE_CLASS_NAME_ID), eq(TEMPLATE_KEY), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MODE), eq(LANGUAGE), eq(SCRIPT), eq(CACHEABLE), anyBoolean(), anyString(), any(), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMTemplate result = resolvers.createDDMTemplateDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createDDMTemplateDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addTemplate(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyString(), anyMap(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyString(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DDMTemplate result = resolvers.createDDMTemplateDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateDDMTemplateDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateId", TEMPLATE_ID);
        arguments.put("classPK", CLASS_PK);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("mode", MODE);
        arguments.put("language", LANGUAGE);
        arguments.put("script", SCRIPT);
        arguments.put("cacheable", CACHEABLE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMTemplate expectedResult = mock(DDMTemplate.class);
        expectedResult.setTemplateId(TEMPLATE_ID);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setMode(MODE);
        expectedResult.setLanguage(LANGUAGE);
        expectedResult.setScript(SCRIPT);
        expectedResult.setCacheable(CACHEABLE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateTemplate(eq(USER_ID), eq(TEMPLATE_ID), eq(CLASS_PK), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MODE), eq(LANGUAGE), eq(SCRIPT), eq(CACHEABLE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMTemplate result = resolvers.updateDDMTemplateDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchTemplateException.class)
    public void updateDDMTemplateDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("classPK", CLASS_PK);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("mode", MODE);
        arguments.put("language", LANGUAGE);
        arguments.put("script", SCRIPT);
        arguments.put("cacheable", CACHEABLE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("templateId")))
                .thenReturn(0L);
        when(localService.updateTemplate(eq(DEFAULT_USER_ID), eq(0L), eq(CLASS_PK), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MODE), eq(LANGUAGE), eq(SCRIPT), eq(CACHEABLE), any(ServiceContext.class)))
                .thenThrow(NoSuchTemplateException.class);

        // Asserts
        DDMTemplate result = resolvers.updateDDMTemplateDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchTemplateException.class)
    public void updateDDMTemplateDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateId", 789456L);
        arguments.put("classPK", CLASS_PK);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("mode", MODE);
        arguments.put("language", LANGUAGE);
        arguments.put("script", SCRIPT);
        arguments.put("cacheable", CACHEABLE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("templateId")))
                .thenReturn(789456L);
        when(localService.updateTemplate(eq(DEFAULT_USER_ID), eq(789456L), eq(CLASS_PK), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MODE), eq(LANGUAGE), eq(SCRIPT), eq(CACHEABLE), any(ServiceContext.class)))
                .thenThrow(NoSuchTemplateException.class);

        // Asserts
        DDMTemplate result = resolvers.updateDDMTemplateDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateDDMTemplateDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("templateId")))
                .thenReturn(TEMPLATE_ID);
        when(localService.updateTemplate(anyLong(), anyLong(), anyLong(), anyMap(), anyMap(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DDMTemplate result = resolvers.updateDDMTemplateDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteDDMTemplateDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateId", TEMPLATE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMTemplate expectedResult = mock(DDMTemplate.class);
        expectedResult.setTemplateId(TEMPLATE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDDMTemplate(eq(TEMPLATE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        DDMTemplate result = resolvers.deleteDDMTemplateDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchTemplateException.class)
    public void deleteDDMTemplateDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        DDMTemplate expectedResult = mock(DDMTemplate.class);
        expectedResult.setTemplateId(TEMPLATE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDDMTemplate(eq(TEMPLATE_ID)))
                .thenThrow(NoSuchTemplateException.class);

        // Asserts
        DDMTemplate result = resolvers.deleteDDMTemplateDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchTemplateException.class)
    public void deleteDDMTemplateDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("templateId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMTemplate expectedResult = mock(DDMTemplate.class);
        expectedResult.setTemplateId(TEMPLATE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("templateId")))
                .thenReturn(789456L);
        when(localService.deleteDDMTemplate(eq(789456L)))
                .thenThrow(NoSuchTemplateException.class);

        // Asserts
        DDMTemplate result = resolvers.deleteDDMTemplateDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
