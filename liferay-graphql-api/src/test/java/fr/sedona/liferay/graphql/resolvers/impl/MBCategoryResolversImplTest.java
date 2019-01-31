package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.exception.NoSuchCategoryException;
import com.liferay.message.boards.kernel.model.MBCategory;
import com.liferay.message.boards.kernel.service.MBCategoryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.MBCategoryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.MBCategoryResolvers;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link MBCategoryResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class MBCategoryResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long CATEGORY_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long PARENT_CATEGORY_ID = 456L;
    private static final String NAME = "Category name";
    private static final String DESCRIPTION = "Category description";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, MBCategory> dataLoader;

    @InjectMocks
    MBCategoryResolvers resolvers = new MBCategoryResolversImpl();

    @Mock
    private MBCategoryLocalService localService;

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
                .getDataLoader(MBCategoryBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((MBCategoryResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                    .thenReturn(CATEGORY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentCategoryId")))
                    .thenReturn(PARENT_CATEGORY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
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
    public void getMBCategoriesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBCategory> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBCategory entity = mock(MBCategory.class);
                    entity.setCategoryId(value);
                    availableObjects.add(entity);
                });
        List<MBCategory> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBCategories(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<MBCategory> results = resolvers.getMBCategoriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBCategoriesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<MBCategory> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBCategory entity = mock(MBCategory.class);
                    entity.setCategoryId(value);
                    availableObjects.add(entity);
                });
        List<MBCategory> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBCategories(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<MBCategory> results = resolvers.getMBCategoriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBCategoriesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBCategory> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBCategory entity = mock(MBCategory.class);
                    entity.setCategoryId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBCategories(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<MBCategory> results = resolvers.getMBCategoriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBCategoriesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBCategory> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBCategories(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<MBCategory> results = resolvers.getMBCategoriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBCategoryDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        MBCategory expectedResult = mock(MBCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("categoryId"))
                .thenReturn(CATEGORY_ID);
        when(dataLoader.load(CATEGORY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<MBCategory> asyncResult = resolvers.getMBCategoryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        MBCategory result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getMBCategoryDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("categoryId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<MBCategory> asyncResult = resolvers.getMBCategoryDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getMBCategoryDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("categoryId"))
                .thenReturn(CATEGORY_ID);
        when(dataLoader.load(CATEGORY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<MBCategory> asyncResult = resolvers.getMBCategoryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        MBCategory result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createMBCategoryDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBCategory expectedResult = mock(MBCategory.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCategoryId(CATEGORY_ID);
        expectedResult.setParentCategoryId(PARENT_CATEGORY_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addCategory(eq(USER_ID), eq(PARENT_CATEGORY_ID), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        MBCategory result = resolvers.createMBCategoryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createMBCategoryDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBCategory expectedResult = mock(MBCategory.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setCategoryId(CATEGORY_ID);
        expectedResult.setParentCategoryId(PARENT_CATEGORY_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addCategory(eq(DEFAULT_USER_ID), eq(PARENT_CATEGORY_ID), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        MBCategory result = resolvers.createMBCategoryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createMBCategoryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addCategory(anyLong(), anyLong(), anyString(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        MBCategory result = resolvers.createMBCategoryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateMBCategoryDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", CATEGORY_ID);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBCategory expectedResult = mock(MBCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);
        expectedResult.setParentCategoryId(PARENT_CATEGORY_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateCategory(eq(CATEGORY_ID), eq(PARENT_CATEGORY_ID), eq(NAME), eq(DESCRIPTION), anyString(), anyString(), anyString(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyInt(), anyString(), anyBoolean(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        MBCategory result = resolvers.updateMBCategoryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void updateMBCategoryDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(0L);
        when(localService.updateCategory(eq(0L), eq(PARENT_CATEGORY_ID), eq(NAME), eq(DESCRIPTION), anyString(), anyString(), anyString(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyInt(), anyString(), anyBoolean(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        MBCategory result = resolvers.updateMBCategoryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void updateMBCategoryDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", 789456L);
        arguments.put("parentCategoryId", PARENT_CATEGORY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(789456L);
        when(localService.updateCategory(eq(789456L), eq(PARENT_CATEGORY_ID), eq(NAME), eq(DESCRIPTION), anyString(), anyString(), anyString(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyInt(), anyString(), anyBoolean(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        MBCategory result = resolvers.updateMBCategoryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateMBCategoryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(0L);
        when(localService.updateCategory(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyInt(), anyString(), anyBoolean(), anyString(), anyInt(), anyBoolean(), anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        MBCategory result = resolvers.updateMBCategoryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteMBCategoryDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", CATEGORY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBCategory expectedResult = mock(MBCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteMBCategory(eq(CATEGORY_ID)))
                .thenReturn(expectedResult);

        // Asserts
        MBCategory result = resolvers.deleteMBCategoryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void deleteMBCategoryDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        MBCategory expectedResult = mock(MBCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteMBCategory(eq(CATEGORY_ID)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        MBCategory result = resolvers.deleteMBCategoryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchCategoryException.class)
    public void deleteMBCategoryDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBCategory expectedResult = mock(MBCategory.class);
        expectedResult.setCategoryId(CATEGORY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                .thenReturn(789456L);
        when(localService.deleteMBCategory(eq(789456L)))
                .thenThrow(NoSuchCategoryException.class);

        // Asserts
        MBCategory result = resolvers.deleteMBCategoryDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
