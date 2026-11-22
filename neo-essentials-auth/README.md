# neo-essentials-auth

**neo-essentials-auth** is part of the **neo-essentials** set — a collection of built-in components whose source code lives inside the LockLogin codebase and are required for the plugin environment to function properly. It provides the core authentication flow — the password-based login and registration layers that form the foundation of player authentication.

Beyond its role as a built-in component, **this module is also intended to serve as a reference implementation** — a practical, real-world example of how to structure a LockLogin essential that implements authentication layers, platform-specific commands, and the challenge-hook pattern.

---

## What is the neo-essentials set?

Before explaining what the auth essential does, it's important to understand the concept behind "neo-essentials."

Neo-essentials is a set of official components whose code always lives inside the LockLogin codebase. They are not third-party add-ons — they are loaded by LockLogin during its own startup lifecycle. Think of them as "built-in features" that are modular and replaceable.

They are called **essentials** because they are essential for the plugin environment itself. Even if a server owner disables an essential feature, the API remains available so that other components depending on it can still function.

### Why are they replaceable?

The essentials system is designed so that no built-in feature is permanent. Disabling an essential feature does not remove the component or its API — it simply tells LockLogin **not to load the essential class**. The API remains available for any other component that requires it.

For example, if a server owner installs a third-party component that provides its own authentication layers (e.g., 2FA, hardware tokens), that component can tell LockLogin to skip loading the built-in auth essential during the `loaded` lifecycle phase:

```java
EssentialFeatures.disable(EssentialFeatures.FEATURE_AUTH);
```

This call should be performed during the `loaded` lifecycle phase, before the essential class is instantiated. The auth essential's API and classes remain on the classpath — LockLogin simply skips the loading step. This means a custom authentication component can fully replace the built-in password-based behavior without any conflict.

### Where do essentials live?

All essentials live inside the LockLogin codebase. Their source code is part of the project, and they are loaded by LockLogin itself during startup — they go through the same lifecycle phases, register the same services, and follow the same conventions as any third-party component.

---

## What does this essential do?

At a high level, the auth essential solves one problem: **how does a player prove their identity to the server?**

It implements the two most fundamental authentication operations:

- **Registration** — a new player creates an account by choosing and confirming a password, which is hashed and stored in their account record.
- **Login** — an existing player proves their identity by entering the password they registered with, which is verified against the stored hash.

These operations are implemented as `AuthLayer` instances, which are the building blocks of LockLogin's authentication pipeline. Each layer represents a single challenge a player must complete, and the auth essential provides the two most common ones: password registration and password login.

### Why AuthLayers?

The `AuthLayer` system replaces the monolithic authentication flow of previous LockLogin generations. Instead of a single hardcoded process, authentication is now a pipeline of independent layers. Each layer can be mandatory or optional, can define its own timeout, and resolves asynchronously through a one-time `Hook` callback. This means third-party components can add their own layers (e.g., 2FA, CAPTCHA, IP verification) that run alongside or instead of the built-in password layers.

---

## Authentication Layers

The essential ships with two authentication layers:

| Layer | ID | Purpose | Required? |
|---|---|---|---|
| **PasswordRegisterLayer** | `locklogin:password-register` | Challenges a new player to create an account with a password | Yes |
| **PasswordLoginLayer** | `locklogin:password-login` | Challenges an existing player to prove their identity with a password | Yes |

Both layers are registered automatically (`autoRegister() = true`) and are marked as required (`required() = true`), meaning a player cannot proceed without completing them. Each layer supports configurable timeouts read from `RegistrationConfig`.

### The Hook Pattern

Both layers use the `AuthLayer.Hook` pattern for async resolution. When a layer's `challenge()` method is called, it returns a `Hook` — a one-time-use callback object. The hook is stored in the player's metadata so that the corresponding command (`/register` or `/login`) can later retrieve it and call either `hook.successful()` or `hook.failure(Result)` to resolve the challenge.

This design separates the challenge initiation from its resolution, allowing the player to take their time entering a password while the layer tracks the pending state.

---

## Architecture

The essential is organized into three layers:

### 1. Entry point (`AuthEntrypoint.java`)

The `AuthEntrypoint` class is the single entry point for the auth essential. All interaction with the LockLogin API goes through `LockLogin.get()`. During the two lifecycle phases it participates in:

- **`setupConfiguration()`** — Registers `RegistrationConfig` as a config part via `LockLogin.get().configurations().append()` so that the plugin configuration file understands the `authentication` section.
- **`setup()`** — Reads the configured registration settings via `LockLogin.get().configurations().part()` and instantiates a `PlatformBootstrap` that prepares the platform-specific components.

### 2. Configuration layer (`RegistrationConfig.java`)

`RegistrationConfig` is a `ConfigPart` that defines the entire `authentication` section of the plugin configuration. It provides:

- **Algorithm selection** — `algorithm()` returns a `NamespacedIdentity` for the password hashing algorithm (default: `locklogin:argon2`). The value supports both short names (e.g., `bcrypt`) and fully qualified namespaced IDs (e.g., `myplugin:custom-hash`).
- **Registration timeout** — `registerTimeout()` returns an `Optional<Integer>` (in seconds; default 120). A value of 0 or less disables the timeout.
- **Password confirmation** — `confirmPassword()` returns a boolean (default `true`) that determines whether the player must type their password twice during registration.
- **Login timeout** — `loginTimeout()` returns an `Optional<Integer>` (in seconds; default 60). A value of 0 or less disables the timeout.

Every field includes documentation strings and validators, so the configuration file is self-documenting and errors are caught at startup.

### 3. Platform layer (`PlatformBootstrap` and `BukkitPlatform`)

`PlatformBootstrap` is the bridge between the platform-agnostic entry point and platform-specific implementations. Currently, it instantiates a single `BukkitPlatform` that:

- Registers both `AuthLayer` instances (`PasswordRegisterLayer`, `PasswordLoginLayer`) via `LockLogin.get().authLayers().register()`.
- Sets up two Bukkit commands (`/register`, `/login`) with dedicated `CommandExecutor` implementations.

This separation means the core configuration and entry point logic is entirely platform-agnostic, while all Bukkit-specific code (commands, metadata, player events) is isolated in the `platform.bukkit` package.

---

## Commands

The essential provides two commands for the Bukkit platform:

### `/register <password> [confirm]`

Implemented by `BukkitRegisterCommand`. When a player runs this command:

1. The command retrieves the pending `Hook` from the player's metadata.
2. If password confirmation is enabled, it validates that both password arguments match.
3. It looks up the configured hashing algorithm via `LockLogin.get().hashes().get()`.
4. The password is hashed, stored in the player's account secret map under `Account.SECRET_PASSWORD`, and the account's `registeredAt` timestamp is set.
5. After saving, the login layer is disabled for this session (since the player just registered), and the hook is resolved successfully.

### `/login <password>`

Implemented by `BukkitLoginCommand`. When a player runs this command:

1. The command retrieves the pending `Hook` from the player's metadata.
2. It fetches the player's account and checks whether a password secret exists. If not, the player is prompted to register.
3. The stored hash is parsed, and the algorithm that created it is resolved via its namespaced identity stored in the hash properties.
4. The password is verified against the stored hash using the original algorithm.
5. If the hash parameters are outdated (e.g., server owner increased security), the password is re-hashed with the new parameters and saved.
6. On success, the hook is resolved; on failure, the player is prompted to try again.

---

## Why this essential is a good example for real-world scenarios

If you're building a LockLogin component and wondering how to structure it, this module demonstrates several patterns that apply to most real-world components:

### Pattern 1: Centralized API access

All interaction with the LockLogin API goes through `LockLogin.get()`. This singleton provides access to dedicated managers — `configurations()`, `hashes()`, `persistence()`, and `authLayers()` — so every component uses the same entry point for registration and configuration.

### Pattern 2: Lifecycle-aware initialization

The essential distinguishes between `setupConfiguration()` and `setup()` phases. Configuration registration happens during `setupConfiguration()` because it needs to be available before the plugin reads the config file. Platform bootstrap happens during `setup()` because it requires the configuration to already be parsed. Understanding when to do what is critical for components that interact with the configuration system.

### Pattern 3: Configuration as a first-class citizen

Rather than reading config values ad-hoc, the essential defines a dedicated `ConfigPart` with a full schema. This pattern ensures that configuration is validated at startup, documented inline, and accessible through typed methods rather than string lookups.

### Pattern 4: Platform separation

The essential cleanly separates platform-agnostic logic from platform-specific code. The entry point and configuration know nothing about Bukkit. All Bukkit-specific code — commands, metadata, player types — lives in the `platform.bukkit` package. Adding support for a new platform requires only a new package under `platform/`, leaving the core untouched.

### Pattern 5: The Hook pattern for async challenges

The `AuthLayer.Hook` pattern is the cornerstone of LockLogin's async authentication. By returning a hook from `challenge()` and storing it for later resolution, a layer can initiate a challenge immediately while deferring the actual verification until the player responds. This pattern is essential for any auth layer that requires player input.

### Pattern 6: Algorithm-agnostic password handling

The login command doesn't assume a specific hashing algorithm. Instead, it reads the algorithm's namespaced identity from the stored hash's properties and looks up the correct `HashMethod` at runtime. This means a player who registered with Argon2 can still log in even if the server owner later switches the default to BCrypt — the original algorithm is preserved per-account.

---

## Platform Support

At present, the only supported platform is **Bukkit** (Spigot/Paper). The platform-specific implementation lives in the `platform.bukkit` package and includes the `BukkitPlatform` class, two `CommandExecutor` implementations, and two `AuthLayer<Player>` implementations.

### This essential as a multi-platform example

The auth essential is designed to demonstrate how to properly implement a multi-platform component **without depending on `neo-api-bridge`**. The core entry point and configuration are entirely platform-agnostic. Platform-specific code is isolated in a separate layer that implements the `AuthLayer` interface with a platform-specific player type.

This means adding support for a new platform (e.g., BungeeCord, Velocity, or Fabric) requires only writing a new platform adapter under `platform/` — the configuration and entry point remain untouched. The essential structure clearly separates:

- **Platform-independent core** — `AuthEntrypoint`, `RegistrationConfig`, and the metadata value abstraction.
- **Platform-specific adapter** — the Bukkit implementation that provides commands, layers, and player metadata.

This per-platform adapter approach is the recommended way to build multi-platform components, as it gives full control over platform-specific behavior without being constrained by an abstraction layer that may not fit every use case.

---

## Configuration Example

The authentication settings are configured under the `authentication` section of the plugin configuration file:

```yaml
authentication:
  algorithm: "argon2"
  registration:
    timeout: 120
    confirm: true
  login: 60
```

If any section is missing, the essential uses the default values shown above. The algorithm field accepts both short names (e.g., `argon2`, `bcrypt`, `scrypt`) and fully qualified namespaced IDs (e.g., `locklogin:argon2`, `myplugin:custom-hash`).

---

## Dependencies

This essential depends on **neo-essentials-crypto** for password hashing. The crypto essential must be loaded and its hash methods registered before the auth essential's commands can verify or hash passwords. The dependency is declared as `compileOnly` in the build configuration, meaning the crypto module must be available at runtime.

---

## Essential Identity

| Property | Value |
|---|---|
| **Name** | LockLogin auth |
| **ID** | `neo-essentials-auth` |
| **Author** | KarmaDev (karmaconfigs@gmail.com) |
| **Version** | 1.0.0 |
| **Lifecycle** | Configuration in `setupConfiguration()`, platform bootstrap in `setup()` |

The essential class is declared as `final`, meaning it cannot be subclassed. If you need to customize its behavior, the recommended approach is to build a separate component that registers its own `AuthLayer` implementations via `LockLogin.get().authLayers().register()`.
