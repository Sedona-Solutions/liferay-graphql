package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.asset.kernel.model.AssetLink;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface AssetLinkResolvers {

    DataFetcher<List<AssetLink>> getAssetLinksDataFetcher();

    DataFetcher<CompletableFuture<AssetLink>> getAssetLinkDataFetcher();

    DataFetcher<AssetLink> createAssetLinkDataFetcher();

    DataFetcher<AssetLink> updateAssetLinkDataFetcher();

    DataFetcher<AssetLink> deleteAssetLinkDataFetcher();
}
