# Liferay GraphQL Maven plugin

## Description

This Maven plugin is a helper to generate all "generic" classes needed for the [API](../liferay-graphql-api/README.md) module.

The class generation uses handlebars templates and provides a "rough estimate" of the final class. For each generated classes, it is recommended to check and customize upon needs. The generation covers the following points:
- GraphQL schema file named `liferay-schema.graphql`
- GraphQL engine implementation named `GraphQLEngineImpl.java`
- GraphQL registry for the data loader named `GraphQLRegistry.java`
- GraphQL batch loader for each Liferay model named `<Model>BatchLoader.java`
- GraphQL resolvers (interface and implementation) for each Liferay model named `<Model>Resolvers.java` and `<Model>ResolversImpl.java`

## Usage

```xml
<plugin>
    <groupId>fr.sedona.liferay</groupId>
    <artifactId>liferay-graphql-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <dependencies>
        <!-- Liferay -->
        <dependency>
            <groupId>com.liferay.portal</groupId>
            <artifactId>com.liferay.portal.kernel</artifactId>
            <version>${com.liferay.portal.kernel.version}</version>
        </dependency>
        <dependency>
            <groupId>com.liferay</groupId>
            <artifactId>com.liferay.blogs.api</artifactId>
            <version>${com.liferay.blogs.api.version}</version>
        </dependency>
        <dependency>
            <groupId>com.liferay</groupId>
            <artifactId>com.liferay.dynamic.data.mapping.api</artifactId>
            <version>${com.liferay.dynamic.data.mapping.api.version}</version>
        </dependency>
        <dependency>
            <groupId>com.liferay</groupId>
            <artifactId>com.liferay.journal.api</artifactId>
            <version>${com.liferay.journal.api.version}</version>
        </dependency>
        <dependency>
            <groupId>com.liferay</groupId>
            <artifactId>com.liferay.oauth2.provider.api</artifactId>
            <version>${com.liferay.oauth2.provider.api.version}</version>
        </dependency>
    </dependencies>
    <executions>
        <execution>
            <id>generate-sources</id>
            <phase>none</phase>
            <goals>
                <goal>generate-sources</goal>
            </goals>
            <configuration>
                <outputResolversDir>
                    ${basedir}/src/main/java/fr/sedona/liferay/graphql/resolvers
                </outputResolversDir>
                <outputResolversImplDir>
                    ${basedir}/src/main/java/fr/sedona/liferay/graphql/resolvers/impl
                </outputResolversImplDir>
                <outputBatchLoaderDir>
                    ${basedir}/src/main/java/fr/sedona/liferay/graphql/loaders
                </outputBatchLoaderDir>
                <outputRegistryDir>
                    ${basedir}/src/main/java/fr/sedona/liferay/graphql/util
                </outputRegistryDir>
                <outputEngineImplDir>
                    ${basedir}/src/main/java/fr/sedona/liferay/graphql/engine/impl
                </outputEngineImplDir>
                <outputSchemaDir>
                    ${basedir}/src/main/resources
                </outputSchemaDir>
                <importablePropertiesFile>
                    ${basedir}/src/main/graphql/generation.properties
                </importablePropertiesFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

The plugin will use reflection to determine all methods and attributes for each Liferay model. Thus, it is important to define the `dependencies` for the Maven plugin. Otherwise the plugin won't find the specified classes.

## Goals

The plugin has a unique goal called `generate-sources`. It is recommended to run this goal during the phase `generate-sources`.

## Configuration parameters

| Name                     | Default value | Mandatory | Description                                                             |
|------------------------- |-------------- |---------- |------------------------------------------------------------------------ |
| outputResolversDir       | N/A           | true      | A path to a folder where the interface resolvers will be generated      |
| outputResolversImplDir   | N/A           | true      | A path to a folder where the resolver implementations will be generated |
| outputBatchLoaderDir     | N/A           | true      | A path to a folder where the batch loaders will be generated            |
| outputRegistryDir        | N/A           | true      | A path to a folder where the registry will be generated                 |
| outputEngineImplDir      | N/A           | true      | A path to a folder where the engine implementation will be generated    |
| outputSchemaDir          | N/A           | true      | A path to a folder where the schema will be generated                   |
| importablePropertiesFile | N/A           | true      | A path to a properties file containing all generation description       |

## Generation properties file

### Global properties

```properties
# Ignored attributes
liferay.model.ignore.attributes=cachedModel,escapedModel,new,primaryKeyObj,expandoBridge,trashHandler,trashEntry
liferay.overwrite.schema.file=false
```

- `liferay.model.ignore.attributes`: Defines all attributes that should be ignored while generating classes.
- `liferay.overwrite.schema.file`: Indicates if the schema file should be overwritten if it already exists.

### Model specific properties

```properties
# Model: com.liferay.portal.kernel.model.Company
liferay.overwrite.com.liferay.portal.kernel.model.Company=true
liferay.service.com.liferay.portal.kernel.model.Company=com.liferay.portal.kernel.service.CompanyLocalService
liferay.model.com.liferay.portal.kernel.model.Company=com.liferay.portal.kernel.model.CompanyModel
liferay.method.read-all.com.liferay.portal.kernel.model.Company=getCompanies
liferay.method.read.com.liferay.portal.kernel.model.Company=getCompany
liferay.method.create.com.liferay.portal.kernel.model.Company=addCompany
liferay.method.update.com.liferay.portal.kernel.model.Company=updateCompany
liferay.method.delete.com.liferay.portal.kernel.model.Company=deleteCompany
```

The properties key have a composite name containing the fully qualified class name (in the example above: `com.liferay.portal.kernel.model.Company`).

- `liferay.overwrite.<model>`: Indicates if the model specific files should be overwritten if they already exist.
- `liferay.service.<model>`: Defines the fully qualified class name of the associated local service interface.
- `liferay.model.<model>`: Defines the fully qualified class name of the associated model interface
- `liferay.method.read-all.<model>`: Defines the name of the 'read all' method. If not specified, it will default to `get<Model name in plural>` (in the example above: `Company` -> `getCompanies`).
- `liferay.method.read.<model>`: Defines the name of the 'read one' method. If not specified, it will default to `get<Model name>` (in the example above: `Company` -> `getCompany`).
- `liferay.method.create.<model>`: Defines the name of the 'create' method. If not specified, it will default to `add<Model name>` (in the example above: `Company` -> `addCompany`).
- `liferay.method.update.<model>`: Defines the name of the 'update' method. If not specified, it will default to `update<Model name>` (in the example above: `Company` -> `updateCompany`).
- `liferay.method.delete.<model>`: Defines the name of the 'delete' method. If not specified, it will default to `delete<Model name>` (in the example above: `Company` -> `deleteCompany`).

The CRUD methods will be looked up in the local service interface and used in the resolver implementation.
