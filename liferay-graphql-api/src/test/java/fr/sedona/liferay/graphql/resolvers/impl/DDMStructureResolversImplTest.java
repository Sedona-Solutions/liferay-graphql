package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.dynamic.data.mapping.kernel.NoSuchStructureException;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormLayout;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.DDMStructureBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DDMStructureResolvers;
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
 * Test suite for {@link DDMStructureResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DDMStructureResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long STRUCTURE_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final String PARENT_STRUCTURE_KEY = "";
    private static final long PARENT_STRUCTURE_ID = 0L;
    private static final long CLASS_NAME_ID = 457L;
    private static final String STRUCTURE_KEY = "STRUCTURE-KEY";
    private static final Map<Locale, String> NAME_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final DDMForm DDM_FORM;
    private static final DDMFormLayout DDM_FORM_LAYOUT;
    private static final String STORAGE_TYPE = "json";
    private static final int TYPE = 1;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DDMStructure> dataLoader;

    static {
        NAME_MAP = new HashMap<>();
        NAME_MAP.put(LocaleUtil.US, "Test title");
        NAME_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");

        DDM_FORM = mock(DDMForm.class);

        DDM_FORM_LAYOUT = mock(DDMFormLayout.class);
    }

    @InjectMocks
    DDMStructureResolvers resolvers = new DDMStructureResolversImpl();

    @Mock
    private DDMStructureLocalService localService;

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
                .getDataLoader(DDMStructureBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DDMStructureResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("structureId")))
                    .thenReturn(STRUCTURE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("parentStructureKey")))
                    .thenReturn(PARENT_STRUCTURE_KEY);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentStructureId")))
                    .thenReturn(PARENT_STRUCTURE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("classNameId")))
                    .thenReturn(CLASS_NAME_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("structureKey")))
                    .thenReturn(STRUCTURE_KEY);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("nameMap")))
                    .thenReturn(NAME_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getDDDMFormArg(eq(environment), eq("ddmForm")))
                    .thenReturn(DDM_FORM);
            when(graphQLUtil.getDDDMFormForJournalArticleArg(eq(environment), eq("ddmForm")))
                    .thenReturn(DDM_FORM);
            when(graphQLUtil.getDDDMFormLayoutArg(eq(environment), eq("ddmFormLayout")))
                    .thenReturn(DDM_FORM_LAYOUT);
            when(graphQLUtil.getDefaultDDDMFormLayout(any(DDMForm.class)))
                    .thenReturn(DDM_FORM_LAYOUT);
            when(graphQLUtil.getStringArg(eq(environment), eq("storageType"), anyString()))
                    .thenReturn(STORAGE_TYPE);
            when(graphQLUtil.getIntArg(eq(environment), eq("type"), anyInt()))
                    .thenReturn(TYPE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString(), anyInt()))
                    .thenReturn(0);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getStringArg(eq(environment), anyString(), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getTranslatedArg(eq(environment), anyString()))
                    .thenReturn(Collections.emptyMap());
            when(graphQLUtil.getDDDMFormArg(eq(environment), anyString()))
                    .thenReturn(null);
            when(graphQLUtil.getDDDMFormForJournalArticleArg(eq(environment), anyString()))
                    .thenReturn(null);
            when(graphQLUtil.getDDDMFormLayoutArg(eq(environment), anyString()))
                    .thenReturn(null);
            when(graphQLUtil.getDefaultDDDMFormLayout(any(DDMForm.class)))
                    .thenReturn(null);
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
    public void getDDMStructuresDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMStructure> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMStructure entity = mock(DDMStructure.class);
                    entity.setStructureId(value);
                    availableObjects.add(entity);
                });
        List<DDMStructure> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMStructures(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMStructure> results = resolvers.getDDMStructuresDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMStructuresDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DDMStructure> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMStructure entity = mock(DDMStructure.class);
                    entity.setStructureId(value);
                    availableObjects.add(entity);
                });
        List<DDMStructure> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMStructures(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMStructure> results = resolvers.getDDMStructuresDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMStructuresDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMStructure> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DDMStructure entity = mock(DDMStructure.class);
                    entity.setStructureId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMStructures(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMStructure> results = resolvers.getDDMStructuresDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMStructuresDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DDMStructure> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDDMStructures(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DDMStructure> results = resolvers.getDDMStructuresDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDDMStructureDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DDMStructure expectedResult = mock(DDMStructure.class);
        expectedResult.setStructureId(STRUCTURE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("structureId"))
                .thenReturn(STRUCTURE_ID);
        when(dataLoader.load(STRUCTURE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DDMStructure> asyncResult = resolvers.getDDMStructureDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DDMStructure result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDDMStructureDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("structureId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DDMStructure> asyncResult = resolvers.getDDMStructureDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDDMStructureDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("structureId"))
                .thenReturn(STRUCTURE_ID);
        when(dataLoader.load(STRUCTURE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DDMStructure> asyncResult = resolvers.getDDMStructureDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DDMStructure result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createDDMStructureDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentStructureKey", PARENT_STRUCTURE_KEY);
        arguments.put("classNameId", CLASS_NAME_ID);
        arguments.put("structureKey", STRUCTURE_KEY);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmForm", DDM_FORM);
        arguments.put("ddmFormLayout", DDM_FORM_LAYOUT);
        arguments.put("storageType", STORAGE_TYPE);
        arguments.put("type", TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMStructure expectedResult = mock(DDMStructure.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setClassNameId(CLASS_NAME_ID);
        expectedResult.setStructureKey(STRUCTURE_KEY);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setDDMForm(DDM_FORM);
        expectedResult.setStorageType(STORAGE_TYPE);
        expectedResult.setType(TYPE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addStructure(eq(USER_ID), eq(GROUP_ID), eq(PARENT_STRUCTURE_KEY), eq(CLASS_NAME_ID), eq(STRUCTURE_KEY), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_FORM), eq(DDM_FORM_LAYOUT), eq(STORAGE_TYPE), eq(TYPE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMStructure result = resolvers.createDDMStructureDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createDDMStructureDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentStructureKey", PARENT_STRUCTURE_KEY);
        arguments.put("classNameId", CLASS_NAME_ID);
        arguments.put("structureKey", STRUCTURE_KEY);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmForm", DDM_FORM);
        arguments.put("ddmFormLayout", DDM_FORM_LAYOUT);
        arguments.put("storageType", STORAGE_TYPE);
        arguments.put("type", TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMStructure expectedResult = mock(DDMStructure.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setClassNameId(CLASS_NAME_ID);
        expectedResult.setStructureKey(STRUCTURE_KEY);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setDDMForm(DDM_FORM);
        expectedResult.setStorageType(STORAGE_TYPE);
        expectedResult.setType(TYPE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addStructure(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(PARENT_STRUCTURE_KEY), eq(CLASS_NAME_ID), eq(STRUCTURE_KEY), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_FORM), eq(DDM_FORM_LAYOUT), eq(STORAGE_TYPE), eq(TYPE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMStructure result = resolvers.createDDMStructureDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createDDMStructureDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addStructure(anyLong(), anyLong(), anyString(), anyLong(), anyString(), anyMap(), anyMap(), any(), any(), anyString(), anyInt(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DDMStructure result = resolvers.createDDMStructureDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateDDMStructureDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("structureId", STRUCTURE_ID);
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentStructureId", PARENT_STRUCTURE_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmForm", DDM_FORM);
        arguments.put("ddmFormLayout", DDM_FORM_LAYOUT);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMStructure expectedResult = mock(DDMStructure.class);
        expectedResult.setStructureId(STRUCTURE_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentStructureId(PARENT_STRUCTURE_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setDDMForm(DDM_FORM);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateStructure(eq(USER_ID), eq(STRUCTURE_ID), eq(PARENT_STRUCTURE_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_FORM), eq(DDM_FORM_LAYOUT), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DDMStructure result = resolvers.updateDDMStructureDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchStructureException.class)
    public void updateDDMStructureDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentStructureId", PARENT_STRUCTURE_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmForm", DDM_FORM);
        arguments.put("ddmFormLayout", DDM_FORM_LAYOUT);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("structureId")))
                .thenReturn(0L);
        when(localService.updateStructure(eq(USER_ID), eq(0L), eq(PARENT_STRUCTURE_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_FORM), eq(DDM_FORM_LAYOUT), any(ServiceContext.class)))
                .thenThrow(NoSuchStructureException.class);

        // Asserts
        DDMStructure result = resolvers.updateDDMStructureDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchStructureException.class)
    public void updateDDMStructureDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("structureId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentStructureId", PARENT_STRUCTURE_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmForm", DDM_FORM);
        arguments.put("ddmFormLayout", DDM_FORM_LAYOUT);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("structureId")))
                .thenReturn(789456L);
        when(localService.updateStructure(eq(USER_ID), eq(789456L), eq(PARENT_STRUCTURE_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_FORM), eq(DDM_FORM_LAYOUT), any(ServiceContext.class)))
                .thenThrow(NoSuchStructureException.class);

        // Asserts
        DDMStructure result = resolvers.updateDDMStructureDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateDDMStructureDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("structureId")))
                .thenReturn(STRUCTURE_ID);
        when(localService.updateStructure(anyLong(), anyLong(), anyLong(), anyMap(), anyMap(), any(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DDMStructure result = resolvers.updateDDMStructureDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteDDMStructureDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("structureId", STRUCTURE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMStructure expectedResult = mock(DDMStructure.class);
        expectedResult.setStructureId(STRUCTURE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDDMStructure(eq(STRUCTURE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        DDMStructure result = resolvers.deleteDDMStructureDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchStructureException.class)
    public void deleteDDMStructureDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        DDMStructure expectedResult = mock(DDMStructure.class);
        expectedResult.setStructureId(STRUCTURE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDDMStructure(eq(STRUCTURE_ID)))
                .thenThrow(NoSuchStructureException.class);

        // Asserts
        DDMStructure result = resolvers.deleteDDMStructureDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchStructureException.class)
    public void deleteDDMStructureDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("structureId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DDMStructure expectedResult = mock(DDMStructure.class);
        expectedResult.setStructureId(STRUCTURE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("structureId")))
                .thenReturn(789456L);
        when(localService.deleteDDMStructure(eq(789456L)))
                .thenThrow(NoSuchStructureException.class);

        // Asserts
        DDMStructure result = resolvers.deleteDDMStructureDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
