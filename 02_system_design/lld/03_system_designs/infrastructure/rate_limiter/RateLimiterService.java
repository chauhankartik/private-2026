package rate_limiter;

import rate_limiter.RateLimiterModels.RateLimitResult;
import rate_limiter.RateLimiterModels.RateLimiterConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiterService {
    private static volatile RateLimiterService instance;

    // ClientId -> IRateLimiter
    private final Map<String, IRateLimiter> limiters = new ConcurrentHashMap<>();
    
    // Default Rule Configuration
    private RateLimiterConfig defaultConfig;

    private RateLimiterService() {
        this.defaultConfig = new RateLimiterConfig.Builder().build();
    }

    public static RateLimiterService getInstance() {
        if (instance == null) {
            synchronized (RateLimiterService.class) {
                if (instance == null) {
                    instance = new RateLimiterService();
                }
            }
        }
        return instance;
    }

    public void setDefaultConfig(RateLimiterConfig config) {
        this.defaultConfig = config;
    }

    public void registerClient(String clientId, RateLimiterConfig config) {
        limiters.put(clientId, RateLimiterFactory.createRateLimiter(config));
        System.out.printf("⚙️ Registered Client [%s] with algorithm [%s] (Capacity: %d)%n",
                clientId, config.getAlgorithm(), config.getCapacity());
    }

    public boolean allowRequest(String clientId) {
        IRateLimiter limiter = limiters.computeIfAbsent(clientId, k -> RateLimiterFactory.createRateLimiter(defaultConfig));
        return limiter.allowRequest();
    }

    public RateLimitResult checkRequest(String clientId) {
        IRateLimiter limiter = limiters.computeIfAbsent(clientId, k -> RateLimiterFactory.createRateLimiter(defaultConfig));
        return limiter.checkRequest();
    }
}
