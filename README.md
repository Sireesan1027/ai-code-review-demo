# AI Code Review Demo

A Java Maven project that demonstrates **AI-powered pull request code review** using **Claude AI** and **Groq (Llama 3.3)** via **GitHub Actions**.

When a pull request is opened (or updated), the workflow automatically:
1. Computes the git diff of the PR
2. Sends the diff to **Claude** and/or **Groq** with a structured Java review prompt
3. Posts each AI's feedback as a separate comment on the PR

You can run with just one provider, or both at the same time — the workflow skips any job whose API key is not configured.

---

## Project Structure

```
ai-code-review-demo/
├── .github/
│   └── workflows/
│       └── ai-code-review.yml      ← GitHub Actions workflow (Claude + Groq)
├── src/
│   ├── main/java/com/demo/
│   │   └── UserService.java        ← Sample service with intentional bugs
│   └── test/java/com/demo/
│       └── UserServiceTest.java    ← JUnit 5 tests (some expose the bugs)
├── pom.xml                         ← Maven build file (Java 17)
├── .env                            ← Local secrets (gitignored)
├── .gitignore
└── README.md
```

---

## AI Providers

| Provider | Model | Cost | Speed |
|---|---|---|---|
| **Claude** (Anthropic) | `claude-sonnet-4-20250514` | Paid | Fast |
| **Groq** (Llama 3.3) | `llama-3.3-70b-versatile` | Free tier | Very fast |

Both providers use the same structured review prompt and produce output in the same format. Configure one or both — the workflow handles it automatically.

---

## What the AI Reviews

| Category | Examples |
|---|---|
| 🔴 **Critical Issues** | SQL injection, NullPointerException, hardcoded passwords |
| ⚠️ **Warnings** | `==` instead of `.equals()`, returning `null` instead of `Optional` |
| 💡 **Suggestions** | `StringBuilder` vs `String` concatenation, named constants |
| ✅ **Well Done** | Correct use of try-with-resources, good test coverage |
| 📋 **Verdict** | MERGE or REQUEST CHANGES recommendation |

---

## Setup

### 1. Fork / clone this repository

```bash
git clone https://github.com/<your-username>/ai-code-review-demo.git
cd ai-code-review-demo
```

### 2. Add API keys as GitHub Secrets

Go to: **Repository → Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Where to get it | Required |
|---|---|---|
| `ANTHROPIC_API_KEY` | [console.anthropic.com](https://console.anthropic.com) | Optional |
| `GROQ_API_KEY` | [console.groq.com](https://console.groq.com) | Optional |

Add at least one. If both are set, **both** Claude and Groq will review every PR.

> `GITHUB_TOKEN` is provided automatically by GitHub Actions — no setup needed.

### 3. Local development (optional)

Copy the `.env` file and fill in your keys for local testing:

```bash
# .env is already gitignored — safe to edit locally
GITHUB_TOKEN=your_github_pat
GROQ_API_KEY=your_groq_key
ANTHROPIC_API_KEY=your_anthropic_key
```

---

## How to Test It

1. **Create a feature branch**
   ```bash
   git checkout -b feature/my-changes
   ```

2. **Make a change to `UserService.java`** — add a method, introduce a bug, or try to fix one.

3. **Commit and push**
   ```bash
   git add src/main/java/com/demo/UserService.java
   git commit -m "Add/change something for AI review"
   git push origin feature/my-changes
   ```

4. **Open a Pull Request** (base: `main` ← compare: `feature/my-changes`)

5. **Watch the Actions tab** — you'll see two jobs: `Claude AI Review` and `Groq AI Review`

6. **Check the PR** — each AI posts its own comment within ~30 seconds

---

## How It Works

```
PR opened / updated
       │
       ├──────────────────────────┬─────────────────────────────
       │                          │
       ▼                          ▼
 [claude-review job]       [groq-review job]
  (if ANTHROPIC_API_KEY)    (if GROQ_API_KEY)
       │                          │
   git diff                   git diff
       │                          │
   POST /v1/messages          POST /openai/v1/chat/completions
   api.anthropic.com          api.groq.com
       │                          │
   .content[0].text           .choices[0].message.content
       │                          │
       └──────────┬───────────────┘
                  ▼
        POST PR comment via GitHub API
        (GITHUB_TOKEN – automatic)
```

---

## Bugs Planted in `UserService.java`

| Bug | Type |
|---|---|
| `role == "ADMIN"` | Bug — `==` vs `.equals()` |
| `user.getEmail()` no null guard | Bug — NPE risk |
| Raw SQL string concat | Security — SQL injection |
| `DB_PASS = "password123"` in code | Security — hardcoded credential |
| `report += ...` in loop | Performance — use `StringBuilder` |
| Magic numbers `18`, `120` | Clean code |
| `total / userList.size()` | Bug — integer division, precision loss |
| `return null` for empty list | Bad practice |
| Non-atomic check-then-add | Thread safety |

---

## Local Build

```bash
mvn clean test
```

The `_bugDemo_` test methods in `UserServiceTest.java` explicitly demonstrate the bugs.

---

## Requirements

| Tool | Version |
|---|---|
| Java | 17 |
| Maven | 3.8+ |
| GitHub Actions runner | `ubuntu-latest` |
| Claude API key | optional (console.anthropic.com) |
| Groq API key | optional, free tier (console.groq.com) |

---

## License

MIT
