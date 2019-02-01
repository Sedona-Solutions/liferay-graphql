package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.ListType;
import com.liferay.portal.kernel.service.ListTypeLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = ListTypeBatchLoader.class
)
public class ListTypeBatchLoader implements BatchLoader<Long, ListType> {
    public static final String KEY = "listType";
    private ListTypeLocalService listTypeLocalService;

    @Reference(unbind = "-")
    public void setListTypeLocalService(ListTypeLocalService listTypeLocalService) {
        this.listTypeLocalService = listTypeLocalService;
    }

    @Override
    public CompletionStage<List<ListType>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }
            
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(ListType.class);
            query.add(PropertyFactoryUtil.forName("listTypeId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return listTypeLocalService.dynamicQuery(query);
        });
    }
}
