package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchAddressException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.service.AddressLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.AddressBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AddressResolvers;
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
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link AddressResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class AddressResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long ADDRESS_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String CLASS_NAME = "fr.sedona.grapql.Test";
    private static final long CLASS_PK = 1L;
    private static final String STREET1 = "20 rue Le Peletier";
    private static final String STREET2 = "";
    private static final String STREET3 = "";
    private static final String CITY = "Paris";
    private static final String ZIP = "75009";
    private static final long REGION_ID = 1L;
    private static final long COUNTRY_ID = 1L;
    private static final long TYPE_ID = 1L;
    private static final boolean MAILING = false;
    private static final boolean PRIMARY = false;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Address> dataLoader;

    @InjectMocks
    AddressResolvers resolvers = new AddressResolversImpl();

    @Mock
    private AddressLocalService localService;

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
                .getDataLoader(AddressBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((AddressResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("addressId")))
                    .thenReturn(ADDRESS_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("street1")))
                    .thenReturn(STREET1);
            when(graphQLUtil.getStringArg(eq(environment), eq("street2")))
                    .thenReturn(STREET2);
            when(graphQLUtil.getStringArg(eq(environment), eq("street3")))
                    .thenReturn(STREET3);
            when(graphQLUtil.getStringArg(eq(environment), eq("city")))
                    .thenReturn(CITY);
            when(graphQLUtil.getStringArg(eq(environment), eq("zip")))
                    .thenReturn(ZIP);
            when(graphQLUtil.getLongArg(eq(environment), eq("regionId")))
                    .thenReturn(REGION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("countryId")))
                    .thenReturn(COUNTRY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("typeId")))
                    .thenReturn(TYPE_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("mailing")))
                    .thenReturn(MAILING);
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
    public void getAddressesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Address> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Address entity = mock(Address.class);
                    entity.setAddressId(value);
                    availableObjects.add(entity);
                });
        List<Address> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAddresses(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Address> results = resolvers.getAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAddressesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Address> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Address entity = mock(Address.class);
                    entity.setAddressId(value);
                    availableObjects.add(entity);
                });
        List<Address> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAddresses(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Address> results = resolvers.getAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAddressesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Address> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Address entity = mock(Address.class);
                    entity.setAddressId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAddresses(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Address> results = resolvers.getAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAddressesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Address> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getAddresses(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Address> results = resolvers.getAddressesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getAddressDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Address expectedResult = mock(Address.class);
        expectedResult.setAddressId(ADDRESS_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("addressId"))
                .thenReturn(ADDRESS_ID);
        when(dataLoader.load(ADDRESS_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Address> asyncResult = resolvers.getAddressDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Address result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getAddressDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("addressId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Address> asyncResult = resolvers.getAddressDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getAddressDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("addressId"))
                .thenReturn(ADDRESS_ID);
        when(dataLoader.load(ADDRESS_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Address> asyncResult = resolvers.getAddressDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Address result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createAddressDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("street1", STREET1);
        arguments.put("street2", STREET2);
        arguments.put("street3", STREET3);
        arguments.put("city", CITY);
        arguments.put("zip", ZIP);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("typeId", TYPE_ID);
        arguments.put("mailing", MAILING);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Address expectedResult = mock(Address.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setStreet1(STREET1);
        expectedResult.setStreet2(STREET2);
        expectedResult.setStreet3(STREET3);
        expectedResult.setCity(CITY);
        expectedResult.setZip(ZIP);
        expectedResult.setRegionId(REGION_ID);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setMailing(MAILING);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addAddress(eq(USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(STREET1), eq(STREET2), eq(STREET3), eq(CITY), eq(ZIP), eq(REGION_ID), eq(COUNTRY_ID), eq(TYPE_ID), eq(MAILING), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Address result = resolvers.createAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createAddressDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("street1", STREET1);
        arguments.put("street2", STREET2);
        arguments.put("street3", STREET3);
        arguments.put("city", CITY);
        arguments.put("zip", ZIP);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("typeId", TYPE_ID);
        arguments.put("mailing", MAILING);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Address expectedResult = mock(Address.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setStreet1(STREET1);
        expectedResult.setStreet2(STREET2);
        expectedResult.setStreet3(STREET3);
        expectedResult.setCity(CITY);
        expectedResult.setZip(ZIP);
        expectedResult.setRegionId(REGION_ID);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setMailing(MAILING);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addAddress(eq(DEFAULT_USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(STREET1), eq(STREET2), eq(STREET3), eq(CITY), eq(ZIP), eq(REGION_ID), eq(COUNTRY_ID), eq(TYPE_ID), eq(MAILING), eq(PRIMARY), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Address result = resolvers.createAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createAddressDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addAddress(eq(DEFAULT_USER_ID), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyLong(), anyBoolean(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Address result = resolvers.createAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateAddressDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("addressId", ADDRESS_ID);
        arguments.put("street1", STREET1);
        arguments.put("street2", STREET2);
        arguments.put("street3", STREET3);
        arguments.put("city", CITY);
        arguments.put("zip", ZIP);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("typeId", TYPE_ID);
        arguments.put("mailing", MAILING);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Address expectedResult = mock(Address.class);
        expectedResult.setAddressId(ADDRESS_ID);
        expectedResult.setStreet1(STREET1);
        expectedResult.setStreet2(STREET2);
        expectedResult.setStreet3(STREET3);
        expectedResult.setCity(CITY);
        expectedResult.setZip(ZIP);
        expectedResult.setRegionId(REGION_ID);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setTypeId(TYPE_ID);
        expectedResult.setMailing(MAILING);
        expectedResult.setPrimary(PRIMARY);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateAddress(eq(ADDRESS_ID), eq(STREET1), eq(STREET2), eq(STREET3), eq(CITY), eq(ZIP), eq(REGION_ID), eq(COUNTRY_ID), eq(TYPE_ID), eq(MAILING), eq(PRIMARY)))
                .thenReturn(expectedResult);

        // Asserts
        Address result = resolvers.updateAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchAddressException.class)
    public void updateAddressDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("street1", STREET1);
        arguments.put("street2", STREET2);
        arguments.put("street3", STREET3);
        arguments.put("city", CITY);
        arguments.put("zip", ZIP);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("typeId", TYPE_ID);
        arguments.put("mailing", MAILING);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("addressId")))
                .thenReturn(0L);
        when(localService.updateAddress(eq(0L), eq(STREET1), eq(STREET2), eq(STREET3), eq(CITY), eq(ZIP), eq(REGION_ID), eq(COUNTRY_ID), eq(TYPE_ID), eq(MAILING), eq(PRIMARY)))
                .thenThrow(NoSuchAddressException.class);

        // Asserts
        Address result = resolvers.updateAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchAddressException.class)
    public void updateAddressDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("addressId", 789456L);
        arguments.put("street1", STREET1);
        arguments.put("street2", STREET2);
        arguments.put("street3", STREET3);
        arguments.put("city", CITY);
        arguments.put("zip", ZIP);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("typeId", TYPE_ID);
        arguments.put("mailing", MAILING);
        arguments.put("primary", PRIMARY);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("addressId")))
                .thenReturn(789456L);
        when(localService.updateAddress(eq(789456L), eq(STREET1), eq(STREET2), eq(STREET3), eq(CITY), eq(ZIP), eq(REGION_ID), eq(COUNTRY_ID), eq(TYPE_ID), eq(MAILING), eq(PRIMARY)))
                .thenThrow(NoSuchAddressException.class);

        // Asserts
        Address result = resolvers.updateAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateAddressDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.updateAddress(anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyLong(), anyBoolean(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        Address result = resolvers.updateAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteAddressDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("addressId", ADDRESS_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Address expectedResult = mock(Address.class);
        expectedResult.setAddressId(ADDRESS_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAddress(eq(ADDRESS_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Address result = resolvers.deleteAddressDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchAddressException.class)
    public void deleteAddressDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Address expectedResult = mock(Address.class);
        expectedResult.setAddressId(ADDRESS_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteAddress(eq(ADDRESS_ID)))
                .thenThrow(NoSuchAddressException.class);

        // Asserts
        Address result = resolvers.deleteAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchAddressException.class)
    public void deleteAddressDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("addressId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Address expectedResult = mock(Address.class);
        expectedResult.setAddressId(ADDRESS_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("addressId")))
                .thenReturn(789456L);
        when(localService.deleteAddress(eq(789456L)))
                .thenThrow(NoSuchAddressException.class);

        // Asserts
        Address result = resolvers.deleteAddressDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
