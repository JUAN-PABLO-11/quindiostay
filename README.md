# QuindioStay

Sistema de agencias y paquetes turísticos del Quindío. Proyecto final de **Bases de Datos I**, Universidad del Quindío.

**Equipo:** Juan José Ospina Varón · Miguel Ángel Betancourt · Juan Pablo Espinosa Prada

**Stack:** Java 17 · Swing (FlatLaf) · Oracle Database · JDBC (ojdbc11) · JasperReports · JFreeChart · jBCrypt

---

## 1. Instalar (una sola vez)

| Herramienta | Notas |
|---|---|
| **Git** | En Mac viene con las herramientas de desarrollo (`xcode-select --install`) |
| **JDK 17** | Eclipse Temurin 17 (en Mac con chip M, la versión *aarch64*) |
| **Docker Desktop** (o OrbStack) | Para correr Oracle |
| **VS Code** + extensiones *Extension Pack for Java* y *Oracle SQL Developer Extension for VS Code* | Editor y cliente SQL |
| **Jaspersoft Studio** (Community, 6.21.x) | Solo quien diseñe los reportes. Usar la misma versión 6.x que el `pom.xml` |

Las librerías Java (driver de Oracle, bcrypt, Jasper, JFreeChart, FlatLaf) **no se descargan a mano**: Maven las baja solas al abrir el proyecto.

## 2. Levantar la base de datos

```bash
docker compose up -d
docker compose logs -f oracle      # esperar "DATABASE IS READY TO USE!" (Ctrl+C para salir)
```

Queda creado el usuario `quindiostay` (clave `quindiostay`) en el servicio `FREEPDB1`.

Conexión desde la extensión de VS Code:

| Campo | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `1521` |
| Servicio | `FREEPDB1` |
| Usuario / clave | `quindiostay` / `quindiostay` |

Para apagarla: `docker compose stop` (los datos se conservan).

## 3. Crear las tablas

Conectado como `quindiostay`, correr en orden:

```
sql/00_reset.sql      ← borra todo (para empezar limpio)
sql/01_ddl.sql
sql/02_triggers.sql
sql/03_datos.sql
```

`sql/04_reportes.sql` tiene las 10 consultas para probarlas.

## 4. Correr la aplicación

```bash
cp db.properties.example db.properties    # solo la primera vez
mvn exec:java
```

O desde VS Code: abrir `Main.java` y darle *Run*. Por ahora solo muestra si la conexión funciona.

## Estructura

```
sql/                      scripts de la base de datos (fuente de verdad)
src/main/java/.../
  vista/                  ventanas Swing
  logica/                 validaciones y reglas de negocio
  datos/                  Conexion y un DAO por tabla
  modelo/                 clases que representan cada tabla
src/main/resources/reportes/   diseños .jrxml de Jasper
docs/                     documento de las entregas, diagramas
```

## Reglas de trabajo

1. **Nunca cambiar la base "a mano"**: todo cambio va en los scripts de `sql/` y se avisa al equipo. Los demás corren `00_reset` y los scripts de nuevo.
2. `main` siempre compila. Cada quien trabaja en su rama (`bd`, `crud`, `reportes`) y une con *pull request*.
3. `git pull` antes de empezar, `git push` al terminar.
4. `db.properties` no se sube (cada uno tiene la suya).

## Entregas

| Entrega | Fecha | Producto |
|---|---|---|
| 3 | Vie 13/11 | BD en Oracle (DDL + datos de prueba), CRUD y los 10 reportes en PDF |
| Sustentación | Vie 20/11 | Defensa del diseño y demostración en vivo |
