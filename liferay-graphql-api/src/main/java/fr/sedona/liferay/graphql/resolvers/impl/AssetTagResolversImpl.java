package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetTagLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.AssetTagBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetTagResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.schema.DataFetcher;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = AssetTagResolvers.class
)
@SuppressWarnings("squid:S1192")
public class AssetTagResolversImpl implements AssetTagResolvers {
    private AssetTagLocalService assetTagLocalService;

    @Reference(unbind = "-")
    public void setAssetTagLocalService(AssetTagLocalService assetTagLocalService) {
        this.assetTagLocalService = assetTagLocalService;
    }

    @Reference
    private GraphQLUtil util;

    @Override
    public DataFetcher<List<AssetTag>> getAssetTagsDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetTagLocalService.getAssetTags(start, end);
        };
    }

    @Override
    public DataFetcher<List<AssetTag>> getAssetTagsForAssetDataFetcher() {
        return environment -> {
            long entryId = util.getLongArg(environment, "entryId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetTagLocalService.getAssetEntryAssetTags(entryId, start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<AssetTag>> getAssetTagDataFetcher() {
        return environment -> {
            long tagId = util.getLongArg(environment, "tagId");
            if (tagId <= 0) {
                return null;
            }

            DataLoader<Long, AssetTag> dataLoader = environment.getDataLoader(AssetTagBatchLoader.KEY);
            return dataLoader.load(tagId);
        };
    }

    @Override
    public DataFetcher<AssetTag> createAssetTagDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            String name = util.getStringArg(environment, "name");
            ServiceContext serviceContext = new ServiceContext();

            return assetTagLocalService.addTag(
                    userId,
                    groupId,
                    name,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<AssetTag> updateAssetTagDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long tagId = util.getLongArg(environment, "tagId");
            String name = util.getStringArg(environment, "name");
            ServiceContext serviceContext = new ServiceContext();

            return assetTagLocalService.updateTag(
                    userId,
                    tagId,
                    name,
                    serviceContext
            );
        };
    }

    @Override
    public DataFetcher<AssetTag> deleteAssetTagDataFetcher() {
        return environment -> {
            long tagId = util.getLongArg(environment, "tagId");

            return assetTagLocalService.deleteAssetTag(tagId);
        };
    }
}
