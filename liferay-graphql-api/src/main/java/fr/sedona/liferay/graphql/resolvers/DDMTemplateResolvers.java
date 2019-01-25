package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DDMTemplateResolvers {

    DataFetcher<List<DDMTemplate>> getDDMTemplatesDataFetcher();

    DataFetcher<CompletableFuture<DDMTemplate>> getDDMTemplateDataFetcher();

    DataFetcher<DDMTemplate> getDDMTemplateByKeyDataFetcher();

    DataFetcher<DDMTemplate> createDDMTemplateDataFetcher();

    DataFetcher<DDMTemplate> updateDDMTemplateDataFetcher();

    DataFetcher<DDMTemplate> deleteDDMTemplateDataFetcher();
}
