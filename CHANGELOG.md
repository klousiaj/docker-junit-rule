# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## 1.3.5 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.3.4)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.3.5)
> 2016.12.05

- Implement enhancement #15. Provide the ability for the user to name the container that is being created.
- Provide an example usage that includes a test suite and potential usage during an integration test.
- Upgraded to version 6.1.1 of the docker-client.
- Add ability to add labels to the container that is being created.
- Implement enhancement #16. Provide ability for the user to specify labels at container creation.
- Fix #17. Provide ability for the user to remove volumes automatically at container removal.


## 1.3.4 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.3.4)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.3.4)
> 2016.11.21

- Fix Issue #13. Provide version that doesn't rely on the shaded version of the docker-client.
- Upgraded to version 6.0.0 of the docker-client.
- Removed duplicate dependencies from the build.gradle.

## 1.3.3 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.3.3)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.3.3)
> 2016.11.20

- Fix Issue #11. Added way to use the library without using it as a JUnitRule. 

## 1.3.2 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.3.2)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.3.2)
> 2016.11.10

- Mark WaitForPort as Deprecated. To be removed in v2.0.0.
- Fix Issue #8. Docker for Mac supported without DOCKER_HOST being set.

## 1.3.1 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.3.1)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.3.1)
> 2016-08-04

- Added signing instructions when publishing to Bintray.
- Improved test coverage.

## 1.3.0 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.3.0)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.3.0)
> 2016-08-02

- Add useRunning parameter.
- Add leaveRunning parameter.
- Improve code coverage with mockito.
- No longer validate support for Docker version 1.10.3

## 1.2.2 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.2.2)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.2.2)
> 2016-07-28

- Only pull if the image isn't already available on the Docker Host.

## 1.2.1 [![codecov.io](https://codecov.io/github/klousiaj/docker-junit-rule/coverage.svg?branch=1.2.1)](https://codecov.io/github/klousiaj/docker-junit-rule?branch=1.2.1)
> 2016-07-23

- Add Jacoco and code coverage analysis. (https://www.codecov.io)

## 1.2.0
> 2016-07-21

- Add the ability to specify environment variables to the container.
- Upgrade the docker-client to use version 5.0.2
- Updated travis-ci to test against more versions of Docker.
- Allow for specific ports to be mapped on the HOST during Rule creation.

## 1.1.0
> 2016-07-16

- Add a builder to remove the need for inheritance
- Add a waitForLog directive

## 1.0.2
> 2016-01-06

- Include guava in the dependencies

## 1.0.1
> 2016-01-06

- Use the shaded jar of the docker-client

## 1.0.0
> 2016-01-06

- Initial release
