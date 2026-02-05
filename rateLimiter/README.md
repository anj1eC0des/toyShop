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
10 threads and 10 connections
Thread Stats   Avg      Stdev     Max   +/- Stdev
Latency    43.86ms    2.90ms  48.49ms   97.94%
Req/Sec    22.78      4.51    30.00     72.00%
2280 requests in 10.03s, 186.94KB read
Non-2xx or 3xx responses: 858
Requests/sec:    227.41
Transfer/sec:     18.65KB