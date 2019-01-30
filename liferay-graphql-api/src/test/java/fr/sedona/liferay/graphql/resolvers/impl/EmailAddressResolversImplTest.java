package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchEmailAddressException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.EmailAddress;
import com.liferay.portal.kernel.service.EmailAddressLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.EmailAddressBatchLoader;
import fr.sedona.liferay.graphql.resolvers.EmailAddressResolvers;
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
 * Test suite for {@link EmailAddressResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class EmailAddressResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long EMAIL_ADDRESS_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final long CLASS_PK = 456L;
    private static final String ADDRESS = "contact@sedona.fr";
    private static final long TYPE_ID = 1L;
    private static final boolean PRIMARY = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, EmailAddress> dataLoader;

    @InjectMocks
    EmailAddressResolvers resolvers = new EmailAddressResolversImpl();

    @Mock
    private EmailAddressLocalService localService;

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
                .getDataLoader(EmailAddressBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((EmailAddressResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("emailAddressId")))
                    .thenReturn(EMAIL_ADDRESS_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("address")))
                    .thenReturn(ADDRESS);
            when(graphQLUtil.getLongArg(eq(environment), eq("typeId")))
                    .thenReturn(TYPE_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("primary")))
                    .thenReturn(PRIMARY);
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
    public void getEmailAddressesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<EmailAddress> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    EmailAddress entity = mock(EmailAddress.class);
                    entity.setEmailAddressId(value);
                    availableObjects.add(entity);
                });
        List<EmailAddress> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getEmailAddresses(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<EmailAddress> results = resolvers.getEmailAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getEmailAddressesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<EmailAddress> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    EmailAddress entity = mock(EmailAddress.class);
                    entity.setEmailAddressId(value);
                    availableObjects.add(entity);
                });
        List<EmailAddress> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getEmailAddresses(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<EmailAddress> results = resolvers.getEmailAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getEmailAddressesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<EmailAddress> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    EmailAddress entity = mock(EmailAddress.class);
                    entity.setEmailAddressId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getEmailAddresses(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<EmailAddress> results = resolvers.getEmailAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getEmailAddressesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<EmailAddress> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getEmailAddresses(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<EmailAddress> results = resolvers.getEmailAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getEmailAddressDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        EmailAddress expectedResult = mock(EmailAddress.class);
        expectedResult.setEmailAddressId(EMAIL_ADDRESS_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("emailAddressId"))
                .thenReturn(EMAIL_ADDRESS_ID);
        when(dataLoader.load(EMAIL_ADDRESS_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<EmailAddress> asyncResult = resolvers.getEmailAddressDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        EmailAddress result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getEmailAddressDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("emailAddressId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<EmailAddress> asyncResult = resolvers.getEmailAddressDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getEmailAddressDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("emailAddressId"))
                .thenReturn(EMAIL_ADDRESS_ID);
        when(dataLoader.load(EMAIL_ADDRESS_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<EmailAddress> asyncResult = resolvers.getEmailAddressDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        EmailAddress result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createEmailAddressDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("address", ADDRESS);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        EmailAddress expectedResult = mock(EmailAddress.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setAddress(ADDRESS);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addEmailAddress(eq(USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(ADDRESS), eq(TYPE_ID), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        EmailAddress result = resolvers.createEmailAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createEmailAddressDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("arg0", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("address", ADDRESS);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        EmailAddress expectedResult = mock(EmailAddress.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setAddress(ADDRESS);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addEmailAddress(eq(DEFAULT_USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(ADDRESS), eq(TYPE_ID), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        EmailAddress result = resolvers.createEmailAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createEmailAddressDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addEmailAddress(anyLong(), anyString(), anyLong(), anyString(), anyLong(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        EmailAddress result = resolvers.createEmailAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateEmailAddressDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("emailAddressId", EMAIL_ADDRESS_ID);
        arguments.put("address", ADDRESS);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        EmailAddress expectedResult = mock(EmailAddress.class);
        expectedResult.setEmailAddressId(EMAIL_ADDRESS_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setAddress(ADDRESS);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateEmailAddress(eq(EMAIL_ADDRESS_ID), eq(ADDRESS), eq(TYPE_ID), eq(PRIMARY)))
                .thenReturn(expectedResult);

        // Asserts
        EmailAddress result = resolvers.updateEmailAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchEmailAddressException.class)
    public void updateEmailAddressDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("address", ADDRESS);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("emailAddressId")))
                .thenReturn(0L);
        when(localService.updateEmailAddress(eq(0L), eq(ADDRESS), eq(TYPE_ID), eq(PRIMARY)))
                .thenThrow(NoSuchEmailAddressException.class);

        // Asserts
        EmailAddress result = resolvers.updateEmailAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchEmailAddressException.class)
    public void updateEmailAddressDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("emailAddressId", 789456L);
        arguments.put("address", ADDRESS);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("emailAddressId")))
                .thenReturn(789456L);
        when(localService.updateEmailAddress(eq(789456L), eq(ADDRESS), eq(TYPE_ID), eq(PRIMARY)))
                .thenThrow(NoSuchEmailAddressException.class);

        // Asserts
        EmailAddress result = resolvers.updateEmailAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateEmailAddressDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("emailAddressId")))
                .thenReturn(0L);
        when(localService.updateEmailAddress(eq(0L), eq(ADDRESS), eq(TYPE_ID), eq(PRIMARY)))
                .thenThrow(PortalException.class);

        // Asserts
        EmailAddress result = resolvers.updateEmailAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteEmailAddressDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("emailAddressId", EMAIL_ADDRESS_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        EmailAddress expectedResult = mock(EmailAddress.class);
        expectedResult.setEmailAddressId(EMAIL_ADDRESS_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteEmailAddress(eq(EMAIL_ADDRESS_ID)))
                .thenReturn(expectedResult);

        // Asserts
        EmailAddress result = resolvers.deleteEmailAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchEmailAddressException.class)
    public void deleteEmailAddressDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        EmailAddress expectedResult = mock(EmailAddress.class);
        expectedResult.setEmailAddressId(EMAIL_ADDRESS_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteEmailAddress(eq(EMAIL_ADDRESS_ID)))
                .thenThrow(NoSuchEmailAddressException.class);

        // Asserts
        EmailAddress result = resolvers.deleteEmailAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchEmailAddressException.class)
    public void deleteEmailAddressDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("emailAddressId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        EmailAddress expectedResult = mock(EmailAddress.class);
        expectedResult.setEmailAddressId(EMAIL_ADDRESS_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("emailAddressId")))
                .thenReturn(789456L);
        when(localService.deleteEmailAddress(eq(789456L)))
                .thenThrow(NoSuchEmailAddressException.class);

        // Asserts
        EmailAddress result = resolvers.deleteEmailAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
