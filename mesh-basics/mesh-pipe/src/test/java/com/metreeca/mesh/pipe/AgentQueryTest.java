package com.metreeca.mesh.pipe;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.Base64;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.URIs.base;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

final class AgentQueryTest {

    private static final Codec DUMMY_CODEC=new Codec() {

        @Override
        public <A extends Appendable> A encode(final A target, final Valuable value) {
            try {
                target.append(encoded(value.toValue()));
                return target;
            } catch ( final IOException e ) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <R extends Readable> Value decode(final R source, final Shape shape) {
            try ( final BufferedReader reader=new BufferedReader((Reader)source) ) {
                return string(decoded(reader.readLine()));
            } catch ( final IOException e ) {
                throw new RuntimeException(e);
            }
        }

    };

    private static String encoded(final Value value) {
        return "encoded: %s".formatted(value);
    }

    private static String decoded(final String json) {
        return "decoded: %s".formatted(json);
    }


    @Test void testURLEncoded() {
        assertThat(AgentQuery.query(
                DUMMY_CODEC,
                URLEncoder.encode("{}", UTF_8),
                shape(),
                base()
        )).isEqualTo(
                string(decoded("{}"))
        );
    }

    @Test void testBase64Encoded() {
        assertThat(AgentQuery.query(
                DUMMY_CODEC,
                Base64.getEncoder().encodeToString("{}".getBytes(UTF_8)),
                shape(),
                base()
        )).isEqualTo(
                string(decoded("{}"))
        );
    }

    @Test void testFormData() {

        final Shape resource=shape()
                .datatype(Value.Object())
                .property(property("name").forward(true).shape(shape()
                        .datatype(Value.String())
                        .maxCount(1)
                ))
                .property(property("age").forward(true).shape(shape()
                        .datatype(Integral())
                        .maxCount(1)
                ));

        final Shape collection=shape()
                .property(property("items").forward(true).shape(resource));

        assertThat(AgentQuery.query(
                DUMMY_CODEC,
                URLEncoder.encode("~name=axel&age>=30", UTF_8),
                collection,
                base()
        )).isEqualTo(object(
                Value.shape(collection),
                field("items", value(query()
                        .model(object(
                                Value.shape(resource)
                        ))
                        .where("name", criterion().like("axel"))
                        .where("age", criterion().gte(integral(30)))
                ))
        ));
    }

    @Test void testPlain() {
        assertThat(AgentQuery.query(
                DUMMY_CODEC,
                "{}",
                shape(),
                base()
        )).isEqualTo(
                string(decoded("{}"))
        );
    }

}
