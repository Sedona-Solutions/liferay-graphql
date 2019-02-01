package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.dynamic.data.mapping.model.DDMContent;
import com.liferay.dynamic.data.mapping.service.DDMContentLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.DDMContentBatchLoader;
import fr.sedona.liferay.graphql.resolvers.DDMContentResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = DDMContentResolvers.class
)
@SuppressWarnings("squid:S1192")
public class DDMContentResolversImpl implements DDMContentResolvers {
    private DDMContentLocalService ddmContentLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setDDMContentLocalService(DDMContentLocalService ddmContentLocalService) {
        this.ddmContentLocalService = ddmContentLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<DDMContent>> getDDMContentsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return ddmContentLocalService.getDDMContents(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<DDMContent>> getDDMContentDataFetcher() {
        return environment -> {
            long contentId = util.getLongArg(environment, "contentId");
            if (contentId <= 0) {
                return null;
            }

            DataLoader<Long, DDMContent> dataLoader = environment.getDataLoader(DDMContentBatchLoader.KEY);
            return dataLoader.load(contentId);
        };
    }

    @Override
    public DataFetcher<DDMContent> createDDMContentDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            String data = util.getStringArg(environment, "data");
            ServiceContext serviceContext = new ServiceContext();

            return ddmContentLocalService.addContent(
                    userId,
                    groupId,
                    name,
                    description,
                    data,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMContent> updateDDMContentDataFetcher() {
        return environment -> {
            long contentId = util.getLongArg(environment, "contentId");
            String name = util.getStringArg(environment, "name");
            String description = util.getStringArg(environment, "description");
            String data = util.getStringArg(environment, "data");
            ServiceContext serviceContext = new ServiceContext();

            return ddmContentLocalService.updateContent(
                    contentId,
                    name,
                    description,
                    data,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<DDMContent> deleteDDMContentDataFetcher() {
        return environment -> {
            long contentId = util.getLongArg(environment, "contentId");

            return ddmContentLocalService.deleteDDMContent(contentId);
        };
    }
}
