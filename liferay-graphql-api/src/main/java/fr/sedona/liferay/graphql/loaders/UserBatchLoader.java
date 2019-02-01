package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = UserBatchLoader.class
)
public class UserBatchLoader implements BatchLoader<Long, User> {
    public static final String KEY = "user";
    private UserLocalService userLocalService;

    @Reference(unbind = "-")
    public void setUserLocalService(UserLocalService userLocalService) {
        this.userLocalService = userLocalService;
    }

    @Override
    public CompletionStage<List<User>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(User.class);
            query.add(PropertyFactoryUtil.forName("userId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return userLocalService.dynamicQuery(query);
        });
    }
}
