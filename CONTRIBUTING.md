# Contributing to EasyEmail

Thank you for your interest in contributing to EasyEmail!

EasyEmail is a lightweight Android library for sending inquiries and replies via EmailJS, with optional Firebase Realtime Database integration and IMAP inbox fetching. Please read this guide before opening an issue or submitting a pull request.

---

# Code of Conduct

This project follows a respectful and professional standard of communication. Be kind, constructive, and inclusive in all discussions, pull requests, and issue threads.

---

# Before You Start

Before contributing, please:

- Read the README to understand the project goals and current features.
- Make sure your change fits the scope of the library.
- Open an issue first for major changes, new features, or breaking changes.
- Avoid adding unrelated refactors in the same pull request.

---

# Project Scope

EasyEmail is focused on:

- Sending inquiry emails via EmailJS
- Sending reply emails via EmailJS
- Fetching replies from an IMAP inbox
- Optional Firebase Realtime Database integration
- Offline queue support using WorkManager and Room
- Clean Java-based Android library usage

Please do not introduce alternative email providers or unrelated functionality unless the change is discussed and approved first.

---

# Development Setup

## Requirements

- Android Studio
- Java 8+ compatible setup
- Gradle
- An Android device or emulator for testing

## Local Setup

1. Fork the repository.
2. Clone your fork to your machine.
3. Open the project in Android Studio.
4. Sync Gradle and make sure the project builds successfully.
5. Create a feature branch before making changes.

Example branch names:

- `feature/add-email-validation`
- `fix/imap-retry-bug`
- `docs/update-readme`

---

# How to Contribute

## 1. Report an Issue

When opening an issue, include:

- A clear title
- What you expected to happen
- What actually happened
- Steps to reproduce
- Logs, stack traces, or screenshots if available
- Device and Android version if relevant

## 2. Improve Documentation

Documentation improvements are always welcome. This includes:

- README corrections
- Usage examples
- API documentation
- Setup instructions
- Security notes

## 3. Fix Bugs

If you want to fix a bug:

- Reproduce the issue first
- Keep the fix as small and focused as possible
- Add comments only when the code would otherwise be hard to understand
- Make sure your fix does not break existing behavior

## 4. Add Features

For new features:

- Open an issue first if the change is significant
- Explain the use case clearly
- Keep backward compatibility whenever possible
- Update the README if the public API changes

---

# Coding Guidelines

Please follow these rules when contributing code:

- Write clean, readable, and maintainable Java code
- Keep methods small and focused
- Use meaningful class, method, and variable names
- Avoid duplicating logic
- Preserve existing public APIs unless a breaking change is necessary
- Do not introduce unnecessary dependencies
- Keep Android-specific code compatible with the project’s current structure

---

# API and Compatibility Rules

Because this is a library project, public API stability matters.

Before changing any public class, method, or config field:

- Check how it is used in the README and demo code
- Think about backward compatibility
- Update examples and documentation if the API changes
- Avoid breaking existing integration for current users

---

# Testing

Before submitting a pull request, please verify that:

- The project builds successfully
- Your change works in a sample app or relevant test case
- Existing behavior is still correct
- Any new edge cases are handled

If you add or change behavior, include a short explanation of how you tested it.

---

# Commit Message Style

Use clear, focused commit messages.

Good examples:

- `fix: handle offline queue retry`
- `feat: add custom notification title`
- `docs: improve usage examples`

Avoid vague commit messages such as:

- `update`
- `changes`
- `fix stuff`

---

# Pull Request Checklist

Before submitting a pull request, make sure:

- The code compiles
- The change is focused on a single task
- The README is updated if needed
- Any new behavior is explained clearly
- Screenshots or logs are included when useful
- The branch is up to date with the latest main branch

---

# Security Notes

This project works with email credentials, API keys, and IMAP settings. Please be careful with sensitive data.

Do not:

- Hardcode secrets in source code
- Commit real EmailJS public keys, app passwords, or Firebase credentials
- Log sensitive credentials or tokens
- Share private test accounts in public issues

Use secure storage or build-time configuration for secrets whenever possible.

---

# Documentation Updates

If your change affects public behavior, please update:

- `README.md`
- Code examples
- Configuration notes
- Known limitations
- Any related issue or release notes

---

# License

By contributing to this project, you agree that your contributions will be licensed under the same license as the project.

Thank you for helping improve EasyEmail!
