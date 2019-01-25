package fr.sedona.liferay.graphql.scalars;

import graphql.language.StringValue;
import graphql.schema.*;

import java.time.Instant;
import java.util.Date;

public class DateScalar extends GraphQLScalarType {
    public static final DateScalar INSTANCE = new DateScalar();

    private DateScalar() {
        super(
                "Date",
                "Scalar handling java.util.Date. The output is an ISO 8601 formatted string",
                new Coercing<Date, String>() {


                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return serializeScalar(dataFetcherResult);
                    }

                    @Override
                    public Date parseValue(Object input) throws CoercingParseValueException {
                        return parseScalarValue(input);
                    }

                    @Override
                    public Date parseLiteral(Object input) throws CoercingParseLiteralException {
                        return parseScalarLiteral(input);
                    }
                });
    }

    private static String serializeScalar(Object dataFetcherResult) throws CoercingSerializeException {
        Date date;
        if (dataFetcherResult instanceof Date) {
            date = (Date) dataFetcherResult;
        } else {
            throw new CoercingSerializeException("Could not serialize as Date: " + dataFetcherResult);
        }

        return date.toInstant().toString();
    }

    private static Date parseScalarValue(Object input) throws CoercingParseValueException {
        if (input instanceof String) {
            String iso8601String = (String) input;
            try {
                Instant parsedInstant = Instant.parse(iso8601String);
                return Date.from(parsedInstant);
            } catch (Exception e) {
                throw new CoercingParseValueException("Could not parse variable value as Date: " + iso8601String);
            }
        }
        throw new CoercingParseValueException("Could not parse variable value as Date: " + input);
    }

    private static Date parseScalarLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            String iso8601String = ((StringValue) input).getValue();
            try {
                Instant parsedInstant = Instant.parse(iso8601String);
                return Date.from(parsedInstant);
            } catch (Exception e) {
                throw new CoercingParseLiteralException("Could not parse literal value as Date: " + iso8601String);
            }
        }
        throw new CoercingParseLiteralException("Could not parse literal value as Date: " + input);
    }
}
