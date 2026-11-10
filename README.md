# QuindioStay

Plataforma de turismo del Quindío, dominio 2: agencias de viajes y paquetes turísticos. Proyecto final de **Bases de Datos I** (2026-2), Universidad del Quindío.

**Equipo:** Juan José Ospina Varón · Miguel Ángel Betancourt · Juan Pablo Espinosa Prada

**Stack:** Oracle Database Free (Docker) · Java 17 · Swing con FlatLaf · JDBC (ojdbc11) · JasperReports 6 con JFreeChart · jBCrypt

---

## Manual de instalación

### 1. Instalar (una sola vez)

| Herramienta | Notas |
|---|---|
| **Git** | En Mac: `xcode-select --install` |
| **JDK 17 o superior** | Eclipse Temurin (adoptium.net). En Mac con chip M, la versión *aarch64* |
| **Docker Desktop** | Para correr Oracle. En Mac con chip M, la versión *Apple Silicon* |
| **VS Code** | Con las extensiones *Extension Pack for Java* (Microsoft) y *Oracle SQL Developer Extension for VS Code* (Oracle) |

Las librerías Java (driver de Oracle, bcrypt, JasperReports, JFreeChart, FlatLaf) **no se descargan a mano**: Maven las baja solas al abrir el proyecto.

### 2. Levantar la base de datos

```bash
docker compose up -d
docker compose logs -f oracle      # esperar "DATABASE IS READY TO USE!" y salir con Ctrl+C
```

Esto crea el usuario `quindiostay` (clave `quindiostay`) en el servicio `FREEPDB1`.

En VS Code → SQL Developer → nueva conexión:

| Campo | Valor |
|---|---|
| Host / Puerto | `localhost` / `1521` |
| Tipo | Nombre de servicio → `FREEPDB1` |
| Usuario / clave | `quindiostay` / `quindiostay` |

### 3. Crear las tablas y cargar los datos

Con la conexión `quindiostay`, abrir cada script y correrlo con el botón **Run Script** (no con F5, que en VS Code inicia la depuración de Java), en este orden:

| Script | Qué hace |
|---|---|
| `sql/00_reset.sql` | Borra todo, para empezar limpio |
| `sql/01_ddl.sql` | 23 tablas con PK, FK, UNIQUE, CHECK y NOT NULL, comentarios y la vista `v_pago_reserva` |
| `sql/02_triggers.sql` | Reglas de negocio que miran otras tablas, procedimientos `cancelar_reserva` y `generar_recordatorios` y función `temporada` |
| `sql/03_datos.sql` | Datos de ejemplo: 150 clientes, 520 reservas, 723 pagos, 1.514 notificaciones… (tarda un par de minutos) |
| `sql/04_reportes.sql` | Las 10 consultas de los reportes, para probarlas sueltas |
| `sql/05_pruebas.sql` | Guion de la sustentación: operaciones que Oracle debe rechazar (termina en ROLLBACK) |

Los datos se generan con `sql/generador/generar_datos.py`, que revisa todas las reglas antes de escribir el script.

### 4. Correr la aplicación

```bash
cp db.properties.example db.properties    # solo la primera vez
mvn exec:java
```

O desde VS Code: abrir `src/main/java/.../Main.java` y darle **Run**.

### Usuarios de prueba

Todos con la contraseña `Clave123`.

| Usuario | Rol | Qué ve |
|---|---|---|
| `jpespinosa` | administrador | Gestión completa, reportes, documentos, generar recordatorios |
| `mabetancourt` | operador | Paquetes (solo lectura), clientes, reservas, reportes, documentos |
| `cafeymontana` | encargado de prestador | Solo la información de Café & Montaña Viajes |
| `maria.gomez@gmail.com` | cliente | Reservar y pagar en línea, mis reservas, notificaciones, mi perfil |

**Pasarela de pago simulada:** cualquier tarjeta válida aprueba (por ejemplo `4111 1111 1111 1111`, vence `12/28`, CVV `123`). Una tarjeta que termine en `0002` (por ejemplo `4000 0000 0000 0002`) simula un rechazo. Los datos de la tarjeta no se guardan.

---

## Qué hace el sistema

- **Login único** para clientes (con su correo) y usuarios internos (con su usuario). Las contraseñas se guardan cifradas con bcrypt y el menú cambia según el rol.
- **Registro y perfil:** el cliente crea su cuenta (con la autorización de datos) y administra sus preferencias de notificación y su contraseña.
- **Reservar y pagar en línea:** se escoge paquete, salida, plan y pasajeros, y se paga con tarjeta o PSE (el total o un anticipo del 30 %).
- **Recordatorios y notificaciones:** se programan según el perfil (1, 3 o 7 días antes, por correo, SMS o WhatsApp). El cliente ve su historial.
- **CRUD** de las entidades del modelo (en `vista/`, con la plantilla de `docs/GUIA_CRUD.md`).
- **10 reportes** (3 simples, 4 intermedios y 3 avanzados). Cuatro llevan gráfico (1, 4, 5 y 7) y todos se ven en la app y se exportan a PDF.
- **Plantillas:** comprobante de reserva (con itinerario), comprobante de pago, factura, autorización de viaje de menores y autorización de tratamiento de datos.

## Estructura

```
sql/                           scripts numerados en orden de ejecución
sql/generador/                 programas que generan los datos y los diseños de reportes
src/main/java/.../vista/       ventanas Swing
src/main/java/.../logica/      validaciones, reglas de negocio, sesión y reportes
src/main/java/.../datos/       conexión y acceso a datos (JDBC con PreparedStatement)
src/main/java/.../modelo/      clases que representan los datos
src/main/resources/reportes/   diseños JasperReports (.jrxml) de los reportes y plantillas
docs/                          entregas y guía para hacer un CRUD
```

## Reglas de trabajo

1. Nunca cambiar la base "a mano": todo cambio va en los scripts de `sql/`.
2. Cada quien trabaja en su rama y une a `main` con pull request.
3. `git pull` antes de empezar y `git push` al terminar.
4. `db.properties` no se sube (cada uno tiene la suya).
