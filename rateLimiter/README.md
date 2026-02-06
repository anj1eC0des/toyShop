# [Rate Limiter]

## Overview
**What**: A component/tool that limits the hits on an endpoint.  
**Why**: To understand how its implemented, algorithms used, etc.  
**Status**: Completed

---
## Goals
- Understand what problems the component addresses.
- How it works and how its implemented.
- Understand trade-offs and different design decisions.

---

## Technical Decisions

### Tech Stack
- **Language**: Java - Go or Rust would be a better choice for performance 
,but I prioritised ease and speed of implementation and learning.
- **Framework/Libraries**: Standard Java library

### Key Design Choices
**Decision**: Rate limit based on Ip.
**Rationale**: Convenience.
**Alternatives considered**: API key, source-ip.

**Decision**: Used TokenBucket algorithm. 
**Rationale**: Widely used in all popular enterprise rate limiters. 
**Alternatives considered**: Leaky Bucket, Sliding window.

---

### Last Benchmark

Running 10s test @ http://localhost:8080/
20 threads and 20 connections
Thread Stats   Avg      Stdev     Max   +/- Stdev
Latency    43.87ms    2.92ms  48.47ms   97.83%
Req/Sec    22.76      4.49    30.00     72.15%
4557 requests in 10.03s, 380.83KB read
Non-2xx or 3xx responses: 2767
Requests/sec:    454.53
Transfer/sec:     37.99KB

---

### Learning highlights

- Different rate limiting algorithms.
- Performance of locks vs cas in high contention scenarios.