package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.UserGroup;
import com.liferay.portal.kernel.service.UserGroupLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = UserGroupBatchLoader.class
)
public class UserGroupBatchLoader implements BatchLoader<Long, UserGroup> {
    public static final String KEY = "userGroup";
    private UserGroupLocalService userGroupLocalService;

    @Reference(unbind = "-")
    public void setUserGroupLocalService(UserGroupLocalService userGroupLocalService) {
        this.userGroupLocalService = userGroupLocalService;
    }

    @Override
    public CompletionStage<List<UserGroup>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(UserGroup.class);
            query.add(PropertyFactoryUtil.forName("userGroupId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return userGroupLocalService.dynamicQuery(query);
        });
    }
}
