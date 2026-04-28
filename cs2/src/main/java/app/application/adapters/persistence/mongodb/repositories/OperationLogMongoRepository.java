package app.application.adapters.persistence.mongodb.repositories;

import app.application.adapters.persistence.mongodb.documents.OperationLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OperationLogMongoRepository extends MongoRepository<OperationLogDocument, String> {

    List<OperationLogDocument> findByAffectedProductIdOrderByTimestampDesc(String affectedProductId);

    List<OperationLogDocument> findAllByOrderByTimestampDesc();
}