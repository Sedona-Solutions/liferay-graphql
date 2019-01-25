package fr.sedona.liferay.graphql.loaders;

import com.liferay.expando.kernel.model.ExpandoColumn;
import com.liferay.expando.kernel.service.ExpandoColumnLocalService;
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
        service = ExpandoColumnBatchLoader.class
)
public class ExpandoColumnBatchLoader implements BatchLoader<Long, ExpandoColumn> {
    public static final String KEY = "expandoColumn";
    private ExpandoColumnLocalService expandoColumnLocalService;

    @Reference(unbind = "-")
    public void setExpandoColumnLocalService(ExpandoColumnLocalService expandoColumnLocalService) {
        this.expandoColumnLocalService = expandoColumnLocalService;
    }

    @Override
    public CompletionStage<List<ExpandoColumn>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(ExpandoColumn.class);
            query.add(PropertyFactoryUtil.forName("columnId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return expandoColumnLocalService.dynamicQuery(query);
        });
    }
}
