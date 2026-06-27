package com.drip.admin.infrastructure.external;

import com.drip.admin.common.exception.BusinessException;
import com.drip.admin.modules.system.entity.SysJobEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Component
public class JobExecutorRegistry {
    private final ApplicationContext applicationContext;
    private final Path scriptRoot;

    public JobExecutorRegistry(ApplicationContext applicationContext, @Value("${drip.job.script-dir:./scripts}") String scriptDir) {
        this.applicationContext = applicationContext;
        this.scriptRoot = Path.of(scriptDir).toAbsolutePath().normalize();
    }

    public void execute(SysJobEntity job) {
        String type = required(job.getExecutorType(), "executorType").toLowerCase(Locale.ROOT);
        if ("java".equals(type)) {
            executeJava(job);
            return;
        }
        executeScript(type, job);
    }

    private void executeScript(String type, SysJobEntity job) {
        Path script = resolveScript(job.getScriptFile());
        List<String> command = new ArrayList<>();
        switch (type) {
            case "shell" -> {
                command.add("bash");
                command.add(script.toString());
            }
            case "bat" -> {
                command.add("cmd.exe");
                command.add("/c");
                command.add(script.toString());
            }
            case "powershell", "ps1" -> {
                command.add("powershell.exe");
                command.add("-ExecutionPolicy");
                command.add("Bypass");
                command.add("-File");
                command.add(script.toString());
            }
            case "python" -> {
                command.add("python");
                command.add(script.toString());
            }
            default -> throw new BusinessException(400000, "executorType is not supported");
        }
        command.addAll(splitArgs(job.getScriptArgs()));
        runProcess(command);
    }

    private void executeJava(SysJobEntity job) {
        try {
            Class<?> type = Class.forName(required(job.getClassName(), "className"));
            Object bean = applicationContext.getBean(type);
            Method method = type.getMethod(required(job.getMethodName(), "methodName"));
            method.invoke(bean);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            throw new BusinessException(400000, "java job target is invalid");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new BusinessException(500000, "java job execution failed: " + cause.getMessage());
        } catch (IllegalAccessException ex) {
            throw new BusinessException(500000, "java job execution failed: " + ex.getMessage());
        }
    }

    private Path resolveScript(String scriptFile) {
        Path script = scriptRoot.resolve(required(scriptFile, "scriptFile")).normalize();
        if (!script.startsWith(scriptRoot)) {
            throw new BusinessException(400000, "script path is not allowed");
        }
        if (!Files.isRegularFile(script)) {
            throw new BusinessException(404000, "script file does not exist");
        }
        return script;
    }

    private static void runProcess(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        try {
            Process process = builder.start();
            boolean finished = process.waitFor(Duration.ofMinutes(30).toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException(500000, "script execution timed out");
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new BusinessException(500000, "script execution failed, exit code: " + exitCode);
            }
        } catch (IOException ex) {
            throw new BusinessException(500000, "script execution failed: " + ex.getMessage());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500000, "script execution interrupted");
        }
    }

    private static List<String> splitArgs(String args) {
        if (args == null || args.isBlank()) {
            return List.of();
        }
        return Arrays.stream(args.trim().split("\\s+")).filter(item -> !item.isBlank()).toList();
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(400000, field + " is required");
        }
        return value.trim();
    }
}
