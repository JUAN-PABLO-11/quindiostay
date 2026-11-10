-- =====================================================================
-- QuindioStay - 05_pruebas.sql
-- Guion para la sustentación: cada sentencia viola a propósito una
-- restricción o una regla de negocio, y Oracle debe RECHAZARLA.
-- Las pruebas buscan sus datos con subconsultas (no dependen de ids fijos).

-- Cómo usarlo: cursor dentro de una prueba y Cmd + Enter, una por una,
-- leyendo el comentario "Esperado". También se puede correr completo con
-- el botón Run Script. Al final hay un ROLLBACK: los datos no cambian.
-- Requiere haber cargado 03_datos.sql.
-- =====================================================================
SET SERVEROUTPUT ON


-- =============== 1. Restricciones declaradas en el DDL ===============

-- CHECK: la duración de un paquete va de 1 a 30 días
-- Esperado: ORA-02290 (CK_PAQUETE_DURACION)
UPDATE paquete SET duracion_dias = 45 WHERE id_paquete = 1;

-- UNIQUE (RN09): dos planes del mismo paquete no se llaman igual
-- Esperado: ORA-00001 (UQ_PLAN_PAQUETE_NOMBRE)
INSERT INTO plan (nombre, descripcion, tipo_plan, precio, fecha_inicio_vigencia, fecha_fin_vigencia, id_paquete)
VALUES ('Estándar', 'Plan repetido', 'estandar', 500000, DATE '2026-01-01', DATE '2026-12-31', 1);

-- FOREIGN KEY: no existe el municipio 99
-- Esperado: ORA-02291 (FK_DESTINO_MUNICIPIO)
INSERT INTO destino (nombre, id_municipio) VALUES ('Destino inventado', 99);

-- RESTRICT: un paquete con planes, salidas y reservas no se puede borrar
-- Esperado: ORA-02292 (FK_SALIDA_PAQUETE o FK_PLAN_PAQUETE)
DELETE FROM paquete WHERE id_paquete = 1;

-- NOT NULL: el cliente debe tener correo
-- Esperado: ORA-01400 ("CLIENTE"."CORREO")
INSERT INTO cliente (nombres, apellidos, tipo_documento, numero_documento, telefono, fecha_nacimiento, ciudad_origen)
VALUES ('Sin', 'Correo', 'CC', '99999999', '3001234567', DATE '1990-01-01', 'Armenia');


-- =============== 2. Reglas de negocio con triggers ===============

-- RN02: un prestador suspendido no puede crear paquetes
-- Esperado: ORA-20002
INSERT INTO paquete (nombre, descripcion, duracion_dias, categoria, minimo_viajeros, id_destino, id_prestador)
SELECT 'Paquete de prueba', 'Prueba', 2, 'naturaleza', 2, 1, id_prestador
  FROM prestador WHERE estado = 'suspendido' AND ROWNUM = 1;

-- RN03: un encargado de prestador debe estar ligado a un prestador
-- Esperado: ORA-20003
INSERT INTO usuario_sistema (nombre_usuario, contrasena_cifrada, id_rol, id_prestador)
VALUES ('encargado_sin_prestador', RPAD('x', 60, 'x'), 2, NULL);

-- RN11: un paquete de 1 día no tiene día 5 en su itinerario
-- Esperado: ORA-20011
INSERT INTO itinerario_dia (id_paquete, numero_dia, descripcion)
SELECT id_paquete, 5, 'Día que no existe' FROM paquete WHERE duracion_dias = 1 AND ROWNUM = 1;

-- RN14: plan de "Ruta del café" (paquete 1) con una salida de otro paquete
-- Esperado: ORA-20014
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
SELECT 650000, 'web',
       (SELECT MIN(id_plan) FROM plan WHERE id_paquete = 1),
       (SELECT MIN(id_salida) FROM salida WHERE id_paquete = 2 AND estado = 'programada'),
       1
  FROM dual;

-- RN10: el plan "Navidad 2026" solo está vigente desde el 1 de diciembre;
-- no sirve para una salida de noviembre (se mira la FECHA DEL SERVICIO)
-- Esperado: ORA-20010
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
SELECT 975000, 'web',
       (SELECT id_plan FROM plan WHERE id_paquete = 1 AND nombre = 'Navidad 2026'),
       (SELECT MIN(id_salida) FROM salida
         WHERE id_paquete = 1 AND estado IN ('programada', 'confirmada') AND fecha_inicio < DATE '2026-12-01'
           AND fecha_inicio > DATE '2026-11-01'),
       1
  FROM dual;

-- RN18: no se reserva en una salida cancelada
-- Esperado: ORA-20018
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
SELECT 100000, 'web', id_plan, id_salida, 1
  FROM (SELECT p.id_plan, s.id_salida
          FROM salida s JOIN plan p ON p.id_paquete = s.id_paquete
         WHERE s.estado = 'cancelada' AND p.estado = 'activo'
           AND s.fecha_inicio BETWEEN p.fecha_inicio_vigencia AND p.fecha_fin_vigencia)
 WHERE ROWNUM = 1;

-- RN27: una cotización "enviada" (no aceptada) no se convierte en reserva
-- Esperado: ORA-20027
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente, id_cotizacion)
SELECT 650000, 'web',
       (SELECT id_plan FROM plan WHERE id_paquete = 1 AND nombre = 'Estándar'),
       (SELECT MAX(id_salida) FROM salida WHERE id_paquete = 1 AND estado = 'programada'),
       c.id_cliente, c.id_cotizacion
  FROM cotizacion c WHERE c.estado = 'enviada' AND ROWNUM = 1;


-- RN16 y RN30: se arma una salida de prueba con cupo de 2 y una reserva vacía
-- Paso 1 (SÍ funciona): salida del 29/12 con cupo 2
INSERT INTO salida (fecha_inicio, cupo_maximo, id_paquete) VALUES (DATE '2026-12-29', 2, 1);
-- Paso 2 (SÍ funciona): reserva en esa salida, todavía sin pasajeros
INSERT INTO reserva (valor_total, canal, id_plan, id_salida, id_cliente)
SELECT 1300000, 'web',
       (SELECT id_plan FROM plan WHERE id_paquete = 1 AND nombre = 'Estándar'),
       (SELECT MAX(id_salida) FROM salida), 1
  FROM dual;

-- RN16: subir 3 pasajeros adultos a una salida de 2 cupos
-- Esperado: ORA-20016 (quedaría con 3 pasajeros y su cupo máximo es 2)
INSERT INTO reserva_pasajero (id_reserva, id_pasajero)
SELECT (SELECT MAX(id_reserva) FROM reserva), id_pasajero
  FROM pasajero
 WHERE MONTHS_BETWEEN(DATE '2026-12-29', fecha_nacimiento) >= 216 AND ROWNUM <= 3;

-- RN30 (regla del dominio): un menor no viaja sin un adulto en la misma reserva
-- Esperado: ORA-20032
INSERT INTO reserva_pasajero (id_reserva, id_pasajero)
SELECT (SELECT MAX(id_reserva) FROM reserva), id_pasajero
  FROM pasajero
 WHERE MONTHS_BETWEEN(DATE '2026-12-29', fecha_nacimiento) < 216 AND ROWNUM = 1;


-- RN17: una salida programada que no alcanza el mínimo no se puede confirmar
-- Esperado: ORA-20017
UPDATE salida SET estado = 'confirmada'
 WHERE id_salida = (SELECT MIN(s.id_salida)
                      FROM salida s JOIN paquete p ON p.id_paquete = s.id_paquete
                     WHERE s.estado = 'programada'
                       AND (SELECT COUNT(*) FROM reserva r
                              JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
                             WHERE r.id_salida = s.id_salida AND r.estado <> 'cancelada') < p.minimo_viajeros);

-- RN21: una reserva ya pagada completa no recibe un pago más
-- Esperado: ORA-20021
INSERT INTO pago (monto, metodo_pago, estado_pago, id_reserva)
SELECT 100000, 'pse', 'exitoso', MIN(id_reserva)
  FROM v_pago_reserva WHERE pagada = 'si' AND estado = 'completada';

-- RN22: un pago en efectivo no lleva referencia de pago en línea
-- Esperado: ORA-20022
INSERT INTO pago_en_linea (id_pago, referencia_transaccion)
SELECT MIN(id_pago), 'TX-PRUEBA-001' FROM pago WHERE metodo_pago = 'efectivo';

-- RN23: una reserva cancelada no recibe pagos nuevos
-- Esperado: ORA-20023
INSERT INTO pago (monto, metodo_pago, estado_pago, id_reserva)
SELECT 50000, 'efectivo', 'exitoso', MIN(id_reserva) FROM reserva WHERE estado = 'cancelada';

-- RN25: la fecha de un servicio cotizado no puede ser anterior a la cotización
-- Esperado: ORA-20025
INSERT INTO detalle_cotizacion (id_cotizacion, numero_linea, fecha_servicio, cantidad_personas, valor, id_componente)
SELECT id_cotizacion, 99, fecha - 10, 2, 100000, 4 FROM cotizacion WHERE estado = 'enviada' AND ROWNUM = 1;

-- RN06: a un cliente sin cuenta solo se le notifica por correo
-- Esperado: ORA-20006
INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, id_cliente)
SELECT 'confirmacion de reserva', 'sms', 'Prueba', SYSTIMESTAMP, MIN(c.id_cliente)
  FROM cliente c WHERE NOT EXISTS (SELECT 1 FROM perfil_cliente pc WHERE pc.id_cliente = c.id_cliente);

-- RN07: las promociones solo van a quien las aceptó
-- Esperado: ORA-20007
INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, id_cliente)
SELECT 'promocion', 'correo', 'Descuento de diciembre', SYSTIMESTAMP, MIN(id_cliente)
  FROM perfil_cliente WHERE acepta_promociones = 'no';

-- Fecha de nacimiento futura (SYSDATE no se permite en un CHECK, por eso es trigger)
-- Esperado: ORA-20030
UPDATE cliente SET fecha_nacimiento = SYSDATE + 30 WHERE id_cliente = 1;

-- RN28: una reserva pendiente no pasa directo a completada
-- Esperado: ORA-20028
UPDATE reserva SET estado = 'completada'
 WHERE id_reserva = (SELECT MIN(id_reserva) FROM reserva WHERE estado = 'pendiente');

-- RN28: una reserva cancelada no vuelve a cambiar de estado
-- Esperado: ORA-20028
UPDATE reserva SET estado = 'confirmada'
 WHERE id_reserva = (SELECT MIN(id_reserva) FROM reserva WHERE estado = 'cancelada');

-- RN29: una reserva cancelada no se borra, queda como historial
-- Esperado: ORA-20029
DELETE FROM reserva WHERE id_reserva = (SELECT MIN(id_reserva) FROM reserva WHERE estado = 'cancelada');


-- =============== 3. Procedimientos ===============

-- RN31: cancelar una reserva confirmada con más de 5 días de anticipación
-- devuelve el 80 % de lo pagado (SÍ funciona) y le avisa al cliente
DECLARE
    v_reserva NUMBER;
BEGIN
    SELECT MIN(r.id_reserva) INTO v_reserva
      FROM reserva r JOIN salida s ON s.id_salida = r.id_salida
     WHERE r.estado = 'confirmada' AND s.fecha_inicio > SYSDATE + 5;
    cancelar_reserva(v_reserva);
    DBMS_OUTPUT.PUT_LINE('Reserva ' || v_reserva || ' cancelada');
END;
/
SELECT r.id_reserva, r.estado, v.pagado, r.valor_reembolso, r.fecha_cancelacion
  FROM reserva r JOIN v_pago_reserva v ON v.id_reserva = r.id_reserva
 WHERE r.fecha_cancelacion >= TRUNC(SYSDATE);

-- RN31: una reserva completada ya no se puede cancelar
-- Esperado: ORA-20033
DECLARE
    v_reserva NUMBER;
BEGIN
    SELECT MIN(id_reserva) INTO v_reserva FROM reserva WHERE estado = 'completada';
    cancelar_reserva(v_reserva);
END;
/

-- RN32: genera los recordatorios que falten según el perfil de cada cliente
-- (SÍ funciona: crea el de la reserva de prueba del paso 2)
DECLARE
    v_creados NUMBER;
BEGIN
    generar_recordatorios(v_creados);
    DBMS_OUTPUT.PUT_LINE(v_creados || ' recordatorios creados');
END;
/


-- =============== 4. CASCADE en acción ===============
-- Al borrar una cotización se borran sus líneas (entidad débil DETALLE_COTIZACION)
DECLARE
    v_cot   NUMBER;
    v_antes NUMBER;
    v_despues NUMBER;
BEGIN
    SELECT MIN(id_cotizacion) INTO v_cot FROM cotizacion WHERE estado = 'borrador';
    SELECT COUNT(*) INTO v_antes FROM detalle_cotizacion WHERE id_cotizacion = v_cot;
    DELETE FROM cotizacion WHERE id_cotizacion = v_cot;
    SELECT COUNT(*) INTO v_despues FROM detalle_cotizacion WHERE id_cotizacion = v_cot;
    DBMS_OUTPUT.PUT_LINE('Cotización ' || v_cot || ': ' || v_antes || ' líneas antes, ' || v_despues || ' después');
END;
/


-- Deshace todo lo que sí funcionó (salida, reserva, cancelación, recordatorios y borrado)
ROLLBACK;
