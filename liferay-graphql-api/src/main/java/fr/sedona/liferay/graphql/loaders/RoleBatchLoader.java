package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.service.RoleLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = RoleBatchLoader.class
)
public class RoleBatchLoader implements BatchLoader<Long, Role> {
    public static final String KEY = "role";
    private RoleLocalService roleLocalService;

    @Reference(unbind = "-")
    public void setRoleLocalService(RoleLocalService roleLocalService) {
        this.roleLocalService = roleLocalService;
    }

    @Override
    public CompletionStage<List<Role>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Role.class);
            query.add(PropertyFactoryUtil.forName("roleId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return roleLocalService.dynamicQuery(query);
        });
    }
}
