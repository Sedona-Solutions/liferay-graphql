package fr.sedona.liferay.graphql.loaders;

import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.model.EmailAddress;
import com.liferay.portal.kernel.service.EmailAddressLocalService;
import org.dataloader.BatchLoader;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component(
        immediate = true,
        service = EmailAddressBatchLoader.class
)
public class EmailAddressBatchLoader implements BatchLoader<Long, EmailAddress> {
    public static final String KEY = "emailAddress";
    private EmailAddressLocalService emailAddressLocalService;

    @Reference(unbind = "-")
    public void setEmailAddressLocalService(EmailAddressLocalService emailAddressLocalService) {
        this.emailAddressLocalService = emailAddressLocalService;
    }

    @Override
    public CompletionStage<List<EmailAddress>> load(List<Long> keys) {
        return CompletableFuture.supplyAsync(() -> {
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }

            DynamicQuery query = DynamicQueryFactoryUtil.forClass(EmailAddress.class);
            query.add(PropertyFactoryUtil.forName("emailAddressId")
                    .in(keys.stream()
                            .mapToLong(l -> l)
                            .toArray()));
            return emailAddressLocalService.dynamicQuery(query);
        });
    }
}
