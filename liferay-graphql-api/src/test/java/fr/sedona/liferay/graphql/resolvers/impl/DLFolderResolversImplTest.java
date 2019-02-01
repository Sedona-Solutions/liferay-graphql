package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.journal.exception.NoSuchFolderException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DLFolderBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFolderResolvers;
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
 * Test suite for {@link DLFolderResolversImpl}
 */
@RunWith(PowerMockRunner.class)
public class DLFolderResolversImplTest {
    private static final long DEFAULT_USER_ID = 456456L;
    private static final long FOLDER_ID = 987L;
    private static final long USER_ID = 123L;
    private static final long GROUP_ID = 456L;
    private static final long REPOSITORY_ID = 457L;
    private static final boolean MOUNT_POINT = true;
    private static final long PARENT_FOLDER_ID = 0L;
    private static final String NAME = "Folder name";
    private static final String DESCRIPTION = "Folder description";
    private static final boolean HIDDEN = false;
    private static final long DEFAULT_FILE_ENTRY_TYPE_ID = 1L;
    private static final long[] FILE_ENTRY_TYPE_IDS = new long[]{1L, 2L, 3L};
    private static final int RESTRICTION_TYPE = 0;
    private ExecutionId executionId;
    private ExecutionContext executionContext;
    private DataFetchingEnvironment mockEnvironment;
    private DataLoader<Long, DLFolder> dataLoader;

    @InjectMocks
    DLFolderResolvers resolvers = new DLFolderResolversImpl();

    @Mock
    private DLFolderLocalService localService;

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
                .getDataLoader(DLFolderBatchLoader.KEY);
    }

    private void useSimpleGraphQLUtil() {
        ((DLFolderResolversImpl) resolvers).setUtil(new GraphQLUtil());
    }

    private void useMockGraphQLUtil(DataFetchingEnvironment environment, long returnedUserId, boolean isValid) {
        when(graphQLUtil.getLongArg(eq(environment), eq("userId"), anyLong()))
                .thenReturn(returnedUserId);
        if (isValid) {
            when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                    .thenReturn(FOLDER_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("groupId")))
                    .thenReturn(GROUP_ID);
            when(graphQLUtil.getLongArg(eq(environment), eq("repositoryId")))
                    .thenReturn(REPOSITORY_ID);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("mountPoint")))
                    .thenReturn(MOUNT_POINT);
            when(graphQLUtil.getLongArg(eq(environment), eq("parentFolderId")))
                    .thenReturn(PARENT_FOLDER_ID);
            when(graphQLUtil.getStringArg(eq(environment), eq("name")))
                    .thenReturn(NAME);
            when(graphQLUtil.getStringArg(eq(environment), eq("description")))
                    .thenReturn(DESCRIPTION);
            when(graphQLUtil.getBooleanArg(eq(environment), eq("hidden")))
                    .thenReturn(HIDDEN);
            when(graphQLUtil.getLongArg(eq(environment), eq("defaultFileEntryTypeId")))
                    .thenReturn(DEFAULT_FILE_ENTRY_TYPE_ID);
            when(graphQLUtil.getLongArrayArg(eq(environment), eq("fileEntryTypeIds")))
                    .thenReturn(FILE_ENTRY_TYPE_IDS);
            when(graphQLUtil.getIntArg(eq(environment), eq("restrictionType")))
                    .thenReturn(RESTRICTION_TYPE);
        } else {
            when(graphQLUtil.getLongArg(eq(environment), anyString()))
                    .thenReturn(0L);
            when(graphQLUtil.getIntArg(eq(environment), anyString()))
                    .thenReturn(0);
            when(graphQLUtil.getBooleanArg(eq(environment), anyString()))
                    .thenReturn(false);
            when(graphQLUtil.getStringArg(eq(environment), anyString()))
                    .thenReturn("");
            when(graphQLUtil.getLongArrayArg(eq(environment), anyString()))
                    .thenReturn(new long[0]);
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
    public void getDLFoldersDataFetcher_should_return_the_specified_number_of_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 3);
        arguments.put("end", 3);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFolder> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFolder entity = mock(DLFolder.class);
                    entity.setFolderId(value);
                    availableObjects.add(entity);
                });
        List<DLFolder> expectedResults = availableObjects.stream()
                .skip(3)
                .limit(3)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFolders(3, 3))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFolder> results = resolvers.getDLFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFoldersDataFetcher_without_args_should_return_10_first_objects() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        List<DLFolder> availableObjects = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFolder entity = mock(DLFolder.class);
                    entity.setFolderId(value);
                    availableObjects.add(entity);
                });
        List<DLFolder> expectedResults = availableObjects.stream()
                .skip(0)
                .limit(10)
                .collect(Collectors.toList());

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFolders(0, 10))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFolder> results = resolvers.getDLFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFoldersDataFetcher_with_big_range_args_should_return_only_available_objects() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFolder> expectedResults = new ArrayList<>();
        IntStream.rangeClosed(1, 20)
                .forEach(value -> {
                    DLFolder entity = mock(DLFolder.class);
                    entity.setFolderId(value);
                    expectedResults.add(entity);
                });

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFolders(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFolder> results = resolvers.getDLFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFoldersDataFetcher_no_objects_available_should_return_empty_list() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("start", 0);
        arguments.put("end", 100);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<DLFolder> expectedResults = new ArrayList<>();

        // When / Then
        useSimpleGraphQLUtil();
        when(localService.getDLFolders(0, 100))
                .thenReturn(expectedResults);

        // Asserts
        List<DLFolder> results = resolvers.getDLFoldersDataFetcher()
                .get(environment);
        assertNotNull(results);
        assertEquals(expectedResults, results);
    }

    @Test
    public void getDLFolderDataFetcher_should_return_the_searched_object() throws Exception {
        // Given
        DLFolder expectedResult = mock(DLFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("folderId"))
                .thenReturn(FOLDER_ID);
        when(dataLoader.load(FOLDER_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> expectedResult));

        // Asserts
        CompletableFuture<DLFolder> asyncResult = resolvers.getDLFolderDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFolder result = asyncResult.get();
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void getDLFolderDataFetcher_no_specified_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        when(mockEnvironment.getArgument("folderId"))
                .thenReturn(0L);

        // Asserts
        CompletableFuture<DLFolder> asyncResult = resolvers.getDLFolderDataFetcher()
                .get(mockEnvironment);
        assertNull(asyncResult);
    }

    @Test
    public void getDLFolderDataFetcher_with_unknown_id_should_return_null() throws Exception {
        // Given
        // Nothing

        // When / Then
        useSimpleGraphQLUtil();
        when(mockEnvironment.getArgument("folderId"))
                .thenReturn(FOLDER_ID);
        when(dataLoader.load(FOLDER_ID))
                .thenReturn(CompletableFuture.supplyAsync(() -> null));

        // Asserts
        CompletableFuture<DLFolder> asyncResult = resolvers.getDLFolderDataFetcher()
                .get(mockEnvironment);
        assertNotNull(asyncResult);

        DLFolder result = asyncResult.get();
        assertNull(result);
    }

    @Test
    public void createDLFolderDataFetcher_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("userId", USER_ID);
        arguments.put("groupId", GROUP_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("mountPoint", MOUNT_POINT);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("hidden", HIDDEN);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFolder expectedResult = mock(DLFolder.class);
        expectedResult.setUserId(USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setRepositoryId(REPOSITORY_ID);
        expectedResult.setMountPoint(MOUNT_POINT);
        expectedResult.setParentFolderId(PARENT_FOLDER_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setHidden(HIDDEN);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.addFolder(eq(USER_ID), eq(GROUP_ID), eq(REPOSITORY_ID), eq(MOUNT_POINT), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), eq(HIDDEN), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFolder result = resolvers.createDLFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    public void createDLFolderDataFetcher_without_user_id_should_return_new_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("groupId", GROUP_ID);
        arguments.put("repositoryId", REPOSITORY_ID);
        arguments.put("mountPoint", MOUNT_POINT);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("hidden", HIDDEN);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFolder expectedResult = mock(DLFolder.class);
        expectedResult.setUserId(DEFAULT_USER_ID);
        expectedResult.setGroupId(GROUP_ID);
        expectedResult.setRepositoryId(REPOSITORY_ID);
        expectedResult.setMountPoint(MOUNT_POINT);
        expectedResult.setParentFolderId(PARENT_FOLDER_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setHidden(HIDDEN);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(localService.addFolder(eq(DEFAULT_USER_ID), eq(GROUP_ID), eq(REPOSITORY_ID), eq(MOUNT_POINT), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), eq(HIDDEN), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFolder result = resolvers.createDLFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = PortalException.class)
    public void createDLFolderDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(localService.addFolder(anyLong(), anyLong(), anyLong(), anyBoolean(), anyLong(), anyString(), anyString(), anyBoolean(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DLFolder result = resolvers.createDLFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void updateDLFolderDataFetcher_should_return_updated_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", FOLDER_ID);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("defaultFileEntryTypeId", DEFAULT_FILE_ENTRY_TYPE_ID);
        arguments.put("fileEntryTypeIds", FILE_ENTRY_TYPE_IDS);
        arguments.put("restrictionType", RESTRICTION_TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Long> fileEntryTypeIds = Arrays.stream(FILE_ENTRY_TYPE_IDS)
                .boxed()
                .collect(Collectors.toList());

        DLFolder expectedResult = mock(DLFolder.class);
        expectedResult.setFolderId(FOLDER_ID);
        expectedResult.setParentFolderId(PARENT_FOLDER_ID);
        expectedResult.setName(NAME);
        expectedResult.setDescription(DESCRIPTION);
        expectedResult.setDefaultFileEntryTypeId(DEFAULT_FILE_ENTRY_TYPE_ID);
        expectedResult.setRestrictionType(RESTRICTION_TYPE);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.updateFolder(eq(FOLDER_ID), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), eq(DEFAULT_FILE_ENTRY_TYPE_ID), eq(fileEntryTypeIds), eq(RESTRICTION_TYPE), any(ServiceContext.class)))
                .thenReturn(expectedResult);

        // Asserts
        DLFolder result = resolvers.updateDLFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void updateDLFolderDataFetcher_with_no_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("defaultFileEntryTypeId", DEFAULT_FILE_ENTRY_TYPE_ID);
        arguments.put("fileEntryTypeIds", FILE_ENTRY_TYPE_IDS);
        arguments.put("restrictionType", RESTRICTION_TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Long> fileEntryTypeIds = Arrays.stream(FILE_ENTRY_TYPE_IDS)
                .boxed()
                .collect(Collectors.toList());

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(0L);
        when(localService.updateFolder(eq(0L), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), eq(DEFAULT_FILE_ENTRY_TYPE_ID), eq(fileEntryTypeIds), eq(RESTRICTION_TYPE), any(ServiceContext.class)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        DLFolder result = resolvers.updateDLFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void updateDLFolderDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", 789456L);
        arguments.put("parentFolderId", PARENT_FOLDER_ID);
        arguments.put("name", NAME);
        arguments.put("description", DESCRIPTION);
        arguments.put("defaultFileEntryTypeId", DEFAULT_FILE_ENTRY_TYPE_ID);
        arguments.put("fileEntryTypeIds", FILE_ENTRY_TYPE_IDS);
        arguments.put("restrictionType", RESTRICTION_TYPE);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        List<Long> fileEntryTypeIds = Arrays.stream(FILE_ENTRY_TYPE_IDS)
                .boxed()
                .collect(Collectors.toList());

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, true);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(789456L);
        when(localService.updateFolder(eq(789456L), eq(PARENT_FOLDER_ID), eq(NAME), eq(DESCRIPTION), eq(DEFAULT_FILE_ENTRY_TYPE_ID), eq(fileEntryTypeIds), eq(RESTRICTION_TYPE), any(ServiceContext.class)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        DLFolder result = resolvers.updateDLFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = PortalException.class)
    public void updateDLFolderDataFetcher_without_args_should_throw_validation_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        // When / Then
        useMockGraphQLUtil(environment, DEFAULT_USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(FOLDER_ID);
        when(localService.updateFolder(anyLong(), anyLong(), anyString(), anyString(), anyLong(), anyList(), anyInt(), any(ServiceContext.class)))
                .thenThrow(PortalException.class);

        // Asserts
        DLFolder result = resolvers.updateDLFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test
    public void deleteDLFolderDataFetcher_should_return_deleted_object() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", FOLDER_ID);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFolder expectedResult = mock(DLFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFolder(eq(FOLDER_ID)))
                .thenReturn(expectedResult);

        // Asserts
        DLFolder result = resolvers.deleteDLFolderDataFetcher()
                .get(environment);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void deleteDLFolderDataFetcher_without_args_should_return_null_with_exception() throws Exception {
        // Given
        DataFetchingEnvironment environment = getTestEnvironment(null);

        DLFolder expectedResult = mock(DLFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, true);
        when(localService.deleteDLFolder(eq(FOLDER_ID)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        DLFolder result = resolvers.deleteDLFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }

    @Test(expected = NoSuchFolderException.class)
    public void deleteDLFolderDataFetcher_with_invalid_id_should_return_null_with_exception() throws Exception {
        // Given
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("folderId", 789456L);
        DataFetchingEnvironment environment = getTestEnvironment(arguments);

        DLFolder expectedResult = mock(DLFolder.class);
        expectedResult.setFolderId(FOLDER_ID);

        // When / Then
        useMockGraphQLUtil(environment, USER_ID, false);
        when(graphQLUtil.getLongArg(eq(environment), eq("folderId")))
                .thenReturn(789456L);
        when(localService.deleteDLFolder(eq(789456L)))
                .thenThrow(NoSuchFolderException.class);

        // Asserts
        DLFolder result = resolvers.deleteDLFolderDataFetcher()
                .get(environment);
        assertNull(result);
    }
}
