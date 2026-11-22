# neo-extensions-crypto

**neo-extensions-crypto** is part of the **neo-essentials** extension set — a collection of official extensions whose source code lives inside the LockLogin codebase and are required for the plugin extension environment to function properly. It provides official password hashing support for multiple cryptographic algorithms (Argon2, BCrypt, SCrypt) via [BouncyCastle](https://www.bouncycastle.org/).

Beyond its role as a built-in extension, **this module is also intended to serve as a reference implementation** — a practical, real-world example of how to structure a LockLogin extension that handles configuration, lifecycle management, and registry-based resource registration.

---

## What is the neo-essentials extension set?

Before explaining what the crypto extension does, it's important to understand the concept behind "neo-essentials."

Neo-essentials is a set of official extensions whose code always lives inside the LockLogin codebase. They are not third-party add-ons — they are loaded by LockLogin during its own startup lifecycle. Think of them as "built-in features" that happen to be implemented as extensions.

They are called **essentials** because they are essential for the plugin extension environment itself. Even if a server owner disables an essential feature, the extension's API remains available so that other extensions depending on it can still function.

### Why are they replaceable?

The essentials system is designed so that no built-in feature is permanent. Disabling an essential feature does not remove the extension or its API — it simply tells LockLogin **not to load the extension class**. The API remains available for any other extension that requires it.

For example, if a server owner installs a third-party extension that provides its own hashing implementation, that extension can tell LockLogin to skip loading the built-in crypto extension during the `loaded` lifecycle phase:

```java
EssentialFeatures.disable(EssentialFeatures.FEATURE_CRYPTO);
```

This call should be performed during the `loaded` lifecycle phase, before the essential extension class is instantiated. The crypto extension's API and classes remain on the classpath — LockLogin simply skips the loading step. This means a custom hashing extension can fully replace the built-in crypto behavior without any conflict, while other extensions that depend on the crypto API can still access it if needed.

### Where do essential extensions live?

All essential extensions live inside the LockLogin codebase. Their source code is part of the project, and they are loaded by LockLogin itself during startup — they go through the same lifecycle phases, register the same services, and follow the same conventions as any third-party extension.

---

## What does this extension do?

At a high level, the crypto extension solves one problem: **how does LockLogin securely store and verify passwords?**

Password hashing is the cornerstone of any authentication system. Storing passwords in plaintext is a security risk, and using a weak or outdated hashing algorithm makes the system vulnerable to brute-force and rainbow table attacks. This extension provides three modern, well-regarded hashing algorithms out of the box, so server owners don't have to choose between security and convenience.

### Why multiple algorithms?

Different server environments have different requirements:

- **Argon2** is the winner of the Password Hashing Competition (2015) and is considered the gold standard for password hashing. It's memory-hard, meaning it resists GPU and ASIC attacks by requiring significant memory. However, it can be slower on low-memory servers.
- **BCrypt** is a battle-tested algorithm that has been the default for many authentication systems for decades. It's based on the Blowfish cipher and provides a good balance between security and performance.
- **SCrypt** is another memory-hard function, designed to be costly to perform in custom hardware. It's a good alternative when Argon2 is not available or when a different memory-hard profile is needed.

By providing all three, the extension lets server owners choose the algorithm that best fits their server's hardware and security requirements.

---

## Supported Algorithms

The extension ships with three hashing algorithms, each powered by BouncyCastle:

| Algorithm | Hash Method Class | Configuration Class | Properties |
|---|---|---|---|
| **Argon2** | `Argon2HashMethod` | `Argon2Configuration` | Parameter type (d/i/id), version, iterations, memory, parallelism |
| **BCrypt** | `BCryptHashMethod` | `BCryptConfiguration` | Cost factor |
| **SCrypt** | `SCryptHashMethod` | `SCryptConfiguration` | Cost, block size, parallelism |

Each algorithm is implemented as a `HashMethod` that registers itself with a namespaced identity (e.g., `locklogin:argon2`) in the LockLogin extension registry. This means multiple hash methods can coexist, and the plugin can select which one to use for different purposes.

---

## Architecture

The extension is organized into three layers:

### 1. Extension entry point (`Extension.java`)

The `Extension` class is the single entry point. It uses annotation-based metadata (`@ExtensionMeta`, `@VersionMeta`, `@AuthorMeta`) to declare its identity to LockLogin. During the two lifecycle phases it participates in:

- **`loaded()`** — Registers three `ConfigPart` instances (`Argon2Configuration`, `BCryptConfiguration`, `SCryptConfiguration`) with a shared `ConfigFile` named `crypto`. This ensures the plugin configuration file understands the `hash.argon`, `hash.bcrypt`, and `hash.scrypt` sections before any hashing occurs.
- **`enabled()`** — Reads the configured parameters for each algorithm, instantiates the corresponding `HashMethod`, and registers them with the LockLogin API's hash registry. If a hash method with the same ID is already registered, the `Conflict` exception is silently caught — this allows the extension to be disabled and replaced by a third-party implementation.

### 2. Configuration layer (`*Configuration.java`)

Each algorithm has a dedicated `ConfigPart` that defines its own scoped section of the configuration file. For example, `Argon2Configuration` owns the path `hash.argon` and defines:

- **Type validation** — every field has a validator (e.g., Argon2 parameter must be one of `argon2i`, `argon2d`, or `argon2id`; BCrypt cost must be between 4 and 31).
- **Default values** — sensible defaults are provided for every field so the configuration works out of the box.
- **Documentation strings** — each field includes descriptions that explain what it does.

This approach means the configuration file is self-documenting and errors are caught at startup, not at runtime.

### 3. Algorithm layer (`*HashMethod.java` and `*Hash.java`)

Each algorithm has two core classes:

- **`*HashMethod`** — implements the `HashMethod` interface from `neo-api`. It handles hashing, verification, parsing, and property computation. The `verify()` method uses constant-time comparison (`MessageDigest.isEqual`) to prevent timing attacks. The `requiresUpdate()` method checks whether an existing hash was created with weaker parameters than the current configuration — enabling automatic re-hashing when security settings are tightened.
- **`*Hash`** — extends the base `Hash` class and provides algorithm-specific accessors (e.g., `Argon2Hash.iterations()`, `Argon2Hash.memory()`). It also includes an `outdated()` method that compares the hash's stored parameters against the current configuration to determine if re-hashing is needed.

---

## Why this extension is a good example for real-world scenarios

If you're building a LockLogin extension and wondering how to structure it, this module demonstrates several patterns that apply to most real-world extensions:

### Pattern 1: Annotation-based metadata

Instead of hardcoding extension details in a manifest or a separate file, the extension declares its name, ID, version, and author directly on the class using annotations. This keeps the metadata close to the code and makes it easy to inspect.

### Pattern 2: Lifecycle-aware initialization

The extension distinguishes between `loaded()` and `enabled()` phases. Configuration registration happens during `loaded()` because it needs to be available before the plugin reads the config file. Hash method registration happens during `enabled()` because it requires the configuration to already be parsed. Understanding when to do what is critical for extensions that interact with the configuration system.

### Pattern 3: Configuration as a first-class citizen

Rather than reading config values ad-hoc, the extension defines dedicated `ConfigPart` classes with full schemas. This pattern ensures that configuration is validated at startup, documented inline, and accessible through typed methods rather than string lookups.

### Pattern 4: Registry-based resource registration

Each hash method registers itself with a unique namespaced identity in the extension registry. This means the plugin can look up a hash method by ID at runtime, and multiple hash methods can coexist without conflicts. The registry pattern is the backbone of LockLogin's extensibility.

### Pattern 5: Graceful conflict handling

When registering hash methods, the extension catches `Conflict` exceptions and silently ignores them. This means if a third-party extension has already registered a hash method with the same ID, the built-in extension won't override it — the third-party implementation takes precedence. This is the essence of replaceability.

### Pattern 6: Security-conscious design

The extension uses constant-time comparison for password verification to prevent timing attacks. It supports automatic hash re-hashing when parameters are updated, ensuring that stored passwords are always protected with the latest security settings. These are not just convenience features — they are security best practices built into the extension's core.

---

## Hash Algorithm Details

### Argon2

Argon2 is the most configurable of the three algorithms. The `Argon2Configuration` defines:

| Property | Default | Description |
|---|---|---|
| `parameter` | `argon2id` | Argon2 variant: `argon2i`, `argon2d`, or `argon2id` (recommended) |
| `version` | `13` | Argon2 version (10 or 13) |
| `iterations` | `2` | Number of passes over the memory |
| `memory` | `16` | Memory cost as a power of 2 (2^16 KiB = 64 MiB) |
| `parallelism` | `1` | Number of parallel threads |

Argon2 stores five properties in the hash (`a`, `v`, `i`, `m`, `p`) so that verification can reconstruct the exact parameters used when the hash was created. The `outdated()` method compares these stored parameters against the current configuration to determine if re-hashing is needed.

### BCrypt

BCrypt is the simplest to configure. The `BCryptConfiguration` defines:

| Property | Default | Description |
|---|---|---|
| `cost` | `12` | Log2 cost factor (4–31, higher = slower but more secure) |

BCrypt stores a single property (`c`) in the hash. The cost factor determines the computational work required — each increment doubles the time and memory needed.

### SCrypt

SCrypt provides a middle ground between Argon2 and BCrypt. The `SCryptConfiguration` defines:

| Property | Default | Description |
|---|---|---|
| `cost` | `131072` (2^17) | CPU/memory cost (must be a power of 2) |
| `block` | `8` | Block size |
| `parallelism` | `1` | Parallelism factor |

SCrypt stores three properties in the hash (`c`, `b`, `p`). The cost parameter must be a power of 2, enforced by the configuration validator.

---

## Configuration Example

All three algorithms are configured under the `hash` section of the `crypto` configuration file:

```yaml
hash:
  argon:
    parameter: "argon2id"
    version: 13
    iterations: 2
    memory: 16
    parallelism: 1
  bcrypt:
    cost: 12
  scrypt:
    cost: 131072
    block: 8
    parallelism: 1
```

If any section is missing, the extension uses the default values shown above. The configuration is validated at startup, so invalid values (e.g., BCrypt cost of 50) will be caught before any passwords are hashed.

---

## Hash Verification and Re-hashing

One of the most important features of this extension is its support for automatic hash re-hashing.

When a user logs in, the extension not only verifies the password but also checks whether the stored hash was created with weaker parameters than the current configuration. For example, if the server owner increases the BCrypt cost from 10 to 12, the next time a user with a cost-10 hash logs in, the extension will:

1. Verify the password against the old hash.
2. Detect that the hash is outdated via `requiresUpdate()`.
3. Re-hash the password with the new cost factor.
4. Save the new hash to the database.

This means server owners can tighten security settings at any time, and existing users' hashes will be gradually upgraded as they log in — no manual migration is required.

---

## BouncyCastle Dependency

The extension depends on [BouncyCastle](https://www.bouncycastle.org/) (`org.bouncycastle:bcprov-jdk18on:1.87`) for all cryptographic operations. BouncyCastle is a well-maintained, open-source cryptography library that provides implementations of Argon2, BCrypt, and SCrypt.

The dependency is declared as `compileOnly` in the build configuration, meaning BouncyCastle must be available at runtime (e.g., included in the plugin's JAR or provided by the server). This keeps the extension's compile-time dependencies minimal while ensuring the cryptographic algorithms are available when needed.

---

## Licensing

BouncyCastle is used under the MIT License and the BouncyCastle License. Both are permissive open-source licenses that allow use in commercial and open-source projects.

---

## Extension Identity

| Property | Value |
|---|---|
| **Name** | LockLogin crypto |
| **ID** | `neo-essentials-crypto` |
| **Base class** | `NeoExtension` |
| **Author** | KarmaDev (karmaconfigs@gmail.com) |
| **Version** | 1.0.0 |
| **Lifecycle** | Configuration in `loaded()`, hash registration in `enabled()` |

The extension class is declared as `final`, meaning it cannot be subclassed. If you need to customize its behavior, the recommended approach is to build a separate extension that registers its own hash methods with the LockLogin registry.
