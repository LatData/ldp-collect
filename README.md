ldp-collect (LatData Platform Collect Sources)
========

Source Code of LatData Platform - Collect Components (Flume Sources, ChannelSelectors, Interceptors & Sinks)

Warning: Only 64bit Linux is supported for now. It should be easy to add support for other platforms.

## Licence ##

ldp code and software components can be modified and distributed under the terms of [The BSD 3-Clause License](http://opensource.org/licenses/BSD-3-Clause).


### Requirements ###
* OpenJ DK 7 or 8 ,also tested on Oracle JDK 7 & 8. (```apt install openjdk-7-jdk```)
* Environment variable JAVA_HOME points to JDK (```echo $JAVA_HOME```)
* Maven 3.x , http://maven.apache.org/ . (```apt install maven ```)
* gcc 4.5.x or higher, binutils & make (```apt install build-essential```)
* Boost C++ Library (http://www.boost.org/) (```apt install libboost-deb```)
* re2:libre2-java:jar in maven local repos (https://github.com/LatData/re2-java)
* wget (```apt install wget```)
* git (```apt install git```)

### INSTALL ###
```shell
~$ git clone https://github.com/LatData/ldp-collect.git
Cloning into 'ldp-collect'...
Checking connectivity... done.
~$ cd ldp-collect/
~/ldp-collect$
 ```
