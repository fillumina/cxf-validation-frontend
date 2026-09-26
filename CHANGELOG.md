# Changelog

## 1.0.0-SNAPSHOT

- Reject misspelled or unknown names under `-XCxfValidationFrontendOptions:` (including a
  bare prefix) instead of silently generating with the default policy. The values are checked as
  well: `generateAnnotations` takes a policy, `verbose` takes a state, and anything else fails.
  XJC checks named arguments early; the frontend also checks the forwarded arguments. Other XJC
  plugins' options remain theirs.

- Fix duplicate `@Valid` on INOUT holders with the default `both` policy: annotate the
  single Java parameter once while preserving request and response selection.
- Do not annotate void method returns; still annotate their selected parameters and
  non-void returns. Generated-source compilation and Jakarta Validation provider metadata
  tests cover the resulting interface.

- First release of the standalone frontend. It was carried inside
  `com.fillumina:krasa-jaxb-tools` until 2.8.0, where it shipped two frontends, `krasa` and
  `krasa-jaxws`, and where its option was written under the name of the annotation plugin.
- The frontend is named `bean-validation`, and it carries CXF's own generators beside this project's
  `ValidSEIGenerator`, so one option is enough to generate the interface and have it validated.
- Jakarta only: it writes `jakarta.validation.Valid`. There is no `javax` flavour.
- The option is written under this project's own name,
  `-xjc-XCxfValidationFrontendOptions:generateAnnotations=...`, and accepts `none`
  beside `in`, `out` and `inOut`. A small XJC plugin accepts it, because XJC refuses an argument no
  plugin consumes; see the README for why it does not answer to the annotation plugin's name.
