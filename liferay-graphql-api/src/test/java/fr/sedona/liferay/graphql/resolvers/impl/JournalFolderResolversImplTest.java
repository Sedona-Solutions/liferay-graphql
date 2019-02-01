package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.journal.exception.NoSuchFolderException;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.JournalFolderBatchLoader;
import fr.sedona.liferay.graphql.resolvers.JournalFolderResolvers;
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
 * Test suite for {@link JournalFolderResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class JournalFolderResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long FOLDER_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final long PARENT_FOLDER_ID = 0L;
    private static final String NAME = "Folder name";
    private static final String DESCRIPTION = "Folder description";
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, JournalFolder> dataLoader;

    @InjectMocks
    JournalFolderResolvers resolvers = new JournalFolderResolversImpl();

    @Mock
    private JournalFolderLocalService localService;

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
                .getDataLoader(JournalFolderBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((JournalFolderResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                    .thenReturn(FOLDER_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentFolderId"), anyLong()))
                    .thenReturn(PARENT_FOLDER_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getLongArg(eq(environment), anyString(), anyLong()))
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
    public void getJournalFoldersDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalFolder> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalFolder entity = mock(JournalFolder.class);
                    entity.setFolderId(value);
                    availableObjects.add(entity);
                });
        List<JournalFolder> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFolders(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFolder> results = resolvers.getJournalFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFoldersDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<JournalFolder> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalFolder entity = mock(JournalFolder.class);
                    entity.setFolderId(value);
                    availableObjects.add(entity);
                });
        List<JournalFolder> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFolders(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFolder> results = resolvers.getJournalFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFoldersDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalFolder> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    JournalFolder entity = mock(JournalFolder.class);
                    entity.setFolderId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFolders(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFolder> results = resolvers.getJournalFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFoldersDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<JournalFolder> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getJournalFolders(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<JournalFolder> results = resolvers.getJournalFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getJournalFolderDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        JournalFolder expectedResult = mock(JournalFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("folderId"))
                .thenReturn(FOLDER_ID);
        when(dataLoader.load(FOLDER_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<JournalFolder> asyncResult = resolvers.getJournalFolderDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        JournalFolder result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getJournalFolderDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("folderId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<JournalFolder> asyncResult = resolvers.getJournalFolderDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getJournalFolderDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("folderId"))
                .thenReturn(FOLDER_ID);
        when(dataLoader.load(FOLDER_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<JournalFolder> asyncResult = resolvers.getJournalFolderDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        JournalFolder result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createJournalFolderDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFolder expectedResult = mock(JournalFolder.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentFolderId(PARENT_FOLDER_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addFolder(eq(USER_ID), eq(GROUP_ID), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFolder result = resolvers.createJournalFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createJournalFolderDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFolder expectedResult = mock(JournalFolder.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentFolderId(PARENT_FOLDER_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addFolder(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFolder result = resolvers.createJournalFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createJournalFolderDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addFolder(anyLong(), anyLong(), anyLong(), anyString(), anyString(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        JournalFolder result = resolvers.createJournalFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateJournalFolderDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", FOLDER_ID);
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFolder expectedResult = mock(JournalFolder.class);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setParentFolderId(PARENT_FOLDER_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateFolder(eq(USER_ID), eq(GROUP_ID), eq(FOLDER_ID), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), anyBoolean(), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFolder result = resolvers.updateJournalFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void updateJournalFolderDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(0L);
        when(localService.updateFolder(eq(USER_ID), eq(GROUP_ID), eq(0L), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        JournalFolder result = resolvers.updateJournalFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void updateJournalFolderDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(789456L);
        when(localService.updateFolder(eq(USER_ID), eq(GROUP_ID), eq(789456L), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        JournalFolder result = resolvers.updateJournalFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateJournalFolderDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(FOLDER_ID);
        when(localService.updateFolder(anyLong(), anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        JournalFolder result = resolvers.updateJournalFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteJournalFolderDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", FOLDER_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFolder expectedResult = mock(JournalFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteJournalFolder(eq(FOLDER_ID)))
                .thenReturn(expectedResult);

        // Asserts
        JournalFolder result = resolvers.deleteJournalFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void deleteJournalFolderDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        JournalFolder expectedResult = mock(JournalFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteJournalFolder(eq(FOLDER_ID)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        JournalFolder result = resolvers.deleteJournalFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void deleteJournalFolderDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        JournalFolder expectedResult = mock(JournalFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(789456L);
        when(localService.deleteJournalFolder(eq(789456L)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        JournalFolder result = resolvers.deleteJournalFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
