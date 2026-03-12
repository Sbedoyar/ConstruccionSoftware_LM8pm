package app.domain.models.operationLog;

import java.time.LocalDate;
import java.util.Map;

import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.person.Customer;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Setter
@Getter
@NoArgsConstructor

public class OperationLog {
    private String logId;
    private OperationType operationType;
    private LocalDate timestamp;
    private Customer user;
    private RoleType userRole;
    private String affectedProductId;
    private Map<String, Object> detailData;

}
