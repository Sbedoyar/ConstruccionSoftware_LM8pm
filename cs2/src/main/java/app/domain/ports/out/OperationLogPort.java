package app.domain.ports.out;

import java.util.List;

import app.domain.models.operationLog.OperationLog;

public interface OperationLogPort {
     void save(OperationLog operationLog);
     List<OperationLog> findByAffectedProductId(String affectedProductId);
     List<OperationLog> findAll();
}
