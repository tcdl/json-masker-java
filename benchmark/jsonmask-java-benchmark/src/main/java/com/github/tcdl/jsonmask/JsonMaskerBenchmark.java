package com.github.tcdl.jsonmask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 1, batchSize = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
public class JsonMaskerBenchmark {

    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode sample0;
    private JsonNode sample1;
    private JsonNode sample2;
    private JsonNode sample3;

    static {
        System.out.println("some test description here");
    }

    @Setup
    public void init() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        sample0 = mapper.readTree(classLoader.getResourceAsStream("sample0.json"));
        sample1 = mapper.readTree(classLoader.getResourceAsStream("sample1.json"));
        sample2 = mapper.readTree(classLoader.getResourceAsStream("sample2.json"));
        sample3 = mapper.readTree(classLoader.getResourceAsStream("sample3.json"));
    }

    @Benchmark
    @Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.MILLISECONDS)
    public JsonNode testSample0() {
        return JsonMasker.mask(sample0);
    }

    @Benchmark
    public JsonNode testSample1() {
        return JsonMasker.mask(sample1);
    }

    @Benchmark
    public JsonNode testSample2() {
        return JsonMasker.mask(sample2);
    }

    @Benchmark
    public JsonNode testSample3() {
        return JsonMasker.mask(sample3);
    }

}
