# Contributing to Petting

Thank you for your interest in contributing to **Petting**! Whether you are reporting a bug, improving the documentation, translating into another language, or submitting code changes, your help is warmly welcomed.

---

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Features](#suggesting-features)
  - [Translations & Localization](#translations--localization)
  - [Documentation & Wiki](#documentation--wiki)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Commit Message Conventions](#commit-message-conventions)
- [Licensing](#licensing)

---

## Code of Conduct

Please be respectful, constructive, and considerate when opening issues, reviewing pull requests, and interacting in discussions or Discord.

---

## How Can I Contribute?

### Reporting Bugs

Encountered a bug or crash? Please submit an issue using our [Bug Report Template](https://github.com/yigit-guven/Petting/issues/new?template=bug_report.md).

**Please include:**
1. **Minecraft & Mod Version**: e.g., Minecraft 26.3, NeoForge 26.3.0.6-beta, Petting 5.0.0.
2. **Environment**: Singleplayer or Multiplayer (Dedicated Server / LAN).
3. **Crash Reports / Logs**: Provide the full log or crash report via [mclo.gs](https://mclo.gs/) or a GitHub Gist.
4. **Steps to Reproduce**: Clear, numbered steps to replicate the bug.
5. **Other Mods**: List any related or conflicting mods.

### Suggesting Features

Have an idea for a new feature, taming mechanic, or mod integration?
Open a [Feature Request](https://github.com/yigit-guven/Petting/issues/new?template=feature_request.md) describing the proposal, why it fits the mod, and potential balance considerations.

### Translations & Localization

Help make Petting accessible worldwide!
Language files are located in:
`src/main/resources/assets/petting/lang/`
- To add or update a language, copy `en_us.json` to your language code (e.g. `es_es.json`, `de_de.json`, `tr_tr.json`) and submit a Pull Request.

### Documentation & Wiki

All wiki pages are managed directly in the [docs/](docs) directory of this repository!
- If you find a typo, outdated information, or want to write a new guide, edit or add a Markdown file in `docs/`.
- When merged, our GitHub Actions workflow automatically syncs the changes to the [GitHub Wiki](https://github.com/yigit-guven/Petting/wiki).

---

## Development Setup

### Prerequisites
- **JDK 25** (Adoptium Temurin recommended)
- **Git**
- An IDE (IntelliJ IDEA recommended, or Eclipse / VS Code)

### Getting Started

1. **Fork and Clone** the repository:
   ```bash
   git clone https://github.com/yigit-guven/Petting.git
   cd Petting
   ```

2. **Open in IDE**:
   - Open the project root in IntelliJ IDEA as a Gradle project.
   - Wait for Gradle sync to complete.

3. **Useful Gradle Commands**:
   - Build and test compile:
     ```bash
     ./gradlew compileJava
     ```
   - Process resources and metadata:
     ```bash
     ./gradlew processResources
     ```
   - Launch the client in development:
     ```bash
     ./gradlew runClient
     ```
   - Full build:
     ```bash
     ./gradlew build
     ```

---

## Coding Guidelines

1. **Client/Server Isolation**:
   - Never call client-only code (screens, rendering, key mappings) directly from common/server code.
   - Keep network payloads well-validated on the server.
2. **Compatibility First**:
   - Petting interacts with any mob in the game. Always use defensive checks (null checks, tag checks) when interacting with unknown or third-party entity types.
3. **Performance & Memory**:
   - Avoid heavy operations or deep iterations every tick. Periodic checks (e.g., every 10–20 ticks) are strongly preferred for background maintenance tasks.
4. **Clean Code**:
   - Write clear, readable code and keep methods focused.
   - Maintain existing naming conventions and formatting.

---

## Commit Message Conventions

We encourage using [Conventional Commits](https://www.conventionalcommits.org/) for clarity:

- `feat: add 3D swimming controls for dolphin pets`
- `fix: resolve crash when opening pet inventory on dedicated server`
- `docs: update riding guide in docs/Riding-System.md`
- `refactor: optimize tick frequency for attribute syncing`
- `chore: update dependencies and gradle wrapper`

---

## Pull Requests

1. Create a branch for your change:
   ```bash
   git checkout -b feature/my-new-feature
   # or
   git checkout -b fix/issue-description
   ```
2. Make your changes, ensuring code builds cleanly via `./gradlew compileJava`.
3. Push to your fork:
   ```bash
   git push origin feature/my-new-feature
   ```
4. Open a Pull Request against the active development branch (e.g., `26.3-NeoForge`).
5. Describe the changes and reference any related issues (e.g., `Fixes #123`).

---

## Licensing

By submitting a Pull Request to this project, you agree that your contributions will be licensed under the project's [GNU Lesser General Public License v3.0 (LGPL-3.0)](LICENSE).
