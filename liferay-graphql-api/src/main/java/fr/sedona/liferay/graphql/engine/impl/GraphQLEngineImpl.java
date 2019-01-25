package fr.sedona.liferay.graphql.engine.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liferay.oauth2.provider.constants.GrantType;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import fr.sedona.liferay.graphql.engine.GraphQLEngine;
import fr.sedona.liferay.graphql.resolvers.*;
import fr.sedona.liferay.graphql.scalars.DateScalar;
import fr.sedona.liferay.graphql.scalars.LocalizedStringScalar;
import fr.sedona.liferay.graphql.util.Constants;
import fr.sedona.liferay.graphql.util.GraphQLRegistry;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.execution.preparsed.PreparsedDocumentEntry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component(
        immediate = true,
        service = GraphQLEngine.class
)
@SuppressWarnings("squid:S1192")
public class GraphQLEngineImpl implements GraphQLEngine {
    private static final Log LOGGER = LogFactoryUtil.getLog(GraphQLEngineImpl.class);
    private Cache<String, PreparsedDocumentEntry> cache;
    private GraphQL graphQL;

    @Reference
    private GraphQLRegistry graphQLRegistry;

    @Reference
    private AssetCategoryResolvers assetCategoryResolvers;

    @Reference
    private AssetEntryResolvers assetEntryResolvers;

    @Reference
    private AssetLinkResolvers assetLinkResolvers;

    @Reference
    private AssetTagResolvers assetTagResolvers;

    @Reference
    private AssetVocabularyResolvers assetVocabularyResolvers;

    @Reference
    private BlogsEntryResolvers blogsEntryResolvers;

    @Reference
    private DLFileEntryResolvers dlFileEntryResolvers;

    @Reference
    private DLFileEntryMetadataResolvers dlFileEntryMetadataResolvers;

    @Reference
    private DLFileEntryTypeResolvers dlFileEntryTypeResolvers;

    @Reference
    private DLFileShortcutResolvers dlFileShortcutResolvers;

    @Reference
    private DLFileVersionResolvers dlFileVersionResolvers;

    @Reference
    private DLFolderResolvers dlFolderResolvers;

    @Reference
    private DDMContentResolvers ddmContentResolvers;

    @Reference
    private DDMStructureResolvers ddmStructureResolvers;

    @Reference
    private DDMTemplateResolvers ddmTemplateResolvers;

    @Reference
    private ExpandoColumnResolvers expandoColumnResolvers;

    @Reference
    private ExpandoRowResolvers expandoRowResolvers;

    @Reference
    private ExpandoTableResolvers expandoTableResolvers;

    @Reference
    private ExpandoValueResolvers expandoValueResolvers;

    @Reference
    private JournalArticleResolvers journalArticleResolvers;

    @Reference
    private JournalFeedResolvers journalFeedResolvers;

    @Reference
    private JournalFolderResolvers journalFolderResolvers;

    @Reference
    private MBCategoryResolvers mbCategoryResolvers;

    @Reference
    private MBMessageResolvers mbMessageResolvers;

    @Reference
    private MBThreadResolvers mbThreadResolvers;

    @Reference
    private OAuth2ApplicationResolvers oAuth2ApplicationResolvers;

    @Reference
    private OAuth2ApplicationScopeAliasesResolvers oAuth2ApplicationScopeAliasesResolvers;

    @Reference
    private OAuth2AuthorizationResolvers oAuth2AuthorizationResolvers;

    @Reference
    private OAuth2ScopeGrantResolvers oAuth2ScopeGrantResolvers;

    @Reference
    private AddressResolvers addressResolvers;

    @Reference
    private ClassNameResolvers classNameResolvers;

    @Reference
    private CompanyResolvers companyResolvers;

    @Reference
    private ContactResolvers contactResolvers;

    @Reference
    private CountryResolvers countryResolvers;

    @Reference
    private EmailAddressResolvers emailAddressResolvers;

    @Reference
    private GroupResolvers groupResolvers;

    @Reference
    private LayoutResolvers layoutResolvers;

    @Reference
    private ListTypeResolvers listTypeResolvers;

    @Reference
    private OrganizationResolvers organizationResolvers;

    @Reference
    private PhoneResolvers phoneResolvers;

    @Reference
    private RegionResolvers regionResolvers;

    @Reference
    private RoleResolvers roleResolvers;

    @Reference
    private UserResolvers userResolvers;

    @Reference
    private UserGroupResolvers userGroupResolvers;

    @Reference
    private WebsiteResolvers websiteResolvers;

    @Reference
    private RatingsEntryResolvers ratingsEntryResolvers;


    @Activate
    public void activateComponent() {
        initializeGraphQLCache();
        initializeEngine();
    }

    private void initializeGraphQLCache() {
        LOGGER.info("Initializing GraphQL cache");
        cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    private void initializeEngine() {
        LOGGER.info("Initializing GraphQL engine");
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = schemaParser.parse(getSchemaFileReader());
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, getSchemaWiring());
        graphQL = GraphQL.newGraphQL(graphQLSchema)
                .preparsedDocumentProvider(cache::get)
                .instrumentation(getInstrumentation())
                .build();
    }

    private ChainedInstrumentation getInstrumentation() {
        // Instrumentation for data loader
        List<Instrumentation> chainedList = new ArrayList<>();
        DataLoaderDispatcherInstrumentationOptions options = DataLoaderDispatcherInstrumentationOptions.newOptions()
                .includeStatistics(true);
        chainedList.add(new DataLoaderDispatcherInstrumentation(options));

        // Instrumentation for tracing
        chainedList.add(new TracingInstrumentation());

        return new ChainedInstrumentation(chainedList);
    }

    private BufferedReader getSchemaFileReader() {
        InputStream is = getClass().getResourceAsStream(Constants.SCHEMA_FILE);
        return new BufferedReader(new InputStreamReader(is));
    }

    @Override
    public String getSchema() {
        return getSchemaFileReader().lines()
                .collect(Collectors.joining("\n"));
    }

    private RuntimeWiring getSchemaWiring() {
        LOGGER.info("Loading GraphQL runtime wiring");
        return RuntimeWiring.newRuntimeWiring()
                .scalar(LocalizedStringScalar.INSTANCE)
                .scalar(DateScalar.INSTANCE)
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        // START -- Query resolvers for class com.liferay.asset.kernel.model.AssetCategory
                        .dataFetcher("assetCategories", assetCategoryResolvers.getAssetCategoriesDataFetcher())
                        .dataFetcher("assetCategoriesForAsset", assetCategoryResolvers.getAssetCategoriesForAssetDataFetcher())
                        .dataFetcher("assetCategory", assetCategoryResolvers.getAssetCategoryDataFetcher())
                        // END -- Query resolvers for class com.liferay.asset.kernel.model.AssetCategory

                        // START -- Query resolvers for class com.liferay.asset.kernel.model.AssetEntry
                        .dataFetcher("assetEntries", assetEntryResolvers.getAssetEntriesDataFetcher())
                        .dataFetcher("assetEntriesWithCategory", assetEntryResolvers.getAssetEntriesWithCategoryDataFetcher())
                        .dataFetcher("assetEntriesWithTag", assetEntryResolvers.getAssetEntriesWithTagDataFetcher())
                        .dataFetcher("assetEntry", assetEntryResolvers.getAssetEntryDataFetcher())
                        // END -- Query resolvers for class com.liferay.asset.kernel.model.AssetEntry

                        // START -- Query resolvers for class com.liferay.asset.kernel.model.AssetLink
                        .dataFetcher("assetLinks", assetLinkResolvers.getAssetLinksDataFetcher())
                        .dataFetcher("assetLink", assetLinkResolvers.getAssetLinkDataFetcher())
                        // END -- Query resolvers for class com.liferay.asset.kernel.model.AssetLink

                        // START -- Query resolvers for class com.liferay.asset.kernel.model.AssetTag
                        .dataFetcher("assetTags", assetTagResolvers.getAssetTagsDataFetcher())
                        .dataFetcher("assetTagsForAsset", assetTagResolvers.getAssetTagsForAssetDataFetcher())
                        .dataFetcher("assetTag", assetTagResolvers.getAssetTagDataFetcher())
                        // END -- Query resolvers for class com.liferay.asset.kernel.model.AssetTag

                        // START -- Query resolvers for class com.liferay.asset.kernel.model.AssetVocabulary
                        .dataFetcher("assetVocabularies", assetVocabularyResolvers.getAssetVocabulariesDataFetcher())
                        .dataFetcher("assetVocabulary", assetVocabularyResolvers.getAssetVocabularyDataFetcher())
                        // END -- Query resolvers for class com.liferay.asset.kernel.model.AssetVocabulary

                        // START -- Query resolvers for class com.liferay.blogs.model.BlogsEntry
                        .dataFetcher("blogsEntries", blogsEntryResolvers.getBlogsEntriesDataFetcher())
                        .dataFetcher("blogsEntry", blogsEntryResolvers.getBlogsEntryDataFetcher())
                        // END -- Query resolvers for class com.liferay.blogs.model.BlogsEntry

                        // START -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileEntry
                        .dataFetcher("dlFileEntries", dlFileEntryResolvers.getDLFileEntriesDataFetcher())
                        .dataFetcher("dlFileEntry", dlFileEntryResolvers.getDLFileEntryDataFetcher())
                        // END -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileEntry

                        // START -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileEntryMetadata
                        .dataFetcher("dlFileEntryMetadatas", dlFileEntryMetadataResolvers.getDLFileEntryMetadatasDataFetcher())
                        .dataFetcher("dlFileEntryMetadata", dlFileEntryMetadataResolvers.getDLFileEntryMetadataDataFetcher())
                        // END -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileEntryMetadata

                        // START -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileEntryType
                        .dataFetcher("dlFileEntryTypes", dlFileEntryTypeResolvers.getDLFileEntryTypesDataFetcher())
                        .dataFetcher("dlFileEntryTypesForFolder", dlFileEntryTypeResolvers.getDLFileEntryTypesForFolderDataFetcher())
                        .dataFetcher("dlFileEntryType", dlFileEntryTypeResolvers.getDLFileEntryTypeDataFetcher())
                        // END -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileEntryType

                        // START -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileShortcut
                        .dataFetcher("dlFileShortcuts", dlFileShortcutResolvers.getDLFileShortcutsDataFetcher())
                        .dataFetcher("dlFileShortcut", dlFileShortcutResolvers.getDLFileShortcutDataFetcher())
                        // END -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileShortcut

                        // START -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileVersion
                        .dataFetcher("dlFileVersions", dlFileVersionResolvers.getDLFileVersionsDataFetcher())
                        .dataFetcher("dlFileVersion", dlFileVersionResolvers.getDLFileVersionDataFetcher())
                        // END -- Query resolvers for class com.liferay.document.library.kernel.model.DLFileVersion

                        // START -- Query resolvers for class com.liferay.document.library.kernel.model.DLFolder
                        .dataFetcher("dlFolders", dlFolderResolvers.getDLFoldersDataFetcher())
                        .dataFetcher("dlFoldersForType", dlFolderResolvers.getDLFoldersForTypeDataFetcher())
                        .dataFetcher("dlFolder", dlFolderResolvers.getDLFolderDataFetcher())
                        // END -- Query resolvers for class com.liferay.document.library.kernel.model.DLFolder

                        // START -- Query resolvers for class com.liferay.dynamic.data.mapping.model.DDMContent
                        .dataFetcher("ddmContents", ddmContentResolvers.getDDMContentsDataFetcher())
                        .dataFetcher("ddmContent", ddmContentResolvers.getDDMContentDataFetcher())
                        // END -- Query resolvers for class com.liferay.dynamic.data.mapping.model.DDMContent

                        // START -- Query resolvers for class com.liferay.dynamic.data.mapping.model.DDMStructure
                        .dataFetcher("ddmStructures", ddmStructureResolvers.getDDMStructuresDataFetcher())
                        .dataFetcher("ddmStructure", ddmStructureResolvers.getDDMStructureDataFetcher())
                        .dataFetcher("ddmStructureByKey", ddmStructureResolvers.getDDMStructureByKeyDataFetcher())
                        // END -- Query resolvers for class com.liferay.dynamic.data.mapping.model.DDMStructure

                        // START -- Query resolvers for class com.liferay.dynamic.data.mapping.model.DDMTemplate
                        .dataFetcher("ddmTemplates", ddmTemplateResolvers.getDDMTemplatesDataFetcher())
                        .dataFetcher("ddmTemplate", ddmTemplateResolvers.getDDMTemplateDataFetcher())
                        .dataFetcher("ddmTemplateByKey", ddmTemplateResolvers.getDDMTemplateByKeyDataFetcher())
                        // END -- Query resolvers for class com.liferay.dynamic.data.mapping.model.DDMTemplate

                        // START -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoColumn
                        .dataFetcher("expandoColumns", expandoColumnResolvers.getExpandoColumnsDataFetcher())
                        .dataFetcher("expandoColumn", expandoColumnResolvers.getExpandoColumnDataFetcher())
                        // END -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoColumn

                        // START -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoRow
                        .dataFetcher("expandoRows", expandoRowResolvers.getExpandoRowsDataFetcher())
                        .dataFetcher("expandoRow", expandoRowResolvers.getExpandoRowDataFetcher())
                        // END -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoRow

                        // START -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoTable
                        .dataFetcher("expandoTables", expandoTableResolvers.getExpandoTablesDataFetcher())
                        .dataFetcher("expandoTable", expandoTableResolvers.getExpandoTableDataFetcher())
                        // END -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoTable

                        // START -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoValue
                        .dataFetcher("expandoValues", expandoValueResolvers.getExpandoValuesDataFetcher())
                        .dataFetcher("expandoValue", expandoValueResolvers.getExpandoValueDataFetcher())
                        // END -- Query resolvers for class com.liferay.expando.kernel.model.ExpandoValue

                        // START -- Query resolvers for class com.liferay.journal.model.JournalArticle
                        .dataFetcher("journalArticles", journalArticleResolvers.getJournalArticlesDataFetcher())
                        .dataFetcher("journalArticle", journalArticleResolvers.getJournalArticleDataFetcher())
                        // END -- Query resolvers for class com.liferay.journal.model.JournalArticle

                        // START -- Query resolvers for class com.liferay.journal.model.JournalFeed
                        .dataFetcher("journalFeeds", journalFeedResolvers.getJournalFeedsDataFetcher())
                        .dataFetcher("journalFeed", journalFeedResolvers.getJournalFeedDataFetcher())
                        // END -- Query resolvers for class com.liferay.journal.model.JournalFeed

                        // START -- Query resolvers for class com.liferay.journal.model.JournalFolder
                        .dataFetcher("journalFolders", journalFolderResolvers.getJournalFoldersDataFetcher())
                        .dataFetcher("journalFolder", journalFolderResolvers.getJournalFolderDataFetcher())
                        // END -- Query resolvers for class com.liferay.journal.model.JournalFolder

                        // START -- Query resolvers for class com.liferay.message.boards.kernel.model.MBCategory
                        .dataFetcher("mbCategories", mbCategoryResolvers.getMBCategoriesDataFetcher())
                        .dataFetcher("mbCategory", mbCategoryResolvers.getMBCategoryDataFetcher())
                        // END -- Query resolvers for class com.liferay.message.boards.kernel.model.MBCategory

                        // START -- Query resolvers for class com.liferay.message.boards.kernel.model.MBMessage
                        .dataFetcher("mbMessages", mbMessageResolvers.getMBMessagesDataFetcher())
                        .dataFetcher("mbMessage", mbMessageResolvers.getMBMessageDataFetcher())
                        // END -- Query resolvers for class com.liferay.message.boards.kernel.model.MBMessage

                        // START -- Query resolvers for class com.liferay.message.boards.kernel.model.MBThread
                        .dataFetcher("mbThreads", mbThreadResolvers.getMBThreadsDataFetcher())
                        .dataFetcher("mbThread", mbThreadResolvers.getMBThreadDataFetcher())
                        // END -- Query resolvers for class com.liferay.message.boards.kernel.model.MBThread

                        // START -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2Application
                        .dataFetcher("oAuth2Applications", oAuth2ApplicationResolvers.getOAuth2ApplicationsDataFetcher())
                        .dataFetcher("oAuth2Application", oAuth2ApplicationResolvers.getOAuth2ApplicationDataFetcher())
                        .dataFetcher("oAuth2ApplicationForClient", oAuth2ApplicationResolvers.getOAuth2ApplicationForClientDataFetcher())
                        // END -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2Application

                        // START -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases
                        .dataFetcher("oAuth2ApplicationScopeAliaseses", oAuth2ApplicationScopeAliasesResolvers.getOAuth2ApplicationScopeAliasesesDataFetcher())
                        .dataFetcher("oAuth2ApplicationScopeAliases", oAuth2ApplicationScopeAliasesResolvers.getOAuth2ApplicationScopeAliasesDataFetcher())
                        // END -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases

                        // START -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2Authorization
                        .dataFetcher("oAuth2Authorizations", oAuth2AuthorizationResolvers.getOAuth2AuthorizationsDataFetcher())
                        .dataFetcher("oAuth2AuthorizationsForGrant", oAuth2AuthorizationResolvers.getOAuth2AuthorizationsForGrantDataFetcher())
                        .dataFetcher("oAuth2AuthorizationsForUser", oAuth2AuthorizationResolvers.getOAuth2AuthorizationsForUserDataFetcher())
                        .dataFetcher("oAuth2Authorization", oAuth2AuthorizationResolvers.getOAuth2AuthorizationDataFetcher())
                        .dataFetcher("oAuth2AuthorizationByAccessToken", oAuth2AuthorizationResolvers.getOAuth2AuthorizationByAccessTokenDataFetcher())
                        .dataFetcher("oAuth2AuthorizationByRefreshToken", oAuth2AuthorizationResolvers.getOAuth2AuthorizationByRefreshTokenDataFetcher())
                        // END -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2Authorization

                        // START -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2ScopeGrant
                        .dataFetcher("oAuth2ScopeGrants", oAuth2ScopeGrantResolvers.getOAuth2ScopeGrantsDataFetcher())
                        .dataFetcher("oAuth2ScopeGrantsForAuthorization", oAuth2ScopeGrantResolvers.getOAuth2ScopeGrantsForAuthorizationDataFetcher())
                        .dataFetcher("oAuth2ScopeGrant", oAuth2ScopeGrantResolvers.getOAuth2ScopeGrantDataFetcher())
                        // END -- Query resolvers for class com.liferay.oauth2.provider.model.OAuth2ScopeGrant

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Address
                        .dataFetcher("addresses", addressResolvers.getAddressesDataFetcher())
                        .dataFetcher("addressesForEntity", addressResolvers.getAddressesForEntityDataFetcher())
                        .dataFetcher("address", addressResolvers.getAddressDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Address

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.ClassName
                        .dataFetcher("classNames", classNameResolvers.getClassNamesDataFetcher())
                        .dataFetcher("className", classNameResolvers.getClassNameDataFetcher())
                        .dataFetcher("classNameByName", classNameResolvers.getClassNameByNameDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.ClassName

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Company
                        .dataFetcher("companies", companyResolvers.getCompaniesDataFetcher())
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("companyByWebId", companyResolvers.getCompanyByWebIdDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Company

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Contact
                        .dataFetcher("contacts", contactResolvers.getContactsDataFetcher())
                        .dataFetcher("contact", contactResolvers.getContactDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Contact

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Country
                        .dataFetcher("countries", countryResolvers.getCountriesDataFetcher())
                        .dataFetcher("country", countryResolvers.getCountryDataFetcher())
                        .dataFetcher("countryByA2", countryResolvers.getCountryByA2DataFetcher())
                        .dataFetcher("countryByA3", countryResolvers.getCountryByA3DataFetcher())
                        .dataFetcher("countryByName", countryResolvers.getCountryByNameDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Country

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.EmailAddress
                        .dataFetcher("emailAddresses", emailAddressResolvers.getEmailAddressesDataFetcher())
                        .dataFetcher("emailAddressesForEntity", emailAddressResolvers.getEmailAddressesForEntityDataFetcher())
                        .dataFetcher("emailAddress", emailAddressResolvers.getEmailAddressDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.EmailAddress

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Group
                        .dataFetcher("groups", groupResolvers.getGroupsDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("groupByKey", groupResolvers.getGroupByKeyDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Group

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Layout
                        .dataFetcher("layouts", layoutResolvers.getLayoutsDataFetcher())
                        .dataFetcher("layoutsForGroup", layoutResolvers.getLayoutsForGroupDataFetcher())
                        .dataFetcher("layout", layoutResolvers.getLayoutDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Layout

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.ListType
                        .dataFetcher("listTypes", listTypeResolvers.getListTypesDataFetcher())
                        .dataFetcher("listTypesByType", listTypeResolvers.getListTypesByTypeDataFetcher())
                        .dataFetcher("listType", listTypeResolvers.getListTypeDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.ListType

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Organization
                        .dataFetcher("organizations", organizationResolvers.getOrganizationsDataFetcher())
                        .dataFetcher("organization", organizationResolvers.getOrganizationDataFetcher())
                        .dataFetcher("organizationByName", organizationResolvers.getOrganizationByNameDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Organization

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Phone
                        .dataFetcher("phones", phoneResolvers.getPhonesDataFetcher())
                        .dataFetcher("phonesForEntity", phoneResolvers.getPhonesForEntityDataFetcher())
                        .dataFetcher("phone", phoneResolvers.getPhoneDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Phone

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Region
                        .dataFetcher("regions", regionResolvers.getRegionsDataFetcher())
                        .dataFetcher("region", regionResolvers.getRegionDataFetcher())
                        .dataFetcher("regionByCode", regionResolvers.getRegionByCodeDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Region

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Role
                        .dataFetcher("roles", roleResolvers.getRolesDataFetcher())
                        .dataFetcher("role", roleResolvers.getRoleDataFetcher())
                        .dataFetcher("roleByName", roleResolvers.getRoleByNameDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Role

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.User
                        .dataFetcher("users", userResolvers.getUsersDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("userByEmail", userResolvers.getUserByEmailDataFetcher())
                        .dataFetcher("userByScreenName", userResolvers.getUserByScreenNameDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.User

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.UserGroup
                        .dataFetcher("userGroups", userGroupResolvers.getUserGroupsDataFetcher())
                        .dataFetcher("userGroup", userGroupResolvers.getUserGroupDataFetcher())
                        .dataFetcher("userGroupByName", userGroupResolvers.getUserGroupByNameDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.UserGroup

                        // START -- Query resolvers for class com.liferay.portal.kernel.model.Website
                        .dataFetcher("websites", websiteResolvers.getWebsitesDataFetcher())
                        .dataFetcher("websitesForEntity", websiteResolvers.getWebsitesForEntityDataFetcher())
                        .dataFetcher("website", websiteResolvers.getWebsiteDataFetcher())
                        // END -- Query resolvers for class com.liferay.portal.kernel.model.Website

                        // START -- Query resolvers for class com.liferay.ratings.kernel.model.RatingsEntry
                        .dataFetcher("ratingsEntries", ratingsEntryResolvers.getRatingsEntriesDataFetcher())
                        .dataFetcher("ratingsEntry", ratingsEntryResolvers.getRatingsEntryDataFetcher())
                        // END -- Query resolvers for class com.liferay.ratings.kernel.model.RatingsEntry

                )
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                        // START -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetCategory
                        .dataFetcher("createAssetCategory", assetCategoryResolvers.createAssetCategoryDataFetcher())
                        .dataFetcher("updateAssetCategory", assetCategoryResolvers.updateAssetCategoryDataFetcher())
                        .dataFetcher("deleteAssetCategory", assetCategoryResolvers.deleteAssetCategoryDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetCategory

                        // START -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetEntry
                        .dataFetcher("associateAssetEntryWithCategory", assetEntryResolvers.associateAssetEntryWithCategoryDataFetcher())
                        .dataFetcher("dissociateAssetEntryFromCategory", assetEntryResolvers.dissociateAssetEntryFromCategoryDataFetcher())
                        .dataFetcher("associateAssetEntryWithTag", assetEntryResolvers.associateAssetEntryWithTagDataFetcher())
                        .dataFetcher("dissociateAssetEntryFromTag", assetEntryResolvers.dissociateAssetEntryFromTagDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetEntry

                        // START -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetLink
                        .dataFetcher("createAssetLink", assetLinkResolvers.createAssetLinkDataFetcher())
                        .dataFetcher("updateAssetLink", assetLinkResolvers.updateAssetLinkDataFetcher())
                        .dataFetcher("deleteAssetLink", assetLinkResolvers.deleteAssetLinkDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetLink

                        // START -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetTag
                        .dataFetcher("createAssetTag", assetTagResolvers.createAssetTagDataFetcher())
                        .dataFetcher("updateAssetTag", assetTagResolvers.updateAssetTagDataFetcher())
                        .dataFetcher("deleteAssetTag", assetTagResolvers.deleteAssetTagDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetTag

                        // START -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetVocabulary
                        .dataFetcher("createAssetVocabulary", assetVocabularyResolvers.createAssetVocabularyDataFetcher())
                        .dataFetcher("updateAssetVocabulary", assetVocabularyResolvers.updateAssetVocabularyDataFetcher())
                        .dataFetcher("deleteAssetVocabulary", assetVocabularyResolvers.deleteAssetVocabularyDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.asset.kernel.model.AssetVocabulary

                        // START -- Mutation resolvers for class com.liferay.blogs.model.BlogsEntry
                        .dataFetcher("createBlogsEntry", blogsEntryResolvers.createBlogsEntryDataFetcher())
                        .dataFetcher("updateBlogsEntry", blogsEntryResolvers.updateBlogsEntryDataFetcher())
                        .dataFetcher("deleteBlogsEntry", blogsEntryResolvers.deleteBlogsEntryDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.blogs.model.BlogsEntry

                        // START -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileEntry
                        .dataFetcher("createDLFileEntry", dlFileEntryResolvers.createDLFileEntryDataFetcher())
                        .dataFetcher("updateDLFileEntry", dlFileEntryResolvers.updateDLFileEntryDataFetcher())
                        .dataFetcher("deleteDLFileEntry", dlFileEntryResolvers.deleteDLFileEntryDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileEntry

                        // START -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileEntryMetadata
                        // No mutations
                        // END -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileEntryMetadata

                        // START -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileEntryType
                        .dataFetcher("createDLFileEntryType", dlFileEntryTypeResolvers.createDLFileEntryTypeDataFetcher())
                        .dataFetcher("updateDLFileEntryType", dlFileEntryTypeResolvers.updateDLFileEntryTypeDataFetcher())
                        .dataFetcher("deleteDLFileEntryType", dlFileEntryTypeResolvers.deleteDLFileEntryTypeDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileEntryType

                        // START -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileShortcut
                        .dataFetcher("createDLFileShortcut", dlFileShortcutResolvers.createDLFileShortcutDataFetcher())
                        .dataFetcher("updateDLFileShortcut", dlFileShortcutResolvers.updateDLFileShortcutDataFetcher())
                        .dataFetcher("deleteDLFileShortcut", dlFileShortcutResolvers.deleteDLFileShortcutDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileShortcut

                        // START -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileVersion
                        // No mutations
                        // END -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFileVersion

                        // START -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFolder
                        .dataFetcher("createDLFolder", dlFolderResolvers.createDLFolderDataFetcher())
                        .dataFetcher("updateDLFolder", dlFolderResolvers.updateDLFolderDataFetcher())
                        .dataFetcher("deleteDLFolder", dlFolderResolvers.deleteDLFolderDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.document.library.kernel.model.DLFolder

                        // START -- Mutation resolvers for class com.liferay.dynamic.data.mapping.model.DDMContent
                        .dataFetcher("createDDMContent", ddmContentResolvers.createDDMContentDataFetcher())
                        .dataFetcher("updateDDMContent", ddmContentResolvers.updateDDMContentDataFetcher())
                        .dataFetcher("deleteDDMContent", ddmContentResolvers.deleteDDMContentDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.dynamic.data.mapping.model.DDMContent

                        // START -- Mutation resolvers for class com.liferay.dynamic.data.mapping.model.DDMStructure
                        .dataFetcher("createDDMStructure", ddmStructureResolvers.createDDMStructureDataFetcher())
                        .dataFetcher("createDDMStructureForJournalArticle", ddmStructureResolvers.createDDMStructureForJournalArticleDataFetcher())
                        .dataFetcher("updateDDMStructure", ddmStructureResolvers.updateDDMStructureDataFetcher())
                        .dataFetcher("updateDDMStructureForJournalArticle", ddmStructureResolvers.updateDDMStructureForJournalArticleDataFetcher())
                        .dataFetcher("deleteDDMStructure", ddmStructureResolvers.deleteDDMStructureDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.dynamic.data.mapping.model.DDMStructure

                        // START -- Mutation resolvers for class com.liferay.dynamic.data.mapping.model.DDMTemplate
                        .dataFetcher("createDDMTemplate", ddmTemplateResolvers.createDDMTemplateDataFetcher())
                        .dataFetcher("updateDDMTemplate", ddmTemplateResolvers.updateDDMTemplateDataFetcher())
                        .dataFetcher("deleteDDMTemplate", ddmTemplateResolvers.deleteDDMTemplateDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.dynamic.data.mapping.model.DDMTemplate

                        // START -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoColumn
                        .dataFetcher("createExpandoColumn", expandoColumnResolvers.createExpandoColumnDataFetcher())
                        .dataFetcher("updateExpandoColumn", expandoColumnResolvers.updateExpandoColumnDataFetcher())
                        .dataFetcher("deleteExpandoColumn", expandoColumnResolvers.deleteExpandoColumnDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoColumn

                        // START -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoRow
                        .dataFetcher("createExpandoRow", expandoRowResolvers.createExpandoRowDataFetcher())
                        .dataFetcher("deleteExpandoRow", expandoRowResolvers.deleteExpandoRowDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoRow

                        // START -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoTable
                        .dataFetcher("createExpandoTable", expandoTableResolvers.createExpandoTableDataFetcher())
                        .dataFetcher("updateExpandoTable", expandoTableResolvers.updateExpandoTableDataFetcher())
                        .dataFetcher("deleteExpandoTable", expandoTableResolvers.deleteExpandoTableDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoTable

                        // START -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoValue
                        .dataFetcher("createExpandoValueForString", expandoValueResolvers.createExpandoValueForStringDataFetcher())
                        .dataFetcher("createExpandoValueForStringForInt", expandoValueResolvers.createExpandoValueForIntDataFetcher())
                        .dataFetcher("createExpandoValueForStringForLong", expandoValueResolvers.createExpandoValueForLongDataFetcher())
                        .dataFetcher("createExpandoValueForStringForDouble", expandoValueResolvers.createExpandoValueForDoubleDataFetcher())
                        .dataFetcher("createExpandoValueForStringForBoolean", expandoValueResolvers.createExpandoValueForBooleanDataFetcher())
                        .dataFetcher("deleteExpandoValue", expandoValueResolvers.deleteExpandoValueDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.expando.kernel.model.ExpandoValue

                        // START -- Mutation resolvers for class com.liferay.journal.model.JournalArticle
                        .dataFetcher("createJournalArticle", journalArticleResolvers.createJournalArticleDataFetcher())
                        .dataFetcher("updateJournalArticle", journalArticleResolvers.updateJournalArticleDataFetcher())
                        .dataFetcher("deleteJournalArticle", journalArticleResolvers.deleteJournalArticleDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.journal.model.JournalArticle

                        // START -- Mutation resolvers for class com.liferay.journal.model.JournalFeed
                        .dataFetcher("createJournalFeed", journalFeedResolvers.createJournalFeedDataFetcher())
                        .dataFetcher("updateJournalFeed", journalFeedResolvers.updateJournalFeedDataFetcher())
                        .dataFetcher("deleteJournalFeed", journalFeedResolvers.deleteJournalFeedDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.journal.model.JournalFeed

                        // START -- Mutation resolvers for class com.liferay.journal.model.JournalFolder
                        .dataFetcher("createJournalFolder", journalFolderResolvers.createJournalFolderDataFetcher())
                        .dataFetcher("updateJournalFolder", journalFolderResolvers.updateJournalFolderDataFetcher())
                        .dataFetcher("deleteJournalFolder", journalFolderResolvers.deleteJournalFolderDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.journal.model.JournalFolder

                        // START -- Mutation resolvers for class com.liferay.message.boards.kernel.model.MBCategory
                        .dataFetcher("createMBCategory", mbCategoryResolvers.createMBCategoryDataFetcher())
                        .dataFetcher("updateMBCategory", mbCategoryResolvers.updateMBCategoryDataFetcher())
                        .dataFetcher("deleteMBCategory", mbCategoryResolvers.deleteMBCategoryDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.message.boards.kernel.model.MBCategory

                        // START -- Mutation resolvers for class com.liferay.message.boards.kernel.model.MBMessage
                        .dataFetcher("createMBMessage", mbMessageResolvers.createMBMessageDataFetcher())
                        .dataFetcher("updateMBMessage", mbMessageResolvers.updateMBMessageDataFetcher())
                        .dataFetcher("deleteMBMessage", mbMessageResolvers.deleteMBMessageDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.message.boards.kernel.model.MBMessage

                        // START -- Mutation resolvers for class com.liferay.message.boards.kernel.model.MBThread
                        .dataFetcher("createMBThread", mbThreadResolvers.createMBThreadDataFetcher())
                        // No update method available
                        .dataFetcher("deleteMBThread", mbThreadResolvers.deleteMBThreadDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.message.boards.kernel.model.MBThread

                        // START -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2Application
                        .dataFetcher("createOAuth2Application", oAuth2ApplicationResolvers.createOAuth2ApplicationDataFetcher())
                        .dataFetcher("updateOAuth2Application", oAuth2ApplicationResolvers.updateOAuth2ApplicationDataFetcher())
                        .dataFetcher("deleteOAuth2Application", oAuth2ApplicationResolvers.deleteOAuth2ApplicationDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2Application

                        // START -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases
                        .dataFetcher("createOAuth2ApplicationScopeAliases", oAuth2ApplicationScopeAliasesResolvers.createOAuth2ApplicationScopeAliasesDataFetcher())
                        // No update method available
                        .dataFetcher("deleteOAuth2ApplicationScopeAliases", oAuth2ApplicationScopeAliasesResolvers.deleteOAuth2ApplicationScopeAliasesDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2ApplicationScopeAliases

                        // START -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2Authorization
                        .dataFetcher("createOAuth2Authorization", oAuth2AuthorizationResolvers.createOAuth2AuthorizationDataFetcher())
                        .dataFetcher("deleteOAuth2Authorization", oAuth2AuthorizationResolvers.deleteOAuth2AuthorizationDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2Authorization

                        // START -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2ScopeGrant
                        .dataFetcher("associateOAuth2ScopeGrantWithAuthorization", oAuth2ScopeGrantResolvers.associateOAuth2ScopeGrantWithAuthorizationDataFetcher())
                        .dataFetcher("dissociateOAuth2ScopeGrantFromAuthorization", oAuth2ScopeGrantResolvers.dissociateOAuth2ScopeGrantFromAuthorizationDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.oauth2.provider.model.OAuth2ScopeGrant

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Address
                        .dataFetcher("createAddress", addressResolvers.createAddressDataFetcher())
                        .dataFetcher("updateAddress", addressResolvers.updateAddressDataFetcher())
                        .dataFetcher("deleteAddress", addressResolvers.deleteAddressDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Address

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.ClassName
                        // No mutations
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.ClassName

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Company
                        .dataFetcher("createCompany", companyResolvers.createCompanyDataFetcher())
                        .dataFetcher("updateCompany", companyResolvers.updateCompanyDataFetcher())
                        .dataFetcher("deleteCompany", companyResolvers.deleteCompanyDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Company

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Contact
                        .dataFetcher("createContact", contactResolvers.createContactDataFetcher())
                        .dataFetcher("updateContact", contactResolvers.updateContactDataFetcher())
                        .dataFetcher("deleteContact", contactResolvers.deleteContactDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Contact

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Country
                        .dataFetcher("createCountry", countryResolvers.createCountryDataFetcher())
                        // No update or delete methods available
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Country

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.EmailAddress
                        .dataFetcher("createEmailAddress", emailAddressResolvers.createEmailAddressDataFetcher())
                        .dataFetcher("updateEmailAddress", emailAddressResolvers.updateEmailAddressDataFetcher())
                        .dataFetcher("deleteEmailAddress", emailAddressResolvers.deleteEmailAddressDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.EmailAddress

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Group
                        .dataFetcher("createGroup", groupResolvers.createGroupDataFetcher())
                        .dataFetcher("updateGroup", groupResolvers.updateGroupDataFetcher())
                        .dataFetcher("deleteGroup", groupResolvers.deleteGroupDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Group

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Layout
                        .dataFetcher("createLayout", layoutResolvers.createLayoutDataFetcher())
                        .dataFetcher("updateLayout", layoutResolvers.updateLayoutDataFetcher())
                        .dataFetcher("deleteLayout", layoutResolvers.deleteLayoutDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Layout

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.ListType
                        .dataFetcher("createListType", listTypeResolvers.createListTypeDataFetcher())
                        // No update method available
                        .dataFetcher("deleteListType", listTypeResolvers.deleteListTypeDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.ListType

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Organization
                        .dataFetcher("createOrganization", organizationResolvers.createOrganizationDataFetcher())
                        .dataFetcher("updateOrganization", organizationResolvers.updateOrganizationDataFetcher())
                        .dataFetcher("deleteOrganization", organizationResolvers.deleteOrganizationDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Organization

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Phone
                        .dataFetcher("createPhone", phoneResolvers.createPhoneDataFetcher())
                        .dataFetcher("updatePhone", phoneResolvers.updatePhoneDataFetcher())
                        .dataFetcher("deletePhone", phoneResolvers.deletePhoneDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Phone

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Region
                        .dataFetcher("createRegion", regionResolvers.createRegionDataFetcher())
                        // No update or delete methods available
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Region

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Role
                        .dataFetcher("createRole", roleResolvers.createRoleDataFetcher())
                        .dataFetcher("updateRole", roleResolvers.updateRoleDataFetcher())
                        .dataFetcher("deleteRole", roleResolvers.deleteRoleDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Role

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.User
                        .dataFetcher("createUser", userResolvers.createUserDataFetcher())
                        .dataFetcher("updateUser", userResolvers.updateUserDataFetcher())
                        .dataFetcher("deleteUser", userResolvers.deleteUserDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.User

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.UserGroup
                        .dataFetcher("createUserGroup", userGroupResolvers.createUserGroupDataFetcher())
                        .dataFetcher("updateUserGroup", userGroupResolvers.updateUserGroupDataFetcher())
                        .dataFetcher("deleteUserGroup", userGroupResolvers.deleteUserGroupDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.UserGroup

                        // START -- Mutation resolvers for class com.liferay.portal.kernel.model.Website
                        .dataFetcher("createWebsite", websiteResolvers.createWebsiteDataFetcher())
                        .dataFetcher("updateWebsite", websiteResolvers.updateWebsiteDataFetcher())
                        .dataFetcher("deleteWebsite", websiteResolvers.deleteWebsiteDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.portal.kernel.model.Website

                        // START -- Mutation resolvers for class com.liferay.ratings.kernel.model.RatingsEntry
                        .dataFetcher("createRatingsEntry", ratingsEntryResolvers.createRatingsEntryDataFetcher())
                        .dataFetcher("deleteRatingsEntry", ratingsEntryResolvers.deleteRatingsEntryDataFetcher())
                        // END -- Mutation resolvers for class com.liferay.ratings.kernel.model.RatingsEntry

                )
                .type(TypeRuntimeWiring.newTypeWiring("AssetCategory")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentCategory", assetCategoryResolvers.getAssetCategoryDataFetcher())
                        .dataFetcher("leftCategory", assetCategoryResolvers.getAssetCategoryDataFetcher())
                        .dataFetcher("rightCategory", assetCategoryResolvers.getAssetCategoryDataFetcher())
                        .dataFetcher("vocabulary", assetVocabularyResolvers.getAssetVocabularyDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("AssetEntry")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("AssetLink")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("entry1", assetEntryResolvers.getAssetEntryDataFetcher())
                        .dataFetcher("entry2", assetEntryResolvers.getAssetEntryDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("AssetTag")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("AssetVocabulary")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("BlogsEntry")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DLFileEntry")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("fileEntryType", dlFileEntryTypeResolvers.getDLFileEntryTypeDataFetcher())
                        .dataFetcher("folder", dlFolderResolvers.getDLFolderDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DLFileEntryMetadata")
                        .dataFetcher("structure", ddmStructureResolvers.getDDMStructureDataFetcher())
                        .dataFetcher("fileEntry", dlFileEntryTypeResolvers.getDLFileEntryTypeDataFetcher())
                        .dataFetcher("fileVersion", dlFileVersionResolvers.getDLFileVersionDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DLFileEntryType")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DLFileShortcut")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("folder", dlFolderResolvers.getDLFolderDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DLFileVersion")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("fileEntry", dlFileEntryTypeResolvers.getDLFileEntryTypeDataFetcher())
                        .dataFetcher("fileEntryType", dlFileEntryTypeResolvers.getDLFileEntryTypeDataFetcher())
                        .dataFetcher("folder", dlFolderResolvers.getDLFolderDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DLFolder")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentFolder", dlFolderResolvers.getDLFolderDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DDMContent")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DDMStructure")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentStructure", ddmStructureResolvers.getDDMStructureDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("DDMTemplate")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("ExpandoColumn")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("table", expandoTableResolvers.getExpandoTableDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("ExpandoRow")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("table", expandoTableResolvers.getExpandoTableDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("ExpandoTable")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("ExpandoValue")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("column", expandoColumnResolvers.getExpandoColumnDataFetcher())
                        .dataFetcher("row", expandoRowResolvers.getExpandoRowDataFetcher())
                        .dataFetcher("table", expandoTableResolvers.getExpandoTableDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("JournalArticle")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("folder", journalFolderResolvers.getJournalFolderDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("JournalFeed")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("JournalFolder")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentFolder", journalFolderResolvers.getJournalFolderDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("MBCategory")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentCategory", mbCategoryResolvers.getMBCategoryDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("MBMessage")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentMessage", mbMessageResolvers.getMBMessageDataFetcher())
                        .dataFetcher("rootMessage", mbMessageResolvers.getMBMessageDataFetcher())
                        .dataFetcher("thread", mbThreadResolvers.getMBThreadDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("MBThread")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("rootMessage", mbMessageResolvers.getMBMessageDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("OAuth2Application")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("oAuth2ApplicationScopeAliases", oAuth2ApplicationScopeAliasesResolvers.getOAuth2ApplicationScopeAliasesDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("OAuth2ApplicationScopeAliases")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("oAuth2Application", oAuth2ApplicationResolvers.getOAuth2ApplicationDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("OAuth2Authorization")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("oAuth2Application", oAuth2ApplicationResolvers.getOAuth2ApplicationDataFetcher())
                        .dataFetcher("oAuth2ApplicationScopeAliases", oAuth2ApplicationScopeAliasesResolvers.getOAuth2ApplicationScopeAliasesDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("OAuth2ScopeGrant")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("oAuth2ApplicationScopeAliases", oAuth2ApplicationScopeAliasesResolvers.getOAuth2ApplicationScopeAliasesDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Address")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("country", countryResolvers.getCountryDataFetcher())
                        .dataFetcher("region", regionResolvers.getRegionDataFetcher())
                        .dataFetcher("type", listTypeResolvers.getListTypeDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Contact")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentContact", contactResolvers.getContactDataFetcher())
                        .dataFetcher("prefix", listTypeResolvers.getListTypeDataFetcher())
                        .dataFetcher("suffix", listTypeResolvers.getListTypeDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("EmailAddress")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("type", listTypeResolvers.getListTypeDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Group")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("creatorUser", userResolvers.getUserDataFetcher())
                        .dataFetcher("liveGroup", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("parentGroup", groupResolvers.getGroupDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Layout")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("group", groupResolvers.getGroupDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Organization")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("country", countryResolvers.getCountryDataFetcher())
                        .dataFetcher("region", regionResolvers.getRegionDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Phone")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("type", listTypeResolvers.getListTypeDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Role")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Role")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("contact", contactResolvers.getContactDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("UserGroup")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("parentUserGroup", userGroupResolvers.getUserGroupDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Website")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                        .dataFetcher("type", listTypeResolvers.getListTypeDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("RatingsEntry")
                        .dataFetcher("company", companyResolvers.getCompanyDataFetcher())
                        .dataFetcher("user", userResolvers.getUserDataFetcher())
                )
                .type(TypeRuntimeWiring.newTypeWiring("GrantType")
                        .enumValues(GrantType::valueOf)
                )
                .build();
    }

    @Override
    public ExecutionResult executeQuery(String query,
                                        String operationName,
                                        Map<String, Object> variables) {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables)
                .dataLoaderRegistry(graphQLRegistry)
                .build();
        return graphQL.execute(executionInput);
    }

    @Override
    public ExecutionResult executeQuery(String query) {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .dataLoaderRegistry(graphQLRegistry)
                .build();
        return graphQL.execute(executionInput);
    }
}
