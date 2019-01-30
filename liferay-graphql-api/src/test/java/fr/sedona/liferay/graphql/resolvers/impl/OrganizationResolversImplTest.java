package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchOrganizationException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.OrganizationBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OrganizationResolvers;
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
 * Test suite for {@link OrganizationResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class OrganizationResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long ORGANIZATION_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long COMPANY_ID = 0;
    private static final long PARENT_ORGANIZATION_ID = 0;
    private static final String NAME = null;
    private static final String TYPE = null;
    private static final long REGION_ID = 0;
    private static final long COUNTRY_ID = 0;
    private static final long STATUS_ID = 0;
    private static final String COMMENTS = null;
    private static final boolean SITE = false;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Organization> dataLoader;

    @InjectMocks
    OrganizationResolvers resolvers = new OrganizationResolversImpl();

    @Mock
    private OrganizationLocalService localService;

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
                .getDataLoader(OrganizationBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((OrganizationResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("organizationId")))
                    .thenReturn(ORGANIZATION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("organizationId")))
                    .thenReturn(ORGANIZATION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentOrganizationId")))
                    .thenReturn(PARENT_ORGANIZATION_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("type")))
                    .thenReturn(TYPE);
            when(graphQLUtil.getLongArg(eq(environment), eq("regionId")))
                    .thenReturn(REGION_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("countryId")))
                    .thenReturn(COUNTRY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("statusId")))
                    .thenReturn(STATUS_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("comments")))
                    .thenReturn(COMMENTS);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("site")))
                    .thenReturn(SITE);
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
    public void getOrganizationsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Organization> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Organization entity = mock(Organization.class);
                    entity.setOrganizationId(value);
                    availableObjects.add(entity);
                });
        List<Organization> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOrganizations(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Organization> results = resolvers.getOrganizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOrganizationsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Organization> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Organization entity = mock(Organization.class);
                    entity.setOrganizationId(value);
                    availableObjects.add(entity);
                });
        List<Organization> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOrganizations(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Organization> results = resolvers.getOrganizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOrganizationsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Organization> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Organization entity = mock(Organization.class);
                    entity.setOrganizationId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOrganizations(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Organization> results = resolvers.getOrganizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOrganizationsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Organization> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getOrganizations(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Organization> results = resolvers.getOrganizationsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getOrganizationDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Organization expectedResult = mock(Organization.class);
        expectedResult.setOrganizationId(ORGANIZATION_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("organizationId"))
                .thenReturn(ORGANIZATION_ID);
        when(dataLoader.load(ORGANIZATION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Organization> asyncResult = resolvers.getOrganizationDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Organization result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getOrganizationDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("organizationId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Organization> asyncResult = resolvers.getOrganizationDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getOrganizationDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("organizationId"))
                .thenReturn(ORGANIZATION_ID);
        when(dataLoader.load(ORGANIZATION_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Organization> asyncResult = resolvers.getOrganizationDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Organization result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createOrganizationDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("parentOrganizationId", PARENT_ORGANIZATION_ID);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("statusId", STATUS_ID);
        arguments.put("comments", COMMENTS);
        arguments.put("site", SITE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Organization expectedResult = mock(Organization.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setOrganizationId(ORGANIZATION_ID);
        expectedResult.setParentOrganizationId(PARENT_ORGANIZATION_ID);
        expectedResult.setName(NAME);
        expectedResult.setType(TYPE);
        expectedResult.setRegionId(REGION_ID);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setStatusId(STATUS_ID);
        expectedResult.setComments(COMMENTS);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addOrganization(eq(USER_ID), eq(PARENT_ORGANIZATION_ID), eq(NAME), eq(TYPE), eq(REGION_ID), eq(COUNTRY_ID), eq(STATUS_ID), eq(COMMENTS), eq(SITE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Organization result = resolvers.createOrganizationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createOrganizationDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("parentOrganizationId", PARENT_ORGANIZATION_ID);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("statusId", STATUS_ID);
        arguments.put("comments", COMMENTS);
        arguments.put("site", SITE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Organization expectedResult = mock(Organization.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setOrganizationId(ORGANIZATION_ID);
        expectedResult.setParentOrganizationId(PARENT_ORGANIZATION_ID);
        expectedResult.setName(NAME);
        expectedResult.setType(TYPE);
        expectedResult.setRegionId(REGION_ID);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setStatusId(STATUS_ID);
        expectedResult.setComments(COMMENTS);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addOrganization(eq(DEFAULT_USER_ID), eq(PARENT_ORGANIZATION_ID), eq(NAME), eq(TYPE), eq(REGION_ID), eq(COUNTRY_ID), eq(STATUS_ID), eq(COMMENTS), eq(SITE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Organization result = resolvers.createOrganizationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createOrganizationDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addOrganization(anyLong(), anyLong(), anyString(), anyString(), anyLong(), anyLong(), anyLong(), anyString(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Organization result = resolvers.createOrganizationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateOrganizationDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("organizationId", ORGANIZATION_ID);
        arguments.put("parentOrganizationId", PARENT_ORGANIZATION_ID);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("statusId", STATUS_ID);
        arguments.put("comments", COMMENTS);
        arguments.put("site", SITE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Organization expectedResult = mock(Organization.class);
        expectedResult.setOrganizationId(ORGANIZATION_ID);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setOrganizationId(ORGANIZATION_ID);
        expectedResult.setParentOrganizationId(PARENT_ORGANIZATION_ID);
        expectedResult.setName(NAME);
        expectedResult.setType(TYPE);
        expectedResult.setRegionId(REGION_ID);
        expectedResult.setCountryId(COUNTRY_ID);
        expectedResult.setStatusId(STATUS_ID);
        expectedResult.setComments(COMMENTS);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateOrganization(eq(COMPANY_ID), eq(ORGANIZATION_ID), eq(PARENT_ORGANIZATION_ID), eq(NAME), eq(TYPE), eq(REGION_ID), eq(COUNTRY_ID), eq(STATUS_ID), eq(COMMENTS), anyBoolean(), any(), eq(SITE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Organization result = resolvers.updateOrganizationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchOrganizationException.class)
    public void updateOrganizationDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("organizationId", ORGANIZATION_ID);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("parentOrganizationId", PARENT_ORGANIZATION_ID);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("statusId", STATUS_ID);
        arguments.put("comments", COMMENTS);
        arguments.put("site", SITE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("organizationId")))
                .thenReturn(0L);
        when(localService.updateOrganization(eq(COMPANY_ID), eq(0L), eq(PARENT_ORGANIZATION_ID), eq(NAME), eq(TYPE), eq(REGION_ID), eq(COUNTRY_ID), eq(STATUS_ID), eq(COMMENTS), anyBoolean(), any(), eq(SITE), any(ServiceContext.class)))
                .thenThrow(NoSuchOrganizationException.class);

        // Asserts
        Organization result = resolvers.updateOrganizationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchOrganizationException.class)
    public void updateOrganizationDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("organizationId", 789456L);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("parentOrganizationId", PARENT_ORGANIZATION_ID);
        arguments.put("name", NAME);
        arguments.put("type", TYPE);
        arguments.put("regionId", REGION_ID);
        arguments.put("countryId", COUNTRY_ID);
        arguments.put("statusId", STATUS_ID);
        arguments.put("comments", COMMENTS);
        arguments.put("site", SITE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("organizationId")))
                .thenReturn(789456L);
        when(localService.updateOrganization(eq(COMPANY_ID), eq(789456L), eq(PARENT_ORGANIZATION_ID), eq(NAME), eq(TYPE), eq(REGION_ID), eq(COUNTRY_ID), eq(STATUS_ID), eq(COMMENTS), anyBoolean(), any(), eq(SITE), any(ServiceContext.class)))
                .thenThrow(NoSuchOrganizationException.class);

        // Asserts
        Organization result = resolvers.updateOrganizationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateOrganizationDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("organizationId")))
                .thenReturn(0L);
        when(localService.updateOrganization(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyLong(), anyLong(), anyLong(), anyString(), anyBoolean(), any(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Organization result = resolvers.updateOrganizationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteOrganizationDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("organizationId", ORGANIZATION_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Organization expectedResult = mock(Organization.class);
        expectedResult.setOrganizationId(ORGANIZATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOrganization(eq(ORGANIZATION_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Organization result = resolvers.deleteOrganizationDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchOrganizationException.class)
    public void deleteOrganizationDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Organization expectedResult = mock(Organization.class);
        expectedResult.setOrganizationId(ORGANIZATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteOrganization(eq(ORGANIZATION_ID)))
                .thenThrow(NoSuchOrganizationException.class);

        // Asserts
        Organization result = resolvers.deleteOrganizationDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchOrganizationException.class)
    public void deleteOrganizationDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("organizationId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Organization expectedResult = mock(Organization.class);
        expectedResult.setOrganizationId(ORGANIZATION_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("organizationId")))
                .thenReturn(789456L);
        when(localService.deleteOrganization(eq(789456L)))
                .thenThrow(NoSuchOrganizationException.class);

        // Asserts
        Organization result = resolvers.deleteOrganizationDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
