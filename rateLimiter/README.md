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
Latency     1.53ms    1.30ms  34.34ms   92.02%
Req/Sec   696.95    229.52     3.92k    66.18%
Latency Distribution
50%    1.34ms
75%    1.88ms
90%    2.57ms
99%    4.55ms
138971 requests in 10.10s, 17.68MB read
Non-2xx or 3xx responses: 136970
Requests/sec:  13760.31
Transfer/sec:      1.75MB

---

### Learning highlights

- Different rate limiting algorithms.
- Performance of locks vs cas in high contention scenarios.
- Using blocking vs non-blocking options.