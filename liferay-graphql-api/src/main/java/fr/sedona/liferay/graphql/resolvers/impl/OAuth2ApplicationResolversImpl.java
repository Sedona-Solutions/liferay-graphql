package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.oauth2.provider.model.OAuth2Application;
import com.liferay.oauth2.provider.service.OAuth2ApplicationLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.OAuth2ApplicationBatchLoader;
import fr.sedona.liferay.graphql.resolvers.OAuth2ApplicationResolvers;
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
        service = OAuth2ApplicationResolvers.class
)
@SuppressWarnings("squid:S1192")
public class OAuth2ApplicationResolversImpl implements OAuth2ApplicationResolvers {
    private OAuth2ApplicationLocalService oAuth2ApplicationLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setOAuth2ApplicationLocalService(OAuth2ApplicationLocalService oAuth2ApplicationLocalService) {
        this.oAuth2ApplicationLocalService = oAuth2ApplicationLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<OAuth2Application>> getOAuth2ApplicationsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return oAuth2ApplicationLocalService.getOAuth2Applications(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<OAuth2Application>> getOAuth2ApplicationDataFetcher() {
        return environment -> {
            long oAuth2ApplicationId = getOAuth2ApplicationId(environment);
            if (oAuth2ApplicationId <= 0) {
                return null;
            }

            DataLoader<Long, OAuth2Application> dataLoader = environment.getDataLoader(OAuth2ApplicationBatchLoader.KEY);
            return dataLoader.load(oAuth2ApplicationId);
        };
    }

    private long getOAuth2ApplicationId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "oAuth2ApplicationId");
        if (environment.getSource() == null) {
            return argValue;
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getOAuth2ApplicationId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<OAuth2Application> getOAuth2ApplicationForClientDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String clientId = util.getStringArg(environment, "clientId");

            return oAuth2ApplicationLocalService.getOAuth2Application(companyId, clientId);
        };
    }

    @Override
    public DataFetcher<OAuth2Application> createOAuth2ApplicationDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            String userName = util.getStringArg(environment, "userName");
            List<GrantType> allowedGrantTypesList = environment.getArgument("allowedGrantTypesList");
            String clientId = util.getStringArg(environment, "clientId");
            int clientProfile = util.getIntArg(environment, "clientProfile");
            String clientSecret = util.getStringArg(environment, "clientSecret");
            String description = util.getStringArg(environment, "description");
            String[] featuresList = util.getStringArrayArg(environment, "featuresList");
            String homePageURL = util.getStringArg(environment, "homePageURL");
            long iconFileEntryId = util.getLongArg(environment, "iconFileEntryId");
            String name = util.getStringArg(environment, "name");
            String privacyPolicyURL = util.getStringArg(environment, "privacyPolicyURL");
            String[] redirectURIsList = util.getStringArrayArg(environment, "redirectURIsList");
            String[] scopeAliasesList = util.getStringArrayArg(environment, "scopeAliasesList");
            ServiceContext serviceContext = new ServiceContext();

            return oAuth2ApplicationLocalService.addOAuth2Application(
                    companyId,
                    userId,
                    userName,
                    allowedGrantTypesList,
                    clientId,
                    clientProfile,
                    clientSecret,
                    description,
                    Arrays.asList(featuresList),
                    homePageURL,
                    iconFileEntryId,
                    name,
                    privacyPolicyURL,
                    Arrays.asList(redirectURIsList),
                    Arrays.asList(scopeAliasesList),
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<OAuth2Application> updateOAuth2ApplicationDataFetcher() {
        return environment -> {
            long oAuth2ApplicationId = util.getLongArg(environment, "oAuth2ApplicationId");
            List<GrantType> allowedGrantTypesList = environment.getArgument("allowedGrantTypesList");
            String clientId = util.getStringArg(environment, "clientId");
            int clientProfile = util.getIntArg(environment, "clientProfile");
            String clientSecret = util.getStringArg(environment, "clientSecret");
            String description = util.getStringArg(environment, "description");
            String[] featuresList = util.getStringArrayArg(environment, "featuresList");
            String homePageURL = util.getStringArg(environment, "homePageURL");
            long iconFileEntryId = util.getLongArg(environment, "iconFileEntryId");
            String name = util.getStringArg(environment, "name");
            String privacyPolicyURL = util.getStringArg(environment, "privacyPolicyURL");
            String[] redirectURIsList = util.getStringArrayArg(environment, "redirectURIsList");
            long auth2ApplicationScopeAliasesId = util.getLongArg(environment, "auth2ApplicationScopeAliasesId");
            ServiceContext serviceContext = new ServiceContext();

            return oAuth2ApplicationLocalService.updateOAuth2Application(
                    oAuth2ApplicationId,
                    allowedGrantTypesList,
                    clientId,
                    clientProfile,
                    clientSecret,
                    description,
                    Arrays.asList(featuresList),
                    homePageURL,
                    iconFileEntryId,
                    name,
                    privacyPolicyURL,
                    Arrays.asList(redirectURIsList),
                    auth2ApplicationScopeAliasesId,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<OAuth2Application> deleteOAuth2ApplicationDataFetcher() {
        return environment -> {
            long oAuth2ApplicationId = util.getLongArg(environment, "oAuth2ApplicationId");

            return oAuth2ApplicationLocalService.deleteOAuth2Application(oAuth2ApplicationId);
        };
    }
}
