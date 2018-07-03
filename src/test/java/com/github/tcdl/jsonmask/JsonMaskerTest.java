package com.github.tcdl.jsonmask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JsonMaskerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testMaskBaseLatinWithX() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": \"Qwerty\"}"));
        assertEquals("Xxxxxx", masked.get("a").asText());
    }

    @Test
    public void testMaskNotBaseLatinWithX() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": \"Ĕőєחβ\"}"));
        assertEquals("xxxxx", masked.get("a").asText());
    }

    @Test
    public void testMaskDigitsWithAsterisk() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": \"8301\"}"));
        assertEquals("****", masked.get("a").asText());
    }

    @Test
    public void testNotMaskPunctuationAndCommonSigns() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": \"-+.,!?@%$[]()\"}"));
        assertEquals("-+.,!?@%$[]()", masked.get("a").asText());
    }

    @Test
    public void testMaskComplexString() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": \"Phone: +1-313-85-93-62, Salary: $100, Name: Κοτζιά;\"}"));
        assertEquals("Xxxxx: +*-***-**-**-**, Xxxxxx: $***, Xxxx: xxxxxx;", masked.get("a").asText());
    }

    @Test
    public void testMaskNumberWithStringOfAsterisks() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": 201}"));
        assertEquals("***", masked.get("a").asText());
    }

    @Test
    public void testMaskPropertiesDeeply() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"foo\": {\"bar\": {\"a\": 123, \"b\": \"!?%\"}}, \"c\": [\"sensitive\"]}"));
        assertEquals(mapper.readTree("{\"foo\": {\"bar\": {\"a\": \"***\", \"b\": \"!?%\"}}, \"c\": [\"xxxxxxxxx\"]}"), masked);
    }

    @Test
    public void testMaskNumberWithFractionPart() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": 20.12}"));
        assertEquals("**.**", masked.get("a").asText());
    }

    @Test
    public void testMaskNumberZero() throws IOException {
        JsonMasker masker = new JsonMasker();
        JsonNode masked = masker.mask(mapper.readTree("{\"a\": 0.0}"));
        assertEquals("*.*", masked.get("a").asText());

        masked = masker.mask(mapper.readTree("{\"a\": 0}"));
        assertEquals("*", masked.get("a").asText());
    }

    @Test
    public void testEmptyJson() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{}"));
        assertEquals("{}", masked.toString());
    }

    @Test
    public void testEmptyArray() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("[]"));
        assertEquals("[]", masked.toString());
    }

    @Test
    public void testEmptyArrayAsValue() throws IOException {
        JsonNode masked = new JsonMasker().mask(mapper.readTree("{\"a\": []}"));
        assertEquals("[]", masked.get("a").toString());
    }

    @Test
    public void testNull() {
        JsonNode masked = new JsonMasker().mask(null);
        assertEquals(null, masked);
    }

    @Test
    public void testWhitelistingByFieldName() throws IOException {
        JsonNode inJson = mapper.readTree(JsonMaskerTest.class.getResourceAsStream("/example_whitelisting.json"));
        JsonMasker masker = new JsonMasker(Arrays.asList("myField", "FIELD2", "nonExistingField", "array1"));

        JsonNode masked = masker.mask(inJson);

        assertEquals("Hi", masked.get("myField").asText());
        assertEquals("**********", masked.get("a").asText());
        assertEquals("Xxxxxx", masked.get("nestedObj").get("b").asText());
        assertEquals("123", masked.get("nestedObj").get("field2").asText());
        assertEquals("1", masked.get("array1").get(0).get("a").asText());
        assertEquals("2", masked.get("array1").get(1).get("b").asText());
        assertEquals("3", masked.get("array1").get(2).get("c").asText());
    }

    @Test
    public void testWhitelistingByJsonPath() throws IOException {
        JsonNode inJson = mapper.readTree(JsonMaskerTest.class.getResourceAsStream("/example_whitelisting.json"));
        JsonMasker masker = new JsonMasker(Arrays.asList("$.a", "$..b", "$.array1[2]"));

        JsonNode masked = masker.mask(inJson);

        assertEquals("Xx", masked.get("myField").asText());
        assertEquals("8301975624", masked.get("a").asText());
        assertEquals("Qwerty", masked.get("nestedObj").get("b").asText());
        assertEquals("***", masked.get("nestedObj").get("field2").asText());
        assertEquals("*", masked.get("array1").get(0).get("a").asText());
        assertEquals("2", masked.get("array1").get(1).get("b").asText());
        assertEquals("3", masked.get("array1").get(2).get("c").asText());
    }

    @Test
    public void testWhitelistingByFieldNameAndJsonPath() throws IOException {
        JsonNode inJson = mapper.readTree(JsonMaskerTest.class.getResourceAsStream("/example_whitelisting.json"));
        JsonMasker masker = new JsonMasker(Arrays.asList("field2", "$.array1[0].a"));

        JsonNode masked = masker.mask(inJson);

        assertEquals("Xx", masked.get("myField").asText());
        assertEquals("**********", masked.get("a").asText());
        assertEquals("Xxxxxx", masked.get("nestedObj").get("b").asText());
        assertEquals("123", masked.get("nestedObj").get("field2").asText());
        assertEquals("1", masked.get("array1").get(0).get("a").asText());
        assertEquals("*", masked.get("array1").get(1).get("b").asText());
        assertEquals("*", masked.get("array1").get(2).get("c").asText());
    }

    @Test
    public void testWhitelistingByNotExistingJsonPath() throws IOException {
        JsonNode inJson = mapper.readTree("{\"a\": \"abc\"}");
        JsonMasker masker = new JsonMasker(Arrays.asList("$..not.existing"));

        JsonNode masked = masker.mask(inJson);

        assertEquals("xxx", masked.get("a").asText());
    }

    @Test
    public void testWhitelistingOfIntersectingJsonPaths() throws Exception {
        String inJson = "{\"a\": \"abc\", \"b\": \"xyz\"}";
        JsonMasker masker = new JsonMasker(Arrays.asList("$.*", "$.a"));

        JsonNode masked = masker.mask(mapper.readTree(inJson));

        assertEquals("abc", masked.get("a").asText());
        assertEquals("xyz", masked.get("b").asText());
    }

    @Test(expected = InvalidPathException.class)
    public void testWhitelistingThrowsIfInvalidJsonPathPassed() {
        new JsonMasker(Arrays.asList("field", "$,invalid],json,[path"));
    }

    @Test
    public void testEnabled() throws Exception {
        String inJson = "{\"a\": \"abc\", \"nested\": {\"b\": \"xyz\"}}";
        JsonMasker masker = new JsonMasker(false);

        JsonNode masked = masker.mask(mapper.readTree(inJson));

        assertEquals("abc", masked.get("a").asText());
        assertEquals("xyz", masked.get("nested").get("b").asText());
    }

    @Test
    public void testWhitelistingAndEnabled() throws Exception {
        String inJson = "{\"a\": \"abc\", \"b\": \"xyz\"}";
        JsonMasker masker = new JsonMasker(Arrays.asList("b", "myField"), false);

        JsonNode masked = masker.mask(mapper.readTree(inJson));

        assertEquals("abc", masked.get("a").asText());
        assertEquals("xyz", masked.get("b").asText());
    }

    @Test
    public void testDoesNotModifyInputJson() throws IOException {
        JsonNode inJson = mapper.readTree("{\"a\": \"abc\", \"b\": \"xyz\"}");
        JsonMasker masker = new JsonMasker();

        JsonNode masked = masker.mask(inJson);

        assertEquals("xxx", masked.get("a").asText());
        assertEquals("xxx", masked.get("b").asText());
        assertEquals("abc", inJson.get("a").asText());
        assertEquals("xyz", inJson.get("b").asText());
    }
}
