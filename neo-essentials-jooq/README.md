# neo-essentials-jooq

**neo-essentials-jooq** is part of the **neo-essentials** set — a collection of built-in components whose source code lives inside the LockLogin codebase and are required for the plugin environment to function properly. It provides official database persistence support through [JOOQ](https://www.jooq.org/) (Java Object Oriented Querying) and serves as the built-in method for handling multiple SQL database engines.

Beyond its role as a built-in component, **this module is also intended to serve as a reference implementation** — a practical, real-world example of how to structure a LockLogin essential that handles configuration, lifecycle management, dependency injection, and platform integration.

---

## What is the neo-essentials set?

Before explaining what the JOOQ essential does, it's important to understand the concept behind "neo-essentials."

Neo-essentials is a set of official components whose code always lives inside the LockLogin codebase. They are not third-party add-ons — they are loaded by LockLogin during its own startup lifecycle. Think of them as "built-in features" that are modular and replaceable.

They are called **essentials** because they are essential for the plugin environment itself. Even if a server owner disables an essential feature, the API remains available so that other components depending on it can still function.

### Why are they replaceable?

The essentials system is designed so that no built-in feature is permanent. Disabling an essential feature does not remove the component or its API — it simply tells LockLogin **not to load the essential class**. The API remains available for any other component that requires it.

For example, if a server owner installs a third-party component that provides its own persistence layer, that component can tell LockLogin to skip loading the built-in JOOQ essential during the `loaded` lifecycle phase:

```java
EssentialFeatures.disable(EssentialFeatures.FEATURE_JOOQ);
```

This call should be performed during the `loaded` lifecycle phase, before the essential class is instantiated. The JOOQ essential's API and classes remain on the classpath — LockLogin simply skips the loading step. This means a custom persistence component can fully replace the built-in JOOQ behavior without any conflict, while other components that depend on the JOOQ API can still access it if needed.

### Where do essentials live?

All essentials live inside the LockLogin codebase. Their source code is part of the project, and they are loaded by LockLogin itself during startup — they go through the same lifecycle phases, register the same services, and follow the same conventions as any third-party component.

---

## What does this essential do?

At a high level, the JOOQ essential solves one problem: **how does LockLogin store and retrieve user data across server restarts?**

Instead of writing raw SQL strings scattered across the codebase, this essential delegates all query building to the JOOQ library. JOOQ generates type-safe, compile-time-checked SQL queries from Java code, which means:

- **No string concatenation for queries** — you build queries using a fluent Java API instead of concatenating SQL strings.
- **Compile-time safety** — if a column name changes, the compiler catches it. If a table doesn't exist, the code won't compile.
- **Dialect abstraction** — the same Java code works across MySQL, PostgreSQL, MariaDB, and SQLite because JOOQ handles the SQL dialect differences.

### The JOOQ code generator

The build configuration includes a [JOOQ Gradle plugin](https://github.com/etiennestuder/gradle-jooq-plugin) setup that generates Java classes from a database schema. This means the `Tables` and `UsersRecord` classes used in the code are not hand-written — they are generated from the actual database structure. If the schema changes, regenerating the code updates all type-safe references automatically.

---

## Supported Databases

The essential ships with drivers for four open-source database engines:

| Driver | Database | JDBC Driver Class | JOOQ Dialect |
|---|---|---|---|
| `MySQLDriver` | MySQL | `com.mysql.cj.jdbc.Driver` | `SQLDialect.MYSQL` |
| `MariaDBDriver` | MariaDB | `org.mariadb.jdbc.Driver` | `SQLDialect.MARIADB` |
| `PostgresDriver` | PostgreSQL | `org.postgresql.Driver` | `SQLDialect.POSTGRES` |
| `SQLiteDriver` | SQLite | `org.sqlite.JDBC` | `SQLDialect.SQLITE` |

Each driver is a thin wrapper around `JooQDriver` that registers the appropriate JDBC driver class and SQL dialect. Adding support for a new dialect is a matter of creating a new subclass — the heavy lifting (connection management, migration, pooling) is already handled by the base class.

---

## Architecture

The essential is organized into three layers:

### 1. Entry point (`JooQEntrypoint.java`)

The `JooQEntrypoint` class is the single entry point for the JOOQ essential. All interaction with the LockLogin API goes through `LockLogin.get()`. During the two lifecycle phases it participates in:

- **`setupConfiguration()`** — Registers `SQLConfiguration` as a config part via `LockLogin.get().configurations().append()` so that the plugin configuration file understands the `persistence` section.
- **`setup()`** — Reads the configured SQL dialect via `LockLogin.get().configurations().part()`, instantiates the corresponding driver, wraps it in a `PersistenceManager`, and registers it with `LockLogin.get().persistence()`.

### 2. Configuration layer (`SQLConfiguration.java`)

`SQLConfiguration` is a `ConfigPart` that defines the entire `persistence` section of the plugin configuration. It does more than just read values — it defines a full schema with:

- **Type validation** — every field has a validator (e.g., host must be a valid IP or domain, port must be 1–65535).
- **Cross-field constraints** — pool minimum must be less than pool maximum, connection lifetime must be at least double the idle timeout.
- **Default values** — sensible defaults are provided for every field so the configuration works out of the box.
- **Documentation strings** — each field includes multi-line descriptions that explain what it does and any caveats.

This approach means the configuration file is self-documenting and errors are caught at startup, not at runtime.

### 3. Persistence layer (`JooQDriver` and providers)

`JooQDriver` is the core abstraction. It implements `PersistenceDriver` and handles:

- **Asynchronous connection management** — `connect()` and `disconnect()` return `CompletableFuture` and run on a dedicated `ExecutorService` (work-stealing pool) so the main thread is never blocked.
- **Connection state tracking** — `AtomicReference` is used to coordinate concurrent connect/disconnect calls without race conditions. If a connection is already in progress, subsequent calls wait on the same future instead of starting a new one.
- **HikariCP integration** — each concrete driver configures a `HikariDataSource` with the connection pooling settings from the configuration.
- **Database migration** — `migrateTo()` supports moving data between different database engines. It creates tables if they don't exist, transfers data in paginated batches (500 rows at a time), and properly cleans up connections afterward.

The provider drivers (`MySQLDriver`, `MariaDBDriver`, `PostgresDriver`, `SQLiteDriver`) are minimal subclasses that only specify the JDBC driver class and SQL dialect. All other behavior is inherited from `JooQDriver`.

---

## Why this essential is a good example for real-world scenarios

If you're building a LockLogin component and wondering how to structure it, this module demonstrates several patterns that apply to most real-world components:

### Pattern 1: Centralized API access

All interaction with the LockLogin API goes through `LockLogin.get()`. This singleton provides access to dedicated managers — `configurations()`, `hashes()`, `persistence()`, and `authLayers()` — so every component uses the same entry point for registration and configuration.

### Pattern 2: Lifecycle-aware initialization

The essential distinguishes between `setupConfiguration()` and `setup()` phases. Configuration registration happens during `setupConfiguration()` because it needs to be available before the plugin reads the config file. Driver instantiation happens during `setup()` because it requires the configuration to already be parsed. Understanding when to do what is critical for components that interact with the configuration system.

### Pattern 3: Configuration as a first-class citizen

Rather than reading config values ad-hoc, the essential defines a dedicated `ConfigPart` with a full schema. This pattern ensures that configuration is validated at startup, documented inline, and accessible through typed methods rather than string lookups.

### Pattern 4: Abstraction over concrete implementations

`JooQDriver` is abstract and handles all shared behavior. Concrete drivers are thin wrappers that only specify what makes them unique. This means adding a new database type requires minimal code, and changes to connection management or migration logic automatically apply to all drivers.

### Pattern 5: Non-blocking I/O

All database operations return `CompletableFuture` and execute on a dedicated thread pool. The main server thread is never blocked waiting for a database connection — a critical requirement for any Minecraft server plugin.

### Pattern 6: Graceful degradation and replaceability

The essential is designed to be disabled and replaced. It doesn't assume it's the only persistence layer. This philosophy — building components that can coexist or be swapped out — is what makes the LockLogin essentials system powerful.

---

## Extending the JOOQ essential

If you want to add support for a new SQL dialect already supported by JOOQ, you don't need to replace the entire essential. Instead, you can include `neo-essentials-jooq` as a dependency in your project's build manager, and then configure your component to depend on `neo-essentials-jooq`. This allows you to build on top of the existing JOOQ infrastructure while adding your own custom drivers or behavior.

For example, to add support for H2:

1. Add `neo-essentials-jooq` as a compile dependency.
2. Create a new class extending `JooQDriver` that registers `org.h2.Driver` and `SQLDialect.H2`.
3. Declare your component as depending on `neo-essentials-jooq` so it loads after the base infrastructure is available.

---

## Connection Pooling

The essential uses [HikariCP](https://github.com/brettwooldridge/HikariCP) — widely considered the fastest and most reliable JDBC connection pool. Pooling is configured through the `persistence.pooling` section and supports:

- **Pool size** — minimum and maximum connection counts, with validation that minimum < maximum.
- **Connection timeout** — how long to wait for a connection from the pool (minimum 250ms).
- **Idle timeout** — how long an idle connection can remain in the pool (minimum 30 seconds).
- **Max lifetime** — maximum duration a connection can exist (must be at least double the idle timeout).
- **Keep-alive** — frequency of ping checks to verify connection health.
- **Validation timeout** — timeout for validating a connection is still alive (minimum 250ms).

These constraints are enforced at configuration load time, so misconfigured pools are caught before they cause runtime issues.

---

## Database Migration

The `JooQDriver` class supports migrating data between different database drivers. The migration process:

1. Connects to both the source and target databases.
2. Creates the target table if it doesn't exist.
3. Reads data from the source in paginated batches (500 rows at a time) using keyset pagination (ordered by ID, greater than the last processed ID).
4. Inserts each batch into the target database.
5. Properly closes connections that were opened during migration.

This means a server owner can switch from SQLite to MySQL without losing any user data.

---

## Configuration Example

Database connection is configured under the `persistence` section of the plugin configuration file:

```yaml
persistence:
  dialect: "mysql"
  host: "127.0.0.1"
  port: 3306
  user: "root"
  password: "secret"
  database: "locklogin"
  pooling:
    size:
      minimum: 4
      maximum: 10
    health:
      connection: 30000
      idle: 600000
      lifetime: 1800000
      keep-alive: 450000
      validation: 5000
```

For SQLite (the default), most of these fields are ignored:

```yaml
persistence:
  dialect: "sqlite"
  database: "locklogin"
```

---

## Platform Support

At present, the only supported platform is **Bukkit** (Spigot/Paper). The platform-specific implementation lives in the `persistence` package as a Bukkit-targeted `UserManager` and related classes that bridge the LockLogin API with Bukkit's player lifecycle events.

### This essential as a multi-platform example

The JOOQ essential is designed to demonstrate how to properly implement a multi-platform component **without depending on `neo-api-bridge`**. The core persistence logic — `JooQDriver`, connection pooling, migration, and schema management — is entirely platform-agnostic. Platform-specific code is isolated in a separate layer that implements the `UserManager` and `PersistenceManager` interfaces from `neo-api`.

This means adding support for a new platform (e.g., BungeeCord, Velocity, or Fabric) requires only writing a new platform adapter that implements the same interfaces — the database layer remains untouched. The essential structure clearly separates:

- **Platform-independent core** — `JooQDriver`, provider drivers, configuration, and connection pooling.
- **Platform-specific adapter** — the Bukkit implementation that connects player events to account operations.

This per-platform adapter approach is the recommended way to build multi-platform components, as it gives full control over platform-specific behavior without being constrained by an abstraction layer that may not fit every use case.

---

## Licensing

JOOQ is used under the Apache Software License 2.0 (ASL 2.0). The free OSS edition is intended and officially supported exclusively for open-source database engines (MySQL, PostgreSQL, MariaDB, SQLite, H2, etc.). Using commercial databases (Oracle, SQL Server, DB2, etc.) or advanced commercial-only features requires a paid commercial license from the JOOQ authors.

Redistributions and derivative works must retain the original copyright notices, conditions, and a copy of the Apache 2.0 license.

---

## Essential Identity

| Property | Value |
|---|---|
| **Name** | LockLogin JOOQ |
| **ID** | `neo-essentials-jooq` |
| **Author** | KarmaDev (karmaconfigs@gmail.com) |
| **Version** | 1.0.0 |
| **Lifecycle** | Plugin load only — throws `UnsupportedOperationException` if loaded via command |

The essential class is declared as `final`, meaning it cannot be subclassed. If you need to customize its behavior, the recommended approach is to build a separate component that depends on `neo-essentials-jooq` rather than modifying this class directly.
