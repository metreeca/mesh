# Lifecycle Management

## Build Commands

- `mvn clean install` - Full build with tests
- `mvn test` - Run all tests
- `mvn test -Dtest=TestClassName` - Run a specific test class
- `mvn test -Dtest=TestClassName#testMethodName` - Run a specific test method

# Coding Guidelines

## Conventions

- Java code follows standard Java conventions
- Naming: PascalCase for classes, camelCase for methods and variables
- Imports: static imports for factory methods and constants
- Use final liberally for immutability
- Follow existing patterns for property naming in bean interfaces
- Error handling: use checked exceptions with functional interfaces
- Documentation: use Javadoc for public APIs

## Testing

- Test classes use JUnit 5 Jupiter (`org.junit.jupiter.api.Test`)
- Use AssertJ for test assertions (`assertThat()`)
- Tests follow BDD pattern with arrange-act-assert structure
- Follow existing patterns for nested test classes with `@Nested` annotation
- Tests are preferably written in a functional, fluent style
- Tests don't cover trivial implementation details, like arguments null checks

# Javadocs Guidelines

- Place Javadoc immediately before the documented element
- Use Javadoc only for public or protected APIs unless required to do otherwise
- Keep package, class and method definitions concise, ideally not exceeding 40 chars
- Cross-link noteworthy mentions of other public or protected packages, classes and methods; use {@linkplain} where
  sensible in the context
- Make absolutely sure not to link, mention or discuss package-protected or private classes or methods in public
  Javadocs, as they won't appear in generated documentation
- Make sure <p> tags are always closed with </p>
- Always add empty lines before and after every element. Never write consecutive elements without blank line separation
- Definitely don't generate example usage sections
- Use British spelling

## package.info

- Provide a package overview in package.info, focusing on overall structure and responsibilities; describe individual
  classes only when critical in the context and keep notes as short as possible
- Don't generate @see links for subpackages

## Class Javadocs

- Introduce the class with a concise definition and a brief description of its role and responsibilities
- Document record parameters with @param tags in the class description
- Don't generate javadocs for test classes

## Method Javadocs

- Introduce boolean methods with "Checks if"
- Introduce read accessors with "Retrieves"
- Introduce write accessors with "Configures"
- Report unexpected null values as "@throws NullPointerException if <param> is {@code null}"; if two parameters are
  to be reported use "if either <param1> or <param2> is {@code null}"; if multiple parameters are to be reported use
  "if any of <param1>, <param2>, ..., <paramN> is {@code null}"
- Always wrap param names, null and true/false in {@code} tags
- Don't generate javadocs for overridden methods

# Tech Writing Guidelines

## Tone and Voice

- **Professional and neutral**: Use a professional, technical tone without excessive familiarity
- **Framework naming**: Always refer to the framework as "Metreeca/Mesh"
- **Avoid overuse of "you/yours"**: Minimize direct address to maintain professional distance
- **Concise and direct**: Provide clear, actionable information without unnecessary elaboration
- Use British spelling

## Document Structure

### Front Matter

Use YAML front matter with title:

```yaml
---
title: "Tutorial Topic Name"
---
```

### Opening Section

- Start with a brief introduction stating what the tutorial covers
- Include learning objectives in 1-2 sentences
- Reference related concepts and cross-link to handbooks and other tutorials

### Heading Hierarchy

- Use H1 (`#`) for major sections
- Use H2 (`##`) and H3 (`###`) for subsections
- Never use H4 or deeper - keep hierarchy shallow
- Ensure proper nesting (start with H1, then H2, then H3)

## Content Organization

### Progressive Examples

- Build concepts step by step with working code examples
- Start with simple cases and add complexity gradually
- Each code block should be complete and runnable
- Use Java code blocks with proper syntax highlighting

### Code Examples

```java
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.*;

final Shape exampleShape=shape()
        .datatype(String())
        .required();
```

### Cross-References

- Link to relevant handbooks: `[standard datatypes](../handbooks/datatypes.md)`
- Link to Javadocs: Use pattern `https://javadoc.io/doc/com.metreeca/{artifact-id}`
- Cross-link related tutorials and concepts within the documentation

## Technical Conventions

### Code Style

- Follow Java naming conventions (PascalCase for classes, camelCase for methods)
- Show complete import statements in examples
- Use static imports for factory methods and constants
- Include proper error handling patterns

### Terminology

- Use consistent technical terms throughout
- Define specialized terms when first introduced
- Maintain consistency with existing framework documentation

## Content Guidelines

### What to Include

- Clear problem statements and use cases
- Step-by-step implementation examples
- Brief explanations of concepts as they're introduced
- Cross-references to related documentation
- Real-world examples using consistent sample data (like BIRT dataset)

### What to Avoid

- Overly detailed explanations of basic concepts
- Example usage sections in code documentation
- Excessive use of notes or warnings
- Redundant explanations already covered in handbooks

## Formatting Standards

### Code Blocks

- Use fenced code blocks with language specification
- Include complete, working examples
- Keep examples focused and minimal while being functional

### Links and References

- Use descriptive link text
- Prefer relative links for internal documentation
- Include external links to specifications and standards where relevant

# General Instructions

- Do what has been asked; nothing more, nothing less.
- NEVER create files unless they're absolutely necessary for achieving your goal.
- ALWAYS prefer editing an existing file to creating a new one.
- NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly
  requested by the User.
