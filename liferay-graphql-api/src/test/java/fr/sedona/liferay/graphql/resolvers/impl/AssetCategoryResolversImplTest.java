package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.exception.NoSuchCategoryException;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.AssetCategoryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetCategoryResolvers;
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
 * Test suite for {@link AssetCategoryResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class AssetCategoryResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long CATEGORY_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 123L;
    private static final long PARENT_CATEGORY_ID = 456L;
    private static final Map<Locale, String> TITLE_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final long VOCABULARY_ID = 789L;
    private static final String[] CATEGORY_PROPERTIES = new String[]{"properties 1", "properties 2"};
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, AssetCategory> dataLoader;

    static {
        TITLE_MAP = new HashMap<>();
        TITLE_MAP.put(LocaleUtil.US, "Test title");
        TITLE_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");
    }

    @InjectMocks
    AssetCategoryResolvers resolvers = new AssetCategoryResolversImpl();

    @Mock
    private AssetCategoryLocalService localService;

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
                .getDataLoader(AssetCategoryBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((AssetCategoryResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                    .thenReturn(CATEGORY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentCategoryId"), anyLong()))
                    .thenReturn(PARENT_CATEGORY_ID);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("titleMap")))
                    .thenReturn(TITLE_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getLongArg(eq(environment), eq("vocabularyId")))
                    .thenReturn(VOCABULARY_ID);
            when(graphQLUtil.getStringArrayArg(eq(environment), eq("categoryProperties")))
                    .thenReturn(CATEGORY_PROPERTIES);
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
    public void getAssetCategoriesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetCategory> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetCategory entity = mock(AssetCategory.class);
                    entity.setCategoryId(value);
                    availableObjects.add(entity);
                });
        List<AssetCategory> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetCategories(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetCategory> results = resolvers.getAssetCategoriesDataFetcher().get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetCategoriesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<AssetCategory> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetCategory entity = mock(AssetCategory.class);
                    entity.setCategoryId(value);
                    availableObjects.add(entity);
                });
        List<AssetCategory> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetCategories(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetCategory> results = resolvers.getAssetCategoriesDataFetcher().get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetCategoriesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetCategory> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetCategory entity = mock(AssetCategory.class);
                    entity.setCategoryId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetCategories(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetCategory> results = resolvers.getAssetCategoriesDataFetcher().get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetCategoriesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetCategory> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetCategories(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetCategory> results = resolvers.getAssetCategoriesDataFetcher().get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetCategoryDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        AssetCategory expectedResult = mock(AssetCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("categoryId"))
                .thenReturn(CATEGORY_ID);
        when(dataLoader.load(CATEGORY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<AssetCategory> asyncResult = resolvers.getAssetCategoryDataFetcher().get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetCategory result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getAssetCategoryDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("categoryId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<AssetCategory> asyncResult = resolvers.getAssetCategoryDataFetcher().get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getAssetCategoryDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("categoryId"))
                .thenReturn(CATEGORY_ID);
        when(dataLoader.load(CATEGORY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<AssetCategory> asyncResult = resolvers.getAssetCategoryDataFetcher().get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetCategory result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createAssetCategoryDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("vocabularyId", VOCABULARY_ID);
        arguments.put("categoryProperties", CATEGORY_PROPERTIES);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetCategory expectedResult = mock(AssetCategory.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentCategoryId(PARENT_CATEGORY_ID);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setVocabularyId(VOCABULARY_ID);
        // REMARK: This field is useless... Thank you Liferay
        // expectedResult.setCategoryProperties(CATEGORY_PROPERTIES);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addCategory(eq(USER_ID), eq(GROUP_ID), eq(PARENT_CATEGORY_ID), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(VOCABULARY_ID), eq(CATEGORY_PROPERTIES), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetCategory result = resolvers.createAssetCategoryDataFetcher().get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createAssetCategoryDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("vocabularyId", VOCABULARY_ID);
        arguments.put("categoryProperties", CATEGORY_PROPERTIES);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetCategory expectedResult = mock(AssetCategory.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentCategoryId(PARENT_CATEGORY_ID);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setVocabularyId(VOCABULARY_ID);
        // REMARK: This field is useless... Thank you Liferay
        // expectedResult.setCategoryProperties(CATEGORY_PROPERTIES);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addCategory(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(PARENT_CATEGORY_ID), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(VOCABULARY_ID), eq(CATEGORY_PROPERTIES), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetCategory result = resolvers.createAssetCategoryDataFetcher().get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createAssetCategoryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addCategory(eq(DEFAULT_USER_ID), anyLong(), anyLong(), anyMap(), anyMap(), anyLong(), any(String[].class), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        AssetCategory result = resolvers.createAssetCategoryDataFetcher().get(environment);
        assertNull(result);
    }

    @Test
    public void updateAssetCategoryDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("categoryId", CATEGORY_ID);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("vocabularyId", VOCABULARY_ID);
        arguments.put("categoryProperties", CATEGORY_PROPERTIES);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetCategory expectedResult = mock(AssetCategory.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCategoryId(CATEGORY_ID);
        expectedResult.setParentCategoryId(PARENT_CATEGORY_ID);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setVocabularyId(VOCABULARY_ID);
        // REMARK: This field is useless... Thank you Liferay
        // expectedResult.setCategoryProperties(CATEGORY_PROPERTIES);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateCategory(eq(USER_ID), eq(CATEGORY_ID), eq(PARENT_CATEGORY_ID), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(VOCABULARY_ID), eq(CATEGORY_PROPERTIES), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetCategory result = resolvers.updateAssetCategoryDataFetcher().get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void updateAssetCategoryDataFetcher_with_no_assetCategory_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("vocabularyId", VOCABULARY_ID);
        arguments.put("categoryProperties", CATEGORY_PROPERTIES);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(0L);
        when(localService.updateCategory(eq(DEFAULT_USER_ID), eq(0L), eq(PARENT_CATEGORY_ID), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(VOCABULARY_ID), eq(CATEGORY_PROPERTIES), any(ServiceContext.class)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        AssetCategory result = resolvers.updateAssetCategoryDataFetcher().get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void updateAssetCategoryDataFetcher_with_invalid_assetCategory_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", 789456L);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("vocabularyId", VOCABULARY_ID);
        arguments.put("categoryProperties", CATEGORY_PROPERTIES);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(789456L);
        when(localService.updateCategory(eq(DEFAULT_USER_ID), eq(789456L), eq(PARENT_CATEGORY_ID), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(VOCABULARY_ID), eq(CATEGORY_PROPERTIES), any(ServiceContext.class)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        AssetCategory result = resolvers.updateAssetCategoryDataFetcher().get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateAssetCategoryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(CATEGORY_ID);
        when(localService.updateCategory(eq(DEFAULT_USER_ID), eq(CATEGORY_ID), anyLong(), anyMap(), anyMap(), anyLong(), any(String[].class), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        AssetCategory result = resolvers.updateAssetCategoryDataFetcher().get(environment);
        assertNull(result);
    }

    @Test
    public void deleteAssetCategoryDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", CATEGORY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetCategory expectedResult = mock(AssetCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetCategory(eq(CATEGORY_ID)))
                .thenReturn(expectedResult);

        // Asserts
        AssetCategory result = resolvers.deleteAssetCategoryDataFetcher().get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void deleteAssetCategoryDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        AssetCategory expectedResult = mock(AssetCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetCategory(eq(CATEGORY_ID)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        AssetCategory result = resolvers.deleteAssetCategoryDataFetcher().get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void deleteAssetCategoryDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetCategory expectedResult = mock(AssetCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(789456L);
        when(localService.deleteAssetCategory(eq(789456L)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        AssetCategory result = resolvers.deleteAssetCategoryDataFetcher().get(environment);
        assertNull(result);
    }
}
