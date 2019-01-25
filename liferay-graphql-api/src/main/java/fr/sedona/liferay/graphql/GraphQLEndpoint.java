package fr.sedona.liferay.graphql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import fr.sedona.liferay.graphql.engine.GraphQLEngine;
import fr.sedona.liferay.graphql.util.Constants;
import graphql.ExecutionResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component(
        immediate = true,
        property = {
                JaxrsWhiteboardConstants.JAX_RS_APPLICATION_BASE + "=/graphql",
                JaxrsWhiteboardConstants.JAX_RS_NAME + "=GraphQL.Rest"
        },
        service = Application.class
)
public class GraphQLEndpoint extends Application {
    private static final Log LOGGER = LogFactoryUtil.getLog(GraphQLEndpoint.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private GraphQLEngine graphQLEngine;

    @Override
    public Set<Object> getSingletons() {
        LOGGER.info("Adding GraphQL endpoint");
        Set<Object> singletons = new HashSet<>();
        //add the automated Jackson marshaller for JSON
        singletons.add(new JacksonJsonProvider());
        // add our REST endpoints (resources)
        singletons.add(this);
        return singletons;
    }

    @GET
    @Path(Constants.ENDPOINT_SCHEMA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getGraphQLSchema() {
        return Response.ok(graphQLEngine.getSchema())
                .build();
    }

    @GET
    @Path(Constants.ENDPOINT_API)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGraphQLRequest(@QueryParam(Constants.PARAM_QUERY) String query,
                                      @QueryParam(Constants.PARAM_OPERATION_NAME) String operationName,
                                      @QueryParam(Constants.PARAM_VARIABLES) String jsonVars) {
        if (query == null) {
            query = "";
        }

        Map<String, Object> variables = new LinkedHashMap<>();
        if (jsonVars != null) {
            try {
                variables = objectMapper.readValue(jsonVars, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                String msg = String.format("Could not convert '%s' parameter to Object: %s", Constants.PARAM_VARIABLES, jsonVars);
                LOGGER.warn(msg);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(msg)
                        .build();
            }
        }

        ExecutionResult executionResult = graphQLEngine.executeQuery(query, operationName, variables);
        Map<String, Object> result = executionResult.toSpecification();
        return Response.ok(result)
                .build();
    }

    @POST
    @Path(Constants.ENDPOINT_API)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postGraphQLRequestAsJson(Map<String, Object> body) {
        String query = (String) body.get(Constants.PARAM_QUERY);
        if (query == null) {
            query = "";
        }

        String operationName = (String) body.get(Constants.PARAM_OPERATION_NAME);
        Map<String, Object> variables = (Map<String, Object>) body.get(Constants.PARAM_VARIABLES);
        if (variables == null) {
            variables = new LinkedHashMap<>();
        }

        ExecutionResult executionResult = graphQLEngine.executeQuery(query, operationName, variables);
        Map<String, Object> result = executionResult.toSpecification();
        return Response.ok(result)
                .build();
    }

    @POST
    @Path(Constants.ENDPOINT_API)
    @Consumes(Constants.MEDIA_TYPE_GRAPHQL)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postGraphQLRequestAsGraphQLQuery(String body) {
        if (body == null) {
            body = "";
        }

        ExecutionResult executionResult = graphQLEngine.executeQuery(body);
        Map<String, Object> result = executionResult.toSpecification();
        return Response.ok(result)
                .build();
    }
}
