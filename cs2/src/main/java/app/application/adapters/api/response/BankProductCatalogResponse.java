package app.application.adapters.api.response;

import app.domain.models.enums.ProductCategory;

public record BankProductCatalogResponse(
        String productCode,
        String productName,
        String description,
        ProductCategory category,
        boolean requiresApproval,
        boolean active
) {}