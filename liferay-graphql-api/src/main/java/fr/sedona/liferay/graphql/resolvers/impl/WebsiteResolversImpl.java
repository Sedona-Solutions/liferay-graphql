package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.portal.kernel.model.Website;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.WebsiteLocalService;
import fr.sedona.liferay.graphql.loaders.WebsiteBatchLoader;
import fr.sedona.liferay.graphql.resolvers.WebsiteResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = WebsiteResolvers.class
)
@SuppressWarnings("squid:S1192")
public class WebsiteResolversImpl implements WebsiteResolvers {
    private WebsiteLocalService websiteLocalService;

    @Reference(unbind = "-")
    public void setWebsiteLocalService(WebsiteLocalService websiteLocalService) {
        this.websiteLocalService = websiteLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<Website>> getWebsitesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return websiteLocalService.getWebsites(start, end);
        };
    }

    @Override
    public DataFetcher<List<Website>> getWebsitesForEntityDataFetcher() {
        return environment -> {
            long companyId = util.getLongArg(environment, "companyId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");

            return websiteLocalService.getWebsites(companyId, className, classPK);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<Website>> getWebsiteDataFetcher() {
        return environment -> {
            long websiteId = util.getLongArg(environment, "websiteId");
            if (websiteId <= 0) {
                return null;
            }

            DataLoader<Long, Website> dataLoader = environment.getDataLoader(WebsiteBatchLoader.KEY);
            return dataLoader.load(websiteId);
        };
    }

    @Override
    public DataFetcher<Website> createWebsiteDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId");
            String className = util.getStringArg(environment, "className");
            long classPK = util.getLongArg(environment, "classPK");
            String url = util.getStringArg(environment, "url");
            long typeId = util.getLongArg(environment, "typeId");
            boolean primary = util.getBooleanArg(environment, "primary");
            ServiceContext serviceContext = new ServiceContext();

            return websiteLocalService.addWebsite(
                    userId,
                    className,
                    classPK,
                    url,
                    typeId,
                    primary,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<Website> updateWebsiteDataFetcher() {
        return environment -> {
            long websiteId = util.getLongArg(environment, "websiteId");
            String url = util.getStringArg(environment, "url");
            long typeId = util.getLongArg(environment, "typeId");
            boolean primary = util.getBooleanArg(environment, "primary");

            return websiteLocalService.updateWebsite(
                    websiteId,
                    url,
                    typeId,
                    primary);
        };
    }

    @Override
    public DataFetcher<Website> deleteWebsiteDataFetcher() {
        return environment -> {
            long websiteId = util.getLongArg(environment, "websiteId");

            return websiteLocalService.deleteWebsite(websiteId);
        };
    }
}
