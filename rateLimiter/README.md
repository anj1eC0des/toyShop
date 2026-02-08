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

**Decision**: Used netty instead of standard-library webserver.
**Rationale**: High performance and asynchronous.

---

### Last Benchmark

Running 10s test @ http://localhost:8080/
20 threads and 20 connections
Thread Stats   Avg      Stdev     Max   +/- Stdev
Latency     1.33ms    1.11ms  31.24ms   91.58%
Req/Sec   797.78    265.46     4.39k    71.34%
Latency Distribution
50%    1.18ms
75%    1.65ms
90%    2.22ms
99%    3.58ms
159121 requests in 10.10s, 20.26MB read
Non-2xx or 3xx responses: 157119
Requests/sec:  15757.13
Transfer/sec:      2.01MB

---

### Learning highlights

- Different rate limiting algorithms.
- Performance of locks vs cas in high contention scenarios.
- Using blocking vs non-blocking options.