package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.ratings.kernel.exception.NoSuchEntryException;
import com.liferay.ratings.kernel.model.RatingsEntry;
import com.liferay.ratings.kernel.service.RatingsEntryLocalService;
import fr.sedona.liferay.graphql.loaders.RatingsEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.RatingsEntryResolvers;
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
 * Test suite for {@link RatingsEntryResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class RatingsEntryResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long ENTRY_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final long CLASS_PK = 456L;
    private static final double SCORE = 5.0;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, RatingsEntry> dataLoader;

    @InjectMocks
    RatingsEntryResolvers resolvers = new RatingsEntryResolversImpl();

    @Mock
    private RatingsEntryLocalService localService;

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
                .getDataLoader(RatingsEntryBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((RatingsEntryResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("entryId")))
                    .thenReturn(ENTRY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getDoubleArg(eq(environment), eq("score")))
                    .thenReturn(SCORE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getDoubleArg(eq(environment), anyString()))
                    .thenReturn(0.0);
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
    public void getRatingsEntriesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<RatingsEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    RatingsEntry entity = mock(RatingsEntry.class);
                    entity.setEntryId(value);
                    availableObjects.add(entity);
                });
        List<RatingsEntry> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRatingsEntries(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<RatingsEntry> results = resolvers.getRatingsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRatingsEntriesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<RatingsEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    RatingsEntry entity = mock(RatingsEntry.class);
                    entity.setEntryId(value);
                    availableObjects.add(entity);
                });
        List<RatingsEntry> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRatingsEntries(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<RatingsEntry> results = resolvers.getRatingsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRatingsEntriesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<RatingsEntry> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    RatingsEntry entity = mock(RatingsEntry.class);
                    entity.setEntryId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRatingsEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<RatingsEntry> results = resolvers.getRatingsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRatingsEntriesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<RatingsEntry> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRatingsEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<RatingsEntry> results = resolvers.getRatingsEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRatingsEntryDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId", ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        RatingsEntry expectedResult = mock(RatingsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(ENTRY_ID);
        when(dataLoader.load(ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<RatingsEntry> asyncResult = resolvers.getRatingsEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        RatingsEntry result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getRatingsEntryDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<RatingsEntry> asyncResult = resolvers.getRatingsEntryDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getRatingsEntryDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("entryId"))
                .thenReturn(ENTRY_ID);
        when(dataLoader.load(ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<RatingsEntry> asyncResult = resolvers.getRatingsEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        RatingsEntry result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createRatingsEntryDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("score", SCORE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        RatingsEntry expectedResult = mock(RatingsEntry.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setScore(SCORE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateEntry(eq(USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(SCORE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        RatingsEntry result = resolvers.createRatingsEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createRatingsEntryDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("score", SCORE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        RatingsEntry expectedResult = mock(RatingsEntry.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setScore(SCORE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.updateEntry(eq(DEFAULT_USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(SCORE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        RatingsEntry result = resolvers.createRatingsEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createRatingsEntryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.updateEntry(eq(DEFAULT_USER_ID), anyString(), anyLong(), anyDouble(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        RatingsEntry result = resolvers.createRatingsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteRatingsEntryDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId", ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        RatingsEntry expectedResult = mock(RatingsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteRatingsEntry(eq(ENTRY_ID)))
                .thenReturn(expectedResult);

        // Asserts
        RatingsEntry result = resolvers.deleteRatingsEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchEntryException.class)
    public void deleteRatingsEntryDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        RatingsEntry expectedResult = mock(RatingsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteRatingsEntry(eq(ENTRY_ID)))
                .thenThrow(NoSuchEntryException.class);

        // Asserts
        RatingsEntry result = resolvers.deleteRatingsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchEntryException.class)
    public void deleteRatingsEntryDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("entryId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        RatingsEntry expectedResult = mock(RatingsEntry.class);
        expectedResult.setEntryId(ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("entryId")))
                .thenReturn(789456L);
        when(localService.deleteRatingsEntry(eq(789456L)))
                .thenThrow(NoSuchEntryException.class);

        // Asserts
        RatingsEntry result = resolvers.deleteRatingsEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
