package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.exception.NoSuchLinkException;
import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.asset.kernel.service.AssetLinkLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import fr.sedona.liferay.graphql.loaders.AssetLinkBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetLinkResolvers;
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
 * Test suite for {@link AssetLinkResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class AssetLinkResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long LINK_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long ENTRY_ID1 = 456L;
    private static final long ENTRY_ID2 = 457L;
    private static final int TYPE = 0;
    private static final int WEIGHT = 10;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, AssetLink> dataLoader;

    @InjectMocks
    AssetLinkResolvers resolvers = new AssetLinkResolversImpl();

    @Mock
    private AssetLinkLocalService localService;

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
                .getDataLoader(AssetLinkBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((AssetLinkResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("linkId")))
                    .thenReturn(LINK_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("entryId1")))
                    .thenReturn(ENTRY_ID1);
            when(graphQLUtil.getLongArg(eq(environment), eq("entryId2")))
                    .thenReturn(ENTRY_ID2);
            when(graphQLUtil.getIntArg(eq(environment), eq("type")))
                    .thenReturn(TYPE);
            when(graphQLUtil.getIntArg(eq(environment), eq("weight")))
                    .thenReturn(WEIGHT);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
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
    public void getAssetLinksDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetLink> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetLink entity = mock(AssetLink.class);
                    entity.setLinkId(value);
                    availableObjects.add(entity);
                });
        List<AssetLink> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetLinks(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetLink> results = resolvers.getAssetLinksDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetLinksDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<AssetLink> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetLink entity = mock(AssetLink.class);
                    entity.setLinkId(value);
                    availableObjects.add(entity);
                });
        List<AssetLink> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetLinks(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetLink> results = resolvers.getAssetLinksDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetLinksDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetLink> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    AssetLink entity = mock(AssetLink.class);
                    entity.setLinkId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetLinks(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetLink> results = resolvers.getAssetLinksDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetLinksDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<AssetLink> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAssetLinks(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<AssetLink> results = resolvers.getAssetLinksDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAssetLinkDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        AssetLink expectedResult = mock(AssetLink.class);
        expectedResult.setLinkId(LINK_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("linkId"))
                .thenReturn(LINK_ID);
        when(dataLoader.load(LINK_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<AssetLink> asyncResult = resolvers.getAssetLinkDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetLink result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getAssetLinkDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("linkId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<AssetLink> asyncResult = resolvers.getAssetLinkDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getAssetLinkDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("linkId"))
                .thenReturn(LINK_ID);
        when(dataLoader.load(LINK_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<AssetLink> asyncResult = resolvers.getAssetLinkDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        AssetLink result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createAssetLinkDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("entryId1", ENTRY_ID1);
        arguments.put("entryId2", ENTRY_ID2);
        arguments.put("type", TYPE);
        arguments.put("weight", WEIGHT);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetLink expectedResult = mock(AssetLink.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setLinkId(LINK_ID);
        expectedResult.setEntryId1(ENTRY_ID1);
        expectedResult.setEntryId2(ENTRY_ID2);
        expectedResult.setType(TYPE);
        expectedResult.setWeight(WEIGHT);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addLink(eq(USER_ID), eq(ENTRY_ID1), eq(ENTRY_ID2), eq(TYPE), eq(WEIGHT)))
                .thenReturn(expectedResult);

        // Asserts
        AssetLink result = resolvers.createAssetLinkDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createAssetLinkDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId1", ENTRY_ID1);
        arguments.put("entryId2", ENTRY_ID2);
        arguments.put("type", TYPE);
        arguments.put("weight", WEIGHT);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetLink expectedResult = mock(AssetLink.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setLinkId(LINK_ID);
        expectedResult.setEntryId1(ENTRY_ID1);
        expectedResult.setEntryId2(ENTRY_ID2);
        expectedResult.setType(TYPE);
        expectedResult.setWeight(WEIGHT);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addLink(eq(DEFAULT_USER_ID), eq(ENTRY_ID1), eq(ENTRY_ID2), eq(TYPE), eq(WEIGHT)))
                .thenReturn(expectedResult);

        // Asserts
        AssetLink result = resolvers.createAssetLinkDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createAssetLinkDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addLink(anyLong(), anyLong(), anyLong(), anyInt(), anyInt()))
                .thenThrow(PortalException.class);

        // Asserts
        AssetLink result = resolvers.createAssetLinkDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateAssetLinkDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("entryId1", ENTRY_ID1);
        arguments.put("entryId2", ENTRY_ID2);
        arguments.put("type", TYPE);
        arguments.put("weight", WEIGHT);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetLink expectedResult = mock(AssetLink.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setLinkId(LINK_ID);
        expectedResult.setEntryId1(ENTRY_ID1);
        expectedResult.setEntryId2(ENTRY_ID2);
        expectedResult.setType(TYPE);
        expectedResult.setWeight(WEIGHT);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateLink(eq(USER_ID), eq(ENTRY_ID1), eq(ENTRY_ID2), eq(TYPE), eq(WEIGHT)))
                .thenReturn(expectedResult);

        // Asserts
        AssetLink result = resolvers.updateAssetLinkDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void updateAssetLinkDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("linkId")))
                .thenReturn(LINK_ID);
        when(localService.updateLink(anyLong(), anyLong(), anyLong(), anyInt(), anyInt()))
                .thenThrow(PortalException.class);

        // Asserts
        AssetLink result = resolvers.updateAssetLinkDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteAssetLinkDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("linkId", LINK_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetLink expectedResult = mock(AssetLink.class);
        expectedResult.setLinkId(LINK_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetLink(eq(LINK_ID)))
                .thenReturn(expectedResult);

        // Asserts
        AssetLink result = resolvers.deleteAssetLinkDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchLinkException.class)
    public void deleteAssetLinkDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        AssetLink expectedResult = mock(AssetLink.class);
        expectedResult.setLinkId(LINK_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAssetLink(eq(LINK_ID)))
                .thenThrow(NoSuchLinkException.class);

        // Asserts
        AssetLink result = resolvers.deleteAssetLinkDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchLinkException.class)
    public void deleteAssetLinkDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("linkId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        AssetLink expectedResult = mock(AssetLink.class);
        expectedResult.setLinkId(LINK_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("linkId")))
                .thenReturn(789456L);
        when(localService.deleteAssetLink(eq(789456L)))
                .thenThrow(NoSuchLinkException.class);

        // Asserts
        AssetLink result = resolvers.deleteAssetLinkDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
