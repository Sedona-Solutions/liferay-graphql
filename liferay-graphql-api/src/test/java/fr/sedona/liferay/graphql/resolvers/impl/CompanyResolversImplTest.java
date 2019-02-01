package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchCompanyException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import fr.sedona.liferay.graphql.loaders.CompanyBatchLoader;
import fr.sedona.liferay.graphql.resolvers.CompanyResolvers;
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
 * Test suite for {@link CompanyResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class CompanyResolversImplTest {
    private static final long COMPANY_ID = 987L;
    private static final String WEB_ID = null;
    private static final String VIRTUAL_HOSTNAME = null;
    private static final String MX = null;
    private static final boolean SYSTEM = false;
    private static final int MAX_USERS = 0;
    private static final boolean ACTIVE = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Company> dataLoader;

    @InjectMocks
    CompanyResolvers resolvers = new CompanyResolversImpl();

    @Mock
    private CompanyLocalService localService;

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
                .getDataLoader(CompanyBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((CompanyResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, boolean isValid) {
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("webId")))
                    .thenReturn(WEB_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("virtualHostname")))
                    .thenReturn(VIRTUAL_HOSTNAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("mx")))
                    .thenReturn(MX);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("system")))
                    .thenReturn(SYSTEM);
            when(graphQLUtil.getIntArg(eq(environment), eq("maxUsers")))
                    .thenReturn(MAX_USERS);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("active"), anyBoolean()))
                    .thenReturn(ACTIVE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString(), anyBoolean()))
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
    public void getCompaniesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Company> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Company entity = mock(Company.class);
                    entity.setCompanyId(value);
                    availableObjects.add(entity);
                });
        List<Company> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getCompanies(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Company> results = resolvers.getCompaniesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getCompaniesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Company> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Company entity = mock(Company.class);
                    entity.setCompanyId(value);
                    availableObjects.add(entity);
                });
        List<Company> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getCompanies(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Company> results = resolvers.getCompaniesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getCompaniesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Company> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Company entity = mock(Company.class);
                    entity.setCompanyId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getCompanies(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Company> results = resolvers.getCompaniesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getCompaniesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Company> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getCompanies(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Company> results = resolvers.getCompaniesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getCompanyDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Company expectedResult = mock(Company.class);
        expectedResult.setCompanyId(COMPANY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("companyId"))
                .thenReturn(COMPANY_ID);
        when(dataLoader.load(COMPANY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Company> asyncResult = resolvers.getCompanyDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Company result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getCompanyDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("companyId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Company> asyncResult = resolvers.getCompanyDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getCompanyDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("companyId"))
                .thenReturn(COMPANY_ID);
        when(dataLoader.load(COMPANY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Company> asyncResult = resolvers.getCompanyDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Company result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createCompanyDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("webId", WEB_ID);
        arguments.put("virtualHostname", VIRTUAL_HOSTNAME);
        arguments.put("mx", MX);
        arguments.put("system", SYSTEM);
        arguments.put("maxUsers", MAX_USERS);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Company expectedResult = mock(Company.class);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setWebId(WEB_ID);
        expectedResult.setVirtualHostname(VIRTUAL_HOSTNAME);
        expectedResult.setMx(MX);
        expectedResult.setSystem(SYSTEM);
        expectedResult.setMaxUsers(MAX_USERS);
        expectedResult.setActive(ACTIVE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.addCompany(eq(WEB_ID), eq(VIRTUAL_HOSTNAME), eq(MX), eq(SYSTEM), eq(MAX_USERS), eq(ACTIVE)))
                .thenReturn(expectedResult);

        // Asserts
        Company result = resolvers.createCompanyDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createCompanyDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(localService.addCompany(anyString(), anyString(), anyString(), anyBoolean(), anyInt(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        Company result = resolvers.createCompanyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateCompanyDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("virtualHostname", VIRTUAL_HOSTNAME);
        arguments.put("mx", MX);
        arguments.put("maxUsers", MAX_USERS);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Company expectedResult = mock(Company.class);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setWebId(WEB_ID);
        expectedResult.setVirtualHostname(VIRTUAL_HOSTNAME);
        expectedResult.setMx(MX);
        expectedResult.setSystem(SYSTEM);
        expectedResult.setMaxUsers(MAX_USERS);
        expectedResult.setActive(ACTIVE);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.updateCompany(eq(COMPANY_ID), eq(VIRTUAL_HOSTNAME), eq(MX), eq(MAX_USERS), eq(ACTIVE)))
                .thenReturn(expectedResult);

        // Asserts
        Company result = resolvers.updateCompanyDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchCompanyException.class)
    public void updateCompanyDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("virtualHostname", VIRTUAL_HOSTNAME);
        arguments.put("mx", MX);
        arguments.put("maxUsers", MAX_USERS);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                .thenReturn(0L);
        when(localService.updateCompany(eq(0L), eq(VIRTUAL_HOSTNAME), eq(MX), eq(MAX_USERS), eq(ACTIVE)))
                .thenThrow(NoSuchCompanyException.class);

        // Asserts
        Company result = resolvers.updateCompanyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchCompanyException.class)
    public void updateCompanyDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", 789456L);
        arguments.put("virtualHostname", VIRTUAL_HOSTNAME);
        arguments.put("mx", MX);
        arguments.put("maxUsers", MAX_USERS);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                .thenReturn(789456L);
        when(localService.updateCompany(eq(789456L), eq(VIRTUAL_HOSTNAME), eq(MX), eq(MAX_USERS), eq(ACTIVE)))
                .thenThrow(NoSuchCompanyException.class);

        // Asserts
        Company result = resolvers.updateCompanyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateCompanyDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                .thenReturn(COMPANY_ID);
        when(localService.updateCompany(anyLong(), anyString(), anyString(), anyInt(), anyBoolean()))
                .thenThrow(PortalException.class);

        // Asserts
        Company result = resolvers.updateCompanyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteCompanyDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Company expectedResult = mock(Company.class);
        expectedResult.setCompanyId(COMPANY_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteCompany(eq(COMPANY_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Company result = resolvers.deleteCompanyDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchCompanyException.class)
    public void deleteCompanyDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Company expectedResult = mock(Company.class);
        expectedResult.setCompanyId(COMPANY_ID);

        // When / Then
        useMockGraphQLUtil(environment, true);
        when(localService.deleteCompany(eq(COMPANY_ID)))
                .thenThrow(NoSuchCompanyException.class);

        // Asserts
        Company result = resolvers.deleteCompanyDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchCompanyException.class)
    public void deleteCompanyDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Company expectedResult = mock(Company.class);
        expectedResult.setCompanyId(COMPANY_ID);

        // When / Then
        useMockGraphQLUtil(environment, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                .thenReturn(789456L);
        when(localService.deleteCompany(eq(789456L)))
                .thenThrow(NoSuchCompanyException.class);

        // Asserts
        Company result = resolvers.deleteCompanyDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
