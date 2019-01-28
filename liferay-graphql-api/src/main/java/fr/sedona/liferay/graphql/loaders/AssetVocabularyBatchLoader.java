package fr.sedona.liferay.graphql.loaders;

import com.liferay.asset.kernel.model.AssetVocabulary;
import com.liferay.asset.kernel.service.AssetVocabularyLocalService;
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
        service = AssetVocabularyBatchLoader.class
)
public class AssetVocabularyBatchLoader implements BatchLoader<Long, AssetVocabulary> {
    public static final String KEY = "assetVocabulary";
    private AssetVocabularyLocalService assetVocabularyLocalService;

    @Reference(unbind = "-")
    public void setAssetVocabularyLocalService(AssetVocabularyLocalService assetVocabularyLocalService) {
        this.assetVocabularyLocalService = assetVocabularyLocalService;
    }

    @Override
    public CompletionStage<List<AssetVocabulary>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }
            
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(AssetVocabulary.class);
            query.add(PropertyFactoryUtil.forName("vocabularyId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return assetVocabularyLocalService.dynamicQuery(query);
        });
    }
}
