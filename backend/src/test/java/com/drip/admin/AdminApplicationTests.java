package com.drip.admin;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminApplicationTests {
    @Test
    void passwordHashIsDeterministicAndNotPlainText() {
        String hash = AdminApplication.hashPassword("admin123", "drip");
        assertEquals("0e47ea190b930cfc52b06737a30930f9a3626c3f426edc679975e8112e996b4c", hash);
        assertNotEquals("admin123", hash);
    }

    @Test
    void treeBuilderReturnsNestedChildren() {
        LinkedHashMap<String, Object> root = new LinkedHashMap<>();
        root.put("id", 1L);
        root.put("parent_id", 0L);
        LinkedHashMap<String, Object> child = new LinkedHashMap<>();
        child.put("id", 2L);
        child.put("parent_id", 1L);

        List<Map<String, Object>> tree = AdminApplication.buildTree(List.of(root, child), "parent_id");

        assertEquals(1, tree.size());
        assertTrue(((List<?>) tree.getFirst().get("children")).size() == 1);
    }

    @Test
    void masksSensitiveOperationParams() {
        String masked = AdminApplication.maskSensitive("{password=secret, token=abc}");
        assertTrue(masked.contains("password=******"));
        assertTrue(masked.contains("token=******"));
    }
}
