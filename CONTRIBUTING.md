# Contracts Contributing Guidance

Thank you for your interest in contributing to Contracts! We welcome and value all contributions, from new features to bug fixes and documentation improvements. This guide will help you navigate the process.

Please note that this project is governed by our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## Table of Contents
1.  [Reporting Bugs](#1-reporting-bugs)
2.  [Suggesting Enhancements](#2-suggesting-enhancements)
3.  [Setting up Your Development Environment](#3-setting-up-your-development-environment)
4.  [Submitting a Pull Request](#4-submitting-a-pull-request)
5.  [Improving the Documentation](#5-improving-the-documentation)
6.  [Commit Message Styleguide](#6-commit-message-styleguide)
7.  [Still Need Help?](#7-still-need-help) [Ask For Help](https://github.com/jonloucks/contracts/discussions/new?category=q-a)

## 1. Reporting Bugs
We use [GitHub Issues](https://github.com/jonloucks/contracts/issues) to track bugs. Before creating a new issue, please check the existing issues to ensure the bug has not already been reported.

When submitting a [bug report](https://github.com/jonloucks/contracts/issues/new), please provide the following information:
-   **Clear and concise title:** A brief summary of the issue.
-   **Steps to reproduce:** Explain the steps to reproduce the behavior.
-   **Expected behavior:** Describe what you expected to happen.
-   **Actual behavior:** Describe what actually happened, including any error messages.
-   **Context:** Include your operating system, software version, and any relevant logs or screenshots.

## 2. Suggesting Enhancements
For suggesting enhancements or new features, please [open a GitHub Issue](https://github.com/jonloucks/contracts/issues/new) and follow these guidelines:
-   **Use a descriptive title:** Identify the suggestion clearly.
-   **Describe the enhancement:** Explain the feature in as much detail as possible, including its use case and why it would be valuable.
-   **Check existing suggestions:** Search for similar ideas in the existing issues. If a similar suggestion exists, add your support or additional context there.

## 3. Setting up Your Development Environment
To get started with local development:
1.  **Fork the repository** to your own GitHub account.
2.  **Clone your forked repository** to your local machine:
    ```sh
    git clone https://github.com/jonloucks/contracts.git
    ```
3.  **Install dependencies**:
    ```sh
    # To build a JDK it is recommended ot use at least 17
    javac --version
    ```
4.  **Run tests** to ensure everything is working correctly:
    ```sh
    ./gradlew test
    ```
5. **Code Coverage Verification** to ensure coverage:
    ```sh
    ./gradlew jacocoTestCoverageVerification
    ```
## 4. Submitting a Pull Request
Before submitting a pull request (PR), please follow these steps:
-   **Create a new branch** from the `main` branch with a descriptive name (e.g., `feature/add-new-feature` or `fix/issue-description`).
-   **Ensure your code is well-tested** and passes all existing tests with minimum coverage of 95% 
-   **Write a clear and detailed commit message** following the style guide below.
-   **Push your changes** to your forked repository and open a PR against our `main` branch.
-   **Fill out the PR template** completely.

## 5. Improving the Documentation
Documentation is vital for any project. If you find a typo, unclear explanation, or want to add a new section, please submit a PR with your changes. All documentation is located in the `docs/` folder.

## 6. Commit Message Styleguide
We use a conventional commit style for our commit messages to maintain a readable and organized history. A commit message should be structured as follows:
### 1. Subject Line:
   Conciseness: Limit the subject line to around 50-72 characters. This encourages a brief, impactful summary and ensures readability in various Git tools.
   Imperative Mood: Use the imperative mood (e.g., "Fix bug," "Add feature," "Update documentation") as if you are giving a command. This aligns with Git's own generated messages (like git merge).
   Capitalization: Capitalize the first letter of the subject line.
   No Period: Do not end the subject line with a period.
   Keywords (Optional): Consider using keywords like "feat," "fix," "chore," "docs," "style," "refactor," "test" at the beginning, especially if following a Conventional Commits specification.
### 2. Body:
   Blank Line Separation: Separate the subject line from the body with a single blank line. This is crucial for Git tools to correctly parse the message.
   Detailed Explanation: Provide a more in-depth explanation of what the change does and why it was made. Focus on the motivation and the problem it solves, rather than simply reiterating how it was implemented (which is evident in the code itself).
   Line Wrapping: Wrap the body text at approximately 72 characters to maintain readability in terminal environments.
   Bullet Points (Optional): Use bullet points or lists for better organization and clarity when describing multiple aspects of the change.
   Issue References (Optional): If the commit addresses a specific issue or bug, reference its identifier (e.g., "Closes #123," "Fixes BUG-456").
   Breaking Changes (Optional): Clearly indicate any breaking changes, often by starting a new section in the body or footer with "BREAKING CHANGE:".

## 7. Still Need Help?
*   [Ask For Help](https://github.com/jonloucks/contracts/discussions/new?category=q-a)

