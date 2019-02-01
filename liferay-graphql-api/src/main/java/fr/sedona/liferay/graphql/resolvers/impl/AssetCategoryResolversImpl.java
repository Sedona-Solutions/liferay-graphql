package fr.sedona.liferay.graphql.resolvers.impl;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetCategoryConstants;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import fr.sedona.liferay.graphql.loaders.AssetCategoryBatchLoader;
import fr.sedona.liferay.graphql.resolvers.AssetCategoryResolvers;
import fr.sedona.liferay.graphql.util.GraphQLUtil;
import graphql.execution.ExecutionPath;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component(
        immediate = true,
        service = AssetCategoryResolvers.class
)
@SuppressWarnings("squid:S1192")
public class AssetCategoryResolversImpl implements AssetCategoryResolvers {
    private AssetCategoryLocalService assetCategoryLocalService;
    private GraphQLUtil util;

    @Reference(unbind = "-")
    public void setAssetCategoryLocalService(AssetCategoryLocalService assetCategoryLocalService) {
        this.assetCategoryLocalService = assetCategoryLocalService;
    }

    @Reference
    public void setUtil(GraphQLUtil util) {
        this.util = util;
    }

    @Override
    public DataFetcher<List<AssetCategory>> getAssetCategoriesDataFetcher() {
        return environment -> {
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetCategoryLocalService.getAssetCategories(start, end);
        };
    }

    @Override
    public DataFetcher<List<AssetCategory>> getAssetCategoriesForAssetDataFetcher() {
        return environment -> {
            long entryId = util.getLongArg(environment, "entryId");
            int start = util.getIntArg(environment, "start", 0);
            int end = util.getIntArg(environment, "end", 10);

            return assetCategoryLocalService.getAssetEntryAssetCategories(entryId, start, end);
        };
    }

    @Override
    public DataFetcher<CompletableFuture<AssetCategory>> getAssetCategoryDataFetcher() {
        return environment -> {
            long categoryId = getCategoryId(environment);
            if (categoryId <= 0) {
                return null;
            }

            DataLoader<Long, AssetCategory> dataLoader = environment.getDataLoader(AssetCategoryBatchLoader.KEY);
            return dataLoader.load(categoryId);
        };
    }

    private long getCategoryId(DataFetchingEnvironment environment) {
        long argValue = util.getLongArg(environment, "categoryId");
        if (environment.getSource() == null) {
            return argValue;
        }

        Object source = environment.getSource();
        if (source instanceof AssetCategory) {
            ExecutionPath segment = environment.getExecutionStepInfo().getPath();
            if (segment.getSegmentName().contains("parentCategory")) {
                return ((AssetCategory) source).getParentCategoryId();
            } else if (segment.getSegmentName().contains("leftCategory")) {
                return ((AssetCategory) source).getLeftCategoryId();
            } else if (segment.getSegmentName().contains("rightCategory")) {
                return ((AssetCategory) source).getRightCategoryId();
            }
        }

        try {
            return util.getEntityIdFromSource(environment.getSource(), "getCategoryId");
        } catch (Exception e) {
            return argValue;
        }
    }

    @Override
    public DataFetcher<AssetCategory> createAssetCategoryDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long groupId = util.getLongArg(environment, "groupId");
            long parentCategoryId = util.getLongArg(environment, "parentCategoryId", AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            long vocabularyId = util.getLongArg(environment, "vocabularyId");
            String[] categoryProperties = util.getStringArrayArg(environment, "categoryProperties");
            ServiceContext serviceContext = new ServiceContext();

            return assetCategoryLocalService.addCategory(
                    userId,
                    groupId,
                    parentCategoryId,
                    titleMap,
                    descriptionMap,
                    vocabularyId,
                    categoryProperties,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<AssetCategory> updateAssetCategoryDataFetcher() {
        return environment -> {
            long userId = util.getLongArg(environment, "userId", util.getDefaultUserId());
            long categoryId = util.getLongArg(environment, "categoryId");
            long parentCategoryId = util.getLongArg(environment, "parentCategoryId", AssetCategoryConstants.DEFAULT_PARENT_CATEGORY_ID);
            Map<Locale, String> titleMap = util.getTranslatedArg(environment, "titleMap");
            Map<Locale, String> descriptionMap = util.getTranslatedArg(environment, "descriptionMap");
            long vocabularyId = util.getLongArg(environment, "vocabularyId");
            String[] categoryProperties = util.getStringArrayArg(environment, "categoryProperties");
            ServiceContext serviceContext = new ServiceContext();

            return assetCategoryLocalService.updateCategory(
                    userId,
                    categoryId,
                    parentCategoryId,
                    titleMap,
                    descriptionMap,
                    vocabularyId,
                    categoryProperties,
                    serviceContext);
        };
    }

    @Override
    public DataFetcher<AssetCategory> deleteAssetCategoryDataFetcher() {
        return environment -> {
            long categoryId = util.getLongArg(environment, "categoryId");

            return assetCategoryLocalService.deleteAssetCategory(categoryId);
        };
    }
}
