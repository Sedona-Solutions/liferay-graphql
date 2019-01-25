package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.oauth2.provider.model.OAuth2Authorization;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface OAuth2AuthorizationResolvers {

    DataFetcher<List<OAuth2Authorization>> getOAuth2AuthorizationsDataFetcher();

    DataFetcher<List<OAuth2Authorization>> getOAuth2AuthorizationsForGrantDataFetcher();

    DataFetcher<List<OAuth2Authorization>> getOAuth2AuthorizationsForUserDataFetcher();

    DataFetcher<CompletableFuture<OAuth2Authorization>> getOAuth2AuthorizationDataFetcher();

    DataFetcher<OAuth2Authorization> getOAuth2AuthorizationByAccessTokenDataFetcher();

    DataFetcher<OAuth2Authorization> getOAuth2AuthorizationByRefreshTokenDataFetcher();

    DataFetcher<OAuth2Authorization> createOAuth2AuthorizationDataFetcher();

    DataFetcher<OAuth2Authorization> deleteOAuth2AuthorizationDataFetcher();
}
