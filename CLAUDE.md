# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

`AGENTS.md` (plus `AGENTS-Architecture.md` and `AGENTS-New-Database-Driver.md`) holds the authoritative project rules: license headers, `@NotNull`/`@Nullable`, NLS messages, logging, import order, commit/branch naming (`dbeaver/repo#issue title`). Read them before changing code; they are not repeated here.

## Build & test

Maven + Eclipse Tycho; all OSGi dependencies come from P2 repos, so single-bundle builds generally fail.

- Full build: `mvn package -f product/aggregate/pom.xml -T1C -Pproduct-dbeaver-ce,product-dbeaver-eclipse-ce`
- Build + tests: `mvn verify -f product/aggregate/pom.xml -T1C -Pproduct-dbeaver-ce,product-dbeaver-eclipse-ce`
- There is no separate test-only command and no single-test shortcut; tests run as part of `verify`. Test plugins live in `test/` and mirror production plugins (`test/org.jkiss.dbeaver.ext.<db>.test`), extending `DBeaverUnitTest` or using `@RunWithApplication`/`@RunWithProduct`.
- Other products: profiles listed in `product/pom.xml`.

## Architecture

- **Bundles**: everything is an OSGi plugin in `plugins/`. Dependencies are declared in `META-INF/MANIFEST.MF` (`Require-Bundle`), not in `pom.xml`. New plugins must be added to the `<modules>` of `plugins/pom.xml` (tests: `test/pom.xml`).
- **Model/UI split**: model bundles (`org.jkiss.dbeaver.model*`, `ext.<db>`) must not depend on SWT/JFace; UI lives in `*.ui` siblings. The same model layer is shared with the commercial CloudBeaver (web) and dbvr (CLI) products, so UI-free model code is a hard requirement.
- **Core layers**: `org.jkiss.dbeaver.model` (DBP*/DBS*/DBC*/DBD*/DBR* abstractions) → `model.jdbc`/`model.sql` → `registry` (extension-point registries, data source/driver registry) → `ui*` and `ui.app.*` (product shells).
- **Extension points**: features are contributed via `plugin.xml` extensions (e.g. `org.jkiss.dbeaver.dataSourceProvider` for DB drivers); prefer adding an extension point to hard-wiring.
- **Database drivers**: `plugins/org.jkiss.dbeaver.ext.<db>` (+ `.ui`). Many are plugin.xml-only on top of `ext.generic`; reuse it and use `treeInjection` for navigator-tree changes rather than copying.
- **Products/features**: `features/` (Eclipse features), `product/` (aggregator in `product/aggregate`, product definitions in `product/community`).

## Repo dependencies

Sibling repos (listed in `project.deps`: `dbeaver-common`, `datadam-api`) must be cloned next to this repo under the same parent folder (`DBEAVER_DEV_HOME`). PRs target `devel`; never commit to `release_*` branches.
