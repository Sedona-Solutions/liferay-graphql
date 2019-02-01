package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.message.boards.kernel.exception.NoSuchThreadException;
import com.liferay.message.boards.kernel.model.MBMessage;
import com.liferay.message.boards.kernel.model.MBThread;
import com.liferay.message.boards.kernel.service.MBThreadLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.MBThreadBatchLoader;
import fr.sedona.liferay.graphql.resolvers.MBThreadResolvers;
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
 * Test suite for {@link MBThreadResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class MBThreadResolversImplTest {
    private static final long THREAD_ID = 987L;
    private static final long CATEGORY_ID = 456L;
    private static final MBMessage MESSAGE;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, MBThread> dataLoader;

    static {
        MESSAGE = mock(MBMessage.class);
        MESSAGE.setMessageId(123L);
        MESSAGE.setSubject("Test subject");
        MESSAGE.setBody("Test body");
    }

    @InjectMocks
    MBThreadResolvers resolvers = new MBThreadResolversImpl();

    @Mock
    private MBThreadLocalService localService;

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
                .getDataLoader(MBThreadBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((MBThreadResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("threadId")))
                    .thenReturn(THREAD_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                    .thenReturn(CATEGORY_ID);
            when(graphQLUtil.getMBMessageArg(eq(environment), eq("message")))
                    .thenReturn(MESSAGE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getMBMessageArg(eq(environment), anyString()))
                    .thenReturn(null);
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
    public void getMBThreadsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBThread> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBThread entity = mock(MBThread.class);
                    entity.setThreadId(value);
                    availableObjects.add(entity);
                });
        List<MBThread> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBThreads(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<MBThread> results = resolvers.getMBThreadsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBThreadsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<MBThread> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBThread entity = mock(MBThread.class);
                    entity.setThreadId(value);
                    availableObjects.add(entity);
                });
        List<MBThread> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBThreads(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<MBThread> results = resolvers.getMBThreadsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBThreadsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBThread> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBThread entity = mock(MBThread.class);
                    entity.setThreadId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBThreads(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<MBThread> results = resolvers.getMBThreadsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBThreadsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBThread> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBThreads(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<MBThread> results = resolvers.getMBThreadsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBThreadDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        MBThread expectedResult = mock(MBThread.class);
        expectedResult.setThreadId(THREAD_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("threadId"))
                .thenReturn(THREAD_ID);
        when(dataLoader.load(THREAD_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<MBThread> asyncResult = resolvers.getMBThreadDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        MBThread result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getMBThreadDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("threadId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<MBThread> asyncResult = resolvers.getMBThreadDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getMBThreadDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("threadId"))
                .thenReturn(THREAD_ID);
        when(dataLoader.load(THREAD_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<MBThread> asyncResult = resolvers.getMBThreadDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        MBThread result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createMBThreadDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("categoryId", CATEGORY_ID);
        arguments.put("message", MESSAGE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBThread expectedResult = mock(MBThread.class);
        expectedResult.setThreadId(THREAD_ID);
        expectedResult.setCategoryId(CATEGORY_ID);
        expectedResult.setRootMessageId(MESSAGE.getMessageId());

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addThread(eq(CATEGORY_ID), eq(MESSAGE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        MBThread result = resolvers.createMBThreadDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createMBThreadDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addThread(anyLong(), any(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        MBThread result = resolvers.createMBThreadDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteMBThreadDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("threadId", THREAD_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBThread expectedResult = mock(MBThread.class);
        expectedResult.setThreadId(THREAD_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteMBThread(eq(THREAD_ID)))
                .thenReturn(expectedResult);

        // Asserts
        MBThread result = resolvers.deleteMBThreadDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchThreadException.class)
    public void deleteMBThreadDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        MBThread expectedResult = mock(MBThread.class);
        expectedResult.setThreadId(THREAD_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteMBThread(eq(THREAD_ID)))
                .thenThrow(NoSuchThreadException.class);

        // Asserts
        MBThread result = resolvers.deleteMBThreadDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchThreadException.class)
    public void deleteMBThreadDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("threadId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBThread expectedResult = mock(MBThread.class);
        expectedResult.setThreadId(THREAD_ID);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("threadId")))
                .thenReturn(789456L);
        when(localService.deleteMBThread(eq(789456L)))
                .thenThrow(NoSuchThreadException.class);

        // Asserts
        MBThread result = resolvers.deleteMBThreadDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
