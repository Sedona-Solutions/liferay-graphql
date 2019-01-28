package fr.sedona.liferay.graphql.loaders;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
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
        service = AssetEntryBatchLoader.class
)
public class AssetEntryBatchLoader implements BatchLoader<Long, AssetEntry> {
    public static final String KEY = "assetEntry";
    private AssetEntryLocalService assetEntryLocalService;

    @Reference(unbind = "-")
    public void setAssetEntryLocalService(AssetEntryLocalService assetEntryLocalService) {
        this.assetEntryLocalService = assetEntryLocalService;
    }

    @Override
    public CompletionStage<List<AssetEntry>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(AssetEntry.class);
            query.add(PropertyFactoryUtil.forName("entryId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return assetEntryLocalService.dynamicQuery(query);
        });
    }
}
