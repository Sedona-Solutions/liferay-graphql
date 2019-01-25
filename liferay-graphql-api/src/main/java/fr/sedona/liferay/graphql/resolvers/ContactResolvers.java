package fr.sedona.liferay.graphql.resolvers;

import aQute.bnd.annotation.ProviderType;
import com.liferay.portal.kernel.model.Contact;
import graphql.schema.DataFetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ProviderType
public interface ContactResolvers {

    DataFetcher<List<Contact>> getContactsDataFetcher();

    DataFetcher<CompletableFuture<Contact>> getContactDataFetcher();

    DataFetcher<Contact> createContactDataFetcher();

    DataFetcher<Contact> updateContactDataFetcher();

    DataFetcher<Contact> deleteContactDataFetcher();
}
