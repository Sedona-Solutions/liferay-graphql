package fr.sedona.liferay.graphql.engine;

import aQute.bnd.annotation.ProviderType;
import graphql.ExecutionResult;

import java.io.BufferedReader;
import java.util.Map;

@ProviderType
public interface GraphQLEngine {

    String getSchema();

    ExecutionResult executeQuery(String query,
                                 String operationName,
                                 Map<String, Object> variables);

    ExecutionResult executeQuery(String query);
}
