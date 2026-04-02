package app.domain.ports;

import java.util.List;

import app.domain.models.operationLog.OperationLog;

public interface OperationLogPort {
     
     void save(OperationLog log);
     List<OperationLog> findByProductId(String productId);

}
