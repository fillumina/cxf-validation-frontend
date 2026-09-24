# cxf-validation-frontend

An [Apache CXF](https://cxf.apache.org/docs/tools.html) frontend for `wsdl2java` that adds the
Jakarta Bean Validation `@Valid` annotation to the service endpoint interface generated from a
WSDL: to the methods, and to the parameters they take and return, each as far as the option asks.

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

An example of it inside a real build, with the test of that wiring, is
[`cxf-validation-frontend-example`](https://github.com/fillumina/cxf-validation-frontend-example).
The three plugins of this line together in one build, which is where the split is shown to do what
the single plugin did, are in
[`xjc-plugins-example`](https://github.com/fillumina/xjc-plugins-example).

## Requirements

- JDK 21 or newer.
- Apache CXF 4.2, which is what it is built against and what supplies the tooling at run time.
- Jakarta Bean Validation 3.1, for the annotation it writes.
- **Jakarta only, and it cannot be otherwise: CXF 4 validates with `jakarta.validation`.** Its
  `BeanValidationProvider` takes a `jakarta.validation.Validator`, and `cxf-core` declares the
  `jakarta.validation` API and never the `javax` one, so a `javax.validation.Valid` written on the
  generated interface would be invisible to the runtime. This frontend writes `jakarta.validation.Valid`
  and has no `javax` flavour. A build that still validates with the `javax` API belongs to the older
  `com.fillumina:krasa-jaxb-tools`, whose frontends run with CXF 3.5.

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
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
</plugin>
```

## Options

The frontend has one option, `generateAnnotations`, and it is written as
`-xjc-XCxfValidationFrontendOptions:generateAnnotations=<value>`:

| value | alternative | what it does |
| --- | --- | --- |
| `request` | `in` | add `@Valid` to the service parameters |
| `response` | `out` | add `@Valid` to the method and to the parameters the service returns |
| `both` | `inOut` | add `@Valid` to both, and this is the default |
| `none` | | add nothing — the service interface is written as CXF writes it, with no annotation |

Both columns are accepted, whatever the case. Those names are the vocabulary the WSDL and
`WebParam.Mode` use for the input and the output message, which is what the generated interface
prints, so a build written by someone who knows it keeps working. `verbose`, on its own, prints
which annotation was written where.

### How an option reaches a CXF frontend, and the XJC plugin that comes with it

A frontend cannot simply be handed its own options. CXF collects the arguments written
`-xjc-…` into one array and passes that array to XJC, and it is the same array, under
`ToolConstants.CFG_XJC_ARGS`, that the frontend reads when it runs. There is no second channel.

XJC, in turn, refuses an argument that none of its plugins consumes: it stops with
`unrecognized parameter` and prints its usage. So an option of a frontend only gets through if
some XJC plugin accepts it on the way. That is the entire job of `FrontendOptionsPlugin`, which
ships with this project:

```java
@Override
public int parseArgument(Options opt, String[] args, int index) {
    return args[index].startsWith("-" + getOptionName()) ? 1 : 0;
}

@Override
public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) {
    // the annotations of the generated classes are not this plugin's business
    return true;
}
```

It accepts the arguments of this frontend and reads nothing from them. The frontend parses the
values itself, in the CXF stage, out of the same array — which is what keeps this project
independent of the plugin that annotates the generated classes: the two can be used together, or
separately, and neither needs the other on its classpath.

Two rules hold the arrangement together, and both are there because of how XJC walks its plugins:

- **the option name belongs to this project** (`-XCxfValidationFrontendOptions`), never to another
  plugin's. For a bare option XJC activates the first plugin whose name matches and returns without
  asking the rest, so two plugins answering to one name mean that one of them silently never runs;
- **the accepting plugin consumes only its own arguments** and returns "not mine" for everything
  else. For an argument that carries a value XJC returns at the first plugin that consumes it, so a
  plugin that claims too much keeps another plugin's options from ever being read.

Both are covered by `ServiceValidationOptionsTest`. The second one is worth a test of its own
because the failure is invisible: the build succeeds and the other plugin's annotations are simply
missing from the generated classes.

## Building

The build needs JDK 21 and Maven, and nothing else. With both on the path:

```
mvn -B verify
```

Build with `verify` rather than `test`. One test — `FrontendJarIT`, which runs the frontend out
of the jar — needs the jar, and the jar is built by `package`, which comes after the test phase; so
`mvn test` runs 14 tests and leaves that one out, while `mvn verify`, `mvn install` and
`mvn deploy` run all 15. CI runs `verify`.
