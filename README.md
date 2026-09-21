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

It is the third piece of the old `com.fillumina:krasa-jaxb-tools`, split out so that a build which
only validates the classes generated from a schema does not pull the CXF tooling at all, and one
that generates a client does not have to carry the annotation plugin.

## Requirements

- JDK 21 or newer.
- Apache CXF 4.2, which is what it is built against and what supplies the tooling at run time.
- Jakarta Bean Validation 3.1, for the annotation it writes.

## Using it

With the CXF command line:

```
wsdl2java -frontend bean-validation -xjc-XCxfValidationFrontendOptions:generateAnnotations=inOut Hello.wsdl
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
              <extraarg>-xjc-XCxfValidationFrontendOptions:generateAnnotations=inOut</extraarg>
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

- `in` — the parameters the service takes carry `@Valid`;
- `out` — the method itself, and the parameters the service returns, carry it;
- `inOut` — both, and this is the default;
- `none` — neither, which is how a build that wants the frontend for something else turns the
  annotations off;
- `verbose`, on its own, prints which annotation was written where.

### Why an XJC plugin comes with it

CXF hands the arguments it is given to XJC, and XJC refuses an argument that none of its plugins
consumes: it stops with `unrecognized parameter` and prints its usage. An option of a CXF frontend
therefore needs an XJC plugin to accept it, and this project ships one,
`FrontendOptionsPlugin`. It does nothing with the option beyond accepting it — the frontend reads
it in the CXF stage — and its name, `-XCxfValidationFrontendOptions`, says exactly that.

That name is deliberately not the name of the plugin that annotates the generated classes
(`-XBeanValidationAnnotations`). Two plugins answering to one option name are a trap: XJC activates
the first one it finds for the bare option, and returns at the first one that consumes an argument,
so the other is left out silently and according to the order of the classpath.

## Building

The build needs nix. `nix-shell` gives JDK 21 and Maven:

```
nix-shell --run 'mvn -B verify'
```
