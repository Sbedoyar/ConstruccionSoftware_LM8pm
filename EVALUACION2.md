# EVALUACION 2 - ConstruccionSoftware_LM8pm

## Informacion general
- Estudiante(s): Santiago Bedoya Rendon
- Rama evaluada: develop
- Commit evaluado: 0a4de806f6ebd5855337c52b260b1fa8617afefc
- Fecha: 2026-04-11

## Tabla de calificacion

| Criterio | Peso | Puntaje (1-5) | Aporte |
|---|---|---|---|
| 1. Modelado de dominio | 20% | 5 | 1.00 |
| 2. Modelado de puertos | 20% | 5 | 1.00 |
| 3. Modelado de servicios de dominio | 20% | 5 | 1.00 |
| 4. Enums y estados | 10% | 5 | 0.50 |
| 5. Reglas de negocio criticas | 10% | 5 | 0.50 |
| 6. Bitacora y trazabilidad | 5% | 5 | 0.25 |
| 7. Estructura interna de dominio | 10% | 5 | 0.50 |
| 8. Calidad tecnica base en domain | 5% | 2 | 0.10 |
| **SUBTOTAL** | | | **4.85** |

## Penalizaciones
- **Acoplamiento del dominio a Spring (-25%):** Los servicios de dominio usan `@Service` y `@Autowired` de Spring.

Calculo: 4.85 x 0.75 = **3.64**

## Bonus
- +0.2: Puertos bien disenados, 6 puertos con firmas semanticas por agregado.
- +0.2: 18 servicios de dominio con alta cohesion, uno por caso de uso.
- +0.1: Excelente trazabilidad con `OperationLog` y `OperationLogPort`.

Total bonus: +0.5

## Nota final
**4.1 / 5.0**

---

## Hallazgos

### Positivos
- **Dominio de maxima calidad estructural:** entidades, puertos y servicios perfectamente organizados.
- Jerarquia: `Person → User`, `Person → Customer → IndividualCustomer / BusinessCustomer`.
- `BankingProduct` como base de `BankAccount` y `Loan`.
- **12 enums completos:** `LoanStatus` (IN_REVIEW, APPROVED, REJECTED, DISBURSED), `TransferStatus`, `AccountStatus`, `AccountType`, `CurrencyType`, `CustomerStatus`, `RoleType`, `LoanType`, `OperationType`, `ProductCategory`, `UserStatus`, `TransferType`.
- **6 puertos semanticos clean:** `UserPort`, `CustomerPort`, `LoanPort`, `AccountPort`, `TransferPort`, `OperationLogPort`.
- **18 servicios de dominio** cubriendo todos los casos de uso:
  - Prestamos: `CreateLoan`, `ApproveLoan`, `RejectLoan`, `DisburseLoan`.
  - Transferencias: `CreateTransfer`, `ApproveTransfer`, `RejectTransfer`, `ExecuteTransfer`, `ExpireTransfer`, `ValidateAccountOperation`.
  - Clientes: `CreateCustomer`, `FindAssignedCustomer`, `FindCustomerHistory`.
  - Usuarios: `CreateUser`, `DelegateCompanyUser`.
  - Cuentas: `CreateAccount`, `FindAccountForTeller`.
  - Bitacora: integrada en cada servicio transaccional.
- `BusinessException` para excepciones de negocio.
- `BigDecimal` para montos monetarios.

### Negativo critico
- **`@Service` y `@Autowired` de Spring en servicios de dominio.** El dominio importa `org.springframework.stereotype.Service` y `org.springframework.beans.factory.annotation.Autowired`. Penaliza -25% por acoplamiento al framework.
- Con este unico punto de mejora, el dominio seria de nota maxima.

## Recomendaciones
1. Eliminar `@Service` y `@Autowired` de todos los servicios en `domain/services/`. Usar inyeccion de dependencias por constructor sin anotaciones del framework.
2. Los puertos (interfaces) son correctos; las implementaciones concretas deben ir en la capa de infraestructura.
3. Este es el dominio mejor estructurado del grupo evaluado; aplicar la correccion de acoplamiento es la unica mejora necesaria.
