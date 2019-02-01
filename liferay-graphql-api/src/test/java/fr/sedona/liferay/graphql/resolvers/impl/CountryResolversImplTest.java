package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchCountryException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Country;
import com.liferay.portal.kernel.service.CountryService;
import fr.sedona.liferay.graphql.resolvers.CountryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionContextBuilder;
import graphql.execution.ExecutionId;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link CountryResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class CountryResolversImplTest {
    private static final long COUNTRY_ID = 987L;
    private static final String NAME = "New France";
    private static final String A2 = "FR";
    private static final String A3 = "FRA";
    private static final String NUMBER = "";
    private static final String IDD = "";
    private static final boolean ACTIVE = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private Random random;

    @InjectMocks
    CountryResolvers resolvers = new CountryResolversImpl();

    @Mock
    private CountryService localService;

    @Mock
    private GraphQLUtil graphQLUtil;

    @Before
    public void setUp() {
        executionId = ExecutionId.from("execution-1");
        executionContext = ExecutionContextBuilder.newExecutionContextBuilder()
                .executionId(executionId)
                .build();

        mockEnvironment = mock(DataFetchingEnvironment.class);

        random = new Random();
    }

    private void useSimpleGraphQLUtil() {
        ((CountryResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("countryId")))
                    .thenReturn(COUNTRY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("a2")))
                    .thenReturn(A2);
            when(graphQLUtil.getStringArg(eq(environment), eq("a3")))
                    .thenReturn(A3);
            when(graphQLUtil.getStringArg(eq(environment), eq("number")))
                    .thenReturn(NUMBER);
            when(graphQLUtil.getStringArg(eq(environment), eq("idd")))
                    .thenReturn(IDD);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("active")))
                    .thenReturn(ACTIVE);
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
    public void getCountriesDataFetcher_should_return_the_active_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Country> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Country entity = mock(Country.class);
                    entity.setCountryId(value);
                    entity.setActive(random.nextBoolean());
                    availableObjects.add(entity);
                });
        List<Country> expectedResults = availableObjects.stream()
                .filter(Country::getActive)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getCountries(ACTIVE))
                .thenReturn(expectedResults);

        // Asserts
        List<Country> results = resolvers.getCountriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getCountriesDataFetcher_without_args_should_return_the_active_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Country> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Country entity = mock(Country.class);
                    entity.setCountryId(value);
                    entity.setActive(random.nextBoolean());
                    availableObjects.add(entity);
                });
        List<Country> expectedResults = availableObjects.stream()
                .filter(Country::getActive)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getCountries(ACTIVE))
                .thenReturn(expectedResults);

        // Asserts
        List<Country> results = resolvers.getCountriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getCountriesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Country> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getCountries(ACTIVE))
                .thenReturn(expectedResults);

        // Asserts
        List<Country> results = resolvers.getCountriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getCountryDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Country expectedResult = mock(Country.class);
        expectedResult.setCountryId(COUNTRY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("countryId"))
                .thenReturn(COUNTRY_ID);
        when(localService.getCountry(COUNTRY_ID))
                .thenReturn(expectedResult);

        // Asserts
        Country result = resolvers.getCountryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getCountryDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("countryId"))
                .thenReturn(0L);

        // Asserts
        Country result = resolvers.getCountryDataFetcher()
                .get(mockEnvironment);
        assertNull(result);
    }

    @Test(expected = NoSuchCountryException.class)
    public void getCountryDataFetcher_with_unknown_id_should_return_null_with_exception() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("countryId"))
                .thenReturn(COUNTRY_ID);
        when(localService.getCountry(COUNTRY_ID))
                .thenThrow(NoSuchCountryException.class);

        // Asserts
        Country result = resolvers.getCountryDataFetcher()
                .get(mockEnvironment);
        assertNull(result);
    }

    @Test
    public void createCountryDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", NAME);
        arguments.put("a2", A2);
        arguments.put("a3", A3);
        arguments.put("number", NUMBER);
        arguments.put("idd", IDD);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Country expectedResult = mock(Country.class);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setName(NAME);
        expectedResult.setA2(A2);
        expectedResult.setA3(A3);
        expectedResult.setNumber(NUMBER);
        expectedResult.setIdd(IDD);
        expectedResult.setActive(ACTIVE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addCountry(eq(NAME), eq(A2), eq(A3), eq(NUMBER), eq(IDD), eq(ACTIVE)))
                .thenReturn(expectedResult);

        // Asserts
        Country result = resolvers.createCountryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createCountryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addCountry(anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        Country result = resolvers.createCountryDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
