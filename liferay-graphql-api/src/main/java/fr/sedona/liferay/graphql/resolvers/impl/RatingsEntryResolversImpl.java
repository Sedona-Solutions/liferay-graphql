package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.ratings.kernel.model.RatingsEntry;
import com.liferay.ratings.kernel.service.RatingsEntryLocalService;
import fr.sedona.liferay.graphql.loaders.RatingsEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.RatingsEntryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = RatingsEntryResolvers.class
)
@SuppressWarnings("squid:S1192")
public class RatingsEntryResolversImpl implements RatingsEntryResolvers {
    private RatingsEntryLocalService ratingsEntryLocalService;

    @Reference(unbind = "-")
    public void setRatingsEntryLocalService(RatingsEntryLocalService ratingsEntryLocalService) {
        this.ratingsEntryLocalService = ratingsEntryLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<RatingsEntry>> getRatingsEntriesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return ratingsEntryLocalService.getRatingsEntries(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<RatingsEntry>> getRatingsEntryDataFetcher() {
        return environment -> {
            long entryId = util.getLongArg(environment, "entryId");
            if (entryId <= 0) {
                return null;
            }

            DataLoader<Long, RatingsEntry> dataLoader = environment.getDataLoader(RatingsEntryBatchLoader.KEY);
            return dataLoader.load(entryId);
        };
    }

    @Override
    public DataFetcher<RatingsEntry> createRatingsEntryDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            double score = util.getDoubleArg(environment, "score");
            ServiceContext serviceContext = new ServiceContext();

            return ratingsEntryLocalService.updateEntry(
                    userId,
                    className,
                    classPK,
                    score,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<RatingsEntry> deleteRatingsEntryDataFetcher() {
        return environment -> {
            long entryId = util.getLongArg(environment, "entryId");

            return ratingsEntryLocalService.deleteRatingsEntry(entryId);
        };
    }
}
