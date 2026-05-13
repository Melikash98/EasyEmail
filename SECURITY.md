# Security Policy

## Supported Versions

Security updates are provided for the latest stable release of EasyEmail only.

| Version        | Supported          |
| -------------- | ------------------ |
| Latest release | :white_check_mark: |
| Older releases | :x:                |

If a security issue affects a published release, please upgrade to the latest version as soon as a fix is available.

## Reporting a Vulnerability

If you believe you have found a security vulnerability in EasyEmail, please do not open a public issue.

Instead, report it privately by emailing the project maintainer with:

- A clear description of the vulnerability
- Steps to reproduce it
- The affected version or commit
- Any logs, screenshots, or proof of concept that help explain the issue

After a report is submitted, you can expect:

- An initial response within a reasonable time
- A review of the report to confirm whether it is a valid security issue
- A fix or mitigation if the vulnerability is accepted
- A follow-up reply once the issue has been resolved or closed

Please do not share exploit details publicly until the issue has been reviewed and addressed.

## Security Best Practices for Contributors

When contributing to EasyEmail, please follow these rules:

- Do not commit secrets, API keys, email passwords, IMAP credentials, or Firebase credentials
- Use environment variables, build-time configuration, or secure storage for sensitive values
- Avoid logging private data in debug output
- Sanitize user input before using it in email content or network requests
- Keep dependencies up to date whenever possible
- Review changes carefully when they affect authentication, networking, storage, or email delivery
