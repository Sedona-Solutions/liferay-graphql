package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Address;
import com.liferay.portal.kernel.service.AddressLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = AddressBatchLoader.class
)
public class AddressBatchLoader implements BatchLoader<Long, Address> {
    public static final String KEY = "address";
    private AddressLocalService addressLocalService;

    @Reference(unbind = "-")
    public void setAddressLocalService(AddressLocalService addressLocalService) {
        this.addressLocalService = addressLocalService;
    }

    @Override
    public CompletionStage<List<Address>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Address.class);
            query.add(PropertyFactoryUtil.forName("addressId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return addressLocalService.dynamicQuery(query);
        });
    }
}
