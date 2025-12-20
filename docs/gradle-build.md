# Building with Gradle — Quick Reference

Short reminders for building the whole repository or individual modules.

## Build everything
- Full build (runs tests):

```bash
./gradlew build
```

- Full build without running tests (faster during development):

```bash
./gradlew build -x test
```

## Build a single module
- Build `server` only:

```bash
./gradlew :server:build
```

- Build the new server persistence module:

```bash
./gradlew :persistence-jpa-server:build
```

## Useful tasks
- Clean build artifacts:

```bash
./gradlew clean
```

- Run tests for a module (example `core`):

```bash
./gradlew :core:test
```

- Run the Spring Boot server (if configured):

```bash
./gradlew :server:bootRun
```

## Performance tips
- Add `--no-daemon` to respect CI settings, or let Gradle manage the daemon locally.
- Use `--parallel` to build independent projects concurrently.
- Use `-x test` to skip tests when iterating quickly.

Example fast local build:

```bash
./gradlew build -x test --parallel --no-daemon
```

## Notes
- Project modules are defined in `settings.gradle.kts`.
- To inspect available tasks for a module: `./gradlew :module:tasks`.
- If you change module inclusion, re-run Gradle to refresh configuration.
