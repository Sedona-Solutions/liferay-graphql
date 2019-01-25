package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.message.boards.kernel.model.MBCategory;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface MBCategoryResolvers {

    DataFetcher<List<MBCategory>> getMBCategoriesDataFetcher();

    DataFetcher<CompletableFuture<MBCategory>> getMBCategoryDataFetcher();

    DataFetcher<MBCategory> createMBCategoryDataFetcher();

    DataFetcher<MBCategory> updateMBCategoryDataFetcher();

    DataFetcher<MBCategory> deleteMBCategoryDataFetcher();
}
