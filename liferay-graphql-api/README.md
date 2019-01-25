# Liferay GraphQL API

## Description

This is a Liferay 7.1 OSGi module that will deploy some JAX-RS REST endpoints to enable GraphQL in Liferay.

## REST Endpoints

### GET /o/graphql/schema

#### Description

This endpoint allows the retrieval of the current GraphQL schema.

### GET /o/graphql/api

#### Description

This endpoint will execute a GraphQL query specified in the parameters.

#### Parameters

| Name          | Mandatory | Description                                                                          |
|-------------- |---------- |------------------------------------------------------------------------------------- |
| query         | true      | Defines the GraphQL query to be executed                                             |
| operationName | false     | Defines which operation should be executed if the query contains multiple operations |
| variables     | false     | Defines the query variables as JSON encoded string                                   |

#### Example request

```
GET /o/graphql/api?query={me{name}}
```

#### Example response

```
{
  "data": { ... },
  "errors": [ ... ],
  "extensions": {
      "tracing": { ... }
  }
}
```

The `data` will contain the returned objects from the GraphQL query.
The `errors` will contain an array of issues encountered during the processing of the query. Might not be sent.
The `extensions.tracing` will contain tracing information of the query. Might not be sent.

### POST /o/graphql/api with JSON content

#### Description

This endpoint will execute a GraphQL query specified in the body. The body contains a JSON object defining the GraphQL query, the operation to execution if multiple are available and the variables values if needed.

#### Parameters

None

#### Headers

- `Content-Type: application/json`

#### Body

| Name          | Mandatory | Description                                                                          |
|-------------- |---------- |------------------------------------------------------------------------------------- |
| query         | true      | Defines the GraphQL query to be executed                                             |
| operationName | false     | Defines which operation should be executed if the query contains multiple operations |
| variables     | false     | Defines the query variables as JSON encoded string                                   |

#### Example request

```
POST /o/graphql/api

{
  "query": "...",
  "operationName": "...",
  "variables": { "myVariable": "someValue", ... }
}
```

#### Example response

```
{
  "data": { ... },
  "errors": [ ... ],
  "extensions": {
      "tracing": { ... }
  }
}
```

The `data` will contain the returned objects from the GraphQL query.
The `errors` will contain an array of issues encountered during the processing of the query. Might not be sent.
The `extensions.tracing` will contain tracing information of the query. Might not be sent.

### POST /o/graphql/api with GraphQL query

This endpoint will execute a GraphQL query specified in the body. The latter will contain only the GraphQL query that will be executed.

#### Parameters

None

#### Headers

- `Content-Type: application/graphql`

#### Body

The body will contain the GraphQL query directly. For example:
```
{
  me {
    name
  }
}
```

#### Example request

```
POST /o/graphql/api

{
  me {
    name
  }
}
```

#### Example response

```
{
  "data": { ... },
  "errors": [ ... ],
  "extensions": {
      "tracing": { ... }
  }
}
```

The `data` will contain the returned objects from the GraphQL query.
The `errors` will contain an array of issues encountered during the processing of the query. Might not be sent.
The `extensions.tracing` will contain tracing information of the query. Might not be sent.

## Authentication / Authorization

This module uses the JAX-RS Liferay extension and thus, it uses the OAuth2 modules to manage the authentication/authorization layer.
It is important to configure the endpoint:
* Go to the `OAuth2 Administration` in the `Control Panel`
* Create a new OAuth2 application
* Give it the name you want
* After validating the creation of the application, it should get a generated client and secret ids.
* Edit the created application
* Ensure that in the `Allowed Authorization Types` section, the option `Client Credentials` is checked
* In the `Scopes` tab, in the `GraphQL.Rest` section, check the options `make HTTP GET requests` and `make HTTP POST requests`.

To retrieve an access token, you can execute the following HTTP request on Liferay server:
POST /o/oauth2/token
- Headers:
-- Content-Type: application/x-www-form-urlencoded
-- Accept: application/json
-- Authorization: Basic <Base64("client id":"secret")>
- Body:
-- grant_type=client_credentials

Example response:
```json
{
    "access_token": "76521931259bf7251be0dc7535c0639db12e9e5bbedbfbd19e85dbf7159d10",
    "token_type": "Bearer",
    "expires_in": 600,
    "scope": "GET POST"
}
```

With this response, you will have access to the different endpoints decribed above. You will need to add the following header: `Authorization: Bearer <access token>`.
