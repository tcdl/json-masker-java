# Overview
This subproject contains performance measurement for [json-mask-java]() library.

# Results

### System
Memory    16G
Processor Intel® Core™ i5-4590 CPU @ 3.30GHz
OS        ubuntu 16.04 64-bit

### Samples
We have the same 3 json files as nodejs version uses for performance tests to compare the implementations.

|Sample file |Size (kB)|Throughput (ops/ms)|AvgTime (ms/ops)|
|------------|:-------:|:-----------------:|---------------:|
|sample0.json|32.5     |0.730              |1.360           |
|sample1.json|16.3     |1.424              |0.740           |
|sample2.json|7.6      |3.209              |0.332           |
|sample3.json|3.9      |13.118             |0.080           |

# Ho To Run
This is a maven project, so first you have to build it with maven.
```
$ mvn clean package
```
_Note_ : Benchmark depends on json-mask-java. In order to build it you need to have a jar with already built artifact in your local maven repository

After successful build performance tests could be run by
```
$ java -jar target/benchmark.jar
```

The whole set of commandline arguments of [JMH](http://openjdk.java.net/projects/code-tools/jmh/) is applicable for our benchmarks. 