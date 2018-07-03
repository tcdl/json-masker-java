# Overview
This subproject contains performance measurement for [json-mask-java]() library.

# Results

### System
Memory    16G
Processor Intel Core i7-6820HQ @ 2.70GHz
OS        macOS 10.13

### Samples
We have the same 3 json files as nodejs version uses for performance tests to compare the implementations.

|Sample file |Size (kB)|Throughput (ops/ms)|AvgTime (ms/ops)|
|------------|:-------:|:-----------------:|---------------:|
|sample0.json|32.5     |0.375              |2.679           |
|sample1.json|16.3     |0.781              |1.279           |
|sample2.json|7.6      |2.017              |0.493           |
|sample3.json|3.9      |3.236              |0.329           |

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