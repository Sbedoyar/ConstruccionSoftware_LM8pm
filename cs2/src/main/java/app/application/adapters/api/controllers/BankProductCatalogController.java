package app.application.adapters.api.controllers;

import app.application.adapters.api.request.BankProductCatalogRequest;
import app.application.adapters.api.response.BankProductCatalogResponse;
import app.application.usecases.BankProductCatalogUseCase;
import app.domain.models.bankingProduct.BankProductCatalog;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog")
public class BankProductCatalogController {

    private final BankProductCatalogUseCase bankProductCatalogUseCase;

    public BankProductCatalogController(BankProductCatalogUseCase bankProductCatalogUseCase) {
        this.bankProductCatalogUseCase = bankProductCatalogUseCase;
    }

    @PostMapping
    public ResponseEntity<BankProductCatalogResponse> createCatalog(
            @Valid @RequestBody BankProductCatalogRequest request) {

        BankProductCatalog catalog = toModel(request);

        bankProductCatalogUseCase.createCatalog(catalog);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(catalog));
    }

    @GetMapping
    public ResponseEntity<List<BankProductCatalogResponse>> findAll() {
        return ResponseEntity.ok(
                bankProductCatalogUseCase.findAll()
                        .stream()
                        .map(BankProductCatalogController::toResponse)
                        .toList()
        );
    }

    private static BankProductCatalog toModel(BankProductCatalogRequest request) {
        BankProductCatalog catalog = new BankProductCatalog();

        catalog.setProductCode(request.getProductCode());
        catalog.setProductName(request.getProductName());
        catalog.setDescription(request.getDescription());
        catalog.setCategory(request.getCategory());
        catalog.setRequiresApproval(
                request.getRequiresApproval() != null && request.getRequiresApproval()
        );
        catalog.setActive(true);

        return catalog;
    }

    private static BankProductCatalogResponse toResponse(BankProductCatalog catalog) {
        return new BankProductCatalogResponse(
                catalog.getProductCode(),
                catalog.getProductName(),
                catalog.getDescription(),
                catalog.getCategory(),
                catalog.isRequiresApproval(),
                catalog.isActive()
        );
    }
}