package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryTypeConstants;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.dynamic.data.mapping.kernel.DDMFormValues;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DLFileEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileEntryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = DLFileEntryResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DLFileEntryResolversImpl implements DLFileEntryResolvers {
    private DLFileEntryLocalService dlFileEntryLocalService;

    @Reference(unbind = "-")
    public void setDLFileEntryLocalService(DLFileEntryLocalService dlFileEntryLocalService) {
        this.dlFileEntryLocalService = dlFileEntryLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<DLFileEntry>> getDLFileEntriesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFileEntryLocalService.getDLFileEntries(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DLFileEntry>> getDLFileEntryDataFetcher() {
        return environment -> {
            long fileEntryId = getFileEntryId(environment);
            if (fileEntryId <= 0) {
                return null;
            }

            DataLoader<Long, DLFileEntry> dataLoader = environment.getDataLoader(DLFileEntryBatchLoader.KEY);
            return dataLoader.load(fileEntryId);
        };
    }

    private long getFileEntryId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "fileEntryId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getFileEntryId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<DLFileEntry> createDLFileEntryDataFetcher() {
        return environment -> {
            // TODO: Finish implementing createDLFileEntryDataFetcher
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long repositoryId = util.getLongArg(environment, "repositoryId");
            long folderId = util.getLongArg(environment, "folderId", DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
            String sourceFileName = util.getStringArg(environment, "sourceFileName");
            String mimeType = util.getStringArg(environment, "mimeType");
            String title = util.getStringArg(environment, "title");
            String description = util.getStringArg(environment, "description");
            String changeLog = util.getStringArg(environment, "changeLog");
            long fileEntryTypeId = util.getLongArg(environment, "fileEntryTypeId", DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT);
            Map<String, DDMFormValues> ddmFormValuesMap = null;
            File file = null;
            InputStream is = null;
            long size = util.getLongArg(environment, "size");
            ServiceContext serviceContext = new ServiceContext();

            return dlFileEntryLocalService.addFileEntry(
                    userId,
                    groupId,
                    repositoryId,
                    folderId,
                    sourceFileName,
                    mimeType,
                    title,
                    description,
                    changeLog,
                    fileEntryTypeId,
                    ddmFormValuesMap,
                    file,
                    is,
                    size,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DLFileEntry> updateDLFileEntryDataFetcher() {
        return environment -> {
            // TODO: Finish implementing updateDLFileEntryDataFetcher
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long fileEntryId = util.getLongArg(environment, "fileEntryId");
            String sourceFileName = util.getStringArg(environment, "sourceFileName");
            String mimeType = util.getStringArg(environment, "mimeType");
            String title = util.getStringArg(environment, "title");
            String description = util.getStringArg(environment, "description");
            String changeLog = util.getStringArg(environment, "changeLog");
            boolean majorVersion = util.getBooleanArg(environment, "majorVersion");
            long fileEntryTypeId = util.getLongArg(environment, "fileEntryTypeId", DLFileEntryTypeConstants.FILE_ENTRY_TYPE_ID_BASIC_DOCUMENT);
            Map<String, DDMFormValues> ddmFormValuesMap = null;
            File file = null;
            InputStream is = null;
            long size = util.getLongArg(environment, "size");
            ServiceContext serviceContext = new ServiceContext();

            return dlFileEntryLocalService.updateFileEntry(
                    userId,
                    fileEntryId,
                    sourceFileName,
                    mimeType,
                    title,
                    description,
                    changeLog,
                    majorVersion,
                    fileEntryTypeId,
                    ddmFormValuesMap,
                    file,
                    is,
                    size,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DLFileEntry> deleteDLFileEntryDataFetcher() {
        return environment -> {
            long fileEntryId = util.getLongArg(environment, "fileEntryId");

            return dlFileEntryLocalService.deleteDLFileEntry(fileEntryId);
        };
    }
}
