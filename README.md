[![Maven Central](https://img.shields.io/maven-central/v/com.fillumina/cxf-validation-frontend.svg)](https://central.sonatype.com/artifact/com.fillumina/cxf-validation-frontend)

# cxf-validation-frontend

An [Apache CXF](https://cxf.apache.org/docs/tools.html) frontend for `wsdl2java` that adds the
Jakarta Bean Validation `@Valid` annotation to the service endpoint interface generated from a
WSDL. You choose what it annotates either or both of:

- the methods
- the parameters they take and return

With `generateAnnotations=both`, the generated interface looks like this:

```java
@WebService(targetNamespace = "...")
public interface HelloPortType {

    @Valid
    String greet(@Valid String name);
}
```

It is the third piece of the old
[`com.fillumina:krasa-jaxb-tools`](https://github.com/fillumina/krasa-jaxb-tools), split out so that
a build which only validates the classes generated from a schema does not pull the CXF tooling at
all, and one that generates a client does not have to carry the annotation plugin.

The other two pieces of that line are
[`xjc-primitives-plugin`](https://github.com/fillumina/xjc-primitives-plugin), which boxes the
generated primitives so that a constraint such as `@NotNull` can mean something on them, and
[`xjc-bean-validation-plugin`](https://github.com/fillumina/xjc-bean-validation-plugin), which
writes the constraints the schema states. `@Valid` cascades through them, so a build that wants the
constraints enforced wants the two of them as well.

An example of it inside a real build, with the test of that wiring, is
[`cxf-validation-frontend-example`](https://github.com/fillumina/cxf-validation-frontend-example).
The three plugins of this line together in one build, which is where the split is shown to do what
the single plugin did, are in
[`xjc-plugins-example`](https://github.com/fillumina/xjc-plugins-example).

## Version and status

The released version is **1.0.0**, published on Maven Central. The option name, its four policies
and the `@Valid` it writes stay the same within 1.x; a method or a parameter it annotates in the
wrong way is a bug fixed in a patch release.

## Requirements

- JDK 21 or newer.
- Apache CXF 4.2, which is what it is built against and what supplies the tooling at run time.
- Jakarta Bean Validation 3.1, for the annotation it writes.
- This plugin supports **Jakarta only**, because CXF 4 validates only with `jakarta.validation`. A
  build that validates with the `javax` API belongs to the older
  [`com.fillumina:krasa-jaxb-tools`](https://github.com/fillumina/krasa-jaxb-tools), whose frontends
  run with CXF 3.5.

## Using it

With the CXF command line:

```
wsdl2java -frontend bean-validation -xjc-XCxfValidationFrontendOptions:generateAnnotations=both Hello.wsdl
```

Inside a Maven build it goes on the classpath of the `cxf-codegen-plugin`, and the frontend and its
option are passed among the extra arguments:

```xml
<plugin>
  <groupId>org.apache.cxf</groupId>
  <artifactId>cxf-codegen-plugin</artifactId>
  <version>4.2.3</version>
  <executions>
    <execution>
      <id>wsdl2java</id>
      <phase>generate-sources</phase>
      <configuration>
        <wsdlOptions>
          <wsdlOption>
            <wsdl>${project.basedir}/wsdl/Hello.wsdl</wsdl>
            <extraargs>
              <extraarg>-frontend</extraarg>
              <extraarg>bean-validation</extraarg>
              <extraarg>-xjc-XCxfValidationFrontendOptions:generateAnnotations=both</extraarg>
            </extraargs>
          </wsdlOption>
        </wsdlOptions>
      </configuration>
      <goals>
        <goal>wsdl2java</goal>
      </goals>
    </execution>
  </executions>
  <dependencies>
    <dependency>
      <groupId>com.fillumina</groupId>
      <artifactId>cxf-validation-frontend</artifactId>
      <version>1.0.0</version>
    </dependency>
  </dependencies>
</plugin>
```

## Options

The frontend has one option, `generateAnnotations`, and it is written as
`-xjc-XCxfValidationFrontendOptions:generateAnnotations=<value>`:

| value      | alternative | what it does                                                              |
| ---------- | ----------- | ------------------------------------------------------------------------- |
| `request`  | `in`        | add `@Valid` to incoming and INOUT parameters                             |
| `response` | `out`       | add `@Valid` to non-void method returns and outgoing and INOUT parameters |
| `both`     | `inOut`     | annotate both sides (once on an INOUT parameter); this is the default     |
| `none`     |             | add nothing to what CXF writes by itself                                  |

A void method has no return value to validate and is never annotated on the method itself.
An INOUT holder is a single Java parameter, so it gets one `@Valid` even when both
directions are selected; `request` and `response` each still select that holder.

Both alternatives are accepted, whatever the case. `verbose`, on its own, prints
which annotation was written where. Unknown or misspelled names after
`-XCxfValidationFrontendOptions:` (and the bare prefix without a name) fail generation instead of
silently using the default `both` policy. The values are checked too: `generateAnnotations` takes
one of the names in the table, and `verbose` takes `true` or `false`, or is written on its own.

### Using an XJC plugin to pass options to the CXF frontend

Extending the full CXF framework this frontend cannot simply be handed its own specific options so we needed to define an XJC plugin that accepts it on its behalf. That is the entire job of `FrontendOptionsPlugin`, which
ships with this project:

```java
@Override
public int parseArgument(Options opt, String[] args, int index)
        throws BadCommandLineException {
    String argument = args[index];
    if (!argument.equals("-" + getOptionName())
            && !argument.startsWith(ServiceValidationOptions.PREFIX)) {
        return 0; // a different plugin's option
    }
    ServiceValidationOptions.builder().parseArgument(argument); // rejects unknown names/values
    return 1;
}
```

XJC offers every argument to its plugins and the first one that accepts it consumes it, so this
project's plugin refuses an unknown name or value under its own prefix, and the build stops
before anything is generated. Its `run` method still does nothing to generated classes.

## Building

The build needs JDK 21 and Maven, and nothing else. With both on the path:

```
mvn -B verify
```

Build with `verify` rather than `test`. One test — `FrontendJarIT`, which runs the frontend out
of the jar — needs the jar, and the jar is built by `package`, which comes after the test phase; so
`mvn test` runs the unit tests and leaves that one out, while `mvn verify`, `mvn install`
and `mvn deploy` also run the jar integration test. CI runs `verify`.
