# Changelog

## 1.0.0-SNAPSHOT

- First release of the standalone frontend. It was carried inside
  `com.fillumina:krasa-jaxb-tools` until 2.8.0, where it shipped two frontends, `krasa` and
  `krasa-jaxws`.
- The frontend is named `bean-validation`, and it carries CXF's own generators beside this project's
  `ValidSEIGenerator`, so one option is enough to generate the interface and have it validated.
- Jakarta only: it writes `jakarta.validation.Valid`. There is no `javax` flavour.
- The `generateServiceValidationAnnotations` option keeps its name and now accepts `none` beside
  `in`, `out` and `inOut`.
