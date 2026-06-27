package com.drip.admin.infrastructure.external;

import com.drip.admin.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class JobScriptCatalog {
    private final Path scriptRoot;

    public JobScriptCatalog(@Value("${drip.job.script-dir:./scripts}") String scriptDir) {
        this.scriptRoot = Path.of(scriptDir).toAbsolutePath().normalize();
    }

    public List<String> list(String executorType) {
        Set<String> extensions = extensions(executorType);
        if (!Files.isDirectory(scriptRoot)) {
            return List.of();
        }
        try (var paths = Files.list(scriptRoot)) {
            return paths
                .filter(Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .filter(name -> extensions.contains(extension(name)))
                .sorted(Comparator.naturalOrder())
                .toList();
        } catch (IOException ex) {
            throw new BusinessException(500000, "script directory read failed");
        }
    }

    private static Set<String> extensions(String executorType) {
        String type = executorType == null ? "" : executorType.toLowerCase(Locale.ROOT);
        return switch (type) {
            case "shell" -> Set.of(".sh");
            case "bat" -> Set.of(".bat", ".cmd");
            case "powershell", "ps1" -> Set.of(".ps1");
            case "python" -> Set.of(".py");
            default -> throw new BusinessException(400000, "executorType is not supported");
        };
    }

    private static String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot).toLowerCase(Locale.ROOT);
    }
}
