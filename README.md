# vbforge-sandbox

<!-- CI / Pages -->
[![Build](https://github.com/vbforge/vbforge-sandbox/actions/workflows/build.yml/badge.svg)](https://github.com/vbforge/vbforge-sandbox/actions/workflows/build.yml)
[![Docs](https://github.com/vbforge/vbforge-sandbox/actions/workflows/update-docs.yml/badge.svg)](https://github.com/vbforge/vbforge-sandbox/actions/workflows/update-docs.yml)
[![Pages](https://img.shields.io/badge/GitHub%20Pages-live-brightgreen)](https://vbforge.github.io/vbforge-sandbox/)

<!-- Language & platform -->
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS-S3-FF9900?logo=amazons3&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-5.x-C5D9C8)

---

## Total Projects: 6


### 📁 `concurrency`

| Project | Description |
|---------|-------------|
| [`concurrency-collections`](./concurrency/concurrency-collections) | ConcurrentHashMap, CopyOnWriteArrayList, Queue based Concurrent |
| [`threads-simple`](./concurrency/threads-simple) | Experiments with threads, and other related topics around threads. |

### 📁 `java-core`

| Project | Description |
|---------|-------------|
| [`generics-lab`](./java-core/generics-lab) | Experiments with Java Generics, type bounds, wildcards and practical examples. |
| [`hello-world`](./java-core/hello-world) | Starting project!)) |

### 📁 `jwt`

| Project | Description |
|---------|-------------|
| [`jwt-spring-security`](./jwt/jwt-spring-security) | How a JWT token travels through the application |
| [`validation-and-sign`](./jwt/validation-and-sign) | Experiments with JWT (pure JWT mechanics), sign and valid by JJWT 0.12.x. |

---

## 🔄 Automation

| Workflow | Trigger | Action |
|----------|---------|--------|
| `build.yml` | push / PR | Compiles and tests all Maven projects |
| `update-docs.yml` | push | Regenerates README + GitHub Pages site |

---

## 🌐 GitHub Pages

Visit the live site: **[https://vbforge.github.io/vbforge-sandbox/](https://vbforge.github.io/vbforge-sandbox/)**

---

*Last updated: 2026-03-17 17:34 UTC — [source](.github/workflows/update-docs.yml)*
