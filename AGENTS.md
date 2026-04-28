# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# About this project

This is a Java Spring Boot application that serializes and replaces text in files containing PHP serialized objects. It's commonly used when migrating WordPress sites (e.g., changing database URLs in SQL dumps).

# Quick start

```bash
# Build and run tests
./gradlew build

# Run a single test
./gradlew test --tests ProcessorTest.testReplace

# Run with Docker (if image created)
docker run -ti --rm --volume $PWD:/local replace-php-serialize-safe:master-SNAPSHOT initial.sql replaced.sql 'http://example.com' 'http://www.example.com'
```

# Architecture

**Core Components:**
- `ReplaceApp.java` - Main entry point, reads file line-by-line, processes and writes output
- `Processor.java` - Core algorithm that replaces URLs within PHP serialized strings while handling length adjustments
- `SerializationContext.java` - Simple inner class tracking serialization boundaries (`s:XX:"..."` pattern)

**Algorithm Design:**
The processor handles serialized strings (`s:XX:"content";`) by:
1. Using `RateLimiter` in `ReplaceApp` to throttle I/O
2. Using `FileTools.readFileLinesIteration()` for memory-efficient streaming
3. Caching the last serialization context to avoid redundant backward searches
4. Limiting backward search to ~1MB to handle large lines efficiently
5. Adjusting serialized length (`s:XX`) when replacing shorter/longer strings

**Dependencies:**
- Spring Boot 4.0.1
- Java 25
- Jackson, Guava, Commons-lang3
- JUnit tests with Spring Boot test support

# Testing

Run tests with:
```bash
./gradlew test
```

Run a specific test:
```bash
./gradlew test --tests ProcessorTest.testReplaceLongLine
```

# Release scripts

- `create-local-release.sh` - Creates local Docker image
- `create-public-release.sh` - Creates public Docker image

# Important implementation notes

- The processor caches `SerializationContext` to avoid redundant backward searches
- Backward search is limited to ~1MB (1048576 chars) to handle very long lines
- Always check `SerializationContext.endQuotePos` when skipping already-processed regions
- When counting replacements in a serialized string, ensure `pos + searchLen <= endPos`
