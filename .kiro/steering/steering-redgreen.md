---
inclusion: always
---
# TDD Workflow
## Philosophy
This project follows strict Test-Driven Development (TDD). All code must be written in response to a failing test. No production code exists without a corresponding test that drove its creation.

## The Red-Green-Refactor Cycle
- **Red**: Write a failing test that describes the desired behavior in plain English
- **Green**: Write the minimum production code to make the test pass — nothing more
- **Refactor**: Clean up the code while keeping all tests green

## Rules
- Never write production code before a failing test exists
- Tests must fail for the right reason before implementing
- Implement only what is needed to pass the current failing test
- After each green phase, consider if refactoring is needed
- All behavior must be described in plain English before generating a test

## What "Minimum" Means
**CRITICAL**: "Minimum" means the simplest possible code that makes ONLY the current test pass.

### Examples
Some simple guidelines using the example of a piece of code that parses strings such as "one" or "twenty seven" into numbers.

#### what NOT to do:
- ❌ Test checks "one" returns 1 → Don't implement a dictionary with "one" through "ten"
- ❌ Test checks addition of two numbers → Don't implement multiplication, division, etc.
- ❌ Test checks parsing a single word → Don't implement comma-separated parsing

#### correct minimal implementations:
- ✅ Test checks "one" returns 1 → Use `if numbers == "one": return 1`
- ✅ Test checks "two" returns 2 → Add `elif numbers == "two": return 2`
- ✅ After 3+ similar cases → Refactor to use a dictionary (driven by duplication, not anticipation)

**The Golden Rule**: If you can delete code and the test still passes, you wrote too much code.

**Resist the urge to be "clever" or "complete"**. Let the tests drive every single line of production code. Premature generalization violates TDD principles.

## Cycle Prompt Pattern
When asked to implement a feature, always:

1. First generate a failing test for ONE specific behavior
2. Confirm the test fails
3. Then generate the minimal implementation (see "What Minimum Means" above)
4. Confirm all tests pass
5. Suggest refactoring opportunities (only if duplication exists)
6. Review request and existing tests to find if additional tests are needed
   - If at least one more test is needed, start another cycle by writing a failing test
   - If not, declare that requirement was met

Break each feature into individual tasks for each step in the TDD lifecycle

## Self-Check Before Implementing
Before writing production code, ask yourself:
1. What is the EXACT assertion in the failing test?
2. What is the SIMPLEST code that makes that assertion pass?
3. Am I implementing anything the test doesn't verify?
4. If I remove this line, does the test still pass? (If yes, delete it)