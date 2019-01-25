package fr.sedona.liferay.graphql.loaders;

import com.liferay.document.library.kernel.model.DLFileEntryMetadata;
import com.liferay.document.library.kernel.service.DLFileEntryMetadataLocalService;
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
        service = DLFileEntryMetadataBatchLoader.class
)
public class DLFileEntryMetadataBatchLoader implements BatchLoader<Long, DLFileEntryMetadata> {
    public static final String KEY = "dlFileEntryMetadata";
    private DLFileEntryMetadataLocalService dlFileEntryMetadataLocalService;

    @Reference(unbind = "-")
    public void setDLFileEntryMetadataLocalService(DLFileEntryMetadataLocalService dlFileEntryMetadataLocalService) {
        this.dlFileEntryMetadataLocalService = dlFileEntryMetadataLocalService;
    }

    @Override
    public CompletionStage<List<DLFileEntryMetadata>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DLFileEntryMetadata.class);
            query.add(PropertyFactoryUtil.forName("fileEntryMetadataId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return dlFileEntryMetadataLocalService.dynamicQuery(query);
        });
    }
}
