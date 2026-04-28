package app.application.adapters.persistence.mongodb.documents;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "operation_logs")
public class OperationLogDocument {

    @Id
    private String id;

    private String operationType;

    @Indexed
    private LocalDateTime timestamp;

    private String userRole;

    @Indexed
    private String affectedProductId;

    private Map<String, Object> detailData;

    private UserSnapshot user;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserSnapshot {
        private String identificationNumber;
        private String name;
        private String username;
        private String systemRole;
    }
}