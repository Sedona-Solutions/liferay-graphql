package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.exception.NoSuchGroupException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import fr.sedona.liferay.graphql.loaders.GroupBatchLoader;
import fr.sedona.liferay.graphql.resolvers.GroupResolvers;
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
 * Test suite for {@link GroupResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class GroupResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long GROUP_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long PARENT_GROUP_ID = 0L;
    private static final String CLASS_NAME = "fr.sedona.Test";
    private static final long CLASS_PK = 123L;
    private static final long LIVE_GROUP_ID = 988L;
    private static final Map<Locale, String> NAME_MAP;
    private static final Map<Locale, String> DESCRIPTION_MAP;
    private static final int TYPE = 0;
    private static final boolean MANUAL_MEMBERSHIP = false;
    private static final int MEMBERSHIP_RESTRICTION = 0;
    private static final String FRIENDLY_URL = "/sedona";
    private static final boolean SITE = true;
    private static final boolean INHERIT_CONTENT = true;
    private static final boolean ACTIVE = true;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, Group> dataLoader;

    static {
        NAME_MAP = new HashMap<>();
        NAME_MAP.put(LocaleUtil.US, "Sedona");
        NAME_MAP.put(LocaleUtil.FRANCE, "Sedona");

        DESCRIPTION_MAP = new HashMap<>();
        DESCRIPTION_MAP.put(LocaleUtil.US, "Test description");
        DESCRIPTION_MAP.put(LocaleUtil.FRANCE, "Description de test");
    }

    @InjectMocks
    GroupResolvers resolvers = new GroupResolversImpl();

    @Mock
    private GroupLocalService localService;

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
                .getDataLoader(GroupBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((GroupResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentGroupId"), anyLong()))
                    .thenReturn(PARENT_GROUP_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("className")))
                    .thenReturn(CLASS_NAME);
            when(graphQLUtil.getLongArg(eq(environment), eq("classPK")))
                    .thenReturn(CLASS_PK);
            when(graphQLUtil.getLongArg(eq(environment), eq("liveGroupId"), anyLong()))
                    .thenReturn(LIVE_GROUP_ID);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("nameMap")))
                    .thenReturn(NAME_MAP);
            when(graphQLUtil.getTranslatedArg(eq(environment), eq("descriptionMap")))
                    .thenReturn(DESCRIPTION_MAP);
            when(graphQLUtil.getIntArg(eq(environment), eq("type"), anyInt()))
                    .thenReturn(TYPE);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("manualMembership")))
                    .thenReturn(MANUAL_MEMBERSHIP);
            when(graphQLUtil.getIntArg(eq(environment), eq("membershipRestriction")))
                    .thenReturn(MEMBERSHIP_RESTRICTION);
            when(graphQLUtil.getStringArg(eq(environment), eq("friendlyURL")))
                    .thenReturn(FRIENDLY_URL);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("site")))
                    .thenReturn(SITE);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("inheritContent"), anyBoolean()))
                    .thenReturn(INHERIT_CONTENT);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("active"), anyBoolean()))
                    .thenReturn(ACTIVE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getLongArg(eq(environment), anyString(), anyLong()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
            when(graphQLUtil.getIntArg(eq(environment), anyString(), anyInt()))
                    .thenReturn(0);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString(), anyBoolean()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getTranslatedArg(eq(environment), anyString()))
                    .thenReturn(Collections.emptyMap());
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
    public void getGroupsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Group> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Group entity = mock(Group.class);
                    entity.setGroupId(value);
                    availableObjects.add(entity);
                });
        List<Group> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getGroups(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<Group> results = resolvers.getGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getGroupsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<Group> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Group entity = mock(Group.class);
                    entity.setGroupId(value);
                    availableObjects.add(entity);
                });
        List<Group> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getGroups(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<Group> results = resolvers.getGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getGroupsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Group> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    Group entity = mock(Group.class);
                    entity.setGroupId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getGroups(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Group> results = resolvers.getGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getGroupsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Group> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getGroups(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<Group> results = resolvers.getGroupsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getGroupDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        Group expectedResult = mock(Group.class);
        expectedResult.setGroupId(GROUP_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("groupId"))
                .thenReturn(GROUP_ID);
        when(dataLoader.load(GROUP_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<Group> asyncResult = resolvers.getGroupDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Group result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getGroupDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("groupId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<Group> asyncResult = resolvers.getGroupDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getGroupDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("groupId"))
                .thenReturn(GROUP_ID);
        when(dataLoader.load(GROUP_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<Group> asyncResult = resolvers.getGroupDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        Group result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createGroupDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("parentGroupId", PARENT_GROUP_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("liveGroupId", LIVE_GROUP_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("manualMembership", MANUAL_MEMBERSHIP);
        arguments.put("membershipRestriction", MEMBERSHIP_RESTRICTION);
        arguments.put("friendlyURL", FRIENDLY_URL);
        arguments.put("site", SITE);
        arguments.put("inheritContent", INHERIT_CONTENT);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Group expectedResult = mock(Group.class);
        expectedResult.setCreatorUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentGroupId(PARENT_GROUP_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setLiveGroupId(LIVE_GROUP_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setManualMembership(MANUAL_MEMBERSHIP);
        expectedResult.setMembershipRestriction(MEMBERSHIP_RESTRICTION);
        expectedResult.setFriendlyURL(FRIENDLY_URL);
        expectedResult.setSite(SITE);
        expectedResult.setInheritContent(INHERIT_CONTENT);
        expectedResult.setActive(ACTIVE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addGroup(eq(USER_ID), eq(PARENT_GROUP_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(LIVE_GROUP_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MANUAL_MEMBERSHIP), eq(MEMBERSHIP_RESTRICTION), eq(FRIENDLY_URL), eq(SITE), eq(INHERIT_CONTENT), eq(ACTIVE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Group result = resolvers.createGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createGroupDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("parentGroupId", PARENT_GROUP_ID);
        arguments.put("className", CLASS_NAME);
        arguments.put("classPK", CLASS_PK);
        arguments.put("liveGroupId", LIVE_GROUP_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("manualMembership", MANUAL_MEMBERSHIP);
        arguments.put("membershipRestriction", MEMBERSHIP_RESTRICTION);
        arguments.put("friendlyURL", FRIENDLY_URL);
        arguments.put("site", SITE);
        arguments.put("inheritContent", INHERIT_CONTENT);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Group expectedResult = mock(Group.class);
        expectedResult.setCreatorUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentGroupId(PARENT_GROUP_ID);
        expectedResult.setClassName(CLASS_NAME);
        expectedResult.setClassPK(CLASS_PK);
        expectedResult.setLiveGroupId(LIVE_GROUP_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setManualMembership(MANUAL_MEMBERSHIP);
        expectedResult.setMembershipRestriction(MEMBERSHIP_RESTRICTION);
        expectedResult.setFriendlyURL(FRIENDLY_URL);
        expectedResult.setSite(SITE);
        expectedResult.setInheritContent(INHERIT_CONTENT);
        expectedResult.setActive(ACTIVE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addGroup(eq(DEFAULT_USER_ID), eq(PARENT_GROUP_ID), eq(CLASS_NAME), eq(CLASS_PK), eq(LIVE_GROUP_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MANUAL_MEMBERSHIP), eq(MEMBERSHIP_RESTRICTION), eq(FRIENDLY_URL), eq(SITE), eq(INHERIT_CONTENT), eq(ACTIVE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Group result = resolvers.createGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createGroupDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addGroup(anyLong(), anyLong(), anyString(), anyLong(), anyLong(), anyMap(), anyMap(), anyInt(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Group result = resolvers.createGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateGroupDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentGroupId", PARENT_GROUP_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("manualMembership", MANUAL_MEMBERSHIP);
        arguments.put("membershipRestriction", MEMBERSHIP_RESTRICTION);
        arguments.put("friendlyURL", FRIENDLY_URL);
        arguments.put("inheritContent", INHERIT_CONTENT);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Group expectedResult = mock(Group.class);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentGroupId(PARENT_GROUP_ID);
        expectedResult.setNameMap(NAME_MAP);
        expectedResult.setDescriptionMap(DESCRIPTION_MAP);
        expectedResult.setType(TYPE);
        expectedResult.setManualMembership(MANUAL_MEMBERSHIP);
        expectedResult.setMembershipRestriction(MEMBERSHIP_RESTRICTION);
        expectedResult.setFriendlyURL(FRIENDLY_URL);
        expectedResult.setInheritContent(INHERIT_CONTENT);
        expectedResult.setActive(ACTIVE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.updateGroup(eq(GROUP_ID), eq(PARENT_GROUP_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MANUAL_MEMBERSHIP), eq(MEMBERSHIP_RESTRICTION), eq(FRIENDLY_URL), eq(INHERIT_CONTENT), eq(ACTIVE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        Group result = resolvers.updateGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchGroupException.class)
    public void updateGroupDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("parentGroupId", PARENT_GROUP_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("manualMembership", MANUAL_MEMBERSHIP);
        arguments.put("membershipRestriction", MEMBERSHIP_RESTRICTION);
        arguments.put("friendlyURL", FRIENDLY_URL);
        arguments.put("inheritContent", INHERIT_CONTENT);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                .thenReturn(0L);
        when(localService.updateGroup(eq(0L), eq(PARENT_GROUP_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MANUAL_MEMBERSHIP), eq(MEMBERSHIP_RESTRICTION), eq(FRIENDLY_URL), eq(INHERIT_CONTENT), eq(ACTIVE), any(ServiceContext.class)))
                .thenThrow(NoSuchGroupException.class);

        // Asserts
        Group result = resolvers.updateGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchGroupException.class)
    public void updateGroupDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", 789456L);
        arguments.put("parentGroupId", PARENT_GROUP_ID);
        arguments.put("nameMap", NAME_MAP);
        arguments.put("descriptionMap", DESCRIPTION_MAP);
        arguments.put("type", TYPE);
        arguments.put("manualMembership", MANUAL_MEMBERSHIP);
        arguments.put("membershipRestriction", MEMBERSHIP_RESTRICTION);
        arguments.put("friendlyURL", FRIENDLY_URL);
        arguments.put("site", SITE);
        arguments.put("inheritContent", INHERIT_CONTENT);
        arguments.put("active", ACTIVE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                .thenReturn(789456L);
        when(localService.updateGroup(eq(789456L), eq(PARENT_GROUP_ID), eq(NAME_MAP), eq(DESCRIPTION_MAP), eq(TYPE), eq(MANUAL_MEMBERSHIP), eq(MEMBERSHIP_RESTRICTION), eq(FRIENDLY_URL), eq(INHERIT_CONTENT), eq(ACTIVE), any(ServiceContext.class)))
                .thenThrow(NoSuchGroupException.class);

        // Asserts
        Group result = resolvers.updateGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateGroupDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                .thenReturn(0L);
        when(localService.updateGroup(anyLong(), anyLong(), anyMap(), anyMap(), anyInt(), anyBoolean(), anyInt(), anyString(), anyBoolean(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        Group result = resolvers.updateGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteGroupDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Group expectedResult = mock(Group.class);
        expectedResult.setGroupId(GROUP_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteGroup(eq(GROUP_ID)))
                .thenReturn(expectedResult);

        // Asserts
        Group result = resolvers.deleteGroupDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchGroupException.class)
    public void deleteGroupDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        Group expectedResult = mock(Group.class);
        expectedResult.setGroupId(GROUP_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteGroup(eq(GROUP_ID)))
                .thenThrow(NoSuchGroupException.class);

        // Asserts
        Group result = resolvers.deleteGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchGroupException.class)
    public void deleteGroupDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        Group expectedResult = mock(Group.class);
        expectedResult.setGroupId(GROUP_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                .thenReturn(789456L);
        when(localService.deleteGroup(eq(789456L)))
                .thenThrow(NoSuchGroupException.class);

        // Asserts
        Group result = resolvers.deleteGroupDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
