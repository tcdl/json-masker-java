# json-masker-java
# Overview

A library for masking field values in JSON. Useful when there is a need to log JSON which potentially contains sensitive data such as PII.
This project is a port of nodejs [json-masker](https://github.com/tcdl/json-masker) library.

## Installation
```
```

## Usage
```java
import com.github.tcdl.jsonmask.JsonMask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//...
private ObjectMapper mapper = new ObjectMapper();
JsonNode jsonNode = objectMapper.readTree("{\"a\":1}");
JsonNode masked = JsonMask.mask(jsonNode);
```

## Masking strategy
Example of input:
```json
{
  "firstName": "Noëlla",
  "lastName": "Maïté",
  "age": 26,
  "gender": "Female",
  "contacts": {
    "email": "cbentson7@nbcnews.com",
    "phone": "62-(819)562-8538",
    "address": "12 Northview Way"
  },
  "employments": [
    {
      "companyName": "Reynolds-Denesik",
      "startDate": "12/7/2016",
      "salary": "$150"
    }
  ],
  "ipAddress": "107.196.186.197"
}
```
Output:
```json
{
  "firstName": "Xxxxxx",
  "lastName": "Xxxxx",
  "age": "**",
  "gender": "Xxxxxx",
  "contacts": {
    "email": "xxxxxxxx*@xxxxxxx.xxx",
    "phone": "**-(***)***-****",
    "address": "** Xxxxxxxxx Xxx"
  },
  "employments": [
    {
      "companyName": "Xxxxxxxx-Xxxxxxx",
      "startDate": "**/*/****",
      "salary": "$***"
    }
  ],
  "ipAddress": "***.***.***.***"
}
```
### Rules
1. strings
    * whitespaces remain unchanged 
    * punctuation marks (non-alphanumeric characters of [latin-1](http://jrgraphix.net/r/Unicode/0020-007F)) remain unchanged
    * latin-1 characters 1-9 become `*`
    * latin-1 characters A-Z become `X`
    * all other UTF-8 characters become `x`
2. numbers are converted to strings where each 1-9 character is replaced with `*` (e.g. `125` becomes `"***"` or `3.95` becomes `"*.**"`) 
3. booleans: remain unchanged
4. nulls: remain unchanged

## Tests
To run unit tests you can just execute:
```
mvn test
```
from project's root directory.

## Benchmark
[JMH](http://openjdk.java.net/projects/code-tools/jmh/) framework is used for benchmarking. To run performance tests:
```
cd benchmark/jsonmask-java-benchmark
mvn package
java -jar target/benchmarks.jar
```
