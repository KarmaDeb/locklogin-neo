# neo-extensions-jooq

**neo-extensions-jooq** is part of the **neo-essentials** extension set — a collection of official extensions packed with LockLogin that are extracted during plugin startup and are required for the plugin to work properly. It provides official database persistence support through [JOOQ](https://www.jooq.org/) (Java Object Oriented Querying) and serves as the built-in method for handling multiple SQL database engines.

Beyond its role as a built-in extension, **this module is also intended to serve as a reference implementation** — a practical, real-world example of how to structure a LockLogin extension that handles configuration, lifecycle management, dependency injection, and platform integration.

---

## What is the neo-essentials extension set?

Before explaining what the JOOQ extension does, it's important to understand the concept behind "neo-essentials."

Neo-essentials is a set of official extensions that ship with LockLogin. They are not third-party add-ons discovered from disk — they live inside the LockLogin codebase itself and are loaded by LockLogin during its own startup lifecycle. Think of them as "built-in features" that happen to be implemented as extensions.

### Why are they replaceable?

The essentials system is designed so that no built-in feature is permanent. If a server owner installs a third-party extension that provides the same functionality in a different way, that extension can tell LockLogin to skip loading the corresponding essential. For example:

```java
EssentialFeatures.disable(EssentialFeatures.FEATURE_JOOQ);
```

This means a custom persistence extension can fully replace the built-in JOOQ extension without any conflict. The essentials loader will simply skip JOOQ and let the third-party extension take over.

### Where do essential extensions live?

All essential extensions are compiled into the LockLogin plugin JAR. During startup, LockLogin extracts them and treats them as regular extensions — they go through the same lifecycle phases, register the same services, and follow the same conventions as any third-party extension.

---

## What does this extension do?

At a high level, the JOOQ extension solves one problem: **how does LockLogin store and retrieve user data across server restarts?**

Instead of writing raw SQL strings scattered across the codebase, this extension delegates all query building to the JOOQ library. JOOQ generates type-safe, compile-time-checked SQL queries from Java code, which means:

- **No string concatenation for queries** — you build queries using a fluent Java API instead of concatenating SQL strings.
- **Compile-time safety** — if a column name changes, the compiler catches it. If a table doesn't exist, the code won't compile.
- **Dialect abstraction** — the same Java code works across MySQL, PostgreSQL, MariaDB, and SQLite because JOOQ handles the SQL dialect differences.

### The JOOQ code generator

The build configuration includes a [JOOQ Gradle plugin](https://github.com/etiennestuder/gradle-jooq-plugin) setup that generates Java classes from a database schema. This means the `Tables` and `UsersRecord` classes used in the code are not hand-written — they are generated from the actual database structure. If the schema changes, regenerating the code updates all type-safe references automatically.

---

## Supported Databases

The extension ships with drivers for four open-source database engines:

| Driver | Database | JDBC Driver Class | JOOQ Dialect |
|---|---|---|---|
| `MySQLDriver` | MySQL | `com.mysql.cj.jdbc.Driver` | `SQLDialect.MYSQL` |
| `MariaDBDriver` | MariaDB | `org.mariadb.jdbc.Driver` | `SQLDialect.MARIADB` |
| `PostgresDriver` | PostgreSQL | `org.postgresql.Driver` | `SQLDialect.POSTGRES` |
| `SQLiteDriver` | SQLite | `org.sqlite.JDBC` | `SQLDialect.SQLITE` |

Each driver is a thin wrapper around `JooQDriver` that registers the appropriate JDBC driver class and SQL dialect. Adding support for a new dialect is a matter of creating a new subclass — the heavy lifting (connection management, migration, pooling) is already handled by the base class.

---

## Architecture

The extension is organized into three layers:

### 1. Extension entry point (`Extension.java`)

The `Extension` class is the single entry point. It uses annotation-based metadata (`@ExtensionMeta`, `@VersionMeta`, `@AuthorMeta`) to declare its identity to LockLogin. During the two lifecycle phases it participates in:

- **`loaded()`** — Registers `SQLConfiguration` as a config part so that the plugin configuration file understands the `persistence` section. This phase also enforces that the extension can only be loaded during plugin startup, not via a runtime command.
- **`enabled()`** — Reads the configured SQL dialect, instantiates the corresponding driver, wraps it in a `PersistenceManager`, and registers it with the LockLogin API.

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

## Why this extension is a good example for real-world scenarios

If you're building a LockLogin extension and wondering how to structure it, this module demonstrates several patterns that apply to most real-world extensions:

### Pattern 1: Annotation-based metadata

Instead of hardcoding extension details in a manifest or a separate file, the extension declares its name, ID, version, and author directly on the class using annotations. This keeps the metadata close to the code and makes it easy to inspect.

### Pattern 2: Lifecycle-aware initialization

The extension distinguishes between `loaded()` and `enabled()` phases. Configuration registration happens during `loaded()` because it needs to be available before the plugin reads the config file. Driver instantiation happens during `enabled()` because it requires the configuration to already be parsed. Understanding when to do what is critical for extensions that interact with the configuration system.

### Pattern 3: Configuration as a first-class citizen

Rather than reading config values ad-hoc, the extension defines a dedicated `ConfigPart` with a full schema. This pattern ensures that configuration is validated at startup, documented inline, and accessible through typed methods rather than string lookups.

### Pattern 4: Abstraction over concrete implementations

`JooQDriver` is abstract and handles all shared behavior. Concrete drivers are thin wrappers that only specify what makes them unique. This means adding a new database type requires minimal code, and changes to connection management or migration logic automatically apply to all drivers.

### Pattern 5: Non-blocking I/O

All database operations return `CompletableFuture` and execute on a dedicated thread pool. The main server thread is never blocked waiting for a database connection — a critical requirement for any Minecraft server plugin.

### Pattern 6: Graceful degradation and replaceability

The extension is designed to be disabled and replaced. It doesn't assume it's the only persistence layer. This philosophy — building extensions that can coexist or be swapped out — is what makes the LockLogin extension system powerful.

---

## Extending the JOOQ extension

If you want to add support for a new SQL dialect already supported by JOOQ, you don't need to replace the entire extension. Instead, you can include `neo-extensions-jooq` as a dependency in your project's build manager, and then configure your extension to depend on `neo-essentials-jooq`. This allows you to build on top of the existing JOOQ infrastructure while adding your own custom drivers or behavior.

For example, to add support for H2:

1. Add `neo-extensions-jooq` as a compile dependency.
2. Create a new class extending `JooQDriver` that registers `org.h2.Driver` and `SQLDialect.H2`.
3. Declare your extension as depending on `neo-essentials-jooq` so it loads after the base infrastructure is available.

---

## Connection Pooling

The extension uses [HikariCP](https://github.com/brettwooldridge/HikariCP) — widely considered the fastest and most reliable JDBC connection pool. Pooling is configured through the `persistence.pooling` section and supports:

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

## Licensing

JOOQ is used under the Apache Software License 2.0 (ASL 2.0). The free OSS edition is intended and officially supported exclusively for open-source database engines (MySQL, PostgreSQL, MariaDB, SQLite, H2, etc.). Using commercial databases (Oracle, SQL Server, DB2, etc.) or advanced commercial-only features requires a paid commercial license from the JOOQ authors.

Redistributions and derivative works must retain the original copyright notices, conditions, and a copy of the Apache 2.0 license.

---

## Extension Identity

| Property | Value |
|---|---|
| **Name** | LockLogin JOOQ |
| **ID** | `neo-essentials-jooq` |
| **Base class** | `NeoExtension` |
| **Author** | KarmaDev (karmaconfigs@gmail.com) |
| **Version** | 1.0.0 |
| **Lifecycle** | Plugin load only — throws `UnsupportedOperationException` if loaded via command |

The extension class is declared as `final`, meaning it cannot be subclassed. If you need to customize its behavior, the recommended approach is to build a separate extension that depends on `neo-essentials-jooq` rather than modifying this class directly.
