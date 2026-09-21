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
wsdl2java -frontend bean-validation Hello.wsdl
```

Inside a Maven build it goes on the classpath of the `cxf-codegen-plugin`, and the frontend and its
options are passed among the extra arguments:

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
              <extraarg>-xjc-XBeanValidationAnnotations:generateServiceValidationAnnotations=inOut</extraarg>
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

- `generateServiceValidationAnnotations` — `in`, `out`, `inOut` or `none`, and `inOut` by default:
  it says which of the method, the incoming parameters and the outgoing parameters carry `@Valid`.
- `verbose` — prints which annotation was written where.

Both are written after the plugin name they belong to, as in
`-xjc-XBeanValidationAnnotations:generateServiceValidationAnnotations=in`.

## Building

The build needs nix. `nix-shell` gives JDK 21 and Maven:

```
nix-shell --run 'mvn -B verify'
```
