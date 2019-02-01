package fr.sedona.liferay.graphql.loaders;

import com.liferay.message.boards.kernel.model.MBMessage;
import com.liferay.message.boards.kernel.service.MBMessageLocalService;
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
        service = MBMessageBatchLoader.class
)
public class MBMessageBatchLoader implements BatchLoader<Long, MBMessage> {
    public static final String KEY = "mbMessage";
    private MBMessageLocalService mbMessageLocalService;

    @Reference(unbind = "-")
    public void setMBMessageLocalService(MBMessageLocalService mbMessageLocalService) {
        this.mbMessageLocalService = mbMessageLocalService;
    }

    @Override
    public CompletionStage<List<MBMessage>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(MBMessage.class);
            query.add(PropertyFactoryUtil.forName("messageId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return mbMessageLocalService.dynamicQuery(query);
        });
    }
}
