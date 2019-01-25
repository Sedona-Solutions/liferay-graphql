package fr.sedona.liferay.graphql.loaders;

import com.liferay.document.library.kernel.model.DLFileVersion;
import com.liferay.document.library.kernel.service.DLFileVersionLocalService;
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
        service = DLFileVersionBatchLoader.class
)
public class DLFileVersionBatchLoader implements BatchLoader<Long, DLFileVersion> {
    public static final String KEY = "dlFileVersion";
    private DLFileVersionLocalService dlFileVersionLocalService;

    @Reference(unbind = "-")
    public void setDLFileVersionLocalService(DLFileVersionLocalService dlFileVersionLocalService) {
        this.dlFileVersionLocalService = dlFileVersionLocalService;
    }

    @Override
    public CompletionStage<List<DLFileVersion>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(DLFileVersion.class);
            query.add(PropertyFactoryUtil.forName("fileVersionId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return dlFileVersionLocalService.dynamicQuery(query);
        });
    }
}
