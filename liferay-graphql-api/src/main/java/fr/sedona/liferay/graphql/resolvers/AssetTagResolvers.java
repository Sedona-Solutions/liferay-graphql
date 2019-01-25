package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.asset.kernel.model.AssetTag;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface AssetTagResolvers {

    DataFetcher<List<AssetTag>> getAssetTagsDataFetcher();

    DataFetcher<List<AssetTag>> getAssetTagsForAssetDataFetcher();

    DataFetcher<CompletableFuture<AssetTag>> getAssetTagDataFetcher();

    DataFetcher<AssetTag> createAssetTagDataFetcher();

    DataFetcher<AssetTag> updateAssetTagDataFetcher();

    DataFetcher<AssetTag> deleteAssetTagDataFetcher();
}
