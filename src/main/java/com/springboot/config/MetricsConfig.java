package com.springboot.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            // Register custom metrics
            registry.gauge("app.events.total", 0);
            registry.gauge("app.rooms.total", 0);
            registry.gauge("app.users.total", 0);
            registry.gauge("app.favorites.total", 0);
            
            // Register custom counters
            registry.counter("app.events.created");
            registry.counter("app.events.updated");
            registry.counter("app.events.deleted");
            registry.counter("app.favorites.added");
            registry.counter("app.favorites.removed");
            
            // Register custom timers
            registry.timer("app.events.creation.time");
            registry.timer("app.events.update.time");
            registry.timer("app.events.deletion.time");
            registry.timer("app.favorites.addition.time");
            registry.timer("app.favorites.removal.time");
            
            // Register custom gauges
            Gauge.builder("app.events.capacity.used", () -> {
                // Calculate used capacity
                return 0.0;
            }).register(registry);
            
            Gauge.builder("app.events.capacity.available", () -> {
                // Calculate available capacity
                return 0.0;
            }).register(registry);
            
            // Register custom counters for security events
            registry.counter("app.security.login.attempts");
            registry.counter("app.security.login.successes");
            registry.counter("app.security.login.failures");
            registry.counter("app.security.logout.successes");
            
            // Register custom timers for security operations
            registry.timer("app.security.login.time");
            registry.timer("app.security.logout.time");
            
            // Register custom counters for API usage
            registry.counter("app.api.requests.total");
            registry.counter("app.api.requests.success");
            registry.counter("app.api.requests.failure");
            
            // Register custom timers for API response times
            registry.timer("app.api.response.time");
            
            // Register custom gauges for system health
            Gauge.builder("app.system.memory.used", () -> {
                return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            }).register(registry);
            
            Gauge.builder("app.system.memory.free", () -> {
                return Runtime.getRuntime().freeMemory();
            }).register(registry);
            
            Gauge.builder("app.system.threads.active", () -> {
                return Thread.activeCount();
            }).register(registry);
        };
    }
} 