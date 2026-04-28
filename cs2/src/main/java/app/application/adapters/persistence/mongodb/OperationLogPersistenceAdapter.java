package app.application.adapters.persistence.mongodb;

import app.application.adapters.persistence.mongodb.documents.OperationLogDocument;
import app.application.adapters.persistence.mongodb.documents.OperationLogDocument.UserSnapshot;
import app.application.adapters.persistence.mongodb.repositories.OperationLogMongoRepository;
import app.domain.models.enums.OperationType;
import app.domain.models.enums.RoleType;
import app.domain.models.operationLog.OperationLog;
import app.domain.models.person.User;
import app.domain.ports.OperationLogPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogPersistenceAdapter implements OperationLogPort {

    private final OperationLogMongoRepository repository;

    public OperationLogPersistenceAdapter(OperationLogMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(OperationLog operationLog) {
        repository.save(toDocument(operationLog));
    }

    @Override
    public List<OperationLog> findByAffectedProductId(String affectedProductId) {
        return repository.findByAffectedProductIdOrderByTimestampDesc(affectedProductId)
                .stream()
                .map(this::toModel)
                .toList();
    }

    @Override
    public List<OperationLog> findAll() {
        return repository.findAllByOrderByTimestampDesc()
                .stream()
                .map(this::toModel)
                .toList();
    }

    private OperationLogDocument toDocument(OperationLog operationLog) {
        OperationLogDocument document = new OperationLogDocument();

        document.setId(operationLog.getLogId());

        if (operationLog.getOperationType() != null) {
            document.setOperationType(operationLog.getOperationType().name());
        }

        document.setTimestamp(
                operationLog.getTimestamp() != null
                        ? operationLog.getTimestamp()
                        : LocalDateTime.now()
        );

        if (operationLog.getUserRole() != null) {
            document.setUserRole(operationLog.getUserRole().name());
        } else if (operationLog.getUser() != null && operationLog.getUser().getSystemRole() != null) {
            document.setUserRole(operationLog.getUser().getSystemRole().name());
        }

        document.setAffectedProductId(operationLog.getAffectedProductId());
        document.setDetailData(operationLog.getDetailData());

        if (operationLog.getUser() != null) {
            UserSnapshot userSnapshot = new UserSnapshot();
            userSnapshot.setIdentificationNumber(operationLog.getUser().getIdentificationNumber());
            userSnapshot.setName(operationLog.getUser().getName());
            userSnapshot.setUsername(operationLog.getUser().getUsername());

            if (operationLog.getUser().getSystemRole() != null) {
                userSnapshot.setSystemRole(operationLog.getUser().getSystemRole().name());
            }

            document.setUser(userSnapshot);
        }

        return document;
    }

    private OperationLog toModel(OperationLogDocument document) {
        OperationLog operationLog = new OperationLog();

        operationLog.setLogId(document.getId());

        if (document.getOperationType() != null) {
            operationLog.setOperationType(OperationType.valueOf(document.getOperationType()));
        }

        operationLog.setTimestamp(document.getTimestamp());

        if (document.getUserRole() != null) {
            operationLog.setUserRole(RoleType.valueOf(document.getUserRole()));
        }

        operationLog.setAffectedProductId(document.getAffectedProductId());
        operationLog.setDetailData(document.getDetailData());

        if (document.getUser() != null) {
            User user = new User();
            user.setIdentificationNumber(document.getUser().getIdentificationNumber());
            user.setName(document.getUser().getName());
            user.setUsername(document.getUser().getUsername());

            if (document.getUser().getSystemRole() != null) {
                user.setSystemRole(RoleType.valueOf(document.getUser().getSystemRole()));
            }

            operationLog.setUser(user);
        }

        return operationLog;
    }
}