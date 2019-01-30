package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchRegionException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.service.RegionService;
import fr.sedona.liferay.graphql.resolvers.RegionResolvers;
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
 * Test suite for {@link RegionResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class RegionResolversImplTest {
    private static final long REGION_ID = 987L;
    private static final long COUNTRY_ID = 123L;
    private static final String REGION_CODE = "IdF";
    private static final String NAME = "Ile de France";
    private static final boolean ACTIVE = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private Random random;

    @InjectMocks
    RegionResolvers resolvers = new RegionResolversImpl();

    @Mock
    private RegionService localService;

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
        ((RegionResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("regionId")))
                    .thenReturn(REGION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("countryId")))
                    .thenReturn(COUNTRY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("regionCode")))
                    .thenReturn(REGION_CODE);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
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
    public void getRegionsDataFetcher_should_return_the_active_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Region> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Region entity = mock(Region.class);
                    entity.setRegionId(value);
                    entity.setActive(random.nextBoolean());
                    availableObjects.add(entity);
                });
        List<Region> expectedResults = availableObjects.stream()
                .filter(Region::getActive)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRegions(ACTIVE))
                .thenReturn(expectedResults);

        // Asserts
        List<Region> results = resolvers.getRegionsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRegionsDataFetcher_without_args_should_return_the_active_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Region> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Region entity = mock(Region.class);
                    entity.setRegionId(value);
                    entity.setActive(random.nextBoolean());
                    availableObjects.add(entity);
                });
        List<Region> expectedResults = availableObjects.stream()
                .filter(Region::getActive)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRegions(ACTIVE))
                .thenReturn(expectedResults);

        // Asserts
        List<Region> results = resolvers.getRegionsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRegionDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("regionId", REGION_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Region expectedResult = mock(Region.class);
        expectedResult.setRegionId(REGION_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRegion(REGION_ID))
                .thenReturn(expectedResult);

        // Asserts
        Region result = resolvers.getRegionDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getRegionDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useSimpleGraphQLUtil();

        // Asserts
        Region result = resolvers.getRegionDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchRegionException.class)
    public void getRegionDataFetcher_with_unknown_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("regionId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRegion(789456L))
                .thenThrow(NoSuchRegionException.class);

        // Asserts
        Region result = resolvers.getRegionDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void createRegionDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("regionCode", REGION_CODE);
        arguments.put("name", NAME);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Region expectedResult = mock(Region.class);
        expectedResult.setRegionId(REGION_ID);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setRegionCode(REGION_CODE);
        expectedResult.setName(NAME);
        expectedResult.setActive(ACTIVE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addRegion(eq(COUNTRY_ID), eq(REGION_CODE), eq(NAME), eq(ACTIVE)))
                .thenReturn(expectedResult);

        // Asserts
        Region result = resolvers.createRegionDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createRegionDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addRegion(anyLong(), anyString(), anyString(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        Region result = resolvers.createRegionDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
