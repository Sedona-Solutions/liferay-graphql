package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases;
import com.liferay.oauth2.provider.service.OAuth2ApplicationScopeAliasesLocalService;
import fr.sedona.liferay.graphql.loaders.OAuth2ApplicationScopeAliasesBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2ApplicationScopeAliasesResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = OAuth2ApplicationScopeAliasesResolvers.class
)
@SuppressWarnings("squid:S1192")
public class OAuth2ApplicationScopeAliasesResolversImpl implements OAuth2ApplicationScopeAliasesResolvers {
    private OAuth2ApplicationScopeAliasesLocalService oAuth2ApplicationScopeAliasesLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setOAuth2ApplicationScopeAliasesLocalService(OAuth2ApplicationScopeAliasesLocalService oAuth2ApplicationScopeAliasesLocalService) {
        this.oAuth2ApplicationScopeAliasesLocalService = oAuth2ApplicationScopeAliasesLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<OAuth2ApplicationScopeAliases>> getOAuth2ApplicationScopeAliasesesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return oAuth2ApplicationScopeAliasesLocalService.getOAuth2ApplicationScopeAliaseses(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<OAuth2ApplicationScopeAliases>> getOAuth2ApplicationScopeAliasesDataFetcher() {
        return environment -> {
            long oAuth2ApplicationScopeAliasesId = getOAuth2ApplicationScopeAliasesId(environment);
            if (oAuth2ApplicationScopeAliasesId <= 0) {
                return null;
            }

            DataLoader<Long, OAuth2ApplicationScopeAliases> dataLoader = environment.getDataLoader(OAuth2ApplicationScopeAliasesBatchLoader.KEY);
            return dataLoader.load(oAuth2ApplicationScopeAliasesId);
        };
    }

    private long getOAuth2ApplicationScopeAliasesId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "oAuth2ApplicationScopeAliasesId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getOAuth2ApplicationScopeAliasesId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<OAuth2ApplicationScopeAliases> createOAuth2ApplicationScopeAliasesDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String userName = util.getStringArg(environment, "userName");
            long oAuth2ApplicationId = util.getLongArg(environment, "oAuth2ApplicationId");
            String[] scopeAliasesList = util.getStringArrayArg(environment, "scopeAliasesList");

            return oAuth2ApplicationScopeAliasesLocalService.addOAuth2ApplicationScopeAliases(
                    companyId,
                    userId,
                    userName,
                    oAuth2ApplicationId,
                    Arrays.asList(scopeAliasesList));
        };
    }

    @Override
    public DataFetcher<OAuth2ApplicationScopeAliases> deleteOAuth2ApplicationScopeAliasesDataFetcher() {
        return environment -> {
            long oAuth2ApplicationScopeAliasesId = util.getLongArg(environment, "oAuth2ApplicationScopeAliasesId");

            return oAuth2ApplicationScopeAliasesLocalService.deleteOAuth2ApplicationScopeAliases(oAuth2ApplicationScopeAliasesId);
        };
    }
}
