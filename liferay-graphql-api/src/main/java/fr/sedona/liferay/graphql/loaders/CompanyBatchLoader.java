package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.service.CompanyLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = CompanyBatchLoader.class
)
public class CompanyBatchLoader implements BatchLoader<Long, Company> {
    public static final String KEY = "company";
    private CompanyLocalService companyLocalService;

    @Reference(unbind = "-")
    public void setCompanyLocalService(CompanyLocalService companyLocalService) {
        this.companyLocalService = companyLocalService;
    }

    @Override
    public CompletionStage<List<Company>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Company.class);
            query.add(PropertyFactoryUtil.forName("companyId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return companyLocalService.dynamicQuery(query);
        });
    }
}
