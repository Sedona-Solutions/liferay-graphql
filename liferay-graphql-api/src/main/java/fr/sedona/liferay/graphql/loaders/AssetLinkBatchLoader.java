package fr.sedona.liferay.graphql.loaders;

import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.asset.kernel.service.AssetLinkLocalService;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = AssetLinkBatchLoader.class
)
public class AssetLinkBatchLoader implements BatchLoader<Long, AssetLink> {
    public static final String KEY = "assetLink";
    private AssetLinkLocalService assetLinkLocalService;

    @Reference(unbind = "-")
    public void setAssetLinkLocalService(AssetLinkLocalService assetLinkLocalService) {
        this.assetLinkLocalService = assetLinkLocalService;
    }

    @Override
    public CompletionStage<List<AssetLink>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(AssetLink.class);
            query.add(PropertyFactoryUtil.forName("linkId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return assetLinkLocalService.dynamicQuery(query);
        });
    }
}
