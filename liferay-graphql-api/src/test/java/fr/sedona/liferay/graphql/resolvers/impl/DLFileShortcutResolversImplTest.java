package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.exception.NoSuchFileShortcutException;
import com.liferay.document.library.kernel.model.DLFileShortcut;
import com.liferay.document.library.kernel.service.DLFileShortcutLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DLFileShortcutBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileShortcutResolvers;
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
 * Test suite for {@link DLFileShortcutResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DLFileShortcutResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long FILE_SHORTCUT_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final long REPOSITORY_ID = 457L;
    private static final long FOLDER_ID = 458L;
    private static final long TO_FILE_ENTRY_ID = 459L;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DLFileShortcut> dataLoader;

    @InjectMocks
    DLFileShortcutResolvers resolvers = new DLFileShortcutResolversImpl();

    @Mock
    private DLFileShortcutLocalService localService;

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
                .getDataLoader(DLFileShortcutBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DLFileShortcutResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("fileShortcutId")))
                    .thenReturn(FILE_SHORTCUT_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("repositoryId")))
                    .thenReturn(REPOSITORY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                    .thenReturn(FOLDER_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("toFileEntryId")))
                    .thenReturn(TO_FILE_ENTRY_ID);
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
    public void getDLFileShortcutsDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileShortcut> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileShortcut entity = mock(DLFileShortcut.class);
                    entity.setFileShortcutId(value);
                    availableObjects.add(entity);
                });
        List<DLFileShortcut> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileShortcuts(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileShortcut> results = resolvers.getDLFileShortcutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileShortcutsDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DLFileShortcut> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileShortcut entity = mock(DLFileShortcut.class);
                    entity.setFileShortcutId(value);
                    availableObjects.add(entity);
                });
        List<DLFileShortcut> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileShortcuts(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileShortcut> results = resolvers.getDLFileShortcutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileShortcutsDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileShortcut> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileShortcut entity = mock(DLFileShortcut.class);
                    entity.setFileShortcutId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileShortcuts(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileShortcut> results = resolvers.getDLFileShortcutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileShortcutsDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileShortcut> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileShortcuts(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileShortcut> results = resolvers.getDLFileShortcutsDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileShortcutDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DLFileShortcut expectedResult = mock(DLFileShortcut.class);
        expectedResult.setFileShortcutId(FILE_SHORTCUT_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileShortcutId"))
                .thenReturn(FILE_SHORTCUT_ID);
        when(dataLoader.load(FILE_SHORTCUT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DLFileShortcut> asyncResult = resolvers.getDLFileShortcutDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileShortcut result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDLFileShortcutDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("fileShortcutId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DLFileShortcut> asyncResult = resolvers.getDLFileShortcutDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDLFileShortcutDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileShortcutId"))
                .thenReturn(FILE_SHORTCUT_ID);
        when(dataLoader.load(FILE_SHORTCUT_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DLFileShortcut> asyncResult = resolvers.getDLFileShortcutDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileShortcut result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createDLFileShortcutDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("toFileEntryId", TO_FILE_ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileShortcut expectedResult = mock(DLFileShortcut.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setFileShortcutId(FILE_SHORTCUT_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setRepositoryId(REPOSITORY_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setToFileEntryId(TO_FILE_ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addFileShortcut(eq(USER_ID), eq(GROUP_ID), eq(REPOSITORY_ID), eq(FOLDER_ID), eq(TO_FILE_ENTRY_ID), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileShortcut result = resolvers.createDLFileShortcutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createDLFileShortcutDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("toFileEntryId", TO_FILE_ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileShortcut expectedResult = mock(DLFileShortcut.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setFileShortcutId(FILE_SHORTCUT_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setRepositoryId(REPOSITORY_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setToFileEntryId(TO_FILE_ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addFileShortcut(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(REPOSITORY_ID), eq(FOLDER_ID), eq(TO_FILE_ENTRY_ID), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileShortcut result = resolvers.createDLFileShortcutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createDLFileShortcutDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addFileShortcut(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DLFileShortcut result = resolvers.createDLFileShortcutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateDLFileShortcutDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileShortcutId", FILE_SHORTCUT_ID);
        arguments.put("userId", USER_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("toFileEntryId", TO_FILE_ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileShortcut expectedResult = mock(DLFileShortcut.class);
        expectedResult.setFileShortcutId(FILE_SHORTCUT_ID);
        expectedResult.setRepositoryId(REPOSITORY_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setToFileEntryId(TO_FILE_ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateFileShortcut(eq(USER_ID), eq(FILE_SHORTCUT_ID), eq(REPOSITORY_ID), eq(FOLDER_ID), eq(TO_FILE_ENTRY_ID), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileShortcut result = resolvers.updateDLFileShortcutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFileShortcutException.class)
    public void updateDLFileShortcutDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("toFileEntryId", TO_FILE_ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileShortcutId")))
                .thenReturn(0L);
        when(localService.updateFileShortcut(eq(USER_ID), eq(0L), eq(REPOSITORY_ID), eq(FOLDER_ID), eq(TO_FILE_ENTRY_ID), any(ServiceContext.class)))
                .thenThrow(NoSuchFileShortcutException.class);

        // Asserts
        DLFileShortcut result = resolvers.updateDLFileShortcutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFileShortcutException.class)
    public void updateDLFileShortcutDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileShortcutId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("toFileEntryId", TO_FILE_ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileShortcutId")))
                .thenReturn(789456L);
        when(localService.updateFileShortcut(eq(USER_ID), eq(789456L), eq(REPOSITORY_ID), eq(FOLDER_ID), eq(TO_FILE_ENTRY_ID), any(ServiceContext.class)))
                .thenThrow(NoSuchFileShortcutException.class);

        // Asserts
        DLFileShortcut result = resolvers.updateDLFileShortcutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateDLFileShortcutDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileShortcutId")))
                .thenReturn(FILE_SHORTCUT_ID);
        when(localService.updateFileShortcut(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DLFileShortcut result = resolvers.updateDLFileShortcutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteDLFileShortcutDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileShortcutId", FILE_SHORTCUT_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileShortcut expectedResult = mock(DLFileShortcut.class);
        expectedResult.setFileShortcutId(FILE_SHORTCUT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFileShortcut(eq(FILE_SHORTCUT_ID)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileShortcut result = resolvers.deleteDLFileShortcutDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFileShortcutException.class)
    public void deleteDLFileShortcutDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        DLFileShortcut expectedResult = mock(DLFileShortcut.class);
        expectedResult.setFileShortcutId(FILE_SHORTCUT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFileShortcut(eq(FILE_SHORTCUT_ID)))
                .thenThrow(NoSuchFileShortcutException.class);

        // Asserts
        DLFileShortcut result = resolvers.deleteDLFileShortcutDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFileShortcutException.class)
    public void deleteDLFileShortcutDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileShortcutId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileShortcut expectedResult = mock(DLFileShortcut.class);
        expectedResult.setFileShortcutId(FILE_SHORTCUT_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileShortcutId")))
                .thenReturn(789456L);
        when(localService.deleteDLFileShortcut(eq(789456L)))
                .thenThrow(NoSuchFileShortcutException.class);

        // Asserts
        DLFileShortcut result = resolvers.deleteDLFileShortcutDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
