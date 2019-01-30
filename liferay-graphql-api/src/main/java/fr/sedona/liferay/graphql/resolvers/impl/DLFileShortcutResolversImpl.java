package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFileShortcut;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLFileShortcutLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DLFileShortcutBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileShortcutResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = DLFileShortcutResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DLFileShortcutResolversImpl implements DLFileShortcutResolvers {
    private DLFileShortcutLocalService dlFileShortcutLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setDLFileShortcutLocalService(DLFileShortcutLocalService dlFileShortcutLocalService) {
        this.dlFileShortcutLocalService = dlFileShortcutLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<DLFileShortcut>> getDLFileShortcutsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFileShortcutLocalService.getDLFileShortcuts(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DLFileShortcut>> getDLFileShortcutDataFetcher() {
        return environment -> {
            long fileShortcutId = util.getLongArg(environment, "fileShortcutId");
            if (fileShortcutId <= 0) {
                return null;
            }

            DataLoader<Long, DLFileShortcut> dataLoader = environment.getDataLoader(DLFileShortcutBatchLoader.KEY);
            return dataLoader.load(fileShortcutId);
        };
    }

    @Override
    public DataFetcher<DLFileShortcut> createDLFileShortcutDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long repositoryId = util.getLongArg(environment, "repositoryId");
            long folderId = util.getLongArg(environment, "folderId", DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
            long toFileEntryId = util.getLongArg(environment, "toFileEntryId");
            ServiceContext serviceContext = new ServiceContext();

            return dlFileShortcutLocalService.addFileShortcut(
                    userId,
                    groupId,
                    repositoryId,
                    folderId,
                    toFileEntryId,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DLFileShortcut> updateDLFileShortcutDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long fileShortcutId = util.getLongArg(environment, "fileShortcutId");
            long repositoryId = util.getLongArg(environment, "repositoryId");
            long folderId = util.getLongArg(environment, "folderId", DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
            long toFileEntryId = util.getLongArg(environment, "toFileEntryId");
            ServiceContext serviceContext = new ServiceContext();

            return dlFileShortcutLocalService.updateFileShortcut(
                    userId,
                    fileShortcutId,
                    repositoryId,
                    folderId,
                    toFileEntryId,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DLFileShortcut> deleteDLFileShortcutDataFetcher() {
        return environment -> {
            long fileShortcutId = util.getLongArg(environment, "fileShortcutId");

            return dlFileShortcutLocalService.deleteDLFileShortcut(fileShortcutId);
        };
    }
}
