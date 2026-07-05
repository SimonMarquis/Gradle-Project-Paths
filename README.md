<h1>
  <img src=".idea/icon.png" alt="Plugin icon" width="32">
  Gradle Project Paths
</h1>

> An IntelliJ plugin that helps autocomplete Gradle project paths.  
> For those of us who'd rather avoid [`enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`](https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html#sec:type-safe-project-accessors).

![Build](https://github.com/SimonMarquis/Gradle-Project-Paths/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/32671.svg)](https://plugins.jetbrains.com/plugin/32671)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/32671.svg)](https://plugins.jetbrains.com/plugin/32671)

| Path Completion | Unknown Module |
|-----------------|----------------|
| <picture><source media="(prefers-color-scheme: dark)" srcset="art/path-completion-dark.png"><img alt="Path completion" src="art/path-completion-light.png"></picture> | <picture><source media="(prefers-color-scheme: dark)" srcset="art/unknown-module-dark.png"><img alt="Unknown module" src="art/unknown-module-light.png"></picture> |

## Features

- **Autocompletion**: suggests all Gradle project paths inside `project("...")` calls
- **Validation**: detect and highlights unknown Gradle project paths
- **Navigation** gutter icons or <kbd>Ctrl</kbd>+<kbd>click</kbd> on Gradle project paths to jump to its Gradle build file

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Gradle Project Paths"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/32671) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/32671/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/SimonMarquis/Gradle-Project-Paths/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Credits

- Plugin based on the [IntelliJ Platform Plugin Template][template] and [slackhq/foundry Skate plugin](https://github.com/slackhq/foundry/tree/main/platforms/intellij/skate)
- Inspired by [@ZacSweers](https://github.com/ZacSweers) - [Don't use Type-safe Project Accessors with Kotlin Gradle DSL](https://www.zacsweers.dev/dont-use-type-safe-project-accessors-with-kotlin-gradle-dsl/)
- [gradle/issues#34088](https://github.com/gradle/gradle/issues/34088)

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
