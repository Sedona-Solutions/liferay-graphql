package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.dynamic.data.mapping.model.DDMContent;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DDMContentResolvers {

    DataFetcher<List<DDMContent>> getDDMContentsDataFetcher();

    DataFetcher<CompletableFuture<DDMContent>> getDDMContentDataFetcher();

    DataFetcher<DDMContent> createDDMContentDataFetcher();

    DataFetcher<DDMContent> updateDDMContentDataFetcher();

    DataFetcher<DDMContent> deleteDDMContentDataFetcher();
}
