package fr.sedona.liferay.graphql.loaders;

import com.liferay.expando.kernel.model.ExpandoRow;
import com.liferay.expando.kernel.service.ExpandoRowLocalService;
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
        service = ExpandoRowBatchLoader.class
)
public class ExpandoRowBatchLoader implements BatchLoader<Long, ExpandoRow> {
    public static final String KEY = "expandoRow";
    private ExpandoRowLocalService expandoRowLocalService;

    @Reference(unbind = "-")
    public void setExpandoRowLocalService(ExpandoRowLocalService expandoRowLocalService) {
        this.expandoRowLocalService = expandoRowLocalService;
    }

    @Override
    public CompletionStage<List<ExpandoRow>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(ExpandoRow.class);
            query.add(PropertyFactoryUtil.forName("rowId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return expandoRowLocalService.dynamicQuery(query);
        });
    }
}
