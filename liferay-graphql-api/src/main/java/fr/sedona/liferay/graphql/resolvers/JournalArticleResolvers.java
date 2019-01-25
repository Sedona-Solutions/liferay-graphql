package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.journal.model.JournalArticle;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface JournalArticleResolvers {

    DataFetcher<List<JournalArticle>> getJournalArticlesDataFetcher();

    DataFetcher<CompletableFuture<JournalArticle>> getJournalArticleDataFetcher();

    DataFetcher<JournalArticle> createJournalArticleDataFetcher();

    DataFetcher<JournalArticle> updateJournalArticleDataFetcher();

    DataFetcher<JournalArticle> deleteJournalArticleDataFetcher();
}
