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

But this doesn't mean features are disappearing forever. Most of them will eventually make their way back — not bundled into the plugin itself, but as **extensions**. By decoupling features from the core, the plugin stays lighter and less prone to errors where one broken feature brings everything down. Instead, each extension operates independently, so if something goes wrong, it's isolated and doesn't break the entire system.

## Project History

LockLogin Neo is the culmination of three generations of development:

| Generation | Project | Status | Description |
|---|---|---|---|
| **1st** | **LockLoginReborn** | Last released version | The original plugin with a monolithic module system built around an abstract `PluginModule` class. Modules were loaded via JAR files with license key validation, used a card-based queue system for async tasks, and relied heavily on reflection for event handling. Source available in `in-memory-of/LockLoginReborn/`. |
| **2nd** | **LockLogin2** | Abandoned refactor | An intermediate attempt to modernize the codebase. Introduced a clean `Module` interface with lifecycle methods (`onLoad`, `onEnable`, `onDisable`), a `PluginModule<T>` generic interface for plugin-registered modules, a static `CurrentPlugin` singleton for lazy API access, and a `ModuleManager` for event and command registration. Source available in `in-memory-of/LockLogin2/`. |
| **3rd** | **LockLogin Neo** | Active development (WIP) | The current project — a complete rewrite that takes the best ideas from both predecessors and reimagines them. Replaces the module system with a powerful extension system, introduces namespaced resource identities, a registry-based architecture, and platform-agnostic abstractions. A PaperMC-compatible version may be closer than it seems. |

## Architecture

The project is split into three Gradle modules, each with a distinct responsibility:

| Module | Description |
|---|---|
| **neo-api** | The core API — defines the contracts for extensions, configuration, messaging, security, and authentication layers. This is the foundation everything else builds on. |
| **neo-api-bridge** | A platform-agnostic bridge layer that provides abstractions for player interaction, protocol messaging (titles, action bars), and positioning. It offers a centralized way to interact with players regardless of the underlying server platform. |
| **neo-common** | Shared implementations and utilities — configuration readers/writers, network buffer handling, and common helpers used across the project. |

## API Evolution

The most significant change across generations is the extension system. Understanding this evolution helps developers migrating from older versions:

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

### Generation 3: LockLogin Neo — Extension API

The current system reimagines extensions from the ground up:

- **NeoExtension base class** — extensions extend the abstract `NeoExtension` class (which implements `Extension`), gaining access to a pre-configured `ExtensionRegistry`, `ExtensionDescription`, and helper methods like `id(String resource)` for namespaced resource identification.
- **ExtensionRegistry** — a powerful registry system where extensions register resources (auth layers, hash methods) with **namespaced identities** (`extension_id:resource_name`).
- **Plugin-based registration** — extensions are registered by plugins, not by extending framework classes. The plugin holds the extension, and the extension holds a registry.
- **NamespacedIdentity** — every registered resource gets a unique namespaced ID, preventing collisions between extensions.
- **Platform-agnostic** — the `neo-api-bridge` module provides abstractions for player interaction, protocol messaging, and positioning that work across any server platform.
- **AuthLayer system** — replaces the old card-based challenge system with a flexible, hook-based authentication layer system supporting mandatory/optional layers, timeouts, and async resolution.

```java
// LockLogin Neo style — extending NeoExtension
public class MyExtension extends NeoExtension {
    @Override
    public void enabled(ExtensionLifecycle lifecycle) {
        registry().register(AuthLayer.class, myAuthLayer);
    }
}
```

### Migration Summary

| Concept | LockLoginReborn | LockLogin2 | LockLogin Neo |
|---|---|---|---|
| Module type | `PluginModule` (abstract class) | `Module` (interface) | `NeoExtension` (abstract class) |
| Plugin registration | N/A (JAR-loaded only) | `PluginModule<T>` | Extension registered by plugin |
| Lifecycle | Constructor + platform init | `onLoad/Enable/Disable()` | `loaded/enabled/disabled()` |
| Event handling | `@ModuleEventHandler` + reflection | `ModuleManager.addEventHandler()` | Registry-based |
| Resource IDs | String-based | String-based | `NamespacedIdentity` |
| API access | Direct module fields | `CurrentPlugin` singleton | Extension lifecycle + registry |
| Async tasks | `APICard` queue system | Standard Java concurrency | `AuthLayer.Hook` system |
| Platform support | Bukkit/Velocity | Bukkit/Velocity | Platform-agnostic (Paper coming) |
| Configuration | Rigid, hardcoded paths | Rigid, hardcoded paths | `ConfigPart` + `ConfigFile` system |
| Auth process expansion | Monolithic auth flow | `UserAuthProcess` chain (conceptual) | `AuthLayer` with Hooks (fully implemented) |

## Auth Process Evolution

One of the most important architectural improvements in Neo is how the authentication process can be extended by third parties:

- **LockLoginReborn** — the authentication flow was entirely monolithic and internal. No way to inject custom steps or modify the process from outside the plugin.
- **LockLogin2** — introduced the `UserAuthProcess` interface with a chain-of-responsibility pattern (`process(AuthProcess previous)`), allowing modules to define custom authentication steps. However, this remained largely conceptual — the API existed but was never fully realized in practice.
- **LockLogin Neo** — fully implements and expands on this idea with the `AuthLayer` system. Each layer is a proper `RegistryItem` with a namespaced identity, supports mandatory/optional modes, configurable timeouts, and ordering via `before()` declarations. The `AuthLayer.Hook` pattern provides a one-time-use callback system (`onSuccess`, `onFailure`, `always`) for async resolution, and typed failure results (`MESSAGE`, `SILENT`, `KICK`) give fine-grained control over what happens on authentication failure.

## Configuration Evolution

Configuration handling has also seen a major improvement across generations:

- **LockLoginReborn & LockLogin2** — configuration was rigid and hardcoded. Configuration paths and structures were fixed within the plugin, making it difficult for extensions to define their own settings or for users to customize behavior beyond what the plugin authors anticipated.
- **LockLogin Neo** — introduces a flexible `ConfigPart` and `ConfigFile` system. Extensions can define their own configuration sections by extending `ConfigPart`, specifying a scoped path via `path()`, and initializing defaults via `init(ConfigWriter)`. The `ConfigFile` class handles file path resolution, directory creation, and stream management. Configuration parts are registered through a `ConfigRegistry`, allowing multiple extensions to each manage their own configuration sections without conflicts — a significant quality-of-life improvement for extension developers.

## Key Features

- **Extension System** — a modular plugin architecture where extensions extend `NeoExtension` to register custom authentication layers, hash methods, and resources via a registry, each with their own lifecycle (loaded, enabled, disabled). Resources are identified by namespaced IDs to prevent collisions.
- **Authentication Layers** — a flexible challenge-based system where each layer defines how a player authenticates. Layers can be mandatory or optional, support timeouts, ordering, and use one-time hooks for async resolution with typed failure results.
- **Multi-Platform Design** — the API and bridge layers are designed to be platform-agnostic, with player adapters that abstract away server-specific details. A PaperMC-compatible version is on the horizon.
- **Configuration Framework** — a flexible `ConfigPart` and `ConfigFile` system that lets extensions define their own configuration sections with scoped paths, replacing the rigid, hardcoded configuration of previous generations.
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
