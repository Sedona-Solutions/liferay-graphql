package fr.sedona.liferay.graphql.loaders;

import com.liferay.dynamic.data.mapping.model.DDMContent;
import com.liferay.dynamic.data.mapping.service.DDMContentLocalService;
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
        service = DDMContentBatchLoader.class
)
public class DDMContentBatchLoader implements BatchLoader<Long, DDMContent> {
    public static final String KEY = "ddmContent";
    private DDMContentLocalService ddmContentLocalService;

    @Reference(unbind = "-")
    public void setDDMContentLocalService(DDMContentLocalService ddmContentLocalService) {
        this.ddmContentLocalService = ddmContentLocalService;
    }

    @Override
    public CompletionStage<List<DDMContent>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DDMContent.class);
            query.add(PropertyFactoryUtil.forName("contentId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return ddmContentLocalService.dynamicQuery(query);
        });
    }
}
