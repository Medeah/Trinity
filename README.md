[![Build Status](https://travis-ci.org/Medeah/Trinity.svg?branch=master)](https://travis-ci.org/Medeah/Trinity)

# The Trinity Programming Language
This is a compiler for Trinity, a statically typed programming language for scientific computing on the GPU. It was made as a student project at Aalborg University.
The only dependency is jdk, it was primarily tested using openJDK 1.7, but newer versions should also work.
This project uses the gradle for building and testing. Using the gradle wrapper is the preferred way of running gradle tasks:
```
./gradlew       # for UN*X
gradlew.bat     # for Windows
```
This will automatically downloaded and use the correct gradle version.
To build the project run the installDist task:
```
./gradlew installDist
```
This will install Trinity in `build/install/tric/`
Trinity can now be evoked with `./bin/tric [options] filename`.
Feel free to move the tric folder and/or add the bin folder to your PATH.

To run the unit tests run the check gradle task: `./gradlew check`
