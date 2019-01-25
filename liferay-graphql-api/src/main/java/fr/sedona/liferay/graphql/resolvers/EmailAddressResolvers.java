package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.EmailAddress;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface EmailAddressResolvers {

    DataFetcher<List<EmailAddress>> getEmailAddressesDataFetcher();

    DataFetcher<List<EmailAddress>> getEmailAddressesForEntityDataFetcher();

    DataFetcher<CompletableFuture<EmailAddress>> getEmailAddressDataFetcher();

    DataFetcher<EmailAddress> createEmailAddressDataFetcher();

    DataFetcher<EmailAddress> updateEmailAddressDataFetcher();

    DataFetcher<EmailAddress> deleteEmailAddressDataFetcher();
}
