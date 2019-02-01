package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchRoleException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.RoleBatchLoader;
import fr.sedona.liferay.graphql.resolvers.RoleResolvers;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for {@link RoleResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class RoleResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long ROLE_ID = 987L;
    private static final long USER_ID = 123L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final long CLASS_PK = 456L;
    private static final String NAME = "Test title";
    private static final Map<Locale, String> TITLE_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final int TYPE = 0;
    private static final String SUBTYPE = "subtype";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Role> dataLoader;

    static {
        TITLE_MAP = new HashMap<>();
        TITLE_MAP.put(LocaleUtil.US, "Test title");
        TITLE_MAP.put(LocaleUtil.FRANCE, "Titre de test");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");
    }

    @InjectMocks
    RoleResolvers resolvers = new RoleResolversImpl();

    @Mock
    private RoleLocalService localService;

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
                .getDataLoader(RoleBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((RoleResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("roleId")))
                    .thenReturn(ROLE_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("titleMap")))
                    .thenReturn(TITLE_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getIntArg(eq(environment), eq("type")))
                    .thenReturn(TYPE);
            when(graphQLUtil.getStringArg(eq(environment), eq("subtype")))
                    .thenReturn(SUBTYPE);
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
    public void getRolesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Role> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Role entity = mock(Role.class);
                    entity.setRoleId(value);
                    availableObjects.add(entity);
                });
        List<Role> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRoles(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Role> results = resolvers.getRolesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRolesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Role> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Role entity = mock(Role.class);
                    entity.setRoleId(value);
                    availableObjects.add(entity);
                });
        List<Role> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRoles(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Role> results = resolvers.getRolesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRolesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Role> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Role entity = mock(Role.class);
                    entity.setRoleId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRoles(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Role> results = resolvers.getRolesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRolesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Role> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getRoles(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Role> results = resolvers.getRolesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getRoleDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Role expectedResult = mock(Role.class);
        expectedResult.setRoleId(ROLE_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("roleId"))
                .thenReturn(ROLE_ID);
        when(dataLoader.load(ROLE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Role> asyncResult = resolvers.getRoleDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Role result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getRoleDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("roleId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Role> asyncResult = resolvers.getRoleDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getRoleDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("roleId"))
                .thenReturn(ROLE_ID);
        when(dataLoader.load(ROLE_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Role> asyncResult = resolvers.getRoleDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Role result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createRoleDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("name", NAME);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("subtype", SUBTYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Role expectedResult = mock(Role.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setName(NAME);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setSubtype(SUBTYPE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addRole(eq(USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(NAME), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(SUBTYPE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Role result = resolvers.createRoleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createRoleDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("name", NAME);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("subtype", SUBTYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Role expectedResult = mock(Role.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setName(NAME);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setSubtype(SUBTYPE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addRole(eq(DEFAULT_USER_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(NAME), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(SUBTYPE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Role result = resolvers.createRoleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createRoleDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addRole(anyLong(), anyString(), anyLong(), anyString(), anyMap(), anyMap(), anyInt(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Role result = resolvers.createRoleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateRoleDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("roleId", ROLE_ID);
        arguments.put("name", NAME);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("subtype", SUBTYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Role expectedResult = mock(Role.class);
        expectedResult.setRoleId(ROLE_ID);
        expectedResult.setName(NAME);
        expectedResult.setTitleMap(TITLE_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setSubtype(SUBTYPE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateRole(eq(ROLE_ID), eq(NAME), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SUBTYPE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Role result = resolvers.updateRoleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchRoleException.class)
    public void updateRoleDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", NAME);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("subtype", SUBTYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("roleId")))
                .thenReturn(0L);
        when(localService.updateRole(eq(0L), eq(NAME), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SUBTYPE), any(ServiceContext.class)))
                .thenThrow(NoSuchRoleException.class);

        // Asserts
        Role result = resolvers.updateRoleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchRoleException.class)
    public void updateRoleDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("roleId", 789456L);
        arguments.put("name", NAME);
        arguments.put("titleMap", TITLE_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("subtype", SUBTYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("roleId")))
                .thenReturn(789456L);
        when(localService.updateRole(eq(789456L), eq(NAME), eq(TITLE_MAP), eq(DESCRIPTION_MAP), eq(SUBTYPE), any(ServiceContext.class)))
                .thenThrow(NoSuchRoleException.class);

        // Asserts
        Role result = resolvers.updateRoleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateRoleDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("roleId")))
                .thenReturn(ROLE_ID);
        when(localService.updateRole(anyLong(), anyString(), anyMap(), anyMap(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Role result = resolvers.updateRoleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteRoleDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("roleId", ROLE_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Role expectedResult = mock(Role.class);
        expectedResult.setRoleId(ROLE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteRole(eq(ROLE_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Role result = resolvers.deleteRoleDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchRoleException.class)
    public void deleteRoleDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Role expectedResult = mock(Role.class);
        expectedResult.setRoleId(ROLE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteRole(eq(ROLE_ID)))
                .thenThrow(NoSuchRoleException.class);

        // Asserts
        Role result = resolvers.deleteRoleDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchRoleException.class)
    public void deleteRoleDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("roleId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Role expectedResult = mock(Role.class);
        expectedResult.setRoleId(ROLE_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("roleId")))
                .thenReturn(789456L);
        when(localService.deleteRole(eq(789456L)))
                .thenThrow(NoSuchRoleException.class);

        // Asserts
        Role result = resolvers.deleteRoleDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
