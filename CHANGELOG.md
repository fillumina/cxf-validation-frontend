# Changelog

## 1.0.0-SNAPSHOT

- First release of the standalone frontend. It was carried inside
  `com.fillumina:krasa-jaxb-tools` until 2.8.0, where it shipped two frontends, `krasa` and
  `krasa-jaxws`, and where its option was written under the name of the annotation plugin.
- The frontend is named `bean-validation`, and it carries CXF's own generators beside this project's
  `ValidSEIGenerator`, so one option is enough to generate the interface and have it validated.
- Jakarta only: it writes `jakarta.validation.Valid`. There is no `javax` flavour.
- The option is written under this project's own name,
  `-xjc-XCxfValidationFrontendOptions:generateServiceValidationAnnotations=...`, and accepts `none`
  beside `in`, `out` and `inOut`. A small XJC plugin accepts it, because XJC refuses an argument no
  plugin consumes; see the README for why it does not answer to the annotation plugin's name.
