package com.github.tcdl.jsonmask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class JsonMasker {

    private static final Pattern digits = Pattern.compile("\\d");
    private static final Pattern capitalLetters = Pattern.compile("[A-Z]");
    private static final Pattern nonSpecialCharacters = Pattern.compile("[^X\\s!-/:-@\\[-`{-~]");

    private final Set<String> whitelist;
    private final boolean enabled;

    public JsonMasker(Collection<String> whitelist, boolean enabled) {
        this.whitelist = whitelist.stream().map(String::toUpperCase).collect(toSet());
        this.enabled = enabled;
    }

    public JsonMasker() {
        this(Collections.emptySet(), true);
    }

    public JsonMasker(boolean enabled) {
        this(Collections.emptySet(), enabled);
    }

    public JsonMasker(Collection<String> whitelist) {
        this(whitelist, true);
    }

    public JsonNode mask(JsonNode target) {
        if (!enabled)
            return target;
        if (target == null)
            return null;

        return traverseAndMask(target.deepCopy());
    }

    @SuppressWarnings("ConstantConditions")
    private JsonNode traverseAndMask(JsonNode target) {
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
                if (!whitelist.contains(field.getKey().toUpperCase()))
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

    private String maskString(String value) {
        String tmpMasked = digits.matcher(value).replaceAll("*");
        tmpMasked = capitalLetters.matcher(tmpMasked).replaceAll("X");
        return nonSpecialCharacters.matcher(tmpMasked).replaceAll("x");
    }

    private String maskNumber(String value) {
        return value.replaceAll("[0-9]", "*");
    }
}
