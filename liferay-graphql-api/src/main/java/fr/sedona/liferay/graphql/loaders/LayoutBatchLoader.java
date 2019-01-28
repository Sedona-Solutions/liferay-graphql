package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.service.LayoutLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = LayoutBatchLoader.class
)
public class LayoutBatchLoader implements BatchLoader<Long, Layout> {
    public static final String KEY = "layout";
    private LayoutLocalService layoutLocalService;

    @Reference(unbind = "-")
    public void setLayoutLocalService(LayoutLocalService layoutLocalService) {
        this.layoutLocalService = layoutLocalService;
    }

    @Override
    public CompletionStage<List<Layout>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Layout.class);
            query.add(PropertyFactoryUtil.forName("layoutId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return layoutLocalService.dynamicQuery(query);
        });
    }
}
