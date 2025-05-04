package ch.uzh.ifi.hase.soprafs24.config;

import ch.uzh.ifi.hase.soprafs24.rest.dto.NoteStatePutDTO;
import ch.uzh.ifi.hase.soprafs24.service.NoteStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;



@Configuration
public class YjsRedisSubscriber {

    @Autowired
    public YjsRedisSubscriber(RedisConnectionFactory cf,
                              NoteStateService noteStateService) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(cf);

        // Subscribe to all channels matching "ydoc:<noteId>"
        PatternTopic topic = new PatternTopic("ydoc:*");

        container.addMessageListener((Message message, byte[] pattern) -> {
            String channel = new String(message.getChannel());  // e.g. "ydoc:42"
            Long noteId = Long.valueOf(channel.split(":")[1]);
            byte[] update = message.getBody();                  // the Yjs-update binary
            // Build the DTO
            NoteStatePutDTO dto = new NoteStatePutDTO();
            dto.setNoteId(noteId);
            dto.setContent(update);
            // Delegate to service
            noteStateService.updateNoteStateContent(dto);
            }, topic);

            container.start();
        }
    }
