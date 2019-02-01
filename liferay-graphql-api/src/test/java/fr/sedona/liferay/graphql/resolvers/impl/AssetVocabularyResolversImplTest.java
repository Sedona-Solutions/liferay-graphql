package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.exception.NoSuchVocabularyException;
import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.AssetVocabularyBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetVocabularyResolvers;
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
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link AssetVocabularyResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class AssetVocabularyResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long VOCABULARY_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 123L;
    private static final String TITLE = "Test title";
    private static final Map<Locale, String> TITLE_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final String SETTINGS = "settings";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, AssetVocabulary> dataLoader;

    static {
        TITLE_MAP = new HashMap<>();
        TITLE_MAP.put(LocaleUtil.US, "Test title");
        TITLE_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");
    }

    @InjectMocks
    AssetVocabularyResolvers resolvers = new AssetVocabularyResolversImpl();

    @Mock
    private AssetVocabularyLocalService localService;

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
        doReturn(dataLoader).when(mockEnvironment).getDataLoader(AssetVocabularyBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((AssetVocabularyResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("vocabularyId")))
                    .thenReturn(VOCABULARY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("title")))
                    .thenReturn(TITLE);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("nameMap")))
                    .thenReturn(TITLE_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getStringArg(eq(environment), eq("settings")))
                    .thenReturn(SETTINGS);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getTranslatedArg(eq(environment), anyString()))
                    .thenReturn(Collections.emptyMap());
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
    public void getAssetVocabulariesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetVocabulary> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetVocabulary entity = mock(AssetVocabulary.class);
                    entity.setVocabularyId(value);
                    availableObjects.add(entity);
                });
        List<AssetVocabulary> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetVocabularies(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetVocabulary> results = resolvers.getAssetVocabulariesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetVocabulariesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<AssetVocabulary> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetVocabulary entity = mock(AssetVocabulary.class);
                    entity.setVocabularyId(value);
                    availableObjects.add(entity);
                });
        List<AssetVocabulary> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetVocabularies(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetVocabulary> results = resolvers.getAssetVocabulariesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetVocabulariesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetVocabulary> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetVocabulary entity = mock(AssetVocabulary.class);
                    entity.setVocabularyId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetVocabularies(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetVocabulary> results = resolvers.getAssetVocabulariesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetVocabulariesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetVocabulary> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetVocabularies(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetVocabulary> results = resolvers.getAssetVocabulariesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetVocabularyDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        AssetVocabulary expectedResult = mock(AssetVocabulary.class);
        expectedResult.setVocabularyId(3L);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("vocabularyId"))
                .thenReturn(3L);
        when(dataLoader.load(3L))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<AssetVocabulary> asyncResult = resolvers.getAssetVocabularyDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetVocabulary result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getAssetVocabularyDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("vocabularyId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<AssetVocabulary> asyncResult = resolvers.getAssetVocabularyDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getAssetVocabularyDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("vocabularyId"))
                .thenReturn(3L);
        when(dataLoader.load(3L))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<AssetVocabulary> asyncResult = resolvers.getAssetVocabularyDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetVocabulary result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createAssetVocabularyDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("title", TITLE);
        arguments.put("nameMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("settings", SETTINGS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetVocabulary expectedResult = mock(AssetVocabulary.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setName(TITLE);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setSettings(SETTINGS);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addVocabulary(eq(USER_ID), eq(GROUP_ID), eq(TITLE), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SETTINGS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetVocabulary result = resolvers.createAssetVocabularyDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createAssetVocabularyDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("title", TITLE);
        arguments.put("nameMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("settings", SETTINGS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetVocabulary expectedResult = mock(AssetVocabulary.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setName(TITLE);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setSettings(SETTINGS);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addVocabulary(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(TITLE), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SETTINGS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetVocabulary result = resolvers.createAssetVocabularyDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createAssetVocabularyDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addVocabulary(eq(DEFAULT_USER_ID), anyLong(), anyString(), anyMap(), anyMap(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        AssetVocabulary result = resolvers.createAssetVocabularyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateAssetVocabularyDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("vocabularyId", VOCABULARY_ID);
        arguments.put("title", TITLE);
        arguments.put("nameMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("settings", SETTINGS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetVocabulary expectedResult = mock(AssetVocabulary.class);
        expectedResult.setVocabularyId(VOCABULARY_ID);
        expectedResult.setName(TITLE);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setSettings(SETTINGS);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateVocabulary(eq(VOCABULARY_ID), eq(TITLE), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SETTINGS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetVocabulary result = resolvers.updateAssetVocabularyDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchVocabularyException.class)
    public void updateAssetVocabularyDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("title", TITLE);
        arguments.put("nameMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("settings", SETTINGS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("vocabularyId")))
                .thenReturn(0L);
        when(localService.updateVocabulary(eq(0L), eq(TITLE), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SETTINGS), any(ServiceContext.class)))
                .thenThrow(NoSuchVocabularyException.class);

        // Asserts
        AssetVocabulary result = resolvers.updateAssetVocabularyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchVocabularyException.class)
    public void updateAssetVocabularyDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("vocabularyId", 789456L);
        arguments.put("groupId", GROUP_ID);
        arguments.put("title", TITLE);
        arguments.put("nameMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("settings", SETTINGS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("vocabularyId")))
                .thenReturn(789456L);
        when(localService.updateVocabulary(eq(789456L), eq(TITLE), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SETTINGS), any(ServiceContext.class)))
                .thenThrow(NoSuchVocabularyException.class);

        // Asserts
        AssetVocabulary result = resolvers.updateAssetVocabularyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateAssetVocabularyDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("vocabularyId")))
                .thenReturn(VOCABULARY_ID);
        when(localService.updateVocabulary(eq(VOCABULARY_ID), anyString(), anyMap(), anyMap(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        AssetVocabulary result = resolvers.updateAssetVocabularyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteAssetVocabularyDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("vocabularyId", VOCABULARY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetVocabulary expectedResult = mock(AssetVocabulary.class);
        expectedResult.setVocabularyId(VOCABULARY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetVocabulary(eq(VOCABULARY_ID)))
                .thenReturn(expectedResult);

        // Asserts
        AssetVocabulary result = resolvers.deleteAssetVocabularyDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchVocabularyException.class)
    public void deleteAssetVocabularyDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        AssetVocabulary expectedResult = mock(AssetVocabulary.class);
        expectedResult.setVocabularyId(VOCABULARY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetVocabulary(eq(VOCABULARY_ID)))
                .thenThrow(NoSuchVocabularyException.class);

        // Asserts
        AssetVocabulary result = resolvers.deleteAssetVocabularyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchVocabularyException.class)
    public void deleteAssetVocabularyDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("vocabularyId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetVocabulary expectedResult = mock(AssetVocabulary.class);
        expectedResult.setVocabularyId(VOCABULARY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("vocabularyId")))
                .thenReturn(789456L);
        when(localService.deleteAssetVocabulary(eq(789456L)))
                .thenThrow(NoSuchVocabularyException.class);

        // Asserts
        AssetVocabulary result = resolvers.deleteAssetVocabularyDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
