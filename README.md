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

## Usage
```
Usage: <main class> [options] filename
  Options:
    -c, --ccompiler
       Name of c compiler command to use. If nothing is specified a default
       value will be chosen depending on the value of gpuenabled
    -f, --format
       Format the generated c code using indent
       Default: false
    -g, --go
       Keep-on-trucking on error
       Default: false
    -gpu, --gpuenabled
       Enabled some functions to be performed on a gpu
       Default: false
    -h, --help
       Display this information
       Default: false
    -i, --indent
       Indentation width
       Default: 4
    -p, --pretty
       Pretty Print mode
       Default: false
    -v, --version
       Display the version number
       Default: false
    -o
       Write output to file
```
