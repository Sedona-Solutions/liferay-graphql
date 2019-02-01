package fr.sedona.liferay.graphql.loaders;

import com.liferay.expando.kernel.model.ExpandoValue;
import com.liferay.expando.kernel.service.ExpandoValueLocalService;
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
        service = ExpandoValueBatchLoader.class
)
public class ExpandoValueBatchLoader implements BatchLoader<Long, ExpandoValue> {
    public static final String KEY = "expandoValue";
    private ExpandoValueLocalService expandoValueLocalService;

    @Reference(unbind = "-")
    public void setExpandoValueLocalService(ExpandoValueLocalService expandoValueLocalService) {
        this.expandoValueLocalService = expandoValueLocalService;
    }

    @Override
    public CompletionStage<List<ExpandoValue>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(ExpandoValue.class);
            query.add(PropertyFactoryUtil.forName("valueId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return expandoValueLocalService.dynamicQuery(query);
        });
    }
}
