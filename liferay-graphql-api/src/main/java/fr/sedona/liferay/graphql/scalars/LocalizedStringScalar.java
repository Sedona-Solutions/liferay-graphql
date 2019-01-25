package fr.sedona.liferay.graphql.scalars;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.StringValue;
import graphql.schema.*;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalizedStringScalar extends GraphQLScalarType {
    public static final LocalizedStringScalar INSTANCE = new LocalizedStringScalar();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LocalizedStringScalar() {
        super(
                "LocalizedString",
                "Scalar handling localized strings in Liferay. It is represented as a Map<String, String> as JSON string with the key being a language Id (like \"en_US\") and the value corresponding to the localized string",
                new Coercing<Map<String, String>, Map<String, String>>() {

                    @Override
                    public Map<String, String> serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return serializeScalar(dataFetcherResult);
                    }

                    @Override
                    public Map<String, String> parseValue(Object input) throws CoercingParseValueException {
                        return parseScalarValue(input);
                    }

                    @Override
                    public Map<String, String> parseLiteral(Object input) throws CoercingParseLiteralException {
                        return parseScalarLiteral(input);
                    }
                });
    }

    private static Map<String, String> serializeScalar(Object dataFetcherResult) throws CoercingSerializeException {
        if (!(dataFetcherResult instanceof Map)) {
            throw new CoercingSerializeException("Could not serialize as LocalizedString: " + dataFetcherResult);
        }

        Map<Object, String> translatedMap = (Map<Object, String>) dataFetcherResult;
        return translatedMap.keySet()
                .stream()
                .map(key -> {
                    if (key instanceof Locale) {
                        return new AbstractMap.SimpleEntry<>(((Locale) key).toString(), translatedMap.get(key));
                    } else {
                        return new AbstractMap.SimpleEntry<>((String) key, translatedMap.get(key));
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, String> parseScalarValue(Object input) throws CoercingParseValueException {
        if (input instanceof String) {
            String jsonString = (String) input;
            try {
                return (Map<String, String>) OBJECT_MAPPER.readValue(jsonString, HashMap.class);
            } catch (IOException e) {
                throw new CoercingParseValueException("Could not parse variable value as LocalizedString: " + jsonString);
            }
        }
        throw new CoercingParseValueException("Could not parse variable value as LocalizedString: " + input);
    }

    private static Map<String, String> parseScalarLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            String jsonString = ((StringValue) input).getValue();
            try {
                return (Map<String, String>) OBJECT_MAPPER.readValue(jsonString, HashMap.class);
            } catch (IOException e) {
                throw new CoercingParseLiteralException("Could not parse literal value as LocalizedString: " + jsonString);
            }
        }
        throw new CoercingParseLiteralException("Could not parse literal value as LocalizedString: " + input);
    }
}
