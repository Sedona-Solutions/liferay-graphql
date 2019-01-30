package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.model.OAuth2Authorization;
import com.liferay.oauth2.provider.service.OAuth2AuthorizationLocalService;
import fr.sedona.liferay.graphql.loaders.OAuth2AuthorizationBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2AuthorizationResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = OAuth2AuthorizationResolvers.class
)
@SuppressWarnings("squid:S1192")
public class OAuth2AuthorizationResolversImpl implements OAuth2AuthorizationResolvers {
    private OAuth2AuthorizationLocalService oAuth2AuthorizationLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setOAuth2AuthorizationLocalService(OAuth2AuthorizationLocalService oAuth2AuthorizationLocalService) {
        this.oAuth2AuthorizationLocalService = oAuth2AuthorizationLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<OAuth2Authorization>> getOAuth2AuthorizationsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return oAuth2AuthorizationLocalService.getOAuth2Authorizations(start, end);
        };
    }

    @Override
    public DataFetcher<List<OAuth2Authorization>> getOAuth2AuthorizationsForGrantDataFetcher() {
        return environment -> {
            long oAuth2ScopeGrantId = util.getLongArg(environment, "oAuth2ScopeGrantId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return oAuth2AuthorizationLocalService.getOAuth2ScopeGrantOAuth2Authorizations(oAuth2ScopeGrantId, start, end);
        };
    }

    @Override
    public DataFetcher<List<OAuth2Authorization>> getOAuth2AuthorizationsForUserDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return oAuth2AuthorizationLocalService.getUserOAuth2Authorizations(userId, start, end, null);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<OAuth2Authorization>> getOAuth2AuthorizationDataFetcher() {
        return environment -> {
            long oAuth2AuthorizationId = util.getLongArg(environment, "oAuth2AuthorizationId");
            if (oAuth2AuthorizationId <= 0) {
                return null;
            }

            DataLoader<Long, OAuth2Authorization> dataLoader = environment.getDataLoader(OAuth2AuthorizationBatchLoader.KEY);
            return dataLoader.load(oAuth2AuthorizationId);
        };
    }

    @Override
    public DataFetcher<OAuth2Authorization> getOAuth2AuthorizationByAccessTokenDataFetcher() {
        return environment -> {
            String accessToken = util.getStringArg(environment, "accessToken");

            return oAuth2AuthorizationLocalService.getOAuth2AuthorizationByAccessTokenContent(accessToken);
        };
    }

    @Override
    public DataFetcher<OAuth2Authorization> getOAuth2AuthorizationByRefreshTokenDataFetcher() {
        return environment -> {
            String refreshToken = util.getStringArg(environment, "refreshToken");

            return oAuth2AuthorizationLocalService.getOAuth2AuthorizationByRefreshTokenContent(refreshToken);
        };
    }

    @Override
    public DataFetcher<OAuth2Authorization> createOAuth2AuthorizationDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String userName = util.getStringArg(environment, "userName");
            long oAuth2ApplicationId = util.getLongArg(environment, "oAuth2ApplicationId");
            long oAuth2ApplicationScopeAliasesId = util.getLongArg(environment, "oAuth2ApplicationScopeAliasesId");
            String accessTokenContent = util.getStringArg(environment, "accessTokenContent");
            Date accessTokenCreateDate = util.getDateArg(environment, "accessTokenCreateDate");
            Date accessTokenExpirationDate = util.getDateArg(environment, "accessTokenExpirationDate");
            String remoteIPInfo = util.getStringArg(environment, "remoteIPInfo");
            String refreshTokenContent = util.getStringArg(environment, "refreshTokenContent");
            Date refreshTokenCreateDate = util.getDateArg(environment, "refreshTokenCreateDate");
            Date refreshTokenExpirationDate = util.getDateArg(environment, "refreshTokenExpirationDate");

            return oAuth2AuthorizationLocalService.addOAuth2Authorization(
                    companyId,
                    userId,
                    userName,
                    oAuth2ApplicationId,
                    oAuth2ApplicationScopeAliasesId,
                    accessTokenContent,
                    accessTokenCreateDate,
                    accessTokenExpirationDate,
                    remoteIPInfo,
                    refreshTokenContent,
                    refreshTokenCreateDate,
                    refreshTokenExpirationDate);
        };
    }

    @Override
    public DataFetcher<OAuth2Authorization> deleteOAuth2AuthorizationDataFetcher() {
        return environment -> {
            long oAuth2AuthorizationId = util.getLongArg(environment, "oAuth2AuthorizationId");

            return oAuth2AuthorizationLocalService.deleteOAuth2Authorization(oAuth2AuthorizationId);
        };
    }
}
