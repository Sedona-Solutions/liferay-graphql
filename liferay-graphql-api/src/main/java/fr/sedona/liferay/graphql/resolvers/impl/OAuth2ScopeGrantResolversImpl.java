package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.model.OAuth2ScopeGrant;
import com.liferay.oauth2.provider.service.OAuth2ScopeGrantLocalService;
import fr.sedona.liferay.graphql.loaders.OAuth2ScopeGrantBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2ScopeGrantResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = OAuth2ScopeGrantResolvers.class
)
@SuppressWarnings("squid:S1192")
public class OAuth2ScopeGrantResolversImpl implements OAuth2ScopeGrantResolvers {
    private OAuth2ScopeGrantLocalService oAuth2ScopeGrantLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setOAuth2ScopeGrantLocalService(OAuth2ScopeGrantLocalService oAuth2ScopeGrantLocalService) {
        this.oAuth2ScopeGrantLocalService = oAuth2ScopeGrantLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<OAuth2ScopeGrant>> getOAuth2ScopeGrantsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return oAuth2ScopeGrantLocalService.getOAuth2ScopeGrants(start, end);
        };
    }

    @Override
    public DataFetcher<List<OAuth2ScopeGrant>> getOAuth2ScopeGrantsForAuthorizationDataFetcher() {
        return environment -> {
            long oAuth2AuthorizationId = util.getLongArg(environment, "oAuth2AuthorizationId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return oAuth2ScopeGrantLocalService.getOAuth2AuthorizationOAuth2ScopeGrants(oAuth2AuthorizationId, start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<OAuth2ScopeGrant>> getOAuth2ScopeGrantDataFetcher() {
        return environment -> {
            long oAuth2ScopeGrantId = util.getLongArg(environment, "oAuth2ScopeGrantId");
            if (oAuth2ScopeGrantId <= 0) {
                return null;
            }

            DataLoader<Long, OAuth2ScopeGrant> dataLoader = environment.getDataLoader(OAuth2ScopeGrantBatchLoader.KEY);
            return dataLoader.load(oAuth2ScopeGrantId);
        };
    }

    @Override
    public DataFetcher<OAuth2ScopeGrant> associateOAuth2ScopeGrantWithAuthorizationDataFetcher() {
        return environment -> {
            long oAuth2AuthorizationId = util.getLongArg(environment, "oAuth2AuthorizationId");
            long oAuth2ScopeGrantId = util.getLongArg(environment, "oAuth2ScopeGrantId");

            oAuth2ScopeGrantLocalService.addOAuth2AuthorizationOAuth2ScopeGrant(
                    oAuth2AuthorizationId,
                    oAuth2ScopeGrantId);
            return oAuth2ScopeGrantLocalService.getOAuth2ScopeGrant(oAuth2ScopeGrantId);
        };
    }

    @Override
    public DataFetcher<OAuth2ScopeGrant> dissociateOAuth2ScopeGrantFromAuthorizationDataFetcher() {
        return environment -> {
            long oAuth2AuthorizationId = util.getLongArg(environment, "oAuth2AuthorizationId");
            long oAuth2ScopeGrantId = util.getLongArg(environment, "oAuth2ScopeGrantId");

            oAuth2ScopeGrantLocalService.deleteOAuth2AuthorizationOAuth2ScopeGrant(
                    oAuth2AuthorizationId,
                    oAuth2ScopeGrantId);
            return oAuth2ScopeGrantLocalService.getOAuth2ScopeGrant(oAuth2ScopeGrantId);
        };
    }
}
