package com.github.tcdl.jsonmask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JsonMaskerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testMaskBaseLatinWithX() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": \"Qwerty\"}"));
        assertEquals("Xxxxxx", masked.get("a").asText());
    }

    @Test
    public void testMaskNotBaseLatinWithX() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": \"Ĕőєחβ\"}"));
        assertEquals("xxxxx", masked.get("a").asText());
    }

    @Test
    public void testMaskDigitsWithAsterisk() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": \"8301\"}"));
        assertEquals("****", masked.get("a").asText());
    }

    @Test
    public void testNotMaskPunctuationAndCommonSigns() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": \"-+.,!?@%$[]()\"}"));
        assertEquals("-+.,!?@%$[]()", masked.get("a").asText());
    }

    @Test
    public void testMaskComplexString() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": \"Phone: +1-313-85-93-62, Salary: $100, Name: Κοτζιά;\"}"));
        assertEquals("Xxxxx: +*-***-**-**-**, Xxxxxx: $***, Xxxx: xxxxxx;", masked.get("a").asText());
    }

    @Test
    public void testMaskNumberWithStringOfAsterisks() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": 201}"));
        assertEquals("***", masked.get("a").asText());
    }

    @Test
    public void testMaskPropertiesDeeply() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"foo\": {\"bar\": {\"a\": 123, \"b\": \"!?%\"}}, \"c\": [\"sensitive\"]}"));
        assertEquals(mapper.readTree("{\"foo\": {\"bar\": {\"a\": \"***\", \"b\": \"!?%\"}}, \"c\": [\"xxxxxxxxx\"]}"), masked);
    }

    @Test
    public void testMaskNumberWithFractionPart() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": 20.12}"));
        assertEquals("**.**", masked.get("a").asText());
    }

    @Test
    public void testMaskNumberZero() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": 0.0}"));
        assertEquals("*.*", masked.get("a").asText());

        masked = JsonMasker.mask(mapper.readTree("{\"a\": 0}"));
        assertEquals("*", masked.get("a").asText());
    }

    @Test
    public void testEmptyJson() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{}"));
        assertEquals("{}", masked.toString());
    }

    @Test
    public void testEmptyArray() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("[]"));
        assertEquals("[]", masked.toString());
    }

    @Test
    public void testEmptyArrayAsValue() throws IOException {
        JsonNode masked = JsonMasker.mask(mapper.readTree("{\"a\": []}"));
        assertEquals("[]", masked.get("a").toString());
    }

    @Test
    public void testNull() {
        JsonNode masked = JsonMasker.mask(null);
        assertEquals(null, masked);
    }
}
