-- =====================================================================
-- QuindioStay - 05_pruebas.sql
-- Guion para la sustentación: cada sentencia viola a propósito una
-- restricción o una regla de negocio, y Oracle debe RECHAZARLA.

-- Cómo usarlo en la demostración: cursor en una prueba y Cmd + Enter, una por una,
-- leyendo el comentario "Esperado". También se puede correr completo con F5.
-- Al final hay un ROLLBACK, así que los datos quedan como estaban.
-- Requiere haber cargado 03_datos.sql.
-- =====================================================================


-- =============== 1. Restricciones declaradas en el DDL ===============

-- CHECK: la duración de un paquete va de 1 a 30 días
-- Esperado: ORA-02290 check constraint (CK_PAQUETE_DURACION) violated
UPDATE paquete SET duracion_dias = 45 WHERE id_paquete = 1;

-- UNIQUE (RN09): dos planes del mismo paquete no se llaman igual
-- Esperado: ORA-00001 unique constraint (UQ_PLAN_PAQUETE_NOMBRE) violated
INSERT INTO plan (nombre, descripcion, tipo_plan, precio, fecha_inicio_vigencia, fecha_fin_vigencia, id_paquete)
VALUES ('Estándar', 'Plan repetido', 'estandar', 500000, DATE '2026-01-01', DATE '2026-12-31', 1);

-- FOREIGN KEY: no existe el municipio 99
-- Esperado: ORA-02291 integrity constraint (FK_DESTINO_MUNICIPIO) violated - parent key not found
INSERT INTO destino (nombre, id_municipio) VALUES ('Destino inventado', 99);

-- RESTRICT: un paquete con planes, salidas y reservas no se puede borrar
-- Esperado: ORA-02292 integrity constraint (FK_PLAN_PAQUETE) violated - child record found
DELETE FROM paquete WHERE id_paquete = 1;

-- NOT NULL: el cliente debe tener correo
-- Esperado: ORA-01400 cannot insert NULL into ("QUINDIOSTAY"."CLIENTE"."CORREO")
INSERT INTO cliente (nombres, apellidos, tipo_documento, numero_documento, telefono, fecha_nacimiento, ciudad_origen)
VALUES ('Sin', 'Correo', 'CC', '99999999', '3001234567', DATE '1990-01-01', 'Armenia');


-- =============== 2. Reglas de negocio (triggers) ===============

-- RN02: un prestador suspendido no puede crear paquetes (Viajes Génova Natural, id 10)
-- Esperado: ORA-20002 RN02: el prestador 10 está suspendido...
INSERT INTO paquete (nombre, descripcion, duracion_dias, categoria, minimo_viajeros, id_destino, id_prestador)
VALUES ('Paquete de prueba', 'Prueba', 2, 'naturaleza', 2, 4, 10);

-- RN03: un encargado de prestador debe estar ligado a un prestador
-- Esperado: ORA-20003 RN03: un encargado de prestador debe estar ligado a un prestador.
INSERT INTO usuario_sistema (nombre_usuario, contrasena_cifrada, id_rol, id_prestador)
VALUES ('encargado_sin_prestador', RPAD('x', 60, 'x'), 2, NULL);

-- RN11: "Salento y Cocora exprés" dura 1 día; no existe el día 5 de su itinerario
-- Esperado: ORA-20011 RN11: el paquete dura 1 días; no existe el día 5.
INSERT INTO itinerario_dia (id_paquete, numero_dia, descripcion) VALUES (6, 5, 'Día que no existe');

-- RN14: el plan 1 es de "Ruta del café" y la salida 8 es de "Quindío de aventura"
-- Esperado: ORA-20014 RN14: el plan y la salida de la reserva deben pertenecer al mismo paquete.
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
VALUES (650000, 'web', 1, 8, 1);

-- RN10: el plan "Temporada diciembre" (id 12) solo está vigente desde el 1 de diciembre
-- Esperado: ORA-20010 RN10: el plan no está activo o no estaba vigente...
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
VALUES (980000, 'web', 12, 13, 1);

-- RN18: la salida 6 (Aves de Génova, 22/08) fue cancelada
-- Esperado: ORA-20018 RN18: no se pueden crear reservas en una salida cancelada.
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
VALUES (380000, 'web', 6, 6, 1);

-- RN27: la cotización COT-000189 (id 3) está "enviada", no "aceptada"
-- Esperado: ORA-20027 RN27: solo una cotización aceptada se convierte en reserva...
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente, id_cotizacion)
VALUES (650000, 'web', 1, 13, 3, 3);

-- RN16: la salida 7 (Ruta del café, 06/11) tiene 18 de 20 cupos vendidos.
-- Paso 1 (este SÍ funciona): se crea una reserva nueva en esa salida
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
VALUES (1950000, 'web', 1, 7, 1);
-- Paso 2: se intentan subir 3 pasajeros (18 + 3 = 21 > 20)
-- Esperado: ORA-20016 RN16: la salida 7 quedaría con 21 pasajeros y su cupo máximo es 20.
INSERT INTO reserva_pasajero (id_reserva, id_pasajero)
SELECT (SELECT MAX(id_reserva) FROM reserva), id_pasajero
  FROM pasajero WHERE id_pasajero BETWEEN 61 AND 63;

-- RN17: la salida 11 (Aves de Génova, 04/12) tiene 2 viajeros y el paquete exige 3
-- Esperado: ORA-20017 RN17: la salida tiene 2 viajeros y el paquete exige mínimo 3...
UPDATE salida SET estado = 'confirmada' WHERE id_salida = 11;

-- RN21: la reserva 1 ya está pagada completa ($2.600.000); un pago más la supera
-- Esperado: ORA-20021 RN21: los pagos exitosos de la reserva 1 sumarían 2700000...
INSERT INTO pago (monto, metodo_pago, estado_pago, id_reserva)
VALUES (100000, 'pse', 'exitoso', 1);

-- RN22: el pago 2 fue en efectivo; no puede tener referencia de pago en línea
-- Esperado: ORA-20022 RN22: un pago por efectivo no es un pago en línea...
INSERT INTO pago_en_linea (id_pago, referencia_transaccion) VALUES (2, 'TX-PRUEBA-001');

-- RN23: la reserva 20 está cancelada y no recibe pagos nuevos
-- Esperado: ORA-20023 RN23: la reserva está cancelada y no recibe pagos nuevos.
INSERT INTO pago (monto, metodo_pago, estado_pago, id_reserva)
VALUES (50000, 'efectivo', 'exitoso', 20);

-- RN25: la cotización 3 es del 29/10; un servicio no puede ser antes de esa fecha
-- Esperado: ORA-20025 RN25: la fecha de servicio debe ser posterior a la fecha de la cotización...
INSERT INTO detalle_cotizacion (id_cotizacion, numero_linea, fecha_servicio, cantidad_personas, valor, id_componente)
VALUES (3, 3, DATE '2026-10-01', 2, 100000, 4);

-- RN06: el cliente 18 no tiene cuenta, así que solo se le notifica por correo
-- Esperado: ORA-20006 RN06: el cliente no tiene cuenta; solo puede notificarse por correo.
INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, id_cliente)
VALUES ('confirmacion de reserva', 'sms', 'Prueba', SYSTIMESTAMP, 18);

-- RN07: el cliente 2 no aceptó recibir promociones
-- Esperado: ORA-20007 RN07: el cliente no acepta promociones.
INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, id_cliente)
VALUES ('promocion', 'correo', 'Descuento de diciembre', SYSTIMESTAMP, 2);

-- Fecha de nacimiento futura (SYSDATE no se permite en un CHECK, por eso es trigger)
-- Esperado: ORA-20030 La fecha de nacimiento del cliente no puede ser futura.
UPDATE cliente SET fecha_nacimiento = SYSDATE + 30 WHERE id_cliente = 1;


-- =============== 3. CASCADE en acción ===============
-- Al borrar una cotización se borran sus líneas (entidad débil DETALLE_COTIZACION)
SELECT COUNT(*) AS lineas_antes FROM detalle_cotizacion WHERE id_cotizacion = 9;
DELETE FROM cotizacion WHERE id_cotizacion = 9;
SELECT COUNT(*) AS lineas_despues FROM detalle_cotizacion WHERE id_cotizacion = 9;


-- Deshace todo (la reserva del paso 1 de RN16 y el borrado del CASCADE)
ROLLBACK;
