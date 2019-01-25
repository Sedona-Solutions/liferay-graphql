package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = GroupBatchLoader.class
)
public class GroupBatchLoader implements BatchLoader<Long, Group> {
    public static final String KEY = "group";
    private GroupLocalService groupLocalService;

    @Reference(unbind = "-")
    public void setGroupLocalService(GroupLocalService groupLocalService) {
        this.groupLocalService = groupLocalService;
    }

    @Override
    public CompletionStage<List<Group>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Group.class);
            query.add(PropertyFactoryUtil.forName("groupId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return groupLocalService.dynamicQuery(query);
        });
    }
}
