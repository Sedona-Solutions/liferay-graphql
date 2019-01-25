package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.journal.model.JournalFeed;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface JournalFeedResolvers {

    DataFetcher<List<JournalFeed>> getJournalFeedsDataFetcher();

    DataFetcher<CompletableFuture<JournalFeed>> getJournalFeedDataFetcher();

    DataFetcher<JournalFeed> createJournalFeedDataFetcher();

    DataFetcher<JournalFeed> updateJournalFeedDataFetcher();

    DataFetcher<JournalFeed> deleteJournalFeedDataFetcher();
}
