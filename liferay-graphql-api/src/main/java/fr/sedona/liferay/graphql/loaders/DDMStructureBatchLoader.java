package fr.sedona.liferay.graphql.loaders;

import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
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
        service = DDMStructureBatchLoader.class
)
public class DDMStructureBatchLoader implements BatchLoader<Long, DDMStructure> {
    public static final String KEY = "ddmStructure";
    private DDMStructureLocalService ddmStructureLocalService;

    @Reference(unbind = "-")
    public void setDDMStructureLocalService(DDMStructureLocalService ddmStructureLocalService) {
        this.ddmStructureLocalService = ddmStructureLocalService;
    }

    @Override
    public CompletionStage<List<DDMStructure>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DDMStructure.class);
            query.add(PropertyFactoryUtil.forName("structureId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return ddmStructureLocalService.dynamicQuery(query);
        });
    }
}
