# Cómo hacer un CRUD nuevo

La plantilla es **Paquetes**. Cada CRUD son 5 archivos, uno por capa, y casi todo se copia y se cambia.

| # | Capa | Copiar de | Qué cambiar |
|---|---|---|---|
| 1 | `modelo/` | `Paquete.java` | Los campos (las columnas de la tabla + los nombres que vienen del JOIN) |
| 2 | `datos/` | `PaqueteDAO.java` | `SELECT_BASE`, `INSERT`, `UPDATE`, `DELETE`, `mapear()` y `asignar()` |
| 3 | `logica/` | `PaqueteService.java` | `validar()`: las mismas reglas de los CHECK y de las reglas de negocio |
| 4 | `vista/` | `PaqueteDialog.java` | Los campos del formulario, `llenar()` y `construir()` |
| 5 | `vista/` | `PaquetesPanel.java` | El título, las columnas y `fila()` |

Y al final, **una línea** en `vista/VentanaPrincipal.java`:

```java
opcion(gestion, "Prestadores", () -> enConstruccion("Prestadores"));   // antes
opcion(gestion, "Prestadores", PrestadoresPanel::new);                 // después
```

## Clases que ya vienen hechas (no hay que tocarlas)

- **`vista/comun/CrudPanel`**: arma toda la pantalla (buscador, botones Nuevo/Editar/Eliminar, tabla, barra de mensajes, confirmación al eliminar). El panel solo implementa 6 métodos.
- **`vista/comun/Formulario`**: arma el formulario en dos columnas con `f.campo("Nombre", txtNombre)`. Trae ayudas para los combos de llaves foráneas: `llenar`, `seleccionar`, `idSeleccionado`.
- **`datos/CatalogoDAO`**: las listas para los combos. Para un combo nuevo se agrega un método de una línea con su `SELECT id, texto`.
- **`logica/ErroresBD`**: traduce los errores de Oracle (CHECK, FK, UNIQUE, triggers RNxx) a un mensaje para la barra inferior. No hay que hacer nada; ya lo usa `CrudPanel`.
- **`logica/Sesion`**: quién inició sesión y su rol (`Sesion.esAdmin()`, `esEncargado()`, `esOperador()`).

## Orden sugerido (de fácil a difícil)

1. **Destinos**: 3 campos y un combo (municipio). Sirve para aprender la plantilla.
2. **Prestadores**: combos de tipo y municipio, y el estado.
3. **Componentes**: combos de tipo y prestador (solo prestadores activos, RN02).
4. **Planes**: combo de paquete y fechas de vigencia.
5. **Salidas**: combo de paquete, fecha, cupo y estado. Al pasar a "confirmada", el trigger RN17 valida el mínimo de viajeros.
6. **Clientes**: documento, correo y teléfono; perfil opcional.
7. **Reservas y pagos**: el más difícil (ver abajo).

## Fechas

En Oracle son `DATE`; en Java se usa `LocalDate`:

```java
// leer (en mapear)
LocalDate inicio = rs.getDate("fecha_inicio").toLocalDate();
// escribir (en asignar)
ps.setDate(3, java.sql.Date.valueOf(p.fechaInicio()));
```

En el formulario lo más simple es un `JTextField` con el formato `aaaa-mm-dd`, y en la lógica convertirlo con `LocalDate.parse(texto)` (si lanza `DateTimeParseException`, se lanza una `ReglaException("Fecha inválida, usa aaaa-mm-dd")`).

## Reservas: reglas que van en Java

Estas reglas no las puede revisar un trigger, porque dependen de varias tablas que se guardan juntas. Van en `ReservaService`:

- **RN15**: toda reserva tiene al menos un pasajero.
- **RN19**: `valor_total` = precio del plan × número de pasajeros (se calcula al reservar y ya no cambia).
- **RN22**: si el pago es con tarjeta o PSE, se guarda también su fila en `PAGO_EN_LINEA` con la referencia.

Lo demás (cupo, plan y salida del mismo paquete, pagos ≤ valor total, salida cancelada…) ya lo hacen los triggers. Basta con mostrar el error con `ErroresBD`.

Para guardar la reserva y sus pasajeros **juntos o nada**, se usa una transacción:

```java
try (Connection cn = Conexion.obtener()) {
    cn.setAutoCommit(false);
    try {
        // INSERT reserva, INSERT reserva_pasajero (uno por pasajero)
        cn.commit();
    } catch (SQLException e) {
        cn.rollback();
        throw e;
    }
}
```

## Usuarios de prueba

Todos con la contraseña `Clave123`:

| Usuario | Rol | Qué ve |
|---|---|---|
| `jpespinosa` | administrador | Todo |
| `mabetancourt` | operador | Paquetes en solo lectura, clientes, reservas, reportes |
| `cafeymontana` | encargado de prestador | Solo los paquetes de Café & Montaña Viajes |
| `maria.gomez1@correo.com` | cliente | Mi cuenta |

Para probar el RESTRICT: entra como `jpespinosa` → Gestión → Paquetes → elimina "Ruta del café 3 días". Debe salir el aviso de que tiene planes y salidas.
