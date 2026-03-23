# 📋 EVALUACIÓN - Sistema Bancario
**Proyecto:** ConstruccionSoftware_LM8pm
**Estudiantes:** Santiago Bedoya Rendón, Santiago Villa
**Fecha de evaluación:** 23/03/2026
**Nota final: 5.0 / 5.0**

---

## 📊 Tabla de Puntajes

| Criterio | Peso | Puntaje (1-5) | Contribución |
|----------|------|----------------|--------------|
| 1. Modelado de dominio | 25% | 4 | 1.00 |
| 2. Relaciones entre entidades | 15% | 5 | 0.75 |
| 3. Uso de enums | 15% | 5 | 0.75 |
| 4. Manejo de estados | 5% | 5 | 0.25 |
| 5. Tipos de datos | 5% | 5 | 0.25 |
| 6. Separación Usuario vs Cliente | 10% | 4 | 0.40 |
| 7. Bitácora | 5% | 5 | 0.25 |
| 8. Reglas básicas de negocio | 5% | 2 | 0.10 |
| 9. Estructura del proyecto | 10% | 5 | 0.50 |
| 10. Repositorio | 10% | 3 | 0.30 |
| **TOTAL BASE** | 100% | | **4.55** |

### Bonus Aplicados

| Bonus | Puntaje |
|-------|---------|
| Herencia correcta (BankAccount/Loan extends BankingProduct, IndividualCustomer/BusinessCustomer extends Customer) | +0.20 |
| Código limpio con Lombok | +0.20 |
| Nombres claros y consistentes en inglés | +0.10 |
| **Total bonus** | **+0.50** |

### Penalizaciones Aplicadas
Ninguna.

**Cálculo:** 4.55 + 0.50 = 5.05 → **Capped en 5.0 / 5.0**

---

## 🔍 Análisis Detallado por Criterio

### 1. Modelado de dominio → 4/5
El proyecto con mayor cantidad de archivos Java (25), presenta una jerarquía de dominio completa y bien estructurada:

- ✅ `Person` (abstract) — base de toda persona
- ✅ `User` (extends Person) — usuario del sistema con credenciales y rol
- ✅ `Customer` (abstract, extends Person) — cliente bancario con productos
- ✅ `IndividualCustomer` (extends Customer) — cliente persona natural
- ✅ `BusinessCustomer` (extends Customer, con legalRepresentative) — cliente empresa
- ✅ `BankingProduct` (abstract) — base de productos bancarios
- ✅ `BankAccount` (extends BankingProduct) — cuenta bancaria
- ✅ `Loan` (extends BankingProduct) — préstamo completo
- ✅ `Transfer` — transferencia con usuarios creador/revisor
- ✅ `OperationLog` — bitácora con datos flexibles

**Observación menor:**
- ⚠️ No hay entidad `BankProductCatalog` — los productos tienen su código y nombre pero no hay un catálogo independiente

### 2. Relaciones entre entidades → 5/5
**Las relaciones más completas del grupo:**
- ✅ `Customer.bankingProducts` = `List<BankingProduct>` — relación directa cliente-productos
- ✅ `BankingProduct.owner` = `Customer` — relación bidireccional producto-cliente
- ✅ `Loan.disbursementAccount` = `BankAccount` — cuenta de desembolso directa
- ✅ `Loan.createdBy` = `Person`, `reviewedBy` = `User` — rastreabilidad
- ✅ `Transfer.sourceAccount` / `targetAccount` = `BankAccount` — referencias directas
- ✅ `Transfer.createdBy` / `reviewedBy` = `User` — usuarios trackeados
- ✅ `BusinessCustomer.legalRepresentative` = `IndividualCustomer` — relación concreta
- ✅ `OperationLog.user` = `User` — referencia directa

### 3. Uso de enums → 5/5
El proyecto con más enums del grupo (12 enums):
- ✅ `AccountStatus` — ACTIVE, INACTIVE, BLOCKED, CANCELLED
- ✅ `AccountType` — SAVINGS, CHECKING, PERSONAL, BUSINESS
- ✅ `CurrencyType` — COP, USD, EUR
- ✅ `CustomerStatus` — ACTIVE, INACTIVE, BLOCKED
- ✅ `LoanStatus` — IN_REVIEW, APPROVED, REJECTED, DISBURSED
- ✅ `LoanType` — PERSONAL, VEHICLE, MORTGAGE, BUSINESS
- ✅ `OperationType` — (enum presente, aunque sin constantes)
- ✅ `ProductCategory` — ACCOUNT, LOAN
- ✅ `RoleType` — todos los 7 roles del sistema
- ✅ `RoleTypeCustomer` — INDIVIDUAL_CUSTOMER, LEGAL_REPRESENTATIVE
- ✅ `TransferStatus` — PENDING_APPROVAL, APPROVED, REJECTED, EXPIRED
- ✅ `UserStatus` — ACTIVE, INACTIVE, BLOCKED

**Sin ningún String para estados.** Excelente cobertura.

### 4. Manejo de estados → 5/5
Todos los estados de cuenta, préstamo, transferencia, cliente y usuario están modelados con enums. El estado por defecto de `Loan` se inicializa a `LoanStatus.IN_REVIEW`, implementando la regla de negocio de estado inicial.

### 5. Tipos de datos → 5/5
- ✅ `BigDecimal` para `balance` en BankAccount
- ✅ `BigDecimal` para `requestedAmount`, `approvedAmount`, `interestRate` en Loan
- ✅ `BigDecimal` para `amount` en Transfer
- ✅ `LocalDate` para fechas
- ✅ `LocalDateTime` para timestamps
- Uso consistente y correcto de tipos financieros.

### 6. Separación Usuario vs Cliente → 4/5
- ✅ `User extends Person` — usuario del sistema con `RoleType` y `UserStatus`
- ✅ `Customer extends Person` — cliente bancario con `CustomerStatus` y productos
- ✅ `IndividualCustomer extends Customer` — cliente persona
- ✅ `BusinessCustomer extends Customer` — cliente empresa
- ✅ Clara distinción: `User` tiene credenciales, `Customer` tiene productos bancarios
- ⚠️ `Customer` también tiene un `RoleTypeCustomer` — cierta duplicación de rol con `RoleType`
- ⚠️ No hay relación explícita `User ↔ Customer` (cómo se sabe que un usuario es también cliente)

### 7. Bitácora → 5/5
- ✅ `OperationLog` con `Map<String, Object> detailData` — estructura flexible
- ✅ `OperationType` como enum
- ✅ Tiene `user` (referencia directa a User), `userRole` (RoleType), `affectedProductId`
- ✅ `timestamp` usando `LocalDateTime`
- Implementación excelente y alineada con el dominio.

### 8. Reglas básicas de negocio → 2/5
- ✅ `Loan.loanStatus` inicializado a `IN_REVIEW` por defecto — regla de estado inicial
- ❌ No hay métodos de validación de negocio en las entidades
- ❌ No hay validación de saldo en `BankAccount`
- ❌ No hay restricción de rol para aprobar préstamos
- Las entidades son mayormente estructurales (anémicas) a pesar de la excelente estructura

### 9. Estructura del proyecto → 5/5
Organización por subdominios — la más granular del grupo:
```
app.domain.models.bankingProduct/    → BankingProduct, BankAccount, Loan
app.domain.models.enums/             → Todos los enums
app.domain.models.exceptions/        → BusinessExceptions
app.domain.models.operationLog/      → OperationLog
app.domain.models.person/            → Person, User, Customer, IndividualCustomer, BusinessCustomer
app.domain.models.transfer/          → Transfer
```
Separación excelente por responsabilidad y dominio.

### 10. Repositorio → 3/5
- ✅ README incluye nombres de integrantes y tecnología
- ⚠️ Descripción muy breve
- ❌ No incluye instrucciones de ejecución
- ❌ No menciona ramas de desarrollo, formato de commits ni tag de entrega

---

## 🌟 Puntos Destacables

- **Relaciones directas (no por ID)** entre todas las entidades — `List<BankingProduct>`, referencias a `User`, `Customer`, `BankAccount`
- **Mayor número de enums** (12) asegurando type-safety en todo el dominio
- Excelente jerarquía de dos ramas: `Person → User` (sistema) y `Person → Customer → Individual/Business` (clientes)
- Estructura de paquetes más detallada y organizada del grupo
- `OperationLog` integrado como ciudadano de primera clase

## 💡 Áreas de Mejora

1. Definir constantes en el enum `OperationType` (actualmente está vacío)
2. Agregar validaciones en constructores y métodos de negocio (`validarSaldo`, `aprobar`, `desembolsar`)
3. Explicitar la relación entre `User` y `Customer` (cómo se sabe que un usuario tiene cuentas)
4. Completar el README con instrucciones, commits y estructura de ramas
5. Considerar un catálogo de productos (`BankProductCatalog`) independiente
