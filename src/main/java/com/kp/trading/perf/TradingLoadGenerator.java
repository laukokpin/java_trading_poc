package com.kp.trading.perf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class TradingLoadGenerator {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private TradingLoadGenerator() {}

    public static void main(String[] args) throws Exception {
        Config config = Config.fromArgs(args);
        BenchmarkResult result = run(config);
        String json = result.toJson();

        System.out.println(result.toConsoleSummary());

        if (config.outputPath() != null) {
            Files.createDirectories(config.outputPath().getParent());
            Files.writeString(config.outputPath(), json);
            System.out.println("Wrote benchmark summary to " + config.outputPath());
        }
    }

    public static BenchmarkResult run(Config config) throws InterruptedException, ExecutionException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();

        AtomicInteger sequence = new AtomicInteger();
        List<Long> latenciesNanos = new ArrayList<>(config.totalRequests());
        int successCount = 0;
        int failureCount = 0;

        long startedAtNanos = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(config.concurrency());
        try {
            List<Callable<RequestResult>> tasks = new ArrayList<>(config.totalRequests());
            for (int requestIndex = 0; requestIndex < config.totalRequests(); requestIndex++) {
                tasks.add(() -> executeRequest(client, config, sequence.incrementAndGet()));
            }

            List<Future<RequestResult>> futures = executor.invokeAll(tasks);
            for (Future<RequestResult> future : futures) {
                RequestResult requestResult = future.get();
                latenciesNanos.add(requestResult.latencyNanos());
                if (requestResult.success()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } finally {
            executor.shutdownNow();
        }
        long finishedAtNanos = System.nanoTime();

        latenciesNanos.sort(Comparator.naturalOrder());
        double elapsedSeconds = (finishedAtNanos - startedAtNanos) / 1_000_000_000.0;
        double throughput = elapsedSeconds == 0.0 ? successCount : successCount / elapsedSeconds;

        return new BenchmarkResult(
                config.profileName(),
                config.baseUrl(),
                config.concurrency(),
                config.totalRequests(),
                successCount,
                failureCount,
                elapsedSeconds,
                throughput,
                nanosToMillis(latenciesNanos.isEmpty() ? 0 : latenciesNanos.get(0)),
                nanosToMillis(latenciesNanos.isEmpty() ? 0 : latenciesNanos.get(latenciesNanos.size() - 1)),
                nanosToMillis(average(latenciesNanos)),
                nanosToMillis(percentile(latenciesNanos, 0.50)),
                nanosToMillis(percentile(latenciesNanos, 0.95)),
                nanosToMillis(percentile(latenciesNanos, 0.99)),
                Instant.now());
    }

    private static RequestResult executeRequest(HttpClient client, Config config, int sequence) {
        boolean orderRequest = sequence % 2 == 0;
        URI uri = URI.create(config.baseUrl() + (orderRequest ? "/api/v1/trading/orders" : "/api/v1/trading/quotes"));
        String payload = orderRequest ? buildOrderPayload(sequence) : buildQuotePayload(sequence);
        int expectedStatus = orderRequest ? 200 : 202;
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        long startedAtNanos = System.nanoTime();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long latencyNanos = System.nanoTime() - startedAtNanos;
            return new RequestResult(response.statusCode() == expectedStatus, latencyNanos);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new RequestResult(false, System.nanoTime() - startedAtNanos);
        } catch (IOException exception) {
            return new RequestResult(false, System.nanoTime() - startedAtNanos);
        }
    }

    private static String buildOrderPayload(int sequence) {
        return "{" +
                quoted("orderId") + ":" + quoted("perf-order-" + sequence) + "," +
                quoted("symbol") + ":" + quoted(sequence % 4 == 0 ? "USDJPY" : "EURUSD") + "," +
                quoted("side") + ":" + quoted(sequence % 3 == 0 ? "SELL" : "BUY") + "," +
                quoted("quantity") + ":1000000," +
                quoted("timestamp") + ":" + quoted(Instant.now().toString()) +
                "}";
    }

    private static String buildQuotePayload(int sequence) {
        double bid = 1.0800 + ((sequence % 25) * 0.0001);
        double ask = bid + 0.0002;
        return "{" +
                quoted("symbol") + ":" + quoted(sequence % 5 == 0 ? "GBPUSD" : "EURUSD") + "," +
                quoted("bid") + ":" + String.format(Locale.US, "%.4f", bid) + "," +
                quoted("ask") + ":" + String.format(Locale.US, "%.4f", ask) + "," +
                quoted("timestamp") + ":" + quoted(Instant.now().toString()) +
                "}";
    }

    private static String quoted(String value) {
        return "\"" + value + "\"";
    }

    private static double average(List<Long> values) {
        if (values.isEmpty()) {
            return 0;
        }

        long total = 0;
        for (Long value : values) {
            total += value;
        }
        return (double) total / values.size();
    }

    private static long percentile(List<Long> values, double percentile) {
        if (values.isEmpty()) {
            return 0;
        }

        int index = (int) Math.ceil(percentile * values.size()) - 1;
        return values.get(Math.max(0, Math.min(index, values.size() - 1)));
    }

    private static double nanosToMillis(double nanos) {
        return nanos / 1_000_000.0;
    }

    public record Config(String profileName, String baseUrl, int concurrency, int totalRequests, Path outputPath) {

        static Config fromArgs(String[] args) {
            String profile = "low";
            String baseUrl = "http://localhost:8080";
            Path outputPath = null;

            for (int index = 0; index < args.length; index++) {
                String arg = args[index];
                if ("--profile".equals(arg) && index + 1 < args.length) {
                    profile = args[++index];
                } else if ("--base-url".equals(arg) && index + 1 < args.length) {
                    baseUrl = args[++index];
                } else if ("--output".equals(arg) && index + 1 < args.length) {
                    outputPath = Path.of(args[++index]);
                } else {
                    throw new IllegalArgumentException("Unsupported argument: " + arg);
                }
            }

            return switch (profile.toLowerCase(Locale.ROOT)) {
                case "low" -> new Config("low", baseUrl, 2, 200, outputPath);
                case "medium" -> new Config("medium", baseUrl, 6, 1200, outputPath);
                case "high" -> new Config("high", baseUrl, 12, 4000, outputPath);
                default -> throw new IllegalArgumentException("Unsupported profile: " + profile);
            };
        }
    }

    public record BenchmarkResult(
            String profile,
            String baseUrl,
            int concurrency,
            int totalRequests,
            int successCount,
            int failureCount,
            double elapsedSeconds,
            double throughputPerSecond,
            double minLatencyMillis,
            double maxLatencyMillis,
            double averageLatencyMillis,
            double p50LatencyMillis,
            double p95LatencyMillis,
            double p99LatencyMillis,
            Instant completedAt) {

        String toJson() {
            return "{" +
                    quoted("profile") + ":" + quoted(profile) + "," +
                    quoted("baseUrl") + ":" + quoted(baseUrl) + "," +
                    quoted("concurrency") + ":" + concurrency + "," +
                    quoted("totalRequests") + ":" + totalRequests + "," +
                    quoted("successCount") + ":" + successCount + "," +
                    quoted("failureCount") + ":" + failureCount + "," +
                    quoted("elapsedSeconds") + ":" + formatDouble(elapsedSeconds) + "," +
                    quoted("throughputPerSecond") + ":" + formatDouble(throughputPerSecond) + "," +
                    quoted("minLatencyMillis") + ":" + formatDouble(minLatencyMillis) + "," +
                    quoted("maxLatencyMillis") + ":" + formatDouble(maxLatencyMillis) + "," +
                    quoted("averageLatencyMillis") + ":" + formatDouble(averageLatencyMillis) + "," +
                    quoted("p50LatencyMillis") + ":" + formatDouble(p50LatencyMillis) + "," +
                    quoted("p95LatencyMillis") + ":" + formatDouble(p95LatencyMillis) + "," +
                    quoted("p99LatencyMillis") + ":" + formatDouble(p99LatencyMillis) + "," +
                    quoted("completedAt") + ":" + quoted(completedAt.toString()) +
                    "}";
        }

        String toConsoleSummary() {
            return String.format(
                    Locale.US,
                    "profile=%s requests=%d success=%d failure=%d throughput=%.2f req/s avg=%.2f ms p95=%.2f ms p99=%.2f ms max=%.2f ms",
                    profile,
                    totalRequests,
                    successCount,
                    failureCount,
                    throughputPerSecond,
                    averageLatencyMillis,
                    p95LatencyMillis,
                    p99LatencyMillis,
                    maxLatencyMillis);
        }

        private String formatDouble(double value) {
            return String.format(Locale.US, "%.3f", value);
        }
    }

    public record RequestResult(boolean success, long latencyNanos) {}
}
