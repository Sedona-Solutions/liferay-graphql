package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.asset.kernel.model.AssetVocabulary;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface AssetVocabularyResolvers {

    DataFetcher<List<AssetVocabulary>> getAssetVocabulariesDataFetcher();

    DataFetcher<CompletableFuture<AssetVocabulary>> getAssetVocabularyDataFetcher();

    DataFetcher<AssetVocabulary> createAssetVocabularyDataFetcher();

    DataFetcher<AssetVocabulary> updateAssetVocabularyDataFetcher();

    DataFetcher<AssetVocabulary> deleteAssetVocabularyDataFetcher();
}
