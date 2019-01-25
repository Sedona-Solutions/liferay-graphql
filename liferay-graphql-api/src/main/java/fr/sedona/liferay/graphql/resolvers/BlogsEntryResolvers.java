package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.blogs.model.BlogsEntry;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface BlogsEntryResolvers {

    DataFetcher<List<BlogsEntry>> getBlogsEntriesDataFetcher();

    DataFetcher<CompletableFuture<BlogsEntry>> getBlogsEntryDataFetcher();

    DataFetcher<BlogsEntry> createBlogsEntryDataFetcher();

    DataFetcher<BlogsEntry> updateBlogsEntryDataFetcher();

    DataFetcher<BlogsEntry> deleteBlogsEntryDataFetcher();
}
