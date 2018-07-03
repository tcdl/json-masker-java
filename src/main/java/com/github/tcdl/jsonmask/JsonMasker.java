package com.github.tcdl.jsonmask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

import java.util.*;
import java.util.regex.Pattern;

public class JsonMasker {

    private static final Pattern digits = Pattern.compile("\\d");
    private static final Pattern capitalLetters = Pattern.compile("[A-Z]");
    private static final Pattern nonSpecialCharacters = Pattern.compile("[^X\\s!-/:-@\\[-`{-~]");

    private static final Configuration jsonPathConfig = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS).build();

    private final Set<String> whitelistedKeys;
    private final Set<JsonPath> whitelistedJsonPaths;
    private final boolean enabled;

    public JsonMasker(Collection<String> whitelist, boolean enabled) {
        this.enabled = enabled;

        whitelistedKeys = new HashSet<>();
        whitelistedJsonPaths = new HashSet<>();

        whitelist.forEach(item -> {
            if (item.startsWith("$")) {
                whitelistedJsonPaths.add(JsonPath.compile(item));
            } else {
                whitelistedKeys.add(item.toUpperCase());
            }
        });
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

        Set<String> expandedWhitelistedPaths = new HashSet<>();
        for (JsonPath jsonPath : whitelistedJsonPaths) {
            if (jsonPath.isDefinite()) {
                expandedWhitelistedPaths.add(jsonPath.getPath());
            } else {
                for (JsonNode node : jsonPath.<ArrayNode>read(target, jsonPathConfig)) {
                    expandedWhitelistedPaths.add(node.asText());
                }
            }
        }

        return traverseAndMask(target.deepCopy(), expandedWhitelistedPaths, "$");
    }

    @SuppressWarnings("ConstantConditions")
    private JsonNode traverseAndMask(JsonNode target, Set<String> expandedWhitelistedPaths, String path) {
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
                if (!whitelistedKeys.contains(field.getKey().toUpperCase())) {
                    String childPath = appendPath(path, field.getKey());
                    if (!expandedWhitelistedPaths.contains(childPath)) {
                        ((ObjectNode) target).replace(field.getKey(), traverseAndMask(field.getValue(), expandedWhitelistedPaths, childPath));
                    }
                }
            }
        }
        if (target.isArray()) {
            for (int i = 0; i < target.size(); i++) {
                String childPath = appendPath(path, i);
                if (!expandedWhitelistedPaths.contains(childPath)) {
                    ((ArrayNode) target).set(i, traverseAndMask(target.get(i), expandedWhitelistedPaths, childPath));
                }
            }
        }
        return target;
    }

    private static String appendPath(String path, String key) {
        return path + "['" + key + "']";
    }

    private static String appendPath(String path, int ind) {
        return path + "[" + ind + "]";
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
