# AI Code Review Demo

A Java Maven project that demonstrates **AI-powered pull request code review** using **Claude AI** and **GitHub Actions**.

When a pull request is opened (or updated), a GitHub Actions workflow automatically:
1. Computes the git diff of the PR
2. Sends the diff to the Claude API with a structured Java review prompt
3. Posts Claude's feedback as a comment directly on the PR

---

## Project Structure

```
ai-code-review-demo/
├── .github/
│   └── workflows/
│       └── ai-code-review.yml      ← GitHub Actions workflow
├── src/
│   ├── main/java/com/demo/
│   │   └── UserService.java        ← Sample service with intentional bugs
│   └── test/java/com/demo/
│       └── UserServiceTest.java    ← Unit tests (some expose the bugs)
├── pom.xml                         ← Maven build file (Java 17)
└── README.md
```

---

## What Claude Reviews

The workflow sends a structured prompt asking Claude to analyse the diff for:

| Category | Examples |
|---|---|
| 🔴 **Critical Issues** | SQL injection, NullPointerException, hardcoded passwords |
| ⚠️ **Warnings** | `==` instead of `.equals()`, returning `null` instead of `Optional` |
| 💡 **Suggestions** | `StringBuilder` vs `String` concatenation in loops, named constants |
| ✅ **Well Done** | Correct use of try-with-resources, good test coverage |
| 📋 **Verdict** | MERGE or REQUEST CHANGES recommendation |

---

## Setup

### 1. Fork / clone this repository

```bash
git clone https://github.com/<your-username>/ai-code-review-demo.git
cd ai-code-review-demo
```

### 2. Add your Anthropic API key as a GitHub secret

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Set:
   - **Name:** `ANTHROPIC_API_KEY`
   - **Value:** your Anthropic API key (`sk-ant-...`)
5. Click **Add secret**

> `GITHUB_TOKEN` is provided automatically by GitHub Actions — no setup needed.

### 3. Enable GitHub Actions (if not already enabled)

Go to the **Actions** tab of your repository and click **"I understand my workflows, go ahead and enable them"** if prompted.

---

## How to Test It

1. **Create a feature branch**
   ```bash
   git checkout -b feature/my-changes
   ```

2. **Make a change to `UserService.java`** — for example, add a new method, introduce a bug, or try to fix an existing one.

3. **Commit and push**
   ```bash
   git add src/main/java/com/demo/UserService.java
   git commit -m "Add/change something for review"
   git push origin feature/my-changes
   ```

4. **Open a Pull Request** on GitHub (base: `main` ← compare: `feature/my-changes`)

5. **Watch the Actions tab** — the `AI Code Review with Claude` workflow will start automatically.

6. **Check the PR** — within ~30 seconds Claude's review comment will appear on the PR.

---

## Sample Claude Review Output

```markdown
## 🔴 Critical Issues
- **SQL Injection** in `findUserByName`: user input is concatenated directly
  into the SQL string. Use `PreparedStatement` instead.
- **Hardcoded credentials**: `DB_USER = "root"` and `DB_PASS = "password123"`
  are committed to source control.

## ⚠️ Warnings
- **`==` vs `.equals()`** in `isAdminUser`: reference comparison will fail for
  non-interned strings. Replace with `"ADMIN".equals(role)`.
- **`getActiveUsers` returns `null`**: callers must null-check. Return
  `Collections.emptyList()` instead.
- **Silent exception swallow** in `findUserByName`: `e.printStackTrace()` is
  not proper error handling. Use a logger and/or rethrow.

## 💡 Suggestions
- Use `StringBuilder` in `buildUserReport` to avoid O(n²) string allocation.
- Replace magic numbers `18` and `120` in `isValidAge` with named constants.
- Consider making `UserService` thread-safe with `CopyOnWriteArrayList` or
  `synchronized` blocks.

## ✅ Well Done
- Correct use of try-with-resources for JDBC connection management.
- Inner `User` class is a clean data holder.

## 📋 Verdict
Multiple critical security vulnerabilities and bugs must be fixed before this
code is production-ready. **REQUEST CHANGES.**
```

---

## How It Works (Workflow Walkthrough)

```
PR opened / updated
       │
       ▼
① actions/checkout  (fetch-depth: 0 for full history)
       │
       ▼
② git diff base..head  →  /tmp/pr_diff.txt  (truncated to 8 000 chars)
       │
       ▼
③ Build JSON payload with jq  →  POST https://api.anthropic.com/v1/messages
       │                          model: claude-sonnet-4-20250514
       ▼
④ Parse Claude's response  →  extract .content[0].text
       │
       ▼
⑤ POST comment to GitHub PR  →  /repos/{owner}/{repo}/issues/{pr}/comments
       │                          Authorization: Bearer GITHUB_TOKEN (automatic)
       ▼
  Review comment visible on PR
```

---

## Requirements

| Tool | Version |
|---|---|
| Java | 17 |
| Maven | 3.8+ |
| GitHub Actions | (any runner with ubuntu-latest) |
| Claude API | Anthropic account with API key |

---

## Local Build

```bash
mvn clean test
```

The tests intentionally expose some of the bugs in `UserService.java` — look for the `_bugDemo_` test methods.

---

## License

MIT
