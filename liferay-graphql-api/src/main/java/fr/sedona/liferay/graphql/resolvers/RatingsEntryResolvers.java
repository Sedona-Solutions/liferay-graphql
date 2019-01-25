package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.ratings.kernel.model.RatingsEntry;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface RatingsEntryResolvers {

    DataFetcher<List<RatingsEntry>> getRatingsEntriesDataFetcher();

    DataFetcher<CompletableFuture<RatingsEntry>> getRatingsEntryDataFetcher();

    DataFetcher<RatingsEntry> createRatingsEntryDataFetcher();

    DataFetcher<RatingsEntry> deleteRatingsEntryDataFetcher();
}
