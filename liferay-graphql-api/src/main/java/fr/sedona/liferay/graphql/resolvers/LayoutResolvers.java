package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Layout;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface LayoutResolvers {

    DataFetcher<List<Layout>> getLayoutsDataFetcher();

    DataFetcher<List<Layout>> getLayoutsForGroupDataFetcher();

    DataFetcher<CompletableFuture<Layout>> getLayoutDataFetcher();

    DataFetcher<Layout> createLayoutDataFetcher();

    DataFetcher<Layout> updateLayoutDataFetcher();

    DataFetcher<Layout> deleteLayoutDataFetcher();
}
