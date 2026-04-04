# ConstruccionSoftware_LM8pm

Proyecto académico de Construcción de Software orientado al diseño de una aplicación para la gestión de información de una entidad bancaria. El sistema modela clientes, productos bancarios, préstamos, transferencias y bitácora de operaciones, aplicando reglas de negocio, restricciones por rol y flujos de aprobación definidos a partir del enunciado del curso.

---

## Integrantes

- Santiago Bedoya Rendón
- Santiago Villa

---

## Tecnologías utilizadas

- Java 17
- Spring Boot
- Maven
- Lombok
- MySQL
- MongoDB
- Git
- GitHub

---

## Descripción del proyecto

El objetivo del proyecto es construir el núcleo lógico de un sistema bancario capaz de administrar información de clientes y productos financieros bajo reglas de negocio específicas. La solución contempla el modelado de:

- Clientes persona natural
- Clientes empresa
- Usuarios del sistema con roles diferenciados
- Cuentas bancarias
- Préstamos
- Transferencias
- Bitácora de operaciones

El sistema está planteado con una separación por dominio, puertos y servicios, siguiendo una lógica orientada a casos de uso. Esto permite encapsular validaciones de negocio como la apertura de cuentas, aprobación y desembolso de préstamos, creación y aprobación de transferencias, control de acceso por rol y trazabilidad de operaciones.

---

## Objetivo académico

Este repositorio corresponde a un proyecto académico cuyo propósito es aplicar conceptos de:

- modelado de dominio,
- reglas de negocio,
- separación de responsabilidades,
- arquitectura basada en puertos y servicios,
- persistencia relacional y no relacional,
- control de acceso por roles.

---

## Estructura general del proyecto

El proyecto se encuentra organizado principalmente en el módulo `cs2`, el cual contiene la aplicación Spring Boot.

### Estructura base

- `src/main/java/app/domain/models`: entidades y modelos del dominio
- `src/main/java/app/domain/services`: casos de uso y lógica de negocio
- `src/main/java/app/domain/ports`: puertos de persistencia y acceso a datos
- `src/main/java/app/domain/exceptions`: excepciones del dominio
- `src/main/resources`: configuración base de la aplicación
- `src/test/java`: pruebas del proyecto

---

## Requisitos previos

Antes de ejecutar el proyecto, se recomienda tener instalado y configurado lo siguiente:

- Java 17
- Maven 3.9 o superior
- Git
- MySQL
- MongoDB
- Un IDE como IntelliJ IDEA, VS Code o Eclipse

---

## Instrucciones de ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/Sbedoyar/ConstruccionSoftware_LM8pm.git
```

### 2. Ingresar a la carpeta del repositorio

```bash
cd ConstruccionSoftware_LM8pm
```

### 3. Ingresar al módulo de la aplicación

```bash
cd cs2
```

### 4. Compilar el proyecto

Con Maven instalado:

```bash
mvn clean install
```

O usando el wrapper incluido:

```bash
./mvnw clean install
```

En Windows:

```bash
mvnw.cmd clean install
```

### 5. Ejecutar la aplicación

Con Maven:

```bash
mvn spring-boot:run
```

O con el wrapper:

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

### 6. Ejecutar pruebas

```bash
mvn test
```

### 7. Configuración adicional

En caso de integrar persistencia relacional y no relacional, se deben completar las propiedades de conexión en el archivo:

```text
cs2/src/main/resources/application.properties
```

Por ejemplo, allí se podrían definir credenciales y URLs para MySQL y MongoDB según el entorno local de ejecución.

---

## Gestión de ramas

Para el desarrollo del proyecto se propone una estrategia simple de ramas que permita trabajar cambios de manera ordenada.

### Ramas principales

- `main`: contiene la versión estable o de entrega.
- `develop`: contiene la integración de avances antes de consolidarlos en `main`.

### Ramas de apoyo

Para nuevas funcionalidades o ajustes se recomienda crear ramas a partir de `develop`, por ejemplo:

- `feature/client-management`
- `feature/loan-rules`
- `feature/transfers`
- `feature/operation-log`
- `fix/validation-errors`
- `docs/readme-update`

### Flujo sugerido

1. Crear una rama desde `develop`.
2. Realizar los cambios de una funcionalidad específica.
3. Hacer commits claros y pequeños.
4. Integrar los cambios en `develop`.
5. Consolidar en `main` cuando se prepare una entrega.

---

## Convención de commits

Para mantener trazabilidad en el historial del repositorio, se recomienda usar commits con formato claro y consistente.

### Estructura

```text
tipo: descripción corta
```

### Tipos sugeridos

- `feat`: nueva funcionalidad
- `fix`: corrección de errores
- `docs`: cambios en documentación
- `refactor`: mejora interna del código sin cambiar el comportamiento esperado
- `test`: creación o ajuste de pruebas
- `chore`: cambios menores de configuración o mantenimiento

### Ejemplos

```text
feat: add create loan service
fix: correct transfer approval validation
docs: update README execution steps
refactor: improve account validation logic
test: add unit tests for create account service
```

---

## Tag de entrega

Para identificar una versión de entrega del proyecto, se recomienda crear un tag anotado en Git.

### Ejemplo

```bash
git tag -a v1.0-entrega -m "Entrega inicial del proyecto bancario"
git push origin v1.0-entrega
```

Esto permite dejar trazabilidad clara de la versión presentada.

---

## Estado actual del proyecto

Actualmente el proyecto se encuentra enfocado en la construcción del dominio, la definición de puertos y servicios, y la implementación de reglas de negocio para clientes, cuentas, préstamos, transferencias y bitácora de operaciones.

---

## Observaciones

- Este repositorio corresponde a una entrega académica.
- La solución se encuentra en evolución conforme al avance del curso.
- Algunas capas de infraestructura, persistencia y configuración pueden ampliarse en etapas posteriores del desarrollo.