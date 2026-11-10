-- =====================================================================
-- QuindioStay - 02_triggers.sql
-- Reglas de negocio que un CHECK no puede verificar porque miran otras
-- tablas o la fecha actual. Correr como QUINDIOSTAY después de 01_ddl.sql
-- (con "Run Script" / F5).

-- Cada error usa RAISE_APPLICATION_ERROR con un código -200xx y el número
-- de la regla (RNxx) en el mensaje, para que la aplicación lo muestre tal
-- cual en la barra inferior de la ventana.

-- Reglas que se dejan a la capa de lógica de Java (no se pueden forzar en
-- el momento del INSERT porque dependen del orden en que se guardan las filas):
--   RN15 toda reserva tiene al menos un pasajero
--   RN22 (inverso) todo pago con tarjeta/PSE debe tener su fila en PAGO_EN_LINEA
--   RN11 (completo) el itinerario debe tener TODOS los días del paquete
--   RN19 el cálculo de valor_total = precio del plan x pasajeros
-- =====================================================================


-- ---------------------------------------------------------------------
-- RN02: solo un prestador "activo" arma paquetes, aporta componentes o
-- emite cotizaciones
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE validar_prestador_activo (p_id_prestador IN NUMBER) AS
    v_estado prestador.estado%TYPE;
BEGIN
    SELECT estado INTO v_estado FROM prestador WHERE id_prestador = p_id_prestador;
    IF v_estado <> 'activo' THEN
        RAISE_APPLICATION_ERROR(-20002,
            'RN02: el prestador ' || p_id_prestador || ' está ' || v_estado ||
            ' y no puede crear paquetes, componentes ni cotizaciones.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;  -- la FK se encarga de reportar el prestador inexistente
END;
/

CREATE OR REPLACE TRIGGER trg_paquete_prestador_activo
BEFORE INSERT OR UPDATE OF id_prestador ON paquete
FOR EACH ROW
BEGIN
    validar_prestador_activo(:NEW.id_prestador);
END;
/

CREATE OR REPLACE TRIGGER trg_componente_prestador_activo
BEFORE INSERT OR UPDATE OF id_prestador ON componente
FOR EACH ROW
BEGIN
    validar_prestador_activo(:NEW.id_prestador);
END;
/

CREATE OR REPLACE TRIGGER trg_cotizacion_validar
BEFORE INSERT OR UPDATE ON cotizacion
FOR EACH ROW
BEGIN
    IF INSERTING OR UPDATING('ID_PRESTADOR') THEN
        validar_prestador_activo(:NEW.id_prestador);
    END IF;

    -- Fecha de emisión no futura
    IF :NEW.fecha > TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20025, 'La fecha de la cotización no puede ser futura.');
    END IF;

    -- RN26: una cotización vencida (más de 8 días) ya no puede aceptarse
    IF UPDATING('ESTADO') AND :NEW.estado = 'aceptada' AND :OLD.estado <> 'aceptada' THEN
        IF TRUNC(SYSDATE) > :NEW.fecha + 8 THEN
            RAISE_APPLICATION_ERROR(-20026,
                'RN26: la cotización ' || :NEW.numero_cotizacion || ' venció el ' ||
                TO_CHAR(:NEW.fecha + 8, 'YYYY-MM-DD') || ' y ya no se puede aceptar.');
        END IF;
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- RN03: el usuario con rol "encargado de prestador" debe estar ligado a un
-- prestador; los demás roles no. (Si se borra el prestador, ON DELETE
-- SET NULL deja al usuario desvinculado: por eso no se valida al quedar NULL.)
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_usuario_rol
BEFORE INSERT OR UPDATE OF id_rol, id_prestador ON usuario_sistema
FOR EACH ROW
DECLARE
    v_rol rol.nombre%TYPE;
BEGIN
    SELECT nombre INTO v_rol FROM rol WHERE id_rol = :NEW.id_rol;

    IF v_rol = 'encargado de prestador' AND :NEW.id_prestador IS NULL
       AND (INSERTING OR UPDATING('ID_ROL')) THEN
        RAISE_APPLICATION_ERROR(-20003,
            'RN03: un encargado de prestador debe estar ligado a un prestador.');
    END IF;

    IF v_rol <> 'encargado de prestador' AND :NEW.id_prestador IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(-20003,
            'RN03: solo el rol "encargado de prestador" puede estar ligado a un prestador.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;  -- rol inexistente: lo reporta la FK
END;
/


-- ---------------------------------------------------------------------
-- Fechas de nacimiento no futuras (SYSDATE no se permite en un CHECK)
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_cliente_nacimiento
BEFORE INSERT OR UPDATE OF fecha_nacimiento ON cliente
FOR EACH ROW
BEGIN
    IF :NEW.fecha_nacimiento > TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20030, 'La fecha de nacimiento del cliente no puede ser futura.');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_pasajero_nacimiento
BEFORE INSERT OR UPDATE OF fecha_nacimiento ON pasajero
FOR EACH ROW
BEGIN
    IF :NEW.fecha_nacimiento > TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20030, 'La fecha de nacimiento del pasajero no puede ser futura.');
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- RN11: el número de día del itinerario no supera la duración del paquete
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_itinerario_dia
BEFORE INSERT OR UPDATE ON itinerario_dia
FOR EACH ROW
DECLARE
    v_duracion paquete.duracion_dias%TYPE;
BEGIN
    SELECT duracion_dias INTO v_duracion FROM paquete WHERE id_paquete = :NEW.id_paquete;
    IF :NEW.numero_dia > v_duracion THEN
        RAISE_APPLICATION_ERROR(-20011,
            'RN11: el paquete dura ' || v_duracion || ' días; no existe el día ' || :NEW.numero_dia || '.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;
END;
/


-- ---------------------------------------------------------------------
-- RN25: la fecha de servicio de cada línea es posterior a la cotización
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_detalle_fecha_servicio
BEFORE INSERT OR UPDATE OF fecha_servicio ON detalle_cotizacion
FOR EACH ROW
DECLARE
    v_fecha cotizacion.fecha%TYPE;
BEGIN
    SELECT fecha INTO v_fecha FROM cotizacion WHERE id_cotizacion = :NEW.id_cotizacion;
    IF :NEW.fecha_servicio <= v_fecha THEN
        RAISE_APPLICATION_ERROR(-20025,
            'RN25: la fecha de servicio debe ser posterior a la fecha de la cotización (' ||
            TO_CHAR(v_fecha, 'YYYY-MM-DD') || ').');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;
END;
/


-- ---------------------------------------------------------------------
-- RN10, RN14, RN18, RN27 y R2: validaciones de la reserva al crearla
-- (la fecha del servicio de una reserva es la fecha de inicio de su salida)
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_reserva_validar
BEFORE INSERT OR UPDATE OF id_plan, id_salida, id_cotizacion, id_cliente, fecha_creacion ON reserva
FOR EACH ROW
DECLARE
    v_plan_paquete   plan.id_paquete%TYPE;
    v_plan_estado    plan.estado%TYPE;
    v_vig_inicio     plan.fecha_inicio_vigencia%TYPE;
    v_vig_fin        plan.fecha_fin_vigencia%TYPE;
    v_salida_paquete salida.id_paquete%TYPE;
    v_salida_estado  salida.estado%TYPE;
    v_fecha_servicio salida.fecha_inicio%TYPE;
    v_cot_cliente    cotizacion.id_cliente%TYPE;
    v_cot_estado     cotizacion.estado%TYPE;
BEGIN
    -- Fecha de creación no futura
    IF :NEW.fecha_creacion > SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20019, 'La fecha de creación de la reserva no puede ser futura.');
    END IF;

    SELECT id_paquete, estado, fecha_inicio_vigencia, fecha_fin_vigencia
      INTO v_plan_paquete, v_plan_estado, v_vig_inicio, v_vig_fin
      FROM plan WHERE id_plan = :NEW.id_plan;

    SELECT id_paquete, estado, fecha_inicio
      INTO v_salida_paquete, v_salida_estado, v_fecha_servicio
      FROM salida WHERE id_salida = :NEW.id_salida;

    -- RN14: plan y salida del mismo paquete
    IF v_plan_paquete <> v_salida_paquete THEN
        RAISE_APPLICATION_ERROR(-20014,
            'RN14: el plan y la salida de la reserva deben pertenecer al mismo paquete.');
    END IF;

    -- RN10: el plan debe estar activo y vigente en la FECHA DEL SERVICIO
    -- (la fecha de la salida), como dice el enunciado
    IF v_plan_estado <> 'activo'
       OR v_fecha_servicio NOT BETWEEN v_vig_inicio AND v_vig_fin THEN
        RAISE_APPLICATION_ERROR(-20010,
            'RN10: el plan no está activo o no está vigente en la fecha del servicio (' ||
            TO_CHAR(v_fecha_servicio, 'YYYY-MM-DD') || ').');
    END IF;

    -- RN18: no se reserva en una salida cancelada o realizada
    IF INSERTING AND v_salida_estado IN ('cancelada', 'realizada') THEN
        RAISE_APPLICATION_ERROR(-20018,
            'RN18: no se pueden crear reservas en una salida ' || v_salida_estado || '.');
    END IF;

    -- RN27 y R2: si viene de una cotización, debe estar aceptada y ser del mismo cliente
    IF :NEW.id_cotizacion IS NOT NULL THEN
        SELECT id_cliente, estado INTO v_cot_cliente, v_cot_estado
          FROM cotizacion WHERE id_cotizacion = :NEW.id_cotizacion;

        IF v_cot_estado <> 'aceptada' THEN
            RAISE_APPLICATION_ERROR(-20027,
                'RN27: solo una cotización aceptada se convierte en reserva (está ' || v_cot_estado || ').');
        END IF;
        IF v_cot_cliente <> :NEW.id_cliente THEN
            RAISE_APPLICATION_ERROR(-20027,
                'RN27: la reserva debe ser del mismo cliente que pidió la cotización.');
        END IF;
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;  -- plan, salida o cotización inexistente: lo reporta la FK
END;
/


-- ---------------------------------------------------------------------
-- RN16: los pasajeros de las reservas no canceladas de una salida no
-- superan su cupo máximo.
-- Se valida a nivel de sentencia (no de fila) para poder leer
-- RESERVA_PASAJERO sin el error ORA-04091 de "tabla mutante".
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE validar_cupos AS
    v_salida NUMBER;
    v_usados NUMBER;
    v_cupo   NUMBER;
BEGIN
    SELECT id_salida, usados, cupo_maximo
      INTO v_salida, v_usados, v_cupo
      FROM (SELECT s.id_salida, COUNT(*) AS usados, s.cupo_maximo
              FROM salida s
              JOIN reserva r           ON r.id_salida  = s.id_salida
              JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
             WHERE r.estado <> 'cancelada'
             GROUP BY s.id_salida, s.cupo_maximo
            HAVING COUNT(*) > s.cupo_maximo)
     WHERE ROWNUM = 1;

    RAISE_APPLICATION_ERROR(-20016,
        'RN16: la salida ' || v_salida || ' quedaría con ' || v_usados ||
        ' pasajeros y su cupo máximo es ' || v_cupo || '.');
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;  -- ninguna salida se pasa del cupo
END;
/

CREATE OR REPLACE TRIGGER trg_rp_cupo
AFTER INSERT OR UPDATE ON reserva_pasajero
BEGIN
    validar_cupos;
END;
/

CREATE OR REPLACE TRIGGER trg_reserva_cupo
AFTER UPDATE OF estado, id_salida ON reserva
BEGIN
    validar_cupos;
END;
/

CREATE OR REPLACE TRIGGER trg_salida_cupo
AFTER UPDATE OF cupo_maximo ON salida
BEGIN
    validar_cupos;
END;
/


-- ---------------------------------------------------------------------
-- RN17: una salida solo pasa a "confirmada" si alcanza el mínimo de
-- viajeros de su paquete
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_salida_confirmar
BEFORE UPDATE OF estado ON salida
FOR EACH ROW
WHEN (NEW.estado = 'confirmada' AND OLD.estado <> 'confirmada')
DECLARE
    v_minimo  paquete.minimo_viajeros%TYPE;
    v_vendidos NUMBER;
BEGIN
    SELECT minimo_viajeros INTO v_minimo FROM paquete WHERE id_paquete = :NEW.id_paquete;

    SELECT COUNT(*) INTO v_vendidos
      FROM reserva r
      JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
     WHERE r.id_salida = :NEW.id_salida
       AND r.estado <> 'cancelada';

    IF v_vendidos < v_minimo THEN
        RAISE_APPLICATION_ERROR(-20017,
            'RN17: la salida tiene ' || v_vendidos || ' viajeros y el paquete exige mínimo ' ||
            v_minimo || ' para confirmarla.');
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- RN23 (parte 1): una reserva cancelada no recibe pagos nuevos;
-- fecha de pago no futura
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_pago_validar
BEFORE INSERT ON pago
FOR EACH ROW
DECLARE
    v_estado reserva.estado%TYPE;
BEGIN
    IF :NEW.fecha_pago > SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20020, 'La fecha del pago no puede ser futura.');
    END IF;

    SELECT estado INTO v_estado FROM reserva WHERE id_reserva = :NEW.id_reserva;
    IF v_estado = 'cancelada' THEN
        RAISE_APPLICATION_ERROR(-20023, 'RN23: la reserva está cancelada y no recibe pagos nuevos.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;
END;
/


-- ---------------------------------------------------------------------
-- RN21: la suma de los pagos exitosos de una reserva no supera su valor
-- total (a nivel de sentencia, por la misma razón que RN16)
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE validar_saldos AS
    v_reserva NUMBER;
    v_pagado  NUMBER;
    v_total   NUMBER;
BEGIN
    SELECT id_reserva, pagado, valor_total
      INTO v_reserva, v_pagado, v_total
      FROM (SELECT r.id_reserva, SUM(p.monto) AS pagado, r.valor_total
              FROM reserva r
              JOIN pago p ON p.id_reserva = r.id_reserva
             WHERE p.estado_pago = 'exitoso'
             GROUP BY r.id_reserva, r.valor_total
            HAVING SUM(p.monto) > r.valor_total)
     WHERE ROWNUM = 1;

    RAISE_APPLICATION_ERROR(-20021,
        'RN21: los pagos exitosos de la reserva ' || v_reserva || ' sumarían ' || v_pagado ||
        ' y su valor total es ' || v_total || '.');
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;
END;
/

CREATE OR REPLACE TRIGGER trg_pago_saldo
AFTER INSERT OR UPDATE OF monto, estado_pago, id_reserva ON pago
BEGIN
    validar_saldos;
END;
/

CREATE OR REPLACE TRIGGER trg_reserva_saldo
AFTER UPDATE OF valor_total ON reserva
BEGIN
    validar_saldos;
END;
/


-- ---------------------------------------------------------------------
-- RN22: solo los pagos con tarjeta o PSE son pagos en línea
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_pago_en_linea_metodo
BEFORE INSERT OR UPDATE OF id_pago ON pago_en_linea
FOR EACH ROW
DECLARE
    v_metodo pago.metodo_pago%TYPE;
BEGIN
    SELECT metodo_pago INTO v_metodo FROM pago WHERE id_pago = :NEW.id_pago;
    IF v_metodo NOT IN ('tarjeta de credito', 'tarjeta debito', 'pse') THEN
        RAISE_APPLICATION_ERROR(-20022,
            'RN22: un pago por ' || v_metodo || ' no es un pago en línea y no lleva referencia de transacción.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;
END;
/


-- ---------------------------------------------------------------------
-- RN06, RN07 y R3: reglas de las notificaciones
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_notificacion_validar
BEFORE INSERT OR UPDATE OF id_cliente, id_reserva, canal, tipo ON notificacion
FOR EACH ROW
DECLARE
    v_tiene_perfil NUMBER;
    v_promos       perfil_cliente.acepta_promociones%TYPE;
    v_cliente_res  reserva.id_cliente%TYPE;
BEGIN
    -- R3: la reserva asociada debe ser del mismo cliente
    IF :NEW.id_reserva IS NOT NULL THEN
        SELECT id_cliente INTO v_cliente_res FROM reserva WHERE id_reserva = :NEW.id_reserva;
        IF v_cliente_res <> :NEW.id_cliente THEN
            RAISE_APPLICATION_ERROR(-20031,
                'La notificación y su reserva deben ser del mismo cliente.');
        END IF;
    END IF;

    SELECT COUNT(*), MAX(acepta_promociones)
      INTO v_tiene_perfil, v_promos
      FROM perfil_cliente WHERE id_cliente = :NEW.id_cliente;

    -- RN06: un cliente sin perfil solo recibe notificaciones por correo
    IF v_tiene_perfil = 0 AND :NEW.canal <> 'correo' THEN
        RAISE_APPLICATION_ERROR(-20006,
            'RN06: el cliente no tiene cuenta; solo puede notificarse por correo.');
    END IF;

    -- RN07: promociones solo a quien las aceptó
    IF :NEW.tipo = 'promocion' AND NVL(v_promos, 'no') <> 'si' THEN
        RAISE_APPLICATION_ERROR(-20007,
            'RN07: el cliente no acepta promociones.');
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- RN28: una reserva nace pendiente o confirmada, solo pasa a completada si
-- estaba confirmada, y una reserva cancelada o completada ya no cambia de estado
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_reserva_estado
BEFORE INSERT OR UPDATE OF estado ON reserva
FOR EACH ROW
BEGIN
    IF INSERTING AND :NEW.estado NOT IN ('pendiente', 'confirmada') THEN
        RAISE_APPLICATION_ERROR(-20028,
            'RN28: una reserva nueva solo puede quedar pendiente o confirmada.');
    END IF;

    IF UPDATING AND :NEW.estado <> :OLD.estado THEN
        IF :OLD.estado IN ('cancelada', 'completada') THEN
            RAISE_APPLICATION_ERROR(-20028,
                'RN28: la reserva ya está ' || :OLD.estado || ' y no puede cambiar de estado.');
        END IF;
        IF :NEW.estado = 'completada' AND :OLD.estado <> 'confirmada' THEN
            RAISE_APPLICATION_ERROR(-20028,
                'RN28: una reserva solo pasa a completada si antes estuvo confirmada (está ' ||
                :OLD.estado || ').');
        END IF;
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- RN29: una reserva cancelada no se borra: queda como historial
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER trg_reserva_no_borrar
BEFORE DELETE ON reserva
FOR EACH ROW
BEGIN
    IF :OLD.estado = 'cancelada' THEN
        RAISE_APPLICATION_ERROR(-20029,
            'RN29: la reserva ' || :OLD.id_reserva || ' está cancelada y se conserva como historial.');
    END IF;
END;
/


-- ---------------------------------------------------------------------
-- RN30 (regla del dominio): un menor de edad no viaja sin al menos un adulto
-- en la misma reserva. La edad se mide en la fecha de la salida.
-- A nivel de sentencia (como RN16), para poder leer RESERVA_PASAJERO.
-- La aplicación debe guardar primero a los adultos de la reserva.
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE validar_menores AS
    v_reserva NUMBER;
BEGIN
    SELECT r.id_reserva INTO v_reserva
      FROM reserva r
      JOIN salida s ON s.id_salida = r.id_salida
     WHERE r.estado <> 'cancelada'
       AND EXISTS (SELECT 1
                     FROM reserva_pasajero rp JOIN pasajero p ON p.id_pasajero = rp.id_pasajero
                    WHERE rp.id_reserva = r.id_reserva
                      AND MONTHS_BETWEEN(s.fecha_inicio, p.fecha_nacimiento) < 216)
       AND NOT EXISTS (SELECT 1
                         FROM reserva_pasajero rp JOIN pasajero p ON p.id_pasajero = rp.id_pasajero
                        WHERE rp.id_reserva = r.id_reserva
                          AND MONTHS_BETWEEN(s.fecha_inicio, p.fecha_nacimiento) >= 216)
       AND ROWNUM = 1;

    RAISE_APPLICATION_ERROR(-20032,
        'RN30: la reserva ' || v_reserva || ' tiene menores de edad y ningún adulto. ' ||
        'Registra primero al adulto responsable.');
EXCEPTION
    WHEN NO_DATA_FOUND THEN NULL;
END;
/

CREATE OR REPLACE TRIGGER trg_rp_menores
AFTER INSERT OR UPDATE ON reserva_pasajero
BEGIN
    validar_menores;
END;
/


-- ---------------------------------------------------------------------
-- RN31 (regla de cancelación del núcleo): si el cliente cancela con más de
-- 5 días de anticipación a la fecha del servicio se le reembolsa el 80 % de
-- lo pagado; con 5 días o menos no hay reembolso.
-- Si la agencia cancela la salida (p_por_agencia = 'si') se devuelve el 100 %.
-- Uso:  EXEC cancelar_reserva(25);
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE cancelar_reserva (
    p_id_reserva  IN NUMBER,
    p_fecha       IN DATE     DEFAULT SYSDATE,
    p_por_agencia IN VARCHAR2 DEFAULT 'no'
) AS
    v_estado    reserva.estado%TYPE;
    v_cliente   reserva.id_cliente%TYPE;
    v_servicio  salida.fecha_inicio%TYPE;
    v_pagado    NUMBER;
    v_reembolso NUMBER;
    v_canal     perfil_cliente.canal_preferido%TYPE;
BEGIN
    SELECT r.estado, r.id_cliente, s.fecha_inicio
      INTO v_estado, v_cliente, v_servicio
      FROM reserva r JOIN salida s ON s.id_salida = r.id_salida
     WHERE r.id_reserva = p_id_reserva;

    IF v_estado IN ('cancelada', 'completada') THEN
        RAISE_APPLICATION_ERROR(-20033,
            'RN31: la reserva ' || p_id_reserva || ' ya está ' || v_estado || '.');
    END IF;

    SELECT NVL(SUM(monto), 0) INTO v_pagado
      FROM pago WHERE id_reserva = p_id_reserva AND estado_pago = 'exitoso';

    IF p_por_agencia = 'si' THEN
        v_reembolso := v_pagado;
    ELSIF TRUNC(v_servicio) - TRUNC(p_fecha) > 5 THEN
        v_reembolso := ROUND(v_pagado * 0.8);
    ELSE
        v_reembolso := 0;
    END IF;

    UPDATE reserva
       SET estado = 'cancelada', fecha_cancelacion = p_fecha, valor_reembolso = v_reembolso
     WHERE id_reserva = p_id_reserva;

    -- Aviso al cliente por su canal preferido (correo si no tiene cuenta, RN06)
    SELECT NVL(MAX(canal_preferido), 'correo') INTO v_canal
      FROM perfil_cliente WHERE id_cliente = v_cliente;

    INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, fecha_envio, estado, id_cliente, id_reserva)
    VALUES ('cambio de estado', v_canal,
            'Tu reserva ' || p_id_reserva || ' fue cancelada. Reembolso: $' ||
            TRIM(TO_CHAR(v_reembolso, '999G999G999', 'NLS_NUMERIC_CHARACTERS='',.''')),
            CAST(p_fecha AS TIMESTAMP), CAST(p_fecha AS TIMESTAMP), 'enviada', v_cliente, p_id_reserva);
END;
/


-- ---------------------------------------------------------------------
-- RN32: los recordatorios se programan según el perfil del cliente:
-- N días antes de la salida (1, 3 o 7) a las 8:00, por su canal preferido.
-- Sin cuenta: 3 días antes y por correo (RN06).
-- Crea el recordatorio de cada reserva activa con salida futura que aún no lo tenga.
-- Uso desde Java:  CALL generar_recordatorios(?)   (devuelve cuántos creó)
-- ---------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE generar_recordatorios (p_creados OUT NUMBER) AS
BEGIN
    INSERT INTO notificacion (tipo, canal, mensaje, fecha_programada, estado, id_cliente, id_reserva)
    SELECT 'recordatorio de reserva',
           NVL(pc.canal_preferido, 'correo'),
           'Hola ' || c.nombres || ', tu viaje "' || p.nombre || '" sale el ' ||
           TO_CHAR(s.fecha_inicio, 'DD/MM/YYYY') || '. ¡Te esperamos!',
           CAST(s.fecha_inicio - NVL(pc.dias_anticipacion, 3) AS TIMESTAMP) + INTERVAL '8' HOUR,
           'pendiente',
           r.id_cliente,
           r.id_reserva
      FROM reserva r
      JOIN cliente c  ON c.id_cliente  = r.id_cliente
      JOIN salida s   ON s.id_salida   = r.id_salida
      JOIN paquete p  ON p.id_paquete  = s.id_paquete
      LEFT JOIN perfil_cliente pc ON pc.id_cliente = r.id_cliente
     WHERE r.estado IN ('pendiente', 'confirmada')
       AND s.fecha_inicio > TRUNC(SYSDATE)
       AND NOT EXISTS (SELECT 1 FROM notificacion n
                        WHERE n.id_reserva = r.id_reserva
                          AND n.tipo = 'recordatorio de reserva');
    p_creados := SQL%ROWCOUNT;
END;
/


-- ---------------------------------------------------------------------
-- Temporada de una fecha (para el reporte 4):
--   alta : Semana Santa, 15/06 a 31/07 y 15/12 a 15/01
--   media: resto de junio, agosto, octubre, noviembre y resto de diciembre/enero
--   baja : febrero, marzo, abril, mayo y septiembre (fuera de Semana Santa)
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION temporada (p_fecha IN DATE) RETURN VARCHAR2 DETERMINISTIC AS
    v_mmdd VARCHAR2(4) := TO_CHAR(p_fecha, 'MMDD');
BEGIN
    IF p_fecha BETWEEN DATE '2025-04-13' AND DATE '2025-04-20'     -- Semana Santa 2025
       OR p_fecha BETWEEN DATE '2026-03-29' AND DATE '2026-04-05'  -- Semana Santa 2026
       OR v_mmdd BETWEEN '0615' AND '0731'
       OR v_mmdd >= '1215' OR v_mmdd <= '0115' THEN
        RETURN 'alta';
    ELSIF SUBSTR(v_mmdd, 1, 2) IN ('01', '06', '08', '10', '11', '12') THEN
        RETURN 'media';
    ELSE
        RETURN 'baja';
    END IF;
END;
/


-- Verificación: lista los triggers y si quedaron válidos
SELECT trigger_name, table_name, status FROM user_triggers ORDER BY table_name, trigger_name;
SELECT object_name, object_type, status FROM user_objects WHERE status <> 'VALID';
