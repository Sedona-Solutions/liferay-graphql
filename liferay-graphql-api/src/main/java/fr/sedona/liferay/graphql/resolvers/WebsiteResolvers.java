package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Website;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface WebsiteResolvers {

    DataFetcher<List<Website>> getWebsitesDataFetcher();

    DataFetcher<List<Website>> getWebsitesForEntityDataFetcher();

    DataFetcher<CompletableFuture<Website>> getWebsiteDataFetcher();

    DataFetcher<Website> createWebsiteDataFetcher();

    DataFetcher<Website> updateWebsiteDataFetcher();

    DataFetcher<Website> deleteWebsiteDataFetcher();
}
