package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.oauth2.provider.model.OAuth2ScopeGrant;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface OAuth2ScopeGrantResolvers {

    DataFetcher<List<OAuth2ScopeGrant>> getOAuth2ScopeGrantsDataFetcher();

    DataFetcher<List<OAuth2ScopeGrant>> getOAuth2ScopeGrantsForAuthorizationDataFetcher();

    DataFetcher<CompletableFuture<OAuth2ScopeGrant>> getOAuth2ScopeGrantDataFetcher();

    DataFetcher<OAuth2ScopeGrant> associateOAuth2ScopeGrantWithAuthorizationDataFetcher();

    DataFetcher<OAuth2ScopeGrant> dissociateOAuth2ScopeGrantFromAuthorizationDataFetcher();
}
