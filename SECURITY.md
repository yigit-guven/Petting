# Security Policy

We take the security and integrity of the **Petting** mod seriously, especially concerning server stability, NBT vulnerabilities, permission checks, and packet handling.

---

## Supported Versions

Security and critical bug fixes are prioritized for the latest release branch, with limited support for active LTS versions:

| Mod Version | Minecraft Version | Mod Loader | Supported |
| :--- | :--- | :--- | :---: |
| **5.0.x** | 26.3 | NeoForge | :white_check_mark: |
| **4.2.x** | 1.21.1 | NeoForge | :white_check_mark: |
| **4.2.x** | 1.20.1 | Forge / Fabric | :warning: *(Critical only)* |
| **< 4.2.0** | Older versions | Any | :x: |

---

## Reporting a Vulnerability

If you discover a security vulnerability (such as a server-crashing exploit, unauthorized command execution, packet spoofing, or item duplication glitch), please report it privately rather than opening a public issue.

### Preferred Reporting Method

1. **GitHub Private Vulnerability Reporting**:
   Go to the [Security Advisories](https://github.com/yigit-guven/Petting/security/advisories/new) page of the repository and click **"Report a vulnerability"**.

2. **Direct Email**:
   If you prefer email, you can send details directly to:
   **[contact@yigitguven.net](mailto:contact@yigitguven.net)** with the subject line `[SECURITY] Petting Mod - Vulnerability Report`.

### What to Include in Your Report

To help us investigate and patch the issue quickly, please provide:
- A clear description of the vulnerability.
- Steps to reproduce the issue (including any specific commands, items, or setup required).
- Affected mod version, Minecraft version, and mod loader (Forge, NeoForge, or Fabric).
- Expected impact (e.g. client crash, dedicated server crash, griefing, or arbitrary command execution).
- Any relevant logs, crash reports, or proof-of-concept clips.

---

## Response & Disclosure Process

- **Acknowledgment**: You can expect an initial acknowledgment within **48 to 72 hours**.
- **Investigation & Fix**: We will investigate, verify the issue, and prepare a patch in a private branch.
- **Release**: Once a patch is validated, an update will be published to CurseForge, Modrinth, and GitHub Releases.
- **Credit**: We will gladly credit you in the release notes and advisory (unless you prefer to remain anonymous).
