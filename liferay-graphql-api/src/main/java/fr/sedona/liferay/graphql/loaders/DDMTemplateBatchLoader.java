package fr.sedona.liferay.graphql.loaders;

import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
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
        service = DDMTemplateBatchLoader.class
)
public class DDMTemplateBatchLoader implements BatchLoader<Long, DDMTemplate> {
    public static final String KEY = "ddmTemplate";
    private DDMTemplateLocalService ddmTemplateLocalService;

    @Reference(unbind = "-")
    public void setDDMTemplateLocalService(DDMTemplateLocalService ddmTemplateLocalService) {
        this.ddmTemplateLocalService = ddmTemplateLocalService;
    }

    @Override
    public CompletionStage<List<DDMTemplate>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DDMTemplate.class);
            query.add(PropertyFactoryUtil.forName("templateId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return ddmTemplateLocalService.dynamicQuery(query);
        });
    }
}
