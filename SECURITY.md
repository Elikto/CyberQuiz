# CyberQuiz Security Baseline

CyberQuiz is hardened for local-first operation and sideload updates.

## Enforced controls

- Release APKs are non-debuggable and built by GitHub Actions.
- Android lint and unit tests run before publishing an APK.
- Application backups are disabled.
- Cleartext HTTP traffic is disabled by manifest and Network Security Config.
- Only system certificate authorities are trusted by the app network configuration.
- Update metadata is fetched over HTTPS from the official Elikto/CyberQuiz release path.
- Update APK URLs are restricted to the official GitHub release path.
- Downloaded APKs are verified by SHA-256 before installation.
- Downloaded APK package name and version are verified.
- Downloaded APK signing certificates must match the currently installed CyberQuiz certificate.
- Update files are shared with the Android package installer through a non-exported FileProvider.

## Known distribution exception

The sideload build requests `REQUEST_INSTALL_PACKAGES` because CyberQuiz can update itself outside Google Play. This permission is intentional and user-controlled: Android still requires the user to authorize installs from CyberQuiz and confirm each installation.

For a future Google Play build, the self-update permission and updater should be removed/disabled and Play-managed updates should be used instead.
