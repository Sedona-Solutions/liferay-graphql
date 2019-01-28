package fr.sedona.liferay.graphql.loaders;

import com.liferay.message.boards.kernel.model.MBThread;
import com.liferay.message.boards.kernel.service.MBThreadLocalService;
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
        service = MBThreadBatchLoader.class
)
public class MBThreadBatchLoader implements BatchLoader<Long, MBThread> {
    public static final String KEY = "mbThread";
    private MBThreadLocalService mbThreadLocalService;

    @Reference(unbind = "-")
    public void setMBThreadLocalService(MBThreadLocalService mbThreadLocalService) {
        this.mbThreadLocalService = mbThreadLocalService;
    }

    @Override
    public CompletionStage<List<MBThread>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(MBThread.class);
            query.add(PropertyFactoryUtil.forName("threadId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return mbThreadLocalService.dynamicQuery(query);
        });
    }
}
