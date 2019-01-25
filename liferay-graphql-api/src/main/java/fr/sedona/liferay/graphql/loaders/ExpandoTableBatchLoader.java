package fr.sedona.liferay.graphql.loaders;

import com.liferay.expando.kernel.model.ExpandoTable;
import com.liferay.expando.kernel.service.ExpandoTableLocalService;
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
        service = ExpandoTableBatchLoader.class
)
public class ExpandoTableBatchLoader implements BatchLoader<Long, ExpandoTable> {
    public static final String KEY = "expandoTable";
    private ExpandoTableLocalService expandoTableLocalService;

    @Reference(unbind = "-")
    public void setExpandoTableLocalService(ExpandoTableLocalService expandoTableLocalService) {
        this.expandoTableLocalService = expandoTableLocalService;
    }

    @Override
    public CompletionStage<List<ExpandoTable>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(ExpandoTable.class);
            query.add(PropertyFactoryUtil.forName("tableId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return expandoTableLocalService.dynamicQuery(query);
        });
    }
}
