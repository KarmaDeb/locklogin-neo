# LockLogin Neo

![Tests status](https://gist.githubusercontent.com/KarmaDeb/12423f1ab057054e243f214c1a09dfcb/raw/neologin-test-badge.svg)

LockLogin Neo is the third major iteration of the **LockLogin** Minecraft server authentication plugin. It is a complete ground-up refactor that learns from two previous codebases — **LockLoginReborn** (the last released version) and **LockLogin2** (an abandoned intermediate refactor) — combining their best ideas into a modern, modular, and extensible architecture designed for flexibility and multi-platform support.

## What is it?

At its core, LockLogin Neo is an authentication framework for Minecraft servers. It provides a robust system for protecting servers by requiring players to authenticate before they can interact with the game world — locking their account until they prove their identity.

## Why Rework?

LockLoginReborn, over time, accumulated so many features that it became what some called "feature hell" — a plugin that tried to do everything but lost sight of what it was always meant to be: **fast and reliable**. The codebase grew complex, configuration became rigid, and extending or customizing the plugin was a struggle.

LockLogin2 tried to fix this. It introduced a cleaner API, interface-based modules, and better abstractions. But during development, it tried to keep all the LockLoginReborn features — carrying forward the same bloated feature set it was meant to simplify. As a result, it reached the same destination before even getting published: another complex, feature-heavy plugin that lost its way.

The main purpose of LockLogin Neo is different. Rather than trying to preserve every feature from the past, it **simplifies** — stripping away the unnecessary complexity and refocusing on what matters: an authentication system that works quickly, depends on nothing it shouldn't, and does its job without getting in the way. But rather than just going back to basics, Neo takes it further by adding something the old versions never truly had: a **very powerful and well-designed API** that lets developers extend the plugin in ways that were previously impossible.

In short: simpler core, faster execution, reliable behavior, and a powerful API for those who need more.

But this doesn't mean features are disappearing forever. Most of them will eventually make their way back — not bundled into the plugin itself, but as **modular components**. By decoupling features from the core, the plugin stays lighter and less prone to errors where one broken feature brings everything down. Instead, each component operates independently, so if something goes wrong, it's isolated and doesn't break the entire system.

## Project History

LockLogin Neo is the culmination of four generations of development:

| Generation | Project | Status | Description |
|---|---|---|---|
| **0th** | **LockLoginLegacy** | Original version (archived) | The very first version of LockLogin — a simple authentication plugin that predates the official API and modular architecture. It had no modular components, no API, and no modularity. It was the most basic version of LockLogin, and LockLogin Neo aims to replicate its simplicity while adding the advantages of modularity, API, and structured essentials. |
| **1st** | **LockLoginReborn** | Last released version | The original plugin with a monolithic module system built around an abstract `PluginModule` class. Modules were loaded via JAR files with license key validation, used a card-based queue system for async tasks, and relied heavily on reflection for event handling. Source available in `in-memory-of/LockLoginReborn/`. |
| **2nd** | **LockLogin2** | Abandoned refactor | An intermediate attempt to modernize the codebase. Introduced a clean `Module` interface with lifecycle methods (`onLoad`, `onEnable`, `onDisable`), a `PluginModule<T>` generic interface for plugin-registered modules, a static `CurrentPlugin` singleton for lazy API access, and a `ModuleManager` for event and command registration. Source available in `in-memory-of/LockLogin2/`. |
| **3rd** | **LockLogin Neo** | Active development (WIP) | The current project — a complete rewrite that takes the best ideas from both predecessors and reimagines them. Replaces the module system with a modular essentials architecture, introduces namespaced resource identities, a manager-based architecture via `LockLogin.get()`, and platform-agnostic abstractions. A PaperMC-compatible version may be closer than it seems. |

## Architecture

The project is split into six Gradle modules, each with a distinct responsibility:

| Module | Description |
|---|---|
| **neo-api** | The core API — defines the contracts for configuration, messaging, security, and authentication layers. This is the foundation everything else builds on. |
| **neo-api-bridge** | A platform-agnostic bridge layer that provides abstractions for player interaction, protocol messaging (titles, action bars), and positioning. It offers a centralized way to interact with players regardless of the underlying server platform. |
| **neo-common** | Shared implementations and utilities — configuration readers/writers, network buffer handling, and common helpers used across the project. |
| **neo-essentials-jooq** | The JOOQ essentials module — provides database integration using JOOQ (Java Object Oriented Querying) for type-safe SQL queries and database operations. |
| **neo-essentials-crypto** | The crypto essentials module — provides password hashing support for multiple algorithms (Argon2, BCrypt, SCrypt) via BouncyCastle. |
| **neo-essentials-auth** | The auth essentials module — provides the core password-based authentication flow with login and registration layers, commands, and the challenge-hook pattern. |

## LockLogin Essentials

LockLogin Essentials are built-in components provided and built directly into the LockLogin codebase. Their source code lives inside the project and they are loaded by LockLogin during its own startup lifecycle. Think of them as core features that are modular and replaceable.

Most essential features can be disabled by using the `EssentialFeatures#disable` method. Disabling an essential feature does not remove its API — it simply tells LockLogin not to load the essential component's class. The API remains available for any other component that requires it, which is why they are called **essentials**: they are essential for the plugin environment.

| Essential | Description |
|---|---|
| **neo-essentials-jooq** | Provides official database persistence support through JOOQ for multiple SQL dialects (MySQL, MariaDB, PostgreSQL, SQLite). See [neo-essentials-jooq/README.md](neo-essentials-jooq/README.md) for details. |
| **neo-essentials-crypto** | Provides official password hashing support for multiple algorithms (Argon2, BCrypt, SCrypt) via BouncyCastle. See [neo-essentials-crypto/README.md](neo-essentials-crypto/README.md) for details. |
| **neo-essentials-auth** | Provides the core password-based authentication flow with login and registration AuthLayers, Bukkit commands (/login, /register), and the challenge-hook pattern. See [neo-essentials-auth/README.md](neo-essentials-auth/README.md) for details. |

## neo-api-bridge (Experimental)

The **neo-api-bridge** module is a currently **experimental** API intended to act as a single source of truth for multi-platform components. Instead of writing separate per-platform adapters, components could depend on the bridge abstractions to interact with players, send protocol messages, and handle positioning in a platform-agnostic way.

However, as an experimental API, **neo-api-bridge is subject to breaking changes without notice** — it could be significantly restructured or even removed entirely in future versions. It should not be relied upon for production use.

**Personal recommendation:** For now, writing per-platform adapters is the preferred approach. The [neo-essentials-jooq](neo-essentials-jooq/README.md) module demonstrates this pattern: the core logic is platform-agnostic, while platform-specific code is isolated in a thin adapter layer. This gives full control over platform-specific behavior without being constrained by an abstraction that may evolve or disappear.

## API Evolution

The most significant change across generations is the modular architecture. Understanding this evolution helps developers migrating from older versions:

### Generation 1: LockLoginReborn — Module API

The original system used an abstract `PluginModule` class that modules extended. Key characteristics:

- **Inheritance-based** — modules extended `PluginModule`, creating tight coupling with the framework.
- **License key validation** — modules required cryptographic license keys validated at construction time.
- **Card-based async queue** — a unique `APICard` system for scheduling async tasks with listeners and lifecycle events.
- **Reflection-based event handling** — `@ModuleEventHandler` annotation scanned via reflection on registered listener objects.
- **Platform-dependent** — tightly coupled to Bukkit/Spigot and Velocity platforms.

```java
// LockLoginReborn style — extending an abstract class
public class MyModule extends PluginModule {
    public MyModule(Platform platform, String key) throws InvalidKeyError {
        super(platform, key);
    }
}
```

### Generation 2: LockLogin2 — Plugin API

The intermediate refactor introduced a cleaner, interface-based approach:

- **Interface-based** — `Module` interface with `onLoad()`, `onEnable()`, `onDisable()` lifecycle methods.
- **PluginModule\<T\>** — a generic interface allowing external plugins to register modules while retaining a reference to their own plugin instance.
- **Static singleton access** — `CurrentPlugin.whenAvailable()` callback pattern for lazy API access.
- **ModuleManager** — centralized event firing, handler registration, and command execution.
- **Resource handles** — structured resource loading with `ResourceHandle` for module assets.

```java
// LockLogin2 style — implementing a Module interface
public class MyModule implements PluginModule<MyPlugin> {
    private final MyPlugin plugin;
    
    public void onEnable() {
        getManager().addEventHandler(this, SomeEvent.class, event -> { /* ... */ });
    }
}
```

### Generation 3: LockLogin Neo — Essentials API

The current system uses a modular essentials architecture centered around the `LockLogin` singleton:

- **Centralized API access** — all registration and configuration goes through `LockLogin.get()`, which provides access to dedicated managers for each subsystem.
- **Configurations manager** — `LockLogin.get().configurations()` manages configuration parts and files, allowing components to register their own config sections via `append()` and retrieve parsed values via `part()`.
- **Localizations manager** — `LockLogin.get().localizations()` manages multi-language localization bundles, allowing components to register translation strings via `append()` and retrieve locale-aware readers via `bundle()`.
- **Hashes manager** — `LockLogin.get().hashes()` manages hash method registration, allowing components to register hash methods with a specific purpose (e.g., `Purpose.PASSWORD`).
- **Persistence manager** — `LockLogin.get().persistence()` gets and sets the persistence layer, allowing components to provide database-backed storage.
- **Auth layers manager** — `LockLogin.get().authLayers()` manages authentication layer registration for custom auth flows.
- **NamespacedIdentity** — every registered resource gets a unique namespaced ID, preventing collisions between components.
- **Platform-agnostic** — the `neo-api-bridge` module provides abstractions for player interaction, protocol messaging, and positioning that work across any server platform.
- **AuthLayer system** — replaces the old card-based challenge system with a flexible, hook-based authentication layer system supporting mandatory/optional layers, timeouts, and async resolution.
- **ConfigPart system** — a flexible configuration framework where components define their own configuration sections with scoped paths.
- **Localization system** — a multi-language i18n framework with placeholder substitution, cross-locale fallback, and bundle-based writing.

```java
// LockLogin Neo style — registering through LockLogin.get()
LockLogin<?> api = LockLogin.get();

// Register configuration parts
api.configurations().append(ConfigFile.PLUGIN_CONFIG, new MyConfiguration());

// Register localization bundles
api.localizations().append(LocaleDir.MESSAGES, new MyLocalization());

// Register hash methods
api.hashes().register(new MyHashMethod(config), Purpose.PASSWORD);

// Set persistence manager
api.persistence(new MyPersistenceManager(driver));
```

### Migration Summary

| Concept | LockLoginReborn | LockLogin2 | LockLogin Neo |
|---|---|---|---|
| Module type | `PluginModule` (abstract class) | `Module` (interface) | Manager-based components |
| Plugin registration | N/A (JAR-loaded only) | `PluginModule<T>` | Built-in essentials |
| Lifecycle | Constructor + platform init | `onLoad/Enable/Disable()` | `loaded/enabled/disabled()` |
| Event handling | `@ModuleEventHandler` + reflection | `ModuleManager.addEventHandler()` | Manager-based via `LockLogin.get()` |
| Resource IDs | String-based | String-based | `NamespacedIdentity` |
| API access | Direct module fields | `CurrentPlugin` singleton | `LockLogin.get()` singleton |
| Async tasks | `APICard` queue system | Standard Java concurrency | `AuthLayer.Hook` system |
| Platform support | Bukkit/Velocity | Bukkit/Velocity | Platform-agnostic (Paper coming) |
| Configuration | Rigid, hardcoded paths | Rigid, hardcoded paths | `ConfigPart` + `SchemaWriter` system |
| Auth process expansion | Monolithic auth flow | `UserAuthProcess` chain (conceptual) | `AuthLayer` with Hooks (fully implemented) |

## Auth Process Evolution

One of the most important architectural improvements in Neo is how the authentication process can be extended by third parties:

- **LockLoginReborn** — the authentication flow was entirely monolithic and internal. No way to inject custom steps or modify the process from outside the plugin.
- **LockLogin2** — introduced the `UserAuthProcess` interface with a chain-of-responsibility pattern (`process(AuthProcess previous)`), allowing modules to define custom authentication steps. However, this remained largely conceptual — the API existed but was never fully realized in practice.
- **LockLogin Neo** — fully implements and expands on this idea with the `AuthLayer` system. Each layer is registered through `LockLogin.get().authLayers()` with a namespaced identity, supports mandatory/optional modes, configurable timeouts, and ordering via `before()` declarations. The `AuthLayer.Hook` pattern provides a one-time-use callback system (`onSuccess`, `onFailure`, `always`) for async resolution, and typed failure results (`MESSAGE`, `SILENT`, `KICK`) give fine-grained control over what happens on authentication failure.

## Configuration API

The Configuration API is an entirely **new feature in LockLogin Neo** — neither LockLoginReborn nor LockLogin2 had a structured, extensible configuration framework. In previous versions, configuration paths and structures were rigid and hardcoded within the plugin, making it impossible for third-party components to define their own settings or for users to customize behavior beyond what the plugin authors anticipated.

LockLogin Neo replaces this with a flexible, schema-driven configuration system built around three core concepts:

### ConfigPart — Scoped Configuration Sections

A `ConfigPart` represents a scoped section of a configuration file. Each part declares a `ConfigPath` (its "scope" in the YAML document) and an `init()` method that defines the schema for that section. Multiple parts can coexist in the same file, each managing its own section without conflicts.

```java
public class MyConfig extends ConfigPart {
    @Override
    public ConfigPath path() {
        return ConfigPath.of("myplugin"); // writes under "myplugin:" in YAML
    }

    @Override
    public void init(ConfigReader reader, ConfigWriter writer) {
        // Define the schema for this section
        writer.schema()
            .with("enabled", "Whether the feature is enabled")
            .bool(v -> v.required(true))
            .with("maxPlayers", "Maximum number of players")
            .intNumber(v -> v.with(() -> 100))
            .build();
    }
}
```

Using `ConfigPath.empty()` writes at the root of the document, while `ConfigPath.of("section", "subsection")` nests the part under `section.subsection:`. If two parts share the same path, their schemas are automatically merged.

### ConfigReader & ConfigWriter — Read and Write

`ConfigReader` provides typed access to configuration values (`getString`, `getInt`, `getBool`, `getSection`, etc.), all returning `Optional` for safe access. `ConfigWriter` extends `ConfigReader` with write capabilities, allowing parts to set defaults, comments, and nested sections during initialization.

The writer supports all common YAML types: strings, bytes, shorts, ints, longs, floats, doubles, booleans, sections (nested mappings), sections lists, and string lists.

### SchemaWriter — Declarative Schema Definition

The `SchemaWriter` is the heart of the configuration system. It provides a fluent, declarative API for defining configuration properties with full control over defaults, validation, and comments:

- **`with(key, comments...)`** — starts a new property definition with optional documentation comments.
- **Type methods** — `string()`, `intNumber()`, `bool()`, `section()`, etc. define the property type.
- **`with(defaultValue)`** — sets an initial value that is written if the key is missing.
- **`required(defaultValue)`** — marks the value as required; if the user provides an invalid type, the default is restored.
- **`validator(predicate)`** — registers a custom validator; if the predicate returns `false`, the value is considered invalid and the required default (if any) is used.
- **`constant(value)`** — locks a value that cannot be changed by the user; any modification is reverted on each initialization.

Schemas can be merged via `SchemaWriter.merge()`, combining definitions from multiple parts that share the same path. The schema system also supports nested sections and lists of sections, enabling deeply structured configuration hierarchies.

### ConfigFile & ConfigRegistry

`ConfigFile` represents a physical YAML file on disk, handling directory creation and stream management. A convenience constant `ConfigFile.PLUGIN_CONFIG` points to the standard `config.yml` file. Custom files can be created with nested routes: `new ConfigFile("settings", "subdir")`.

`ConfigRegistry` (accessed via `LockLogin.get().configurations()`) manages all registered parts and files. Parts are appended to files via `append(file, part)`, and the registry asynchronously processes all configuration files using virtual threads, reading existing YAML, applying schemas, and writing the result back with proper formatting.

### Why This Matters

The Configuration API is one of the defining features of LockLogin Neo. It enables:

- **Component isolation** — each component defines its own configuration section without touching or depending on other components' config.
- **Schema validation** — configuration values are validated at startup, not at runtime, catching errors early.
- **Self-documenting configs** — comments defined in schemas are written directly to the YAML file, making configuration self-documenting.
- **Safe migrations** — validators can detect outdated values and automatically migrate them to new formats.
- **Merged schemas** — multiple components can contribute to the same config section, with their schemas automatically merged.

## Localization API

Localization **existed** in previous LockLogin versions, but with severe limitations. Messages were stored in monolithic files — one per locale (`messages_en.yml`, `messages_es.yml`, etc.). Users could modify the **values** of existing translation keys, but any structural change — **adding** a new key, **removing** an existing one, or **changing its type** (e.g., from a `string` to a `string list`) — would cause that key to be **reset** on the next reload, as the plugin overwrote the files with its own hardcoded schema. This made it impossible to extend the messaging system or add custom translations without modifying the plugin itself.

LockLogin Neo replaces this rigid, monolithic approach with a complete i18n system built around locale bundles, phrase objects with automatic placeholder detection, and cross-locale fallback. Keys are still reset to the schema defined by registered `Localization` instances — but the key difference is that the localization schema itself can now be **changed** through the API by adding more `Localization` instances to an existing `LocaleDir`. This means components can contribute new keys, remove existing ones, or change key types, and later-registered localizations take precedence over earlier ones, effectively allowing extensions to override built-in messages.

### Localization — Multi-Language Entry Point

A `Localization` is the entry point for defining translation strings. It provides a single `init(LocaleBundle bundle)` method where all phrases are registered. The bundle manages multiple locales simultaneously, so writing a phrase in one locale propagates the key to all other locales (with the original value as a fallback), while allowing each locale to override the value with its own translation.

```java
public class MyLocalization extends Localization {
    @Override
    public void init(LocaleBundle bundle) {
        // Write to English — the key is automatically available in other locales
        bundle.writer(Locale.ENGLISH).write("greeting", "Hello, {username}!");
        bundle.writer(Locale.ENGLISH).write("goodbye", "Goodbye!");

        // Override with Spanish translation
        bundle.writer(Locale.of("es")).write("greeting", "¡Hola, {username}!");
        bundle.writer(Locale.of("es")).write("goodbye", "¡Adiós!");
    }
}
```

### LocaleBundle — Coordinated Multi-Locale Writing

The `LocaleBundle` coordinates reading and writing across all configured locales. When a key is written in one locale, it is automatically propagated to all other locales that do not already have that key. This means the first locale to define a key acts as the "source of truth" for the default value, while subsequent locales can override it with translations.

The bundle tracks which locales have been accessed and provides `reader(locale)` and `writer(locale)` methods for locale-specific access.

### Phrase — Messages with Placeholder Detection

A `Phrase` wraps a localized string and automatically detects all placeholders (`{key}`) in the message. The placeholder scanner handles edge cases like escaped braces (`\{`), double braces (`{{` → `{}`), and nested braces, ensuring accurate placeholder extraction.

Phrases carry their locale, raw message, and the set of detected placeholders — information used by the `Placeholders` resolver to substitute values at runtime.

### Placeholders — Fluent Value Substitution

The `Placeholders` class provides a fluent API for substituting placeholders in phrases. Since `LocaleReader#find()` returns `Optional<Phrase[]>`, API consumers typically apply placeholders to an array, receiving a `String[]` in return:

```java
Phrase[] phrases = reader.find("greeting").orElse(new Phrase[0]);
String[] result = Placeholders.parser()
    .with("username", player.getName())
    .with("count", 42)
    .apply(phrases);
```

Placeholders are detected automatically by the `Phrase` class using the `{key}` syntax. When `apply()` is called, each placeholder is replaced by the corresponding value registered via `with(key, value)`.

#### Parser Resolution Order

When a placeholder value is substituted, `Placeholders` determines how to format the value by following this resolution order:

1. **Type-specific parser** — if a parser is registered for the exact class of the value via `registerParser(Class<T>, Function)`, it is used immediately.
2. **Generic parser** — if no type-specific parser matches, each registered generic parser (registered via `registerParser(Predicate<Object>, Function)`) is tested in turn; the first one whose predicate returns `true` is used.
3. **Array fallback** — if no parser matches and the value is an array, `Arrays.deepToString()` is used.
4. **Default fallback** — if nothing else matches, `String.valueOf()` is used.

This means type-specific parsers always take priority over generic parsers, and generic parsers are only a last resort before the default string conversion.

#### Type-Specific Parsers

A type-specific parser is registered for an exact class. When a placeholder value's class matches the registered class, the parser function is called to format the value:

```java
// Register a parser for java.util.Date
Placeholders.registerParser(Date.class, (Date date) -> {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
});

// Now any placeholder bound to a Date will be formatted automatically
Phrase[] phrases = reader.find("lastLogin").orElse(new Phrase[0]);
// Message: "You last logged in at {date}"
String[] result = Placeholders.parser()
    .with("date", new Date())
    .apply(phrases);
// Result: "You last logged in at 2026-11-23 23:10"
```

Type-specific parsers are ideal when you know the exact type of values you will pass — for example, formatting `Duration` objects, `UUID` objects, or custom value objects.

```java
// Format Duration as a human-readable string
Placeholders.registerParser(Duration.class, (Duration dur) -> {
    long hours = dur.toHours();
    long minutes = dur.toMinutesPart();
    return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
});

// Format UUID without hyphens
Placeholders.registerParser(UUID.class, (UUID uuid) -> uuid.toString().replace("-", ""));
```

**Important:** the class match is exact — superclasses and implementations are **not** considered. Registering a parser for `List.class` will not match an `ArrayList` value; you would need to register the parser for `ArrayList.class` specifically, or use a generic parser instead.

#### Generic Parsers

A generic parser uses a predicate to match any object type, making it useful for broad matching strategies that type-specific parsers cannot express:

```java
// Match any object that implements CharSequence
Placeholders.registerParser(
    obj -> obj instanceof CharSequence,
    (Object obj) -> ((CharSequence) obj).toString().toUpperCase()
);
```

Generic parsers are evaluated in insertion order, and the **first** matching predicate wins. This makes them suitable for fallback strategies:

```java
// Any enum value — format as its name in lowercase
Placeholders.registerParser(
    obj -> obj instanceof Enum,
    (Object obj) -> ((Enum<?>) obj).name().toLowerCase()
);

// Any number — format with thousand separators
Placeholders.registerParser(
    obj -> obj instanceof Number,
    (Object obj) -> NumberFormat.getInstance().format((Number) obj)
);
```

#### Combined Example

Type-specific and generic parsers can work together, with type-specific parsers taking priority:

```java
// Type-specific: exact match for LocalDateTime
Placeholders.registerParser(LocalDateTime.class, (LocalDateTime dt) ->
    dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
);

// Generic: any TemporalAccessor (covers ZonedDateTime, OffsetDateTime, etc.)
Placeholders.registerParser(
    obj -> obj instanceof TemporalAccessor,
    (Object obj) -> ((TemporalAccessor) obj).toString()
);

// LocalDateTime uses the type-specific parser (priority)
// ZonedDateTime falls through to the generic parser
// An unknown type falls back to String.valueOf()
```

#### Common Use Cases

- **Date/Time formatting** — register parsers for `Date`, `LocalDateTime`, `Instant`, etc., to control how timestamps appear in messages.
- **Enum formatting** — convert enum values to human-readable labels (e.g., `Status.ONLINE` → `"online"`).
- **Number formatting** — apply locale-aware number formatting, currency symbols, or percentage signs.
- **Custom objects** — format domain-specific objects (e.g., a `Money` class, a `Version` class) without requiring `toString()` overrides.
- **Collection formatting** — join lists or sets into comma-separated strings with custom delimiters.

### LocaleDir & LocaleRegistry

`LocaleDir` represents a directory of localization files (e.g., `messages/`, `commands/`). Files follow the naming convention `localization.<locale>.yml` (e.g., `localization.en.yml`, `localization.es.yml`, `localization.pt_BR.yml`). A convenience constant `LocaleDir.MESSAGES` points to the standard `messages/` directory.

`LocaleRegistry` (accessed via `LockLogin.get().localizations()`) manages all registered localization bundles. Localizations are appended via `append(dir, localization)`, and the registry asynchronously processes all directories using virtual threads.

Unlike configuration — where schemas from multiple `ConfigPart` instances are merged — localization does **not** apply any kind of merging. When multiple `Localization` instances are registered to the same `LocaleDir`, they are applied in order, and later instances **overwrite** earlier ones. This means:

1. The registry reads existing YAML files from disk first.
2. Each registered `Localization` runs its `init(bundle)` method in insertion order.
3. The final bundle is composed and written back to disk, resetting any keys not defined by the registered localizations.

This overwrite behavior is the mechanism that allows extensions to **extend** or **override** built-in messages: an extension can register its own `Localization` to an existing `LocaleDir`, and its keys will be added to (or overwrite) the built-in ones.

```java
// Built-in essential registers first
LockLogin.get().localizations().append(LocaleDir.MESSAGES, new BuiltInMessages());

// An extension can later register to the same directory to override
LockLogin.get().localizations().append(LocaleDir.MESSAGES, new CustomMessages());
// CustomMessages keys will overwrite BuiltInMessages keys with the same name
```

To read localized messages, use `bundle(dir, locale)` to obtain a `LocaleReader`, then call `find(key)` or `get(key, default...)` to retrieve phrases. If the requested locale does not exist, the first available locale is used as a fallback.

### Why This Matters

The Localization API enables:

- **True internationalization** — server owners and translators can maintain translations in multiple languages without touching code.
- **Cross-locale fallback** — untranslated phrases automatically fall back to the first locale that defined them, preventing missing messages.
- **Placeholder safety** — automatic placeholder detection ensures translators know exactly which variables a message expects.
- **Component isolation** — each component registers its own localization bundle in its own directory, avoiding key collisions.
- **Runtime substitution** — the `Placeholders` API makes it trivial to inject dynamic values into localized messages.

## Key Features

- **Modular Essentials** — a modular architecture where built-in essentials provide core functionality (database persistence, password hashing) that can be disabled or replaced. Resources are identified by namespaced IDs to prevent collisions.
- **Authentication Layers** — a flexible challenge-based system where each layer defines how a player authenticates. Layers can be mandatory or optional, support timeouts, ordering, and use one-time hooks for async resolution with typed failure results.
- **Multi-Platform Design** — the API and bridge layers are designed to be platform-agnostic, with player adapters that abstract away server-specific details. A PaperMC-compatible version is on the horizon.
- **Configuration Framework** — a schema-driven configuration system (`ConfigPart` + `SchemaWriter`) that lets components define their own configuration sections with scoped paths, validation, comments, and safe migrations — a feature that did not exist in previous LockLogin versions.
- **Localization Framework** — a complete i18n system with multi-language locale bundles, automatic placeholder detection, cross-locale fallback, and fluent value substitution — replacing the rigid, monolithic localization files of previous versions where structural changes (adding, removing, or changing key types) would reset translation keys. Now the localization schema itself can be changed through the API by registering additional `Localization` instances to an existing `LocaleDir`.
- **Security & Hashing** — built-in support for password hashing with configurable hash methods and properties.
- **Messaging Protocol** — a protocol-based messaging system with codecs, envelopes, and payloads for structured communication.

## Status

**Work in Progress.** LockLogin Neo is actively under development. While the core architecture and API are taking shape, the project is not yet feature-complete. Development is moving forward steadily, and a PaperMC-compatible version may be closer than it seems.

## Building

The project uses Gradle with Kotlin DSL:

```bash
./gradlew build
```

## License

See the [LICENSE](LICENSE) file for details.
