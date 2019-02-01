package fr.sedona.liferay.graphql.loaders;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = AssetCategoryBatchLoader.class
)
public class AssetCategoryBatchLoader implements BatchLoader<Long, AssetCategory> {
    public static final String KEY = "assetCategory";
    private AssetCategoryLocalService assetCategoryLocalService;

    @Reference(unbind = "-")
    public void setAssetCategoryLocalService(AssetCategoryLocalService assetCategoryLocalService) {
        this.assetCategoryLocalService = assetCategoryLocalService;
    }

    @Override
    public CompletionStage<List<AssetCategory>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(AssetCategory.class);
            query.add(PropertyFactoryUtil.forName("categoryId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return assetCategoryLocalService.dynamicQuery(query);
        });
    }
}
