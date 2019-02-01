package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.exception.NoSuchTagException;
import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.AssetTagBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetTagResolvers;
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
 * Test suite for {@link AssetTagResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class AssetTagResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long TAG_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final String NAME = "tag";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, AssetTag> dataLoader;

    @InjectMocks
    AssetTagResolvers resolvers = new AssetTagResolversImpl();

    @Mock
    private AssetTagLocalService localService;

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
                .getDataLoader(AssetTagBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((AssetTagResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("tagId")))
                    .thenReturn(TAG_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
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
    public void getAssetTagsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetTag> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetTag entity = mock(AssetTag.class);
                    entity.setTagId(value);
                    availableObjects.add(entity);
                });
        List<AssetTag> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetTags(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetTag> results = resolvers.getAssetTagsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetTagsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<AssetTag> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetTag entity = mock(AssetTag.class);
                    entity.setTagId(value);
                    availableObjects.add(entity);
                });
        List<AssetTag> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetTags(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetTag> results = resolvers.getAssetTagsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetTagsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetTag> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetTag entity = mock(AssetTag.class);
                    entity.setTagId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetTags(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetTag> results = resolvers.getAssetTagsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetTagsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetTag> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetTags(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetTag> results = resolvers.getAssetTagsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetTagDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        AssetTag expectedResult = mock(AssetTag.class);
        expectedResult.setTagId(TAG_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("tagId"))
                .thenReturn(TAG_ID);
        when(dataLoader.load(TAG_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<AssetTag> asyncResult = resolvers.getAssetTagDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetTag result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getAssetTagDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("tagId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<AssetTag> asyncResult = resolvers.getAssetTagDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getAssetTagDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("tagId"))
                .thenReturn(TAG_ID);
        when(dataLoader.load(TAG_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<AssetTag> asyncResult = resolvers.getAssetTagDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetTag result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createAssetTagDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetTag expectedResult = mock(AssetTag.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setTagId(TAG_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setName(NAME);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addTag(eq(USER_ID), eq(GROUP_ID), eq(NAME), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetTag result = resolvers.createAssetTagDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createAssetTagDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetTag expectedResult = mock(AssetTag.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setTagId(TAG_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setName(NAME);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addTag(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(NAME), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetTag result = resolvers.createAssetTagDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createAssetTagDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addTag(anyLong(), anyLong(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        AssetTag result = resolvers.createAssetTagDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateAssetTagDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tagId", TAG_ID);
        arguments.put("userId", USER_ID);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetTag expectedResult = mock(AssetTag.class);
        expectedResult.setTagId(TAG_ID);
        expectedResult.setUserId(USER_ID);
        expectedResult.setName(NAME);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateTag(eq(USER_ID), eq(TAG_ID), eq(NAME), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        AssetTag result = resolvers.updateAssetTagDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchTagException.class)
    public void updateAssetTagDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("tagId")))
                .thenReturn(0L);
        when(localService.updateTag(eq(USER_ID), eq(0L), eq(NAME), any(ServiceContext.class)))
                .thenThrow(NoSuchTagException.class);

        // Asserts
        AssetTag result = resolvers.updateAssetTagDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchTagException.class)
    public void updateAssetTagDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tagId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("name", NAME);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("tagId")))
                .thenReturn(789456L);
        when(localService.updateTag(eq(USER_ID), eq(789456L), eq(NAME), any(ServiceContext.class)))
                .thenThrow(NoSuchTagException.class);

        // Asserts
        AssetTag result = resolvers.updateAssetTagDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateAssetTagDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("tagId")))
                .thenReturn(TAG_ID);
        when(localService.updateTag(anyLong(), anyLong(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        AssetTag result = resolvers.updateAssetTagDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteAssetTagDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tagId", TAG_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetTag expectedResult = mock(AssetTag.class);
        expectedResult.setTagId(TAG_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetTag(eq(TAG_ID)))
                .thenReturn(expectedResult);

        // Asserts
        AssetTag result = resolvers.deleteAssetTagDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchTagException.class)
    public void deleteAssetTagDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        AssetTag expectedResult = mock(AssetTag.class);
        expectedResult.setTagId(TAG_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetTag(eq(TAG_ID)))
                .thenThrow(NoSuchTagException.class);

        // Asserts
        AssetTag result = resolvers.deleteAssetTagDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchTagException.class)
    public void deleteAssetTagDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("tagId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetTag expectedResult = mock(AssetTag.class);
        expectedResult.setTagId(TAG_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("tagId")))
                .thenReturn(789456L);
        when(localService.deleteAssetTag(eq(789456L)))
                .thenThrow(NoSuchTagException.class);

        // Asserts
        AssetTag result = resolvers.deleteAssetTagDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
