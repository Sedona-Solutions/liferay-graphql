package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.oauth2.provider.model.OAuth2Application;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface OAuth2ApplicationResolvers {

    DataFetcher<List<OAuth2Application>> getOAuth2ApplicationsDataFetcher();

    DataFetcher<CompletableFuture<OAuth2Application>> getOAuth2ApplicationDataFetcher();

    DataFetcher<OAuth2Application> getOAuth2ApplicationForClientDataFetcher();

    DataFetcher<OAuth2Application> createOAuth2ApplicationDataFetcher();

    DataFetcher<OAuth2Application> updateOAuth2ApplicationDataFetcher();

    DataFetcher<OAuth2Application> deleteOAuth2ApplicationDataFetcher();
}
