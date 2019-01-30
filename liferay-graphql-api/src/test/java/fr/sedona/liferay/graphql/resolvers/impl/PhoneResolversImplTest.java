package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchPhoneException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Phone;
import com.liferay.portal.kernel.service.PhoneLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.PhoneBatchLoader;
import fr.sedona.liferay.graphql.resolvers.PhoneResolvers;
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
 * Test suite for {@link PhoneResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class PhoneResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long PHONE_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final long CLASS_PK = 456L;
    private static final String NUMBER = "+33 1 02 03 04 05";
    private static final String EXTENSION = "123";
    private static final long TYPE_ID = 1L;
    private static final boolean PRIMARY = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Phone> dataLoader;

    @InjectMocks
    PhoneResolvers resolvers = new PhoneResolversImpl();

    @Mock
    private PhoneLocalService localService;

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
                .getDataLoader(PhoneBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((PhoneResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("phoneId")))
                    .thenReturn(PHONE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("number")))
                    .thenReturn(NUMBER);
            when(graphQLUtil.getStringArg(eq(environment), eq("extension")))
                    .thenReturn(EXTENSION);
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
    public void getPhonesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Phone> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Phone entity = mock(Phone.class);
                    entity.setPhoneId(value);
                    availableObjects.add(entity);
                });
        List<Phone> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getPhones(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Phone> results = resolvers.getPhonesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getPhonesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Phone> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Phone entity = mock(Phone.class);
                    entity.setPhoneId(value);
                    availableObjects.add(entity);
                });
        List<Phone> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getPhones(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Phone> results = resolvers.getPhonesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getPhonesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Phone> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Phone entity = mock(Phone.class);
                    entity.setPhoneId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getPhones(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Phone> results = resolvers.getPhonesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getPhonesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Phone> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getPhones(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Phone> results = resolvers.getPhonesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getPhoneDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Phone expectedResult = mock(Phone.class);
        expectedResult.setPhoneId(PHONE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("phoneId"))
                .thenReturn(PHONE_ID);
        when(dataLoader.load(PHONE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Phone> asyncResult = resolvers.getPhoneDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Phone result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getPhoneDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("phoneId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Phone> asyncResult = resolvers.getPhoneDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getPhoneDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("phoneId"))
                .thenReturn(PHONE_ID);
        when(dataLoader.load(PHONE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Phone> asyncResult = resolvers.getPhoneDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Phone result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createPhoneDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("number", NUMBER);
        arguments.put("extension", EXTENSION);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Phone expectedResult = mock(Phone.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setNumber(NUMBER);
        expectedResult.setExtension(EXTENSION);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addPhone(eq(USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(NUMBER), eq(EXTENSION), eq(TYPE_ID), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Phone result = resolvers.createPhoneDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createPhoneDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("number", NUMBER);
        arguments.put("extension", EXTENSION);
        arguments.put("typeId", TYPE_ID);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Phone expectedResult = mock(Phone.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setNumber(NUMBER);
        expectedResult.setExtension(EXTENSION);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addPhone(eq(DEFAULT_USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(NUMBER), eq(EXTENSION), eq(TYPE_ID), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Phone result = resolvers.createPhoneDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createPhoneDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addPhone(anyLong(), anyString(), anyLong(), anyString(), anyString(), anyLong(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Phone result = resolvers.createPhoneDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updatePhoneDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("phoneId", PHONE_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("number", NUMBER);
        arguments.put("extension", EXTENSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Phone expectedResult = mock(Phone.class);
        expectedResult.setPhoneId(PHONE_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setNumber(NUMBER);
        expectedResult.setExtension(EXTENSION);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updatePhone(eq(PHONE_ID), eq(NUMBER), eq(EXTENSION), eq(TYPE_ID), eq(PRIMARY)))
                .thenReturn(expectedResult);

        // Asserts
        Phone result = resolvers.updatePhoneDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchPhoneException.class)
    public void updatePhoneDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("number", NUMBER);
        arguments.put("extension", EXTENSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("phoneId")))
                .thenReturn(0L);
        when(localService.updatePhone(eq(PHONE_ID), eq(NUMBER), eq(EXTENSION), eq(TYPE_ID), eq(PRIMARY)))
                .thenThrow(NoSuchPhoneException.class);

        // Asserts
        Phone result = resolvers.updatePhoneDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchPhoneException.class)
    public void updatePhoneDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("phoneId", 789456L);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("number", NUMBER);
        arguments.put("extension", EXTENSION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("phoneId")))
                .thenReturn(789456L);
        when(localService.updatePhone(eq(789456L), eq(NUMBER), eq(EXTENSION), eq(TYPE_ID), eq(PRIMARY)))
                .thenThrow(NoSuchPhoneException.class);

        // Asserts
        Phone result = resolvers.updatePhoneDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updatePhoneDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("phoneId")))
                .thenReturn(0L);
        when(localService.updatePhone(anyLong(), anyString(), anyString(), anyLong(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        Phone result = resolvers.updatePhoneDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deletePhoneDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("phoneId", PHONE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Phone expectedResult = mock(Phone.class);
        expectedResult.setPhoneId(PHONE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deletePhone(eq(PHONE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Phone result = resolvers.deletePhoneDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchPhoneException.class)
    public void deletePhoneDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Phone expectedResult = mock(Phone.class);
        expectedResult.setPhoneId(PHONE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deletePhone(eq(PHONE_ID)))
                .thenThrow(NoSuchPhoneException.class);

        // Asserts
        Phone result = resolvers.deletePhoneDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchPhoneException.class)
    public void deletePhoneDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("phoneId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Phone expectedResult = mock(Phone.class);
        expectedResult.setPhoneId(PHONE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("phoneId")))
                .thenReturn(789456L);
        when(localService.deletePhone(eq(789456L)))
                .thenThrow(NoSuchPhoneException.class);

        // Asserts
        Phone result = resolvers.deletePhoneDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
