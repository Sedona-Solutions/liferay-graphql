package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.message.boards.kernel.exception.NoSuchMessageException;
import com.liferay.message.boards.kernel.model.MBMessage;
import com.liferay.message.boards.kernel.service.MBMessageLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.MBMessageBatchLoader;
import fr.sedona.liferay.graphql.resolvers.MBMessageResolvers;
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
 * Test suite for {@link MBMessageResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class MBMessageResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long MESSAGE_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String USER_NAME = "Paul Mars";
    private static final long GROUP_ID = 456L;
    private static final long CATEGORY_ID = 1L;
    private static final long THREAD_ID = 0L;
    private static final long PARENT_MESSAGE_ID = 0L;
    private static final String SUBJECT = "Subject";
    private static final String BODY = "body";
    private static final String FORMAT = "format";
    private static final boolean ANONYMOUS = false;
    private static final double PRIORITY = 10.0;
    private static final boolean ALLOW_PINGBACKS = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, MBMessage> dataLoader;

    @InjectMocks
    MBMessageResolvers resolvers = new MBMessageResolversImpl();

    @Mock
    private MBMessageLocalService localService;

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
                .getDataLoader(MBMessageBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((MBMessageResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("messageId")))
                    .thenReturn(MESSAGE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("userName")))
                    .thenReturn(USER_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("categoryId")))
                    .thenReturn(CATEGORY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("threadId")))
                    .thenReturn(THREAD_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentMessageId")))
                    .thenReturn(PARENT_MESSAGE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("subject")))
                    .thenReturn(SUBJECT);
            when(graphQLUtil.getStringArg(eq(environment), eq("body")))
                    .thenReturn(BODY);
            when(graphQLUtil.getStringArg(eq(environment), eq("format")))
                    .thenReturn(FORMAT);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("anonymous")))
                    .thenReturn(ANONYMOUS);
            when(graphQLUtil.getDoubleArg(eq(environment), eq("priority")))
                    .thenReturn(PRIORITY);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("allowPingbacks")))
                    .thenReturn(ALLOW_PINGBACKS);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getDoubleArg(eq(environment), anyString()))
                    .thenReturn(0.0);
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
    public void getMBMessagesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBMessage> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBMessage entity = mock(MBMessage.class);
                    entity.setMessageId(value);
                    availableObjects.add(entity);
                });
        List<MBMessage> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBMessages(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<MBMessage> results = resolvers.getMBMessagesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBMessagesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<MBMessage> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBMessage entity = mock(MBMessage.class);
                    entity.setMessageId(value);
                    availableObjects.add(entity);
                });
        List<MBMessage> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBMessages(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<MBMessage> results = resolvers.getMBMessagesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBMessagesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBMessage> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    MBMessage entity = mock(MBMessage.class);
                    entity.setMessageId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBMessages(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<MBMessage> results = resolvers.getMBMessagesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBMessagesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<MBMessage> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getMBMessages(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<MBMessage> results = resolvers.getMBMessagesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getMBMessageDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        MBMessage expectedResult = mock(MBMessage.class);
        expectedResult.setMessageId(MESSAGE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("messageId"))
                .thenReturn(MESSAGE_ID);
        when(dataLoader.load(MESSAGE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<MBMessage> asyncResult = resolvers.getMBMessageDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        MBMessage result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getMBMessageDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("messageId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<MBMessage> asyncResult = resolvers.getMBMessageDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getMBMessageDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("messageId"))
                .thenReturn(MESSAGE_ID);
        when(dataLoader.load(MESSAGE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<MBMessage> asyncResult = resolvers.getMBMessageDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        MBMessage result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createMBMessageDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("userName", USER_NAME);
        arguments.put("groupId", GROUP_ID);
        arguments.put("categoryId", CATEGORY_ID);
        arguments.put("threadId", THREAD_ID);
        arguments.put("parentMessageId", PARENT_MESSAGE_ID);
        arguments.put("subject", SUBJECT);
        arguments.put("body", BODY);
        arguments.put("format", FORMAT);
        arguments.put("anonymous", ANONYMOUS);
        arguments.put("priority", PRIORITY);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBMessage expectedResult = mock(MBMessage.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setCategoryId(CATEGORY_ID);
        expectedResult.setThreadId(THREAD_ID);
        expectedResult.setParentMessageId(PARENT_MESSAGE_ID);
        expectedResult.setSubject(SUBJECT);
        expectedResult.setBody(BODY);
        expectedResult.setFormat(FORMAT);
        expectedResult.setAnonymous(ANONYMOUS);
        expectedResult.setPriority(PRIORITY);
        expectedResult.setAllowPingbacks(ALLOW_PINGBACKS);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addMessage(eq(USER_ID), eq(USER_NAME), eq(GROUP_ID), eq(CATEGORY_ID), eq(THREAD_ID), eq(PARENT_MESSAGE_ID), eq(SUBJECT), eq(BODY), eq(FORMAT), anyList(), eq(ANONYMOUS), eq(PRIORITY), eq(ALLOW_PINGBACKS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        MBMessage result = resolvers.createMBMessageDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createMBMessageDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userName", USER_NAME);
        arguments.put("groupId", GROUP_ID);
        arguments.put("categoryId", CATEGORY_ID);
        arguments.put("threadId", THREAD_ID);
        arguments.put("parentMessageId", PARENT_MESSAGE_ID);
        arguments.put("subject", SUBJECT);
        arguments.put("body", BODY);
        arguments.put("format", FORMAT);
        arguments.put("anonymous", ANONYMOUS);
        arguments.put("priority", PRIORITY);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBMessage expectedResult = mock(MBMessage.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setUserName(USER_NAME);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setCategoryId(CATEGORY_ID);
        expectedResult.setThreadId(THREAD_ID);
        expectedResult.setParentMessageId(PARENT_MESSAGE_ID);
        expectedResult.setSubject(SUBJECT);
        expectedResult.setBody(BODY);
        expectedResult.setFormat(FORMAT);
        expectedResult.setAnonymous(ANONYMOUS);
        expectedResult.setPriority(PRIORITY);
        expectedResult.setAllowPingbacks(ALLOW_PINGBACKS);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addMessage(eq(DEFAULT_USER_ID), eq(USER_NAME), eq(GROUP_ID), eq(CATEGORY_ID), eq(THREAD_ID), eq(PARENT_MESSAGE_ID), eq(SUBJECT), eq(BODY), eq(FORMAT), anyList(), eq(ANONYMOUS), eq(PRIORITY), eq(ALLOW_PINGBACKS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        MBMessage result = resolvers.createMBMessageDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createMBMessageDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addMessage(anyLong(), anyString(), anyLong(), anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), anyDouble(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        MBMessage result = resolvers.createMBMessageDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateMBMessageDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("messageId", MESSAGE_ID);
        arguments.put("userId", USER_ID);
        arguments.put("subject", SUBJECT);
        arguments.put("body", BODY);
        arguments.put("priority", PRIORITY);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBMessage expectedResult = mock(MBMessage.class);
        expectedResult.setMessageId(MESSAGE_ID);
        expectedResult.setUserId(USER_ID);
        expectedResult.setSubject(SUBJECT);
        expectedResult.setBody(BODY);
        expectedResult.setPriority(PRIORITY);
        expectedResult.setAllowPingbacks(ALLOW_PINGBACKS);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateMessage(eq(USER_ID), eq(MESSAGE_ID), eq(SUBJECT), eq(BODY), anyList(), anyList(), eq(PRIORITY), eq(ALLOW_PINGBACKS), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        MBMessage result = resolvers.updateMBMessageDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchMessageException.class)
    public void updateMBMessageDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("subject", SUBJECT);
        arguments.put("body", BODY);
        arguments.put("priority", PRIORITY);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("messageId")))
                .thenReturn(0L);
        when(localService.updateMessage(eq(USER_ID), eq(0L), eq(SUBJECT), eq(BODY), anyList(), anyList(), eq(PRIORITY), eq(ALLOW_PINGBACKS), any(ServiceContext.class)))
                .thenThrow(NoSuchMessageException.class);

        // Asserts
        MBMessage result = resolvers.updateMBMessageDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchMessageException.class)
    public void updateMBMessageDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("messageId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("subject", SUBJECT);
        arguments.put("body", BODY);
        arguments.put("priority", PRIORITY);
        arguments.put("allowPingbacks", ALLOW_PINGBACKS);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("messageId")))
                .thenReturn(789456L);
        when(localService.updateMessage(eq(USER_ID), eq(789456L), eq(SUBJECT), eq(BODY), anyList(), anyList(), eq(PRIORITY), eq(ALLOW_PINGBACKS), any(ServiceContext.class)))
                .thenThrow(NoSuchMessageException.class);

        // Asserts
        MBMessage result = resolvers.updateMBMessageDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateMBMessageDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("messageId")))
                .thenReturn(0L);
        when(localService.updateMessage(anyLong(), anyLong(), anyString(), anyString(), anyList(), anyList(), anyDouble(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        MBMessage result = resolvers.updateMBMessageDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteMBMessageDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("messageId", MESSAGE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBMessage expectedResult = mock(MBMessage.class);
        expectedResult.setMessageId(MESSAGE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteMBMessage(eq(MESSAGE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        MBMessage result = resolvers.deleteMBMessageDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchMessageException.class)
    public void deleteMBMessageDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        MBMessage expectedResult = mock(MBMessage.class);
        expectedResult.setMessageId(MESSAGE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteMBMessage(eq(MESSAGE_ID)))
                .thenThrow(NoSuchMessageException.class);

        // Asserts
        MBMessage result = resolvers.deleteMBMessageDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchMessageException.class)
    public void deleteMBMessageDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("messageId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        MBMessage expectedResult = mock(MBMessage.class);
        expectedResult.setMessageId(MESSAGE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("messageId")))
                .thenReturn(789456L);
        when(localService.deleteMBMessage(eq(789456L)))
                .thenThrow(NoSuchMessageException.class);

        // Asserts
        MBMessage result = resolvers.deleteMBMessageDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
