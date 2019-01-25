package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.document.library.kernel.service.DLFileEntryMetadataLocalService;
import fr.sedona.liferay.graphql.loaders.DLFileEntryMetadataBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DLFileEntryMetadataResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = DLFileEntryMetadataResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DLFileEntryMetadataResolversImpl implements DLFileEntryMetadataResolvers {
    private DLFileEntryMetadataLocalService dlFileEntryMetadataLocalService;

    @Reference(unbind = "-")
    public void setDLFileEntryMetadataLocalService(DLFileEntryMetadataLocalService dlFileEntryMetadataLocalService) {
        this.dlFileEntryMetadataLocalService = dlFileEntryMetadataLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<DLFileEntryMetadata>> getDLFileEntryMetadatasDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return dlFileEntryMetadataLocalService.getDLFileEntryMetadatas(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DLFileEntryMetadata>> getDLFileEntryMetadataDataFetcher() {
        return environment -> {
            long fileEntryMetadataId = util.getLongArg(environment, "fileEntryMetadataId");
            if (fileEntryMetadataId <= 0) {
                return null;
            }

            DataLoader<Long, DLFileEntryMetadata> dataLoader = environment.getDataLoader(DLFileEntryMetadataBatchLoader.KEY);
            return dataLoader.load(fileEntryMetadataId);
        };
    }
}
