# ConstruccionSoftware_LM8pm

Proyecto académico de Construcción de Software enfocado en el desarrollo de una aplicación de gestión de información para un banco.

El sistema busca administrar clientes, productos bancarios, préstamos, transferencias y bitácora de operaciones, aplicando reglas de negocio, control de acceso por roles y flujos de aprobación.

---

## Integrantes

1. Santiago Bedoya Rendón  
2. Santiago Villa  

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

Este proyecto implementa un sistema de información bancario que permite gestionar:

- Clientes persona natural
- Clientes empresa
- Usuarios del sistema con roles definidos
- Cuentas bancarias
- Préstamos
- Transferencias
- Bitácora de operaciones

El desarrollo está guiado por reglas de negocio y restricciones de acceso según el rol del usuario, tomando como referencia el enunciado académico del curso.

---

## Estructura general del proyecto

El proyecto está organizado en capas de dominio y servicios, incluyendo modelos para:

- Personas y usuarios
- Clientes naturales y jurídicos
- Productos bancarios
- Operaciones y bitácora
- Enumeraciones de estado y rol
- Servicios de negocio
- Puertos para persistencia

---

## Instrucciones de ejecución

Sigue estos pasos para clonar, compilar y ejecutar el proyecto localmente.

## Instrucciones de ejecución

Sigue estos pasos para clonar, compilar y ejecutar el proyecto localmente.

### 1. Clonar el repositorio

```bash
git clone https://github.com/Sbedoyar/ConstruccionSoftware_LM8pm.git
```

### 2. Ingresar a la carpeta del proyecto

```bash
cd ConstruccionSoftware_LM8pm
```

### 3. Compilar el proyecto con Maven

```bash
mvn clean install
```

### 4. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

### 5. Verificar dependencias y configuración del entorno

Asegúrate de tener instalado y configurado lo siguiente:

- **Java 17**
- **Maven**
- **Base de datos** configurada en `application.properties`

---

## Gestión de ramas

Para el desarrollo del proyecto se utilizarán ramas de trabajo con el fin de organizar los cambios.

### Ramas principales

- `main`: rama principal
- `develop`: rama de integración

### Ramas de apoyo por funcionalidad

Por ejemplo:

- `feature/client-management`
- `feature/loan-rules`
- `feature/transfers`
- `feature/logging`

### Flujo de integración

La integración final debe realizarse sobre la rama `develop` y posteriormente consolidarse en la rama principal según las indicaciones del curso.

---

## Convención de commits

Se usará un formato de commits claro y descriptivo.

### Estructura recomendada

```text
tipo: descripción corta
```

### Tipos sugeridos

- `feat`: nueva funcionalidad
- `fix`: corrección de errores
- `docs`: cambios en documentación
- `refactor`: mejora interna del código
- `test`: pruebas
- `chore`: ajustes menores o de configuración

### Ejemplos

```text
feat: add business customer model
fix: correct loan status validation
docs: update README with execution steps
refactor: improve customer-user relationship
```

---

## Tag de entrega

La versión de entrega del proyecto debe marcarse con un tag en Git.

### Ejemplo

```bash
git tag -a v1.0-entrega -m "Entrega inicial del proyecto bancario"
git push origin v1.0-entrega
```

---

## Estado actual del proyecto

Actualmente el proyecto se encuentra en construcción del dominio y definición de reglas de negocio para clientes, cuentas, préstamos, transferencias y control de acceso por roles.

---

## Observaciones

Este repositorio corresponde a un proyecto académico y su implementación se encuentra en evolución conforme al avance del curso y a la retroalimentación del docente.