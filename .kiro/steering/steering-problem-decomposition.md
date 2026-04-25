---
inclusion: always
---
# Problem Decomposition
After analysing a problem, there will typically be multiple steps involved. Treat each step as a separate task, and work on them one at a time. Structure the code following hexagonal architecture principles to allow each step to be tested independently.

For each step of the solution:
- isolate the logic in a class or public methid that can be tested independently
- consider the edge cases thoroughly for that step, so that the test coverage is good not just in terms of lines of code exercised, but variations in the data that we may expect

When considering which steps to code first, try to start with those where the number of edge cases are largest, because our first guess at the step's interfaces with the rest of the code might need to be modified once we consider the edges.
