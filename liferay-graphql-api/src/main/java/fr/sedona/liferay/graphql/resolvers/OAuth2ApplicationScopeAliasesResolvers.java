package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface OAuth2ApplicationScopeAliasesResolvers {

    DataFetcher<List<OAuth2ApplicationScopeAliases>> getOAuth2ApplicationScopeAliasesesDataFetcher();

    DataFetcher<CompletableFuture<OAuth2ApplicationScopeAliases>> getOAuth2ApplicationScopeAliasesDataFetcher();

    DataFetcher<OAuth2ApplicationScopeAliases> createOAuth2ApplicationScopeAliasesDataFetcher();

    DataFetcher<OAuth2ApplicationScopeAliases> deleteOAuth2ApplicationScopeAliasesDataFetcher();
}
