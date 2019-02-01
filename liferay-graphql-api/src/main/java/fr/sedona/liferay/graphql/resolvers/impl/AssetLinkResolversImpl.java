package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.asset.kernel.model.AssetLinkConstants;
import com.liferay.asset.kernel.service.AssetLinkLocalService;
import fr.sedona.liferay.graphql.loaders.AssetLinkBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetLinkResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = AssetLinkResolvers.class
)
@SuppressWarnings("squid:S1192")
public class AssetLinkResolversImpl implements AssetLinkResolvers {
    private AssetLinkLocalService assetLinkLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setAssetLinkLocalService(AssetLinkLocalService assetLinkLocalService) {
        this.assetLinkLocalService = assetLinkLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<AssetLink>> getAssetLinksDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetLinkLocalService.getAssetLinks(start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<AssetLink>> getAssetLinkDataFetcher() {
        return environment -> {
            long linkId = util.getLongArg(environment, "linkId");
            if (linkId <= 0) {
                return null;
            }

            DataLoader<Long, AssetLink> dataLoader = environment.getDataLoader(AssetLinkBatchLoader.KEY);
            return dataLoader.load(linkId);
        };
    }

    @Override
    public DataFetcher<AssetLink> createAssetLinkDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long entryId1 = util.getLongArg(environment, "entryId1");
            long entryId2 = util.getLongArg(environment, "entryId2");
            int type = util.getIntArg(environment, "type", AssetLinkConstants.TYPE_RELATED);
            int weight = util.getIntArg(environment, "weight");

            return assetLinkLocalService.addLink(
                    userId,
                    entryId1,
                    entryId2,
                    type,
                    weight);
        };
    }

    @Override
    public DataFetcher<AssetLink> updateAssetLinkDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long entryId1 = util.getLongArg(environment, "entryId1");
            long entryId2 = util.getLongArg(environment, "entryId2");
            int type = util.getIntArg(environment, "type", AssetLinkConstants.TYPE_RELATED);
            int weight = util.getIntArg(environment, "weight");

            return assetLinkLocalService.updateLink(
                    userId,
                    entryId1,
                    entryId2,
                    type,
                    weight);
        };
    }

    @Override
    public DataFetcher<AssetLink> deleteAssetLinkDataFetcher() {
        return environment -> {
            long linkId = util.getLongArg(environment, "linkId");

            return assetLinkLocalService.deleteAssetLink(linkId);
        };
    }
}
