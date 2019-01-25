package fr.sedona.liferay.graphql.loaders;

import com.liferay.message.boards.kernel.model.MBCategory;
import com.liferay.message.boards.kernel.service.MBCategoryLocalService;
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
        service = MBCategoryBatchLoader.class
)
public class MBCategoryBatchLoader implements BatchLoader<Long, MBCategory> {
    public static final String KEY = "mbCategory";
    private MBCategoryLocalService mbCategoryLocalService;

    @Reference(unbind = "-")
    public void setMBCategoryLocalService(MBCategoryLocalService mbCategoryLocalService) {
        this.mbCategoryLocalService = mbCategoryLocalService;
    }

    @Override
    public CompletionStage<List<MBCategory>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(MBCategory.class);
            query.add(PropertyFactoryUtil.forName("categoryId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return mbCategoryLocalService.dynamicQuery(query);
        });
    }
}
