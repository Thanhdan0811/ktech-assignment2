package helper;

import java.time.LocalDateTime;
import java.util.UUID;

public class GenerateId {

    public static String generateStringId() {
        LocalDateTime lcd = LocalDateTime.now();
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + lcd;
    }

}
