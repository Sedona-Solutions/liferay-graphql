package fr.sedona.liferay.graphql.loaders;

import com.liferay.asset.kernel.model.AssetTag;
import com.liferay.asset.kernel.service.AssetTagLocalService;
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
        service = AssetTagBatchLoader.class
)
public class AssetTagBatchLoader implements BatchLoader<Long, AssetTag> {
    public static final String KEY = "assetTag";
    private AssetTagLocalService assetTagLocalService;

    @Reference(unbind = "-")
    public void setAssetTagLocalService(AssetTagLocalService assetTagLocalService) {
        this.assetTagLocalService = assetTagLocalService;
    }

    @Override
    public CompletionStage<List<AssetTag>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(AssetTag.class);
            query.add(PropertyFactoryUtil.forName("tagId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return assetTagLocalService.dynamicQuery(query);
        });
    }
}
