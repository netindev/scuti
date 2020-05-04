# scuti [![Build Status](https://travis-ci.org/netindev/scuti.svg?branch=master)](https://travis-ci.org/netindev/scuti)

## Download
* Binary releases: https://github.com/netindev/scuti/releases
* Git tree: https://github.com/netindev/scuti.git

## Build
* Install [Maven](https://maven.apache.org/download.html)
* Go to: `..\scuti` and execute `mvn clean install`

## How to use:

```java -jar scuti.jar configuration.json```

## Options:

### Obfuscation:

| Module | Description |
| --- | --- |
| Class Encrypt | Encrypt all classes, creates a custom class loader and load all them in memory |
| Control Flow | Creates randoms conditionals and put them inside the method |
| Hide Code | Hide all classes and members |
| Invoke Dynamic | Replace invokestatic and invokevirtual with dynamics |
| Miscellaneous Obfuscation | Miscellaneous obfuscation, varargs, local variable renaming, etc |
| Number Obfuscation | Split numbers into operations |
| Rename Members | Rename classes, methods and fields |
| Shuffle Members | Shuffles all class members |
| String Encrption | Encrypt strings (that's obvious lol) |

### Optimization:

| Module | Description |
| --- | --- |
| Dead Code | Remove unused code |
| No Operation | Clean all no operations |

### Shrinking:

| Module | Description |
| --- | --- |
| Inner Class | Remove inner classes |
| Unused Member | Remove all unused classes, methods and fields |

## Contribute

Any contribution is welcome, just create a pr.

## Contacts
* [email](mailto:contact@netindev.tk)
* [twitter](https://twitter.com/netindev)