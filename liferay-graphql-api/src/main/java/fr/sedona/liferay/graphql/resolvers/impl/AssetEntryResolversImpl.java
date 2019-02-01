package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import fr.sedona.liferay.graphql.loaders.AssetEntryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetEntryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = AssetEntryResolvers.class
)
@SuppressWarnings("squid:S1192")
public class AssetEntryResolversImpl implements AssetEntryResolvers {
    private AssetEntryLocalService assetEntryLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setAssetEntryLocalService(AssetEntryLocalService assetEntryLocalService) {
        this.assetEntryLocalService = assetEntryLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<AssetEntry>> getAssetEntriesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetEntryLocalService.getAssetEntries(start, end);
        };
    }

    @Override
    public DataFetcher<List<AssetEntry>> getAssetEntriesWithCategoryDataFetcher() {
        return environment -> {
            long categoryId = util.getLongArg(environment, "categoryId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetEntryLocalService.getAssetCategoryAssetEntries(categoryId, start, end);
        };
    }

    @Override
    public DataFetcher<List<AssetEntry>> getAssetEntriesWithTagDataFetcher() {
        return environment -> {
            long tagId = util.getLongArg(environment, "tagId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetEntryLocalService.getAssetTagAssetEntries(tagId, start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<AssetEntry>> getAssetEntryDataFetcher() {
        return environment -> {
            long entryId = getEntryId(environment);
            if (entryId <= 0) {
                return null;
            }

            DataLoader<Long, AssetEntry> dataLoader = environment.getDataLoader(AssetEntryBatchLoader.KEY);
            return dataLoader.load(entryId);
        };
    }

    private long getEntryId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "entryId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof AssetLink) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("entry1")) {
                return ((AssetLink) source).getEntryId1();
            } else if (segment.getSegmentName().contains("entry2")) {
                return ((AssetLink) source).getEntryId2();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getEntryId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<AssetEntry> associateAssetEntryWithCategoryDataFetcher() {
        return environment -> {
            long categoryId = util.getLongArg(environment, "categoryId");
            long entryId = util.getLongArg(environment, "entryId");

            assetEntryLocalService.addAssetCategoryAssetEntry(
                    categoryId,
                    entryId);
            return assetEntryLocalService.getEntry(entryId);
        };
    }

    @Override
    public DataFetcher<AssetEntry> dissociateAssetEntryFromCategoryDataFetcher() {
        return environment -> {
            long categoryId = util.getLongArg(environment, "categoryId");
            long entryId = util.getLongArg(environment, "entryId");

            assetEntryLocalService.deleteAssetCategoryAssetEntry(
                    categoryId,
                    entryId);
            return assetEntryLocalService.getEntry(entryId);
        };
    }

    @Override
    public DataFetcher<AssetEntry> associateAssetEntryWithTagDataFetcher() {
        return environment -> {
            long tagId = util.getLongArg(environment, "tagId");
            long entryId = util.getLongArg(environment, "entryId");

            assetEntryLocalService.addAssetTagAssetEntry(
                    tagId,
                    entryId);
            return assetEntryLocalService.getEntry(entryId);
        };
    }

    @Override
    public DataFetcher<AssetEntry> dissociateAssetEntryFromTagDataFetcher() {
        return environment -> {
            long tagId = util.getLongArg(environment, "tagId");
            long entryId = util.getLongArg(environment, "entryId");

            assetEntryLocalService.deleteAssetTagAssetEntry(
                    tagId,
                    entryId);
            return assetEntryLocalService.getEntry(entryId);
        };
    }
}
