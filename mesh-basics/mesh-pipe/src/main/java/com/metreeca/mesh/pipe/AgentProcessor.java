package com.metreeca.mesh.pipe;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;

import java.net.URI;
import java.util.function.Function;

import static com.metreeca.mesh.shapes.Shape.shape;


/**
 * Value processor for customizing HTTP agent request and response handling.
 *
 * <p>Provides hooks for intercepting and transforming values during HTTP request processing,
 * enabling custom validation, transformation, and post-processing logic for REST API operations.</p>
 *
 * <p>The processor integrates with {@linkplain Agent} HTTP method handlers through two methods:</p>
 *
 * <ul>
 *   <li>{@linkplain #decode(URI, Function)} - transforms request data before store operations</li>
 *   <li>{@linkplain #review(Value)} - transforms response data before client delivery</li>
 * </ul>
 *
 * <p>Method-specific behavior and integration details are documented in {@linkplain Agent}.</p>
 *
 * <p>Default implementations provide pass-through behaviour suitable for basic operations.
 * Override methods to implement custom processing patterns such as validation, enrichment,
 * access control, or format conversion.</p>
 */
public interface AgentProcessor {

    /**
     * Processes request data before store operations.
     *
     * @param id      the target resource URI
     * @param decoder the decoder function that accepts a driving shape and returns a decoded value
     *
     * @return the processed value for validation and store operations
     *
     * @throws NullPointerException if either {@code id} or {@code decoder} is {@code null}
     */
    default Valuable decode(final URI id, final Function<Shape, Value> decoder) {
        return decoder.apply(shape());
    }

    /**
     * Processes response data before client delivery.
     *
     * @param value the value retrieved from the store
     *
     * @return the processed value for HTTP response serialization
     *
     * @throws NullPointerException if {@code value} is {@code null}
     */
    default Valuable review(final Value value) {
        return value;
    }

}
