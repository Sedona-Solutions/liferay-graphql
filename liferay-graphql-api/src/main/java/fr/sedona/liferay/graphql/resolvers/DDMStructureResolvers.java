package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface DDMStructureResolvers {

    DataFetcher<List<DDMStructure>> getDDMStructuresDataFetcher();

    DataFetcher<CompletableFuture<DDMStructure>> getDDMStructureDataFetcher();

    DataFetcher<DDMStructure> getDDMStructureByKeyDataFetcher();

    DataFetcher<DDMStructure> createDDMStructureDataFetcher();

    DataFetcher<DDMStructure> createDDMStructureForJournalArticleDataFetcher();

    DataFetcher<DDMStructure> updateDDMStructureDataFetcher();

    DataFetcher<DDMStructure> updateDDMStructureForJournalArticleDataFetcher();

    DataFetcher<DDMStructure> deleteDDMStructureDataFetcher();
}
