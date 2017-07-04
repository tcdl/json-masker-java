package com.github.tcdl.jsonmask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class JsonMasker {

    private static final Pattern digits = Pattern.compile("\\d");
    private static final Pattern capitalLetters = Pattern.compile("[A-Z]");
    private static final Pattern nonSpecialCharacters = Pattern.compile("[^X\\s!-/:-@\\[-`{-~]");

    public static JsonNode mask(JsonNode target) {
        if (target == null)
            return null;
        return traverseAndMask(target.deepCopy());
    }

    @SuppressWarnings("ConstantConditions")
    private static JsonNode traverseAndMask(JsonNode target) {
        if (target.isTextual()) {
            return new TextNode(maskString(target.asText()));
        }
        if (target.isNumber()) {
            return new TextNode(maskNumber(target.asText()));
        }

        if (target.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = target.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                ((ObjectNode) target).replace(field.getKey(), traverseAndMask(field.getValue()));
            }
        }
        if (target.isArray()) {
            for (int i = 0; i < target.size(); i++) {
                ((ArrayNode) target).set(i, traverseAndMask(target.get(i)));
            }
        }
        return target;
    }

    private static String maskString(String value) {
        String tmpMasked = digits.matcher(value).replaceAll("*");
        tmpMasked = capitalLetters.matcher(tmpMasked).replaceAll("X");
        return nonSpecialCharacters.matcher(tmpMasked).replaceAll("x");
    }

    private static String maskNumber(String value) {
        return value.replaceAll("[0-9]", "*");
    }
}
