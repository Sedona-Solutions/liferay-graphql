package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchUserGroupException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import fr.sedona.liferay.graphql.loaders.UserGroupBatchLoader;
import fr.sedona.liferay.graphql.resolvers.UserGroupResolvers;
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
 * Test suite for {@link UserGroupResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class UserGroupResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long USER_GROUP_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long COMPANY_ID = 456L;
    private static final String NAME = null;
    private static final String DESCRIPTION = null;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, UserGroup> dataLoader;

    @InjectMocks
    UserGroupResolvers resolvers = new UserGroupResolversImpl();

    @Mock
    private UserGroupLocalService localService;

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
                .getDataLoader(UserGroupBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((UserGroupResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("userGroupId")))
                    .thenReturn(USER_GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("companyId")))
                    .thenReturn(COMPANY_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
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
    public void getUserGroupsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<UserGroup> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    UserGroup entity = mock(UserGroup.class);
                    entity.setUserGroupId(value);
                    availableObjects.add(entity);
                });
        List<UserGroup> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUserGroups(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<UserGroup> results = resolvers.getUserGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUserGroupsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<UserGroup> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    UserGroup entity = mock(UserGroup.class);
                    entity.setUserGroupId(value);
                    availableObjects.add(entity);
                });
        List<UserGroup> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUserGroups(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<UserGroup> results = resolvers.getUserGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUserGroupsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<UserGroup> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    UserGroup entity = mock(UserGroup.class);
                    entity.setUserGroupId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUserGroups(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<UserGroup> results = resolvers.getUserGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUserGroupsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<UserGroup> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getUserGroups(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<UserGroup> results = resolvers.getUserGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getUserGroupDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        UserGroup expectedResult = mock(UserGroup.class);
        expectedResult.setUserGroupId(USER_GROUP_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("userGroupId"))
                .thenReturn(USER_GROUP_ID);
        when(dataLoader.load(USER_GROUP_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<UserGroup> asyncResult = resolvers.getUserGroupDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        UserGroup result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getUserGroupDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("userGroupId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<UserGroup> asyncResult = resolvers.getUserGroupDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getUserGroupDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("userGroupId"))
                .thenReturn(USER_GROUP_ID);
        when(dataLoader.load(USER_GROUP_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<UserGroup> asyncResult = resolvers.getUserGroupDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        UserGroup result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createUserGroupDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        UserGroup expectedResult = mock(UserGroup.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addUserGroup(eq(USER_ID), eq(COMPANY_ID), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        UserGroup result = resolvers.createUserGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createUserGroupDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        UserGroup expectedResult = mock(UserGroup.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addUserGroup(eq(DEFAULT_USER_ID), eq(COMPANY_ID), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        UserGroup result = resolvers.createUserGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createUserGroupDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addUserGroup(anyLong(), anyLong(), anyString(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        UserGroup result = resolvers.createUserGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateUserGroupDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("userGroupId", USER_GROUP_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        UserGroup expectedResult = mock(UserGroup.class);
        expectedResult.setUserGroupId(USER_GROUP_ID);
        expectedResult.setCompanyId(COMPANY_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateUserGroup(eq(COMPANY_ID), eq(USER_GROUP_ID), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        UserGroup result = resolvers.updateUserGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchUserGroupException.class)
    public void updateUserGroupDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("companyId", COMPANY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("userGroupId")))
                .thenReturn(0L);
        when(localService.updateUserGroup(eq(COMPANY_ID), eq(0L), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenThrow(NoSuchUserGroupException.class);

        // Asserts
        UserGroup result = resolvers.updateUserGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchUserGroupException.class)
    public void updateUserGroupDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userGroupId", 789456L);
        arguments.put("companyId", COMPANY_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("userGroupId")))
                .thenReturn(789456L);
        when(localService.updateUserGroup(eq(COMPANY_ID), eq(789456L), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenThrow(NoSuchUserGroupException.class);

        // Asserts
        UserGroup result = resolvers.updateUserGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateUserGroupDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("userGroupId")))
                .thenReturn(USER_GROUP_ID);
        when(localService.updateUserGroup(anyLong(), anyLong(), anyString(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        UserGroup result = resolvers.updateUserGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteUserGroupDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userGroupId", USER_GROUP_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        UserGroup expectedResult = mock(UserGroup.class);
        expectedResult.setUserGroupId(USER_GROUP_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteUserGroup(eq(USER_GROUP_ID)))
                .thenReturn(expectedResult);

        // Asserts
        UserGroup result = resolvers.deleteUserGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchUserGroupException.class)
    public void deleteUserGroupDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        UserGroup expectedResult = mock(UserGroup.class);
        expectedResult.setUserGroupId(USER_GROUP_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteUserGroup(eq(USER_GROUP_ID)))
                .thenThrow(NoSuchUserGroupException.class);

        // Asserts
        UserGroup result = resolvers.deleteUserGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchUserGroupException.class)
    public void deleteUserGroupDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userGroupId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        UserGroup expectedResult = mock(UserGroup.class);
        expectedResult.setUserGroupId(USER_GROUP_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("userGroupId")))
                .thenReturn(789456L);
        when(localService.deleteUserGroup(eq(789456L)))
                .thenThrow(NoSuchUserGroupException.class);

        // Asserts
        UserGroup result = resolvers.deleteUserGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
