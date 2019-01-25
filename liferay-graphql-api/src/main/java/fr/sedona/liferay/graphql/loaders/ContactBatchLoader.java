package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.Contact;
import com.liferay.portal.kernel.service.ContactLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = ContactBatchLoader.class
)
public class ContactBatchLoader implements BatchLoader<Long, Contact> {
    public static final String KEY = "contact";
    private ContactLocalService contactLocalService;

    @Reference(unbind = "-")
    public void setContactLocalService(ContactLocalService contactLocalService) {
        this.contactLocalService = contactLocalService;
    }

    @Override
    public CompletionStage<List<Contact>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            DynamicQuery query = DynamicQueryFactoryUtil.forClass(Contact.class);
            query.add(PropertyFactoryUtil.forName("contactId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return contactLocalService.dynamicQuery(query);
        });
    }
}
