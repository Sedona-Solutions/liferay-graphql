package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.exception.NoSuchFileEntryException;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DLFileEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileEntryResolvers;
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
 * Test suite for {@link DLFileEntryResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DLFileEntryResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long FILE_ENTRY_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final long REPOSITORY_ID = 457L;
    private static final long FOLDER_ID = 458L;
    private static final String SOURCE_FILE_NAME = "test.txt";
    private static final String MIME_TYPE = "plain/text";
    private static final String TITLE = "Test";
    private static final String DESCRIPTION = "Some description";
    private static final String CHANGE_LOG = "Initial commit";
    private static final boolean MAJOR_VERSION = true;
    private static final long FILE_ENTRY_TYPE_ID = 1L;
    private static final long SIZE = 0L;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DLFileEntry> dataLoader;

    @InjectMocks
    DLFileEntryResolvers resolvers = new DLFileEntryResolversImpl();

    @Mock
    private DLFileEntryLocalService localService;

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
                .getDataLoader(DLFileEntryBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DLFileEntryResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryId")))
                    .thenReturn(FILE_ENTRY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("repositoryId")))
                    .thenReturn(REPOSITORY_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                    .thenReturn(FOLDER_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("sourceFileName")))
                    .thenReturn(SOURCE_FILE_NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("mimeType")))
                    .thenReturn(MIME_TYPE);
            when(graphQLUtil.getStringArg(eq(environment), eq("title")))
                    .thenReturn(TITLE);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
            when(graphQLUtil.getStringArg(eq(environment), eq("changeLog")))
                    .thenReturn(CHANGE_LOG);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("majorVersion")))
                    .thenReturn(MAJOR_VERSION);
            when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryTypeId")))
                    .thenReturn(FILE_ENTRY_TYPE_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("size")))
                    .thenReturn(SIZE);
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
    public void getDLFileEntriesDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntry entity = mock(DLFileEntry.class);
                    entity.setFileEntryId(value);
                    availableObjects.add(entity);
                });
        List<DLFileEntry> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntries(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntry> results = resolvers.getDLFileEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntriesDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DLFileEntry> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntry entity = mock(DLFileEntry.class);
                    entity.setFileEntryId(value);
                    availableObjects.add(entity);
                });
        List<DLFileEntry> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntries(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntry> results = resolvers.getDLFileEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntriesDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntry> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFileEntry entity = mock(DLFileEntry.class);
                    entity.setFileEntryId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntry> results = resolvers.getDLFileEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntriesDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFileEntry> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFileEntries(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFileEntry> results = resolvers.getDLFileEntriesDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFileEntryDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DLFileEntry expectedResult = mock(DLFileEntry.class);
        expectedResult.setFileEntryId(FILE_ENTRY_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileEntryId"))
                .thenReturn(FILE_ENTRY_ID);
        when(dataLoader.load(FILE_ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DLFileEntry> asyncResult = resolvers.getDLFileEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileEntry result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDLFileEntryDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("fileEntryId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DLFileEntry> asyncResult = resolvers.getDLFileEntryDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDLFileEntryDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("fileEntryId"))
                .thenReturn(FILE_ENTRY_ID);
        when(dataLoader.load(FILE_ENTRY_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DLFileEntry> asyncResult = resolvers.getDLFileEntryDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFileEntry result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createDLFileEntryDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("sourceFileName", SOURCE_FILE_NAME);
        arguments.put("mimeType", MIME_TYPE);
        arguments.put("title", TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("changeLog", CHANGE_LOG);
        arguments.put("fileEntryTypeId", FILE_ENTRY_TYPE_ID);
        arguments.put("size", SIZE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntry expectedResult = mock(DLFileEntry.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setRepositoryId(REPOSITORY_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setFileName(SOURCE_FILE_NAME);
        expectedResult.setMimeType(MIME_TYPE);
        expectedResult.setTitle(TITLE);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);
        expectedResult.setSize(SIZE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addFileEntry(eq(USER_ID), eq(GROUP_ID), eq(REPOSITORY_ID), eq(FOLDER_ID), eq(SOURCE_FILE_NAME), eq(MIME_TYPE), eq(TITLE), eq(DESCRIPTION), eq(CHANGE_LOG), eq(FILE_ENTRY_TYPE_ID), anyMap(), any(), any(), eq(SIZE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntry result = resolvers.createDLFileEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createDLFileEntryDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("folderId", FOLDER_ID);
        arguments.put("sourceFileName", SOURCE_FILE_NAME);
        arguments.put("mimeType", MIME_TYPE);
        arguments.put("title", TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("changeLog", CHANGE_LOG);
        arguments.put("fileEntryTypeId", FILE_ENTRY_TYPE_ID);
        arguments.put("size", SIZE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntry expectedResult = mock(DLFileEntry.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setRepositoryId(REPOSITORY_ID);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setFileName(SOURCE_FILE_NAME);
        expectedResult.setMimeType(MIME_TYPE);
        expectedResult.setTitle(TITLE);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);
        expectedResult.setSize(SIZE);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addFileEntry(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(REPOSITORY_ID), eq(FOLDER_ID), eq(SOURCE_FILE_NAME), eq(MIME_TYPE), eq(TITLE), eq(DESCRIPTION), eq(CHANGE_LOG), eq(FILE_ENTRY_TYPE_ID), anyMap(), any(), any(), eq(SIZE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntry result = resolvers.createDLFileEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createDLFileEntryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addFileEntry(anyLong(), anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyLong(), anyMap(), any(), any(), anyLong(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DLFileEntry result = resolvers.createDLFileEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateDLFileEntryDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryId", FILE_ENTRY_ID);
        arguments.put("userId", USER_ID);
        arguments.put("sourceFileName", SOURCE_FILE_NAME);
        arguments.put("mimeType", MIME_TYPE);
        arguments.put("title", TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("changeLog", CHANGE_LOG);
        arguments.put("majorVersion", MAJOR_VERSION);
        arguments.put("fileEntryTypeId", FILE_ENTRY_TYPE_ID);
        arguments.put("size", SIZE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntry expectedResult = mock(DLFileEntry.class);
        expectedResult.setFileEntryId(FILE_ENTRY_ID);
        expectedResult.setUserId(USER_ID);
        expectedResult.setFileName(SOURCE_FILE_NAME);
        expectedResult.setMimeType(MIME_TYPE);
        expectedResult.setTitle(TITLE);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setFileEntryTypeId(FILE_ENTRY_TYPE_ID);
        expectedResult.setSize(SIZE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateFileEntry(eq(USER_ID), eq(FILE_ENTRY_ID), eq(SOURCE_FILE_NAME), eq(MIME_TYPE), eq(TITLE), eq(DESCRIPTION), eq(CHANGE_LOG), eq(MAJOR_VERSION), eq(FILE_ENTRY_TYPE_ID), anyMap(), any(), any(), eq(SIZE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntry result = resolvers.updateDLFileEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFileEntryException.class)
    public void updateDLFileEntryDataFetcher_with_no_address_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("sourceFileName", SOURCE_FILE_NAME);
        arguments.put("mimeType", MIME_TYPE);
        arguments.put("title", TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("changeLog", CHANGE_LOG);
        arguments.put("majorVersion", MAJOR_VERSION);
        arguments.put("fileEntryTypeId", FILE_ENTRY_TYPE_ID);
        arguments.put("size", SIZE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryId")))
                .thenReturn(0L);
        when(localService.updateFileEntry(eq(USER_ID), eq(0L), eq(SOURCE_FILE_NAME), eq(MIME_TYPE), eq(TITLE), eq(DESCRIPTION), eq(CHANGE_LOG), eq(MAJOR_VERSION), eq(FILE_ENTRY_TYPE_ID), anyMap(), any(), any(), eq(SIZE), any(ServiceContext.class)))
                .thenThrow(NoSuchFileEntryException.class);

        // Asserts
        DLFileEntry result = resolvers.updateDLFileEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFileEntryException.class)
    public void updateDLFileEntryDataFetcher_with_invalid_address_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryId", 789456L);
        arguments.put("userId", USER_ID);
        arguments.put("sourceFileName", SOURCE_FILE_NAME);
        arguments.put("mimeType", MIME_TYPE);
        arguments.put("title", TITLE);
        arguments.put("description", DESCRIPTION);
        arguments.put("changeLog", CHANGE_LOG);
        arguments.put("majorVersion", MAJOR_VERSION);
        arguments.put("fileEntryTypeId", FILE_ENTRY_TYPE_ID);
        arguments.put("size", SIZE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryId")))
                .thenReturn(789456L);
        when(localService.updateFileEntry(eq(USER_ID), eq(789456L), eq(SOURCE_FILE_NAME), eq(MIME_TYPE), eq(TITLE), eq(DESCRIPTION), eq(CHANGE_LOG), eq(MAJOR_VERSION), eq(FILE_ENTRY_TYPE_ID), anyMap(), any(), any(), eq(SIZE), any(ServiceContext.class)))
                .thenThrow(NoSuchFileEntryException.class);

        // Asserts
        DLFileEntry result = resolvers.updateDLFileEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateDLFileEntryDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryId")))
                .thenReturn(FILE_ENTRY_ID);
        when(localService.updateFileEntry(anyLong(), anyLong(), anyString(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyLong(), anyMap(), any(), any(), anyLong(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DLFileEntry result = resolvers.updateDLFileEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteDLFileEntryDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryId", FILE_ENTRY_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntry expectedResult = mock(DLFileEntry.class);
        expectedResult.setFileEntryId(FILE_ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFileEntry(eq(FILE_ENTRY_ID)))
                .thenReturn(expectedResult);

        // Asserts
        DLFileEntry result = resolvers.deleteDLFileEntryDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFileEntryException.class)
    public void deleteDLFileEntryDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        DLFileEntry expectedResult = mock(DLFileEntry.class);
        expectedResult.setFileEntryId(FILE_ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFileEntry(eq(FILE_ENTRY_ID)))
                .thenThrow(NoSuchFileEntryException.class);

        // Asserts
        DLFileEntry result = resolvers.deleteDLFileEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFileEntryException.class)
    public void deleteDLFileEntryDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("fileEntryId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFileEntry expectedResult = mock(DLFileEntry.class);
        expectedResult.setFileEntryId(FILE_ENTRY_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("fileEntryId")))
                .thenReturn(789456L);
        when(localService.deleteDLFileEntry(eq(789456L)))
                .thenThrow(NoSuchFileEntryException.class);

        // Asserts
        DLFileEntry result = resolvers.deleteDLFileEntryDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
