package ch.uzh.ifi.hase.soprafs24.config;

import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;
import ch.uzh.ifi.hase.soprafs24.service.NoteStateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class YjsRedisSubscriber {

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Configure RedisStandaloneConfiguration
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("redis"); // Redis container hostname
        redisConfig.setPort(6379); // Redis container port
        redisConfig.setPassword(redisPassword); // Set Redis password from application properties

        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            NoteStateService noteStateService) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory); // Use the RedisConnectionFactory bean

        PatternTopic topic = new PatternTopic("ydoc:*");

        container.addMessageListener((Message message, byte[] pattern) -> {
            String channel = new String(message.getChannel());
            Long noteId = Long.parseLong(channel.split(":")[1]);
            byte[] update = message.getBody();

            NoteStatePutDTO dto = new NoteStatePutDTO();
            dto.setNoteId(noteId);
            dto.setContent(update);
            noteStateService.updateNoteStateContent(dto);
        }, topic);

        return container; // Spring will manage this bean and start it automatically
    }
}
