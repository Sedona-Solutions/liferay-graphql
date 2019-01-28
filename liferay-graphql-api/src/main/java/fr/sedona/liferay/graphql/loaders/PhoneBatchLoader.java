package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Phone;
import com.liferay.portal.kernel.service.PhoneLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = PhoneBatchLoader.class
)
public class PhoneBatchLoader implements BatchLoader<Long, Phone> {
    public static final String KEY = "phone";
    private PhoneLocalService phoneLocalService;

    @Reference(unbind = "-")
    public void setPhoneLocalService(PhoneLocalService phoneLocalService) {
        this.phoneLocalService = phoneLocalService;
    }

    @Override
    public CompletionStage<List<Phone>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Phone.class);
            query.add(PropertyFactoryUtil.forName("phoneId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return phoneLocalService.dynamicQuery(query);
        });
    }
}
