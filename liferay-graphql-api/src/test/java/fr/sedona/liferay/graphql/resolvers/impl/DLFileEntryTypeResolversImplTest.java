package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.exception.NoSuchFileEntryTypeException;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLFileEntryTypeLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.DLFileEntryTypeBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileEntryTypeResolvers;
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
 * Test suite for {@link DLFileEntryTypeResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DLFileEntryTypeResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long FILE_ENTRY_TYPE_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final String FILE_ENTRY_TYPE_KEY = "KEY";
    private static final Map<Locale, String> NAME_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final long[] DDM_STRUCTURE_IDS = new long[]{1L, 2L, 3L};
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DLFileEntryType> dataLoader;

    static {
        NAME_MAP = new HashMap<>();
        NAME_MAP.put(LocaleUtil.US, "Test title");
        NAME_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");
    }

    @InjectMocks
    DLFileEntryTypeResolvers resolvers = new DLFileEntryTypeResolversImpl();

    @Mock
    private DLFileEntryTypeLocalService localService;

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
                .getDataLoader(DLFileEntryTypeBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DLFileEntryTypeResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryTypeId")))
                    .thenReturn(FILE_ENTRY_TYPE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("fileEntryTypeKey")))
                    .thenReturn(FILE_ENTRY_TYPE_KEY);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("nameMap")))
                    .thenReturn(NAME_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getLongArrayArg(eq(environment), eq("ddmStructureIds")))
                    .thenReturn(DDM_STRUCTURE_IDS);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getTranslatedArg(eq(environment), anyString()))
                    .thenReturn(Collections.emptyMap());
            when(graphQLUtil.getLongArrayArg(eq(environment), anyString()))
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
    public void getDLFileEntryTypesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntryType> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntryType entity = mock(DLFileEntryType.class);
                    entity.setFileEntryTypeId(value);
                    availableObjects.add(entity);
                });
        List<DLFileEntryType> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryTypes(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryType> results = resolvers.getDLFileEntryTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryTypesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DLFileEntryType> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntryType entity = mock(DLFileEntryType.class);
                    entity.setFileEntryTypeId(value);
                    availableObjects.add(entity);
                });
        List<DLFileEntryType> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryTypes(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryType> results = resolvers.getDLFileEntryTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryTypesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntryType> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntryType entity = mock(DLFileEntryType.class);
                    entity.setFileEntryTypeId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryTypes(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryType> results = resolvers.getDLFileEntryTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryTypesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntryType> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntryTypes(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntryType> results = resolvers.getDLFileEntryTypesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryTypeDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DLFileEntryType expectedResult = mock(DLFileEntryType.class);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileEntryTypeId"))
                .thenReturn(FILE_ENTRY_TYPE_ID);
        when(dataLoader.load(FILE_ENTRY_TYPE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DLFileEntryType> asyncResult = resolvers.getDLFileEntryTypeDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileEntryType result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDLFileEntryTypeDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("fileEntryTypeId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DLFileEntryType> asyncResult = resolvers.getDLFileEntryTypeDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDLFileEntryTypeDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileEntryTypeId"))
                .thenReturn(FILE_ENTRY_TYPE_ID);
        when(dataLoader.load(FILE_ENTRY_TYPE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DLFileEntryType> asyncResult = resolvers.getDLFileEntryTypeDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileEntryType result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createDLFileEntryTypeDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("fileEntryTypeKey", FILE_ENTRY_TYPE_KEY);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmStructureIds", DDM_STRUCTURE_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntryType expectedResult = mock(DLFileEntryType.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFileEntryTypeKey(FILE_ENTRY_TYPE_KEY);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addFileEntryType(eq(USER_ID), eq(GROUP_ID), eq(FILE_ENTRY_TYPE_KEY), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_STRUCTURE_IDS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntryType result = resolvers.createDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createDLFileEntryTypeDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("fileEntryTypeKey", FILE_ENTRY_TYPE_KEY);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmStructureIds", DDM_STRUCTURE_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntryType expectedResult = mock(DLFileEntryType.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setFileEntryTypeKey(FILE_ENTRY_TYPE_KEY);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addFileEntryType(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(FILE_ENTRY_TYPE_KEY), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_STRUCTURE_IDS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntryType result = resolvers.createDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createDLFileEntryTypeDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addFileEntryType(anyLong(), anyLong(), anyString(), anyMap(), anyMap(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DLFileEntryType result = resolvers.createDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateDLFileEntryTypeDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryTypeId", FILE_ENTRY_TYPE_ID);
        arguments.put("userId", USER_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmStructureIds", DDM_STRUCTURE_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntryType expectedResult = mock(DLFileEntryType.class);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);
        expectedResult.setUserId(USER_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        doNothing().when(localService)
                .updateFileEntryType(eq(USER_ID), eq(FILE_ENTRY_TYPE_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_STRUCTURE_IDS), any(ServiceContext.class));
        when(localService.getDLFileEntryType(FILE_ENTRY_TYPE_ID))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntryType result = resolvers.updateDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFileEntryTypeException.class)
    public void updateDLFileEntryTypeDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmStructureIds", DDM_STRUCTURE_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryTypeId")))
                .thenReturn(0L);
        doThrow(NoSuchFileEntryTypeException.class).when(localService)
                .updateFileEntryType(eq(USER_ID), eq(0L), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_STRUCTURE_IDS), any(ServiceContext.class));

        // Asserts
        DLFileEntryType result = resolvers.updateDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFileEntryTypeException.class)
    public void updateDLFileEntryTypeDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryTypeId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("ddmStructureIds", DDM_STRUCTURE_IDS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryTypeId")))
                .thenReturn(789456L);
        doThrow(NoSuchFileEntryTypeException.class).when(localService)
                .updateFileEntryType(eq(USER_ID), eq(789456L), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(DDM_STRUCTURE_IDS), any(ServiceContext.class));

        // Asserts
        DLFileEntryType result = resolvers.updateDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateDLFileEntryTypeDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryTypeId")))
                .thenReturn(FILE_ENTRY_TYPE_ID);
        doThrow(PortalException.class).when(localService)
                .updateFileEntryType(anyLong(), anyLong(), anyMap(), anyMap(), any(), any(ServiceContext.class));

        // Asserts
        DLFileEntryType result = resolvers.updateDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteDLFileEntryTypeDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryTypeId", FILE_ENTRY_TYPE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntryType expectedResult = mock(DLFileEntryType.class);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFileEntryType(eq(FILE_ENTRY_TYPE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntryType result = resolvers.deleteDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFileEntryTypeException.class)
    public void deleteDLFileEntryTypeDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        DLFileEntryType expectedResult = mock(DLFileEntryType.class);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFileEntryType(eq(FILE_ENTRY_TYPE_ID)))
                .thenThrow(NoSuchFileEntryTypeException.class);

        // Asserts
        DLFileEntryType result = resolvers.deleteDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFileEntryTypeException.class)
    public void deleteDLFileEntryTypeDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryTypeId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntryType expectedResult = mock(DLFileEntryType.class);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryTypeId")))
                .thenReturn(789456L);
        when(localService.deleteDLFileEntryType(eq(789456L)))
                .thenThrow(NoSuchFileEntryTypeException.class);

        // Asserts
        DLFileEntryType result = resolvers.deleteDLFileEntryTypeDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
