package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.asset.kernel.model.AssetCategory;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface AssetCategoryResolvers {

    DataFetcher<List<AssetCategory>> getAssetCategoriesDataFetcher();

    DataFetcher<List<AssetCategory>> getAssetCategoriesForAssetDataFetcher();

    DataFetcher<CompletableFuture<AssetCategory>> getAssetCategoryDataFetcher();

    DataFetcher<AssetCategory> createAssetCategoryDataFetcher();

    DataFetcher<AssetCategory> updateAssetCategoryDataFetcher();

    DataFetcher<AssetCategory> deleteAssetCategoryDataFetcher();
}
