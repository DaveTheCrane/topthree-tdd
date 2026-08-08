---
inclusion: always
---
# Git Workflow

## Experiment Setup
Each run of the task is an isolated experiment. The **first step in conducting an experiment is to create a new feature branch** from the current base branch.

- All work for the experiment happens on this feature branch.
- The feature branch is never merged back into the base branch.
- After the feature branch is created, do not switch or merge branches for the rest of the experiment.
- In a spec task list (`tasks.md`), the very first task is to create and check out the feature branch.

## Commit Strategy
Every completed Red-Green-Refactor cycle ends with a single git commit to the **current (feature) branch**.

- Commit once per cycle, after the cycle is green and any refactoring is done — not after each individual phase.
- Never commit failing code. Only commit when all tests pass.
- Commit to whatever branch is currently checked out; never create, switch, or merge branches after the initial feature branch.
- Use a concise commit message that names the behavior the cycle added, e.g. `feat: parse "one" to 1` or `test+impl: reject empty input`.

## Task List
Structure the spec task list so the branching and commit strategy are explicit:

- The first task creates and checks out the feature branch.
- Each behavior is a single Red-Green-Refactor subtask (see the TDD Workflow steering); committing the completed cycle to the current branch is the final action of that subtask.
- Because every cycle ends in a commit, the overall task list always ends on a committed cycle.
