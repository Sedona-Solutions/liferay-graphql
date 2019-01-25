package fr.sedona.liferay.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sedona.liferay.graphql.engine.GraphQLEngine;
import fr.sedona.liferay.graphql.util.Constants;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.InvalidSyntaxError;
import graphql.language.SourceLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.valid4j.matchers.http.HttpResponseMatchers.hasEntity;
import static org.valid4j.matchers.http.HttpResponseMatchers.hasStatus;

/**
 * Test suite for {@link GraphQLEndpoint}
 */
@RunWith(PowerMockRunner.class)
public class GraphQLEndpointTest {
    private String query;
    private String operationName;
    private Map<String, Object> jsonVars;
    private String jsonVarsAsString;
    private ExpectedResult expectedOkResult;
    private InvalidSyntaxError invalidSyntaxError;

    private class ExpectedResult {
        private String status;

        ExpectedResult(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    @InjectMocks
    GraphQLEndpoint graphQLEndpoint = new GraphQLEndpoint();

    @Mock
    private GraphQLEngine graphQLEngine;

    @Before
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        query = "{ testQuery { status } }";
        operationName = "testQuery";
        jsonVars = new HashMap<>();
        jsonVarsAsString = objectMapper.writeValueAsString(jsonVars);

        expectedOkResult = new ExpectedResult("OK!");
        invalidSyntaxError = new InvalidSyntaxError(new SourceLocation(1, 0), "Invalid Syntax");
    }

    private String getSchema() {
        InputStream is = getClass().getResourceAsStream(Constants.SCHEMA_FILE);
        return new BufferedReader(new InputStreamReader(is))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    @Test
    public void getGraphQLSchema_should_return_ok_with_schema() {
        // Given
        // Nothing

        // When / Then
        when(graphQLEngine.getSchema())
                .thenReturn(getSchema());

        // Asserts
        Response response = graphQLEndpoint.getGraphQLSchema();
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), getSchema());
    }

    @Test
    public void getGraphQLRequest_with_all_params_should_return_ok_with_data() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(query, operationName, jsonVars))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.getGraphQLRequest(query, operationName, jsonVarsAsString);
        verify(graphQLEngine, times(1))
                .executeQuery(query, operationName, jsonVars);
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void getGraphQLRequest_without_operationName_should_return_ok_with_data() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(query, null, jsonVars))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.getGraphQLRequest(query, null, jsonVarsAsString);
        verify(graphQLEngine, times(1))
                .executeQuery(query, null, jsonVars);
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void getGraphQLRequest_without_jsonVars_should_return_ok_with_data() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq(query), eq(operationName), anyMap()))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.getGraphQLRequest(query, operationName, null);
        verify(graphQLEngine, times(1))
                .executeQuery(eq(query), eq(operationName), anyMap());
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void getGraphQLRequest_with_only_query_should_return_ok_with_data() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq(query), eq(null), anyMap()))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.getGraphQLRequest(query, null, null);
        verify(graphQLEngine, times(1))
                .executeQuery(eq(query), eq(null), anyMap());
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void getGraphQLRequest_without_any_params_should_return_ok_with_errors() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(Collections.singletonList(invalidSyntaxError));
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq(""), eq(null), anyMap()))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.getGraphQLRequest(null, null, null);
        verify(graphQLEngine, times(1))
                .executeQuery(eq(""), eq(null), anyMap());
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void postGraphQLRequestAsJson_with_all_params_should_return_ok_with_data() {
        // Given
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.PARAM_QUERY, query);
        body.put(Constants.PARAM_OPERATION_NAME, operationName);
        body.put(Constants.PARAM_VARIABLES, jsonVars);

        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(query, operationName, jsonVars))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.postGraphQLRequestAsJson(body);
        verify(graphQLEngine, times(1))
                .executeQuery(query, operationName, jsonVars);
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void postGraphQLRequestAsJson_without_operationName_should_return_ok_with_data() {
        // Given
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.PARAM_QUERY, query);
        body.put(Constants.PARAM_VARIABLES, jsonVars);

        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(query, null, jsonVars))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.postGraphQLRequestAsJson(body);
        verify(graphQLEngine, times(1))
                .executeQuery(query, null, jsonVars);
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void postGraphQLRequestAsJson_without_jsonVars_should_return_ok_with_data() {
        // Given
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.PARAM_QUERY, query);
        body.put(Constants.PARAM_OPERATION_NAME, operationName);

        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq(query), eq(operationName), anyMap()))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.postGraphQLRequestAsJson(body);
        verify(graphQLEngine, times(1))
                .executeQuery(eq(query), eq(operationName), anyMap());
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void postGraphQLRequestAsJson_with_only_query_should_return_ok_with_data() {
        // Given
        Map<String, Object> body = new HashMap<>();
        body.put(Constants.PARAM_QUERY, query);

        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq(query), eq(null), anyMap()))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.postGraphQLRequestAsJson(body);
        verify(graphQLEngine, times(1))
                .executeQuery(eq(query), eq(null), anyMap());
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void postGraphQLRequestAsJson_without_any_params_should_return_ok_with_errors() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(Collections.singletonList(invalidSyntaxError));
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq(""), eq(null), anyMap()))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.postGraphQLRequestAsJson(new HashMap<>());
        verify(graphQLEngine, times(1))
                .executeQuery(eq(""), eq(null), anyMap());
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void postGraphQLRequestAsGraphQLQuery_with_query_should_return_ok_with_data() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(expectedOkResult, null);
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq(query)))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.postGraphQLRequestAsGraphQLQuery(query);
        verify(graphQLEngine, times(1))
                .executeQuery(eq(query));
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }

    @Test
    public void postGraphQLRequestAsGraphQLQuery_without_query_should_return_ok_with_errors() {
        // Given
        ExecutionResult expectedExecutionResult = new ExecutionResultImpl(Collections.singletonList(invalidSyntaxError));
        Map expectedOutput = expectedExecutionResult.toSpecification();

        // When / Then
        when(graphQLEngine.executeQuery(eq("")))
                .thenReturn(expectedExecutionResult);

        // Asserts
        Response response = graphQLEndpoint.postGraphQLRequestAsGraphQLQuery(null);
        verify(graphQLEngine, times(1))
                .executeQuery(eq(""));
        assertThat(response, hasStatus(Response.Status.OK));
        assertThat(response, hasEntity());
        assertEquals(response.getEntity(), expectedOutput);
    }
}
