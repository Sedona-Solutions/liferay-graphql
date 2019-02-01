package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.service.DLFileVersionLocalService;
import fr.sedona.liferay.graphql.loaders.DLFileVersionBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileVersionResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = DLFileVersionResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DLFileVersionResolversImpl implements DLFileVersionResolvers {
    private DLFileVersionLocalService dlFileVersionLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setDLFileVersionLocalService(DLFileVersionLocalService dlFileVersionLocalService) {
        this.dlFileVersionLocalService = dlFileVersionLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<DLFileVersion>> getDLFileVersionsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFileVersionLocalService.getDLFileVersions(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DLFileVersion>> getDLFileVersionDataFetcher() {
        return environment -> {
            long fileVersionId = getFileVersionId(environment);
            if (fileVersionId <= 0) {
                return null;
            }

            DataLoader<Long, DLFileVersion> dataLoader = environment.getDataLoader(DLFileVersionBatchLoader.KEY);
            return dataLoader.load(fileVersionId);
        };
    }

    private long getFileVersionId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "fileVersionId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getFileVersionId");
        } catch (Exception e) {
            return argValue;
        }
    }
}
