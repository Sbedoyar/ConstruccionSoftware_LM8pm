package app.application.adapters.api.request;

import app.domain.models.enums.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankProductCatalogRequest {

    @NotBlank(message = "El código del producto es obligatorio")
    private String productCode;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String productName;

    private String description;

    @NotNull(message = "La categoría del producto es obligatoria")
    private ProductCategory category;

    private Boolean requiresApproval;
}