package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLFolderLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DLFolderBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFolderResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component(
        immediate = true,
        service = DLFolderResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DLFolderResolversImpl implements DLFolderResolvers {
    private DLFolderLocalService dlFolderLocalService;

    @Reference(unbind = "-")
    public void setDLFolderLocalService(DLFolderLocalService dlFolderLocalService) {
        this.dlFolderLocalService = dlFolderLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<DLFolder>> getDLFoldersDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFolderLocalService.getDLFolders(start, end);
        };
    }

    @Override
    public DataFetcher<List<DLFolder>> getDLFoldersForTypeDataFetcher() {
        return environment -> {
            long fileEntryTypeId = util.getLongArg(environment, "fileEntryTypeId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFolderLocalService.getDLFileEntryTypeDLFolders(fileEntryTypeId, start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DLFolder>> getDLFolderDataFetcher() {
        return environment -> {
            long folderId = getFolderId(environment);
            if (folderId <= 0) {
                return null;
            }

            DataLoader<Long, DLFolder> dataLoader = environment.getDataLoader(DLFolderBatchLoader.KEY);
            return dataLoader.load(folderId);
        };
    }

    private long getFolderId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "folderId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof DLFolder) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("parentFolder")) {
                return ((DLFolder) source).getParentFolderId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getFolderId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<DLFolder> createDLFolderDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long repositoryId = util.getLongArg(environment, "repositoryId");
            boolean mountPoint = util.getBooleanArg(environment, "mountPoint");
            long parentFolderId = util.getLongArg(environment, "parentFolderId", DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            boolean hidden = util.getBooleanArg(environment, "hidden");
            ServiceContext serviceContext = new ServiceContext();

            return dlFolderLocalService.addFolder(
                    userId,
                    groupId,
                    repositoryId,
                    mountPoint,
                    parentFolderId,
                    name,
                    description,
                    hidden,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DLFolder> updateDLFolderDataFetcher() {
        return environment -> {
            long folderId = util.getLongArg(environment, "folderId");
            long parentFolderId = util.getLongArg(environment, "parentFolderId", DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            long defaultFileEntryTypeId = util.getLongArg(environment, "defaultFileEntryTypeId");
            long[] fileEntryTypeIds = util.getLongArrayArg(environment, "fileEntryTypeIds");
            int restrictionType = util.getIntArg(environment, "restrictionType", DLFolderConstants.RESTRICTION_TYPE_INHERIT);
            ServiceContext serviceContext = new ServiceContext();

            return dlFolderLocalService.updateFolder(
                    folderId,
                    parentFolderId,
                    name,
                    description,
                    defaultFileEntryTypeId,
                    Arrays.stream(fileEntryTypeIds)
                            .boxed()
                            .collect(Collectors.toList()),
                    restrictionType,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DLFolder> deleteDLFolderDataFetcher() {
        return environment -> {
            long folderId = util.getLongArg(environment, "folderId");

            return dlFolderLocalService.deleteDLFolder(folderId);
        };
    }
}
