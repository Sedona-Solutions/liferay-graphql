package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Organization;
import com.liferay.portal.kernel.service.OrganizationLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = OrganizationBatchLoader.class
)
public class OrganizationBatchLoader implements BatchLoader<Long, Organization> {
    public static final String KEY = "organization";
    private OrganizationLocalService organizationLocalService;

    @Reference(unbind = "-")
    public void setOrganizationLocalService(OrganizationLocalService organizationLocalService) {
        this.organizationLocalService = organizationLocalService;
    }

    @Override
    public CompletionStage<List<Organization>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Organization.class);
            query.add(PropertyFactoryUtil.forName("organizationId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return organizationLocalService.dynamicQuery(query);
        });
    }
}
