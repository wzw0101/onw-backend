package priv.wzw.onw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JacksonUtils {
    private final ObjectMapper objectMapper;

    public String toJson(Object object) {
        String toRet = null;
        try {
            toRet = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            log.error("convert to json error", exception);
            log.info("object: {}", object);
        }
        return toRet;
    }

    public <T> T convert(Object from, Class<T> clazz) {
        return objectMapper.convertValue(from, clazz);
    }
}
