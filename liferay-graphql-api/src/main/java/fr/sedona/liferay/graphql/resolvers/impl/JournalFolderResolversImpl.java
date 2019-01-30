package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.model.JournalFolderConstants;
import com.liferay.journal.service.JournalFolderLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.JournalFolderBatchLoader;
import fr.sedona.liferay.graphql.resolvers.JournalFolderResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = JournalFolderResolvers.class
)
@SuppressWarnings("squid:S1192")
public class JournalFolderResolversImpl implements JournalFolderResolvers {
    private JournalFolderLocalService journalFolderLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setJournalFolderLocalService(JournalFolderLocalService journalFolderLocalService) {
        this.journalFolderLocalService = journalFolderLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<JournalFolder>> getJournalFoldersDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return journalFolderLocalService.getJournalFolders(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<JournalFolder>> getJournalFolderDataFetcher() {
        return environment -> {
            long folderId = getFolderId(environment);
            if (folderId <= 0) {
                return null;
            }

            DataLoader<Long, JournalFolder> dataLoader = environment.getDataLoader(JournalFolderBatchLoader.KEY);
            return dataLoader.load(folderId);
        };
    }

    private long getFolderId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "folderId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof JournalFolder) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("parentFolder")) {
                return ((JournalFolder) source).getParentFolderId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getFolderId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<JournalFolder> createJournalFolderDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long parentFolderId = util.getLongArg(environment, "parentFolderId", JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            ServiceContext serviceContext = new ServiceContext();

            return journalFolderLocalService.addFolder(
                    userId,
                    groupId,
                    parentFolderId,
                    name,
                    description,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<JournalFolder> updateJournalFolderDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long folderId = util.getLongArg(environment, "folderId");
            long parentFolderId = util.getLongArg(environment, "parentFolderId", JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID);
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            ServiceContext serviceContext = new ServiceContext();

            return journalFolderLocalService.updateFolder(
                    userId,
                    groupId,
                    folderId,
                    parentFolderId,
                    name,
                    description,
                    false,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<JournalFolder> deleteJournalFolderDataFetcher() {
        return environment -> {
            long folderId = util.getLongArg(environment, "folderId");

            return journalFolderLocalService.deleteJournalFolder(folderId);
        };
    }
}
