package org.example;

import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.metrics.MicrometerCommandLatencyRecorder;
import io.lettuce.core.metrics.MicrometerOptions;
import io.lettuce.core.resource.ClientResources;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration extends CachingConfigurerSupport {

    @Autowired
    private MeterRegistry meterRegistry;

    @Override
    public CacheManager cacheManager() {
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(lettuceConnectionFactory())
                .build();
    }

    private LettuceConnectionFactory lettuceConnectionFactory() {
        RedisConfiguration redisConfiguration = new RedisClusterConfiguration().clusterNode(new RedisNode("localhost", 15000));

        MicrometerOptions options = MicrometerOptions.create();
        ClientResources resources = ClientResources.builder().commandLatencyRecorder(new MicrometerCommandLatencyRecorder(meterRegistry, options)).build();

        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder().build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(300))
                .clientResources(resources)
                .clientOptions(clusterClientOptions)
                .build();

        return new LettuceConnectionFactory(redisConfiguration, clientConfig);
    }
}
