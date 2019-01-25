package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.asset.kernel.model.AssetEntry;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface AssetEntryResolvers {

    DataFetcher<List<AssetEntry>> getAssetEntriesDataFetcher();

    DataFetcher<List<AssetEntry>> getAssetEntriesWithCategoryDataFetcher();

    DataFetcher<List<AssetEntry>> getAssetEntriesWithTagDataFetcher();

    DataFetcher<CompletableFuture<AssetEntry>> getAssetEntryDataFetcher();

    DataFetcher<AssetEntry> associateAssetEntryWithCategoryDataFetcher();

    DataFetcher<AssetEntry> dissociateAssetEntryFromCategoryDataFetcher();

    DataFetcher<AssetEntry> associateAssetEntryWithTagDataFetcher();

    DataFetcher<AssetEntry> dissociateAssetEntryFromTagDataFetcher();
}
