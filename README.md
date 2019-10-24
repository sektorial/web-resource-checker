# gRPC Web Resource Checker
## Description
This project contains a _gRPC server_, a _gRPC client_ and integration tests covering basic test-cases.
## Goal
The goal of the project is to show basics of gRPC. The _gRPC client_ is used to get information about<br>
Web Resources and the _gRPC server_ implements the logic.
## Integration tests
Integration tests are written with JUnit 5 in a manner compatible with JUnit 4. It was done intentionally<br>
to be able to utilize the <code>GrpcCleanupRule</code>. For more information please refer to the issue [#5331](https://github.com/grpc/grpc-java/issues/5331).
## Build
The project is composed of modules built in a particular order. The main goal of the build is to run<br>
integration tests.<br>
To build the project execute <code>mvn clean install</code> from the project root directory.