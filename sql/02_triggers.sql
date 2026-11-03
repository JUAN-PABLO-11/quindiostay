-- =====================================================================
-- QuindioStay - 02_triggers.sql
-- Reglas de negocio que un CHECK no puede verificar porque miran otras
-- tablas o la fecha actual. Correr como QUINDIOSTAY después de 01_ddl.sql
-- (con "Run Script" / F5).
--
-- Cada error usa RAISE_APPLICATION_ERROR con un código -200xx y el número
-- de la regla (RNxx) en el mensaje, para que la aplicación lo muestre tal
-- cual en la barra inferior de la ventana.
--
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
create or replace procedure validar_prestador_activo (
   p_id_prestador in number
) as
   v_estado prestador.estado%type;
begin
   select estado
     into v_estado
     from prestador
    where id_prestador = p_id_prestador;
   if v_estado <> 'activo' then
      raise_application_error(
         -20002,
         'RN02: el prestador '
         || p_id_prestador
         || ' está '
         || v_estado
         || ' y no puede crear paquetes, componentes ni cotizaciones.'
      );
   end if;
exception
   when no_data_found then
      null;  -- la FK se encarga de reportar el prestador inexistente
end;
/

create or replace trigger trg_paquete_prestador_activo before
   insert or update of id_prestador on paquete
   for each row
begin
   validar_prestador_activo(:new.id_prestador);
end;
/

create or replace trigger trg_componente_prestador_activo before
   insert or update of id_prestador on componente
   for each row
begin
   validar_prestador_activo(:new.id_prestador);
end;
/

create or replace trigger trg_cotizacion_validar before
   insert or update on cotizacion
   for each row
begin
   if inserting
   or updating('ID_PRESTADOR') then
      validar_prestador_activo(:new.id_prestador);
   end if;

    -- Fecha de emisión no futura
   if :new.fecha > trunc(sysdate) then
      raise_application_error(
         -20025,
         'La fecha de la cotización no puede ser futura.'
      );
   end if;

    -- RN26: una cotización vencida (más de 8 días) ya no puede aceptarse
   if
      updating('ESTADO')
      and :new.estado = 'aceptada'
      and :old.estado <> 'aceptada'
   then
      if trunc(sysdate) > :new.fecha + 8 then
         raise_application_error(
            -20026,
            'RN26: la cotización '
            || :new.numero_cotizacion
            || ' venció el '
            || to_char(
               :new.fecha + 8,
               'YYYY-MM-DD'
            )
            || ' y ya no se puede aceptar.'
         );
      end if;
   end if;
end;
/


-- ---------------------------------------------------------------------
-- RN03: el usuario con rol "encargado de prestador" debe estar ligado a un
-- prestador; los demás roles no. (Si se borra el prestador, ON DELETE
-- SET NULL deja al usuario desvinculado: por eso no se valida al quedar NULL.)
-- ---------------------------------------------------------------------
create or replace trigger trg_usuario_rol before
   insert or update of id_rol,id_prestador on usuario_sistema
   for each row
declare
   v_rol rol.nombre%type;
begin
   select nombre
     into v_rol
     from rol
    where id_rol = :new.id_rol;

   if
      v_rol = 'encargado de prestador'
      and :new.id_prestador is null
      and ( inserting
      or updating('ID_ROL') )
   then
      raise_application_error(
         -20003,
         'RN03: un encargado de prestador debe estar ligado a un prestador.'
      );
   end if;

   if
      v_rol <> 'encargado de prestador'
      and :new.id_prestador is not null
   then
      raise_application_error(
         -20003,
         'RN03: solo el rol "encargado de prestador" puede estar ligado a un prestador.'
      );
   end if;
exception
   when no_data_found then
      null;  -- rol inexistente: lo reporta la FK
end;
/


-- ---------------------------------------------------------------------
-- Fechas de nacimiento no futuras (SYSDATE no se permite en un CHECK)
-- ---------------------------------------------------------------------
create or replace trigger trg_cliente_nacimiento before
   insert or update of fecha_nacimiento on cliente
   for each row
begin
   if :new.fecha_nacimiento > trunc(sysdate) then
      raise_application_error(
         -20030,
         'La fecha de nacimiento del cliente no puede ser futura.'
      );
   end if;
end;
/

create or replace trigger trg_pasajero_nacimiento before
   insert or update of fecha_nacimiento on pasajero
   for each row
begin
   if :new.fecha_nacimiento > trunc(sysdate) then
      raise_application_error(
         -20030,
         'La fecha de nacimiento del pasajero no puede ser futura.'
      );
   end if;
end;
/


-- ---------------------------------------------------------------------
-- RN11: el número de día del itinerario no supera la duración del paquete
-- ---------------------------------------------------------------------
create or replace trigger trg_itinerario_dia before
   insert or update on itinerario_dia
   for each row
declare
   v_duracion paquete.duracion_dias%type;
begin
   select duracion_dias
     into v_duracion
     from paquete
    where id_paquete = :new.id_paquete;
   if :new.numero_dia > v_duracion then
      raise_application_error(
         -20011,
         'RN11: el paquete dura '
         || v_duracion
         || ' días; no existe el día '
         || :new.numero_dia
         || '.'
      );
   end if;
exception
   when no_data_found then
      null;
end;
/


-- ---------------------------------------------------------------------
-- RN25: la fecha de servicio de cada línea es posterior a la cotización
-- ---------------------------------------------------------------------
create or replace trigger trg_detalle_fecha_servicio before
   insert or update of fecha_servicio on detalle_cotizacion
   for each row
declare
   v_fecha cotizacion.fecha%type;
begin
   select fecha
     into v_fecha
     from cotizacion
    where id_cotizacion = :new.id_cotizacion;
   if :new.fecha_servicio <= v_fecha then
      raise_application_error(
         -20025,
         'RN25: la fecha de servicio debe ser posterior a la fecha de la cotización ('
         || to_char(
            v_fecha,
            'YYYY-MM-DD'
         )
         || ').'
      );
   end if;
exception
   when no_data_found then
      null;
end;
/


-- ---------------------------------------------------------------------
-- RN10, RN14, RN18, RN27 y R2: validaciones de la reserva al crearla
-- ---------------------------------------------------------------------
create or replace trigger trg_reserva_validar before
   insert or update of id_plan,id_salida,id_cotizacion,id_cliente,fecha_creacion on reserva
   for each row
declare
   v_plan_paquete   plan.id_paquete%type;
   v_plan_estado    plan.estado%type;
   v_vig_inicio     plan.fecha_inicio_vigencia%type;
   v_vig_fin        plan.fecha_fin_vigencia%type;
   v_salida_paquete salida.id_paquete%type;
   v_salida_estado  salida.estado%type;
   v_cot_cliente    cotizacion.id_cliente%type;
   v_cot_estado     cotizacion.estado%type;
begin
    -- Fecha de creación no futura
   if :new.fecha_creacion > sysdate then
      raise_application_error(
         -20019,
         'La fecha de creación de la reserva no puede ser futura.'
      );
   end if;

   select id_paquete,
          estado,
          fecha_inicio_vigencia,
          fecha_fin_vigencia
     into
      v_plan_paquete,
      v_plan_estado,
      v_vig_inicio,
      v_vig_fin
     from plan
    where id_plan = :new.id_plan;

   select id_paquete,
          estado
     into
      v_salida_paquete,
      v_salida_estado
     from salida
    where id_salida = :new.id_salida;

    -- RN14: plan y salida del mismo paquete
   if v_plan_paquete <> v_salida_paquete then
      raise_application_error(
         -20014,
         'RN14: el plan y la salida de la reserva deben pertenecer al mismo paquete.'
      );
   end if;

    -- RN10: plan activo y vigente el día en que se crea la reserva
   if v_plan_estado <> 'activo'
   or trunc(:new.fecha_creacion) not between v_vig_inicio and v_vig_fin then
      raise_application_error(
         -20010,
         'RN10: el plan no está activo o no estaba vigente el '
         || to_char(
            :new.fecha_creacion,
            'YYYY-MM-DD'
         )
         || '.'
      );
   end if;

    -- RN18: no se reserva en una salida cancelada o realizada
   if
      inserting
      and v_salida_estado in ( 'cancelada',
                               'realizada' )
   then
      raise_application_error(
         -20018,
         'RN18: no se pueden crear reservas en una salida '
         || v_salida_estado
         || '.'
      );
   end if;

    -- RN27 y R2: si viene de una cotización, debe estar aceptada y ser del mismo cliente
   if :new.id_cotizacion is not null then
      select id_cliente,
             estado
        into
         v_cot_cliente,
         v_cot_estado
        from cotizacion
       where id_cotizacion = :new.id_cotizacion;

      if v_cot_estado <> 'aceptada' then
         raise_application_error(
            -20027,
            'RN27: solo una cotización aceptada se convierte en reserva (está '
            || v_cot_estado
            || ').'
         );
      end if;
      if v_cot_cliente <> :new.id_cliente then
         raise_application_error(
            -20027,
            'RN27: la reserva debe ser del mismo cliente que pidió la cotización.'
         );
      end if;
   end if;
exception
   when no_data_found then
      null;  -- plan, salida o cotización inexistente: lo reporta la FK
end;
/


-- ---------------------------------------------------------------------
-- RN16: los pasajeros de las reservas no canceladas de una salida no
-- superan su cupo máximo.
-- Se valida a nivel de sentencia (no de fila) para poder leer
-- RESERVA_PASAJERO sin el error ORA-04091 de "tabla mutante".
-- ---------------------------------------------------------------------
create or replace procedure validar_cupos as
   v_salida number;
   v_usados number;
   v_cupo   number;
begin
   select id_salida,
          usados,
          cupo_maximo
     into
      v_salida,
      v_usados,
      v_cupo
     from (
      select s.id_salida,
             count(*) as usados,
             s.cupo_maximo
        from salida s
        join reserva r
      on r.id_salida = s.id_salida
        join reserva_pasajero rp
      on rp.id_reserva = r.id_reserva
       where r.estado <> 'cancelada'
       group by s.id_salida,
                s.cupo_maximo
      having count(*) > s.cupo_maximo
   )
    where rownum = 1;

   raise_application_error(
      -20016,
      'RN16: la salida '
      || v_salida
      || ' quedaría con '
      || v_usados
      || ' pasajeros y su cupo máximo es '
      || v_cupo
      || '.'
   );
exception
   when no_data_found then
      null;  -- ninguna salida se pasa del cupo
end;
/

create or replace trigger trg_rp_cupo after
   insert or update on reserva_pasajero
begin
   validar_cupos;
end;
/

create or replace trigger trg_reserva_cupo after
   update of estado,id_salida on reserva
begin
   validar_cupos;
end;
/

create or replace trigger trg_salida_cupo after
   update of cupo_maximo on salida
begin
   validar_cupos;
end;
/


-- ---------------------------------------------------------------------
-- RN17: una salida solo pasa a "confirmada" si alcanza el mínimo de
-- viajeros de su paquete
-- ---------------------------------------------------------------------
create or replace trigger trg_salida_confirmar before
   update of estado on salida
   for each row
   when ( new.estado = 'confirmada'
      and old.estado <> 'confirmada' )
declare
   v_minimo   paquete.minimo_viajeros%type;
   v_vendidos number;
begin
   select minimo_viajeros
     into v_minimo
     from paquete
    where id_paquete = :new.id_paquete;

   select count(*)
     into v_vendidos
     from reserva r
     join reserva_pasajero rp
   on rp.id_reserva = r.id_reserva
    where r.id_salida = :new.id_salida
      and r.estado <> 'cancelada';

   if v_vendidos < v_minimo then
      raise_application_error(
         -20017,
         'RN17: la salida tiene '
         || v_vendidos
         || ' viajeros y el paquete exige mínimo '
         || v_minimo
         || ' para confirmarla.'
      );
   end if;
end;
/


-- ---------------------------------------------------------------------
-- RN23 (parte 1): una reserva cancelada no recibe pagos nuevos;
-- fecha de pago no futura
-- ---------------------------------------------------------------------
create or replace trigger trg_pago_validar before
   insert on pago
   for each row
declare
   v_estado reserva.estado%type;
begin
   if :new.fecha_pago > sysdate then
      raise_application_error(
         -20020,
         'La fecha del pago no puede ser futura.'
      );
   end if;

   select estado
     into v_estado
     from reserva
    where id_reserva = :new.id_reserva;
   if v_estado = 'cancelada' then
      raise_application_error(
         -20023,
         'RN23: la reserva está cancelada y no recibe pagos nuevos.'
      );
   end if;
exception
   when no_data_found then
      null;
end;
/


-- ---------------------------------------------------------------------
-- RN21: la suma de los pagos exitosos de una reserva no supera su valor
-- total (a nivel de sentencia, por la misma razón que RN16)
-- ---------------------------------------------------------------------
create or replace procedure validar_saldos as
   v_reserva number;
   v_pagado  number;
   v_total   number;
begin
   select id_reserva,
          pagado,
          valor_total
     into
      v_reserva,
      v_pagado,
      v_total
     from (
      select r.id_reserva,
             sum(p.monto) as pagado,
             r.valor_total
        from reserva r
        join pago p
      on p.id_reserva = r.id_reserva
       where p.estado_pago = 'exitoso'
       group by r.id_reserva,
                r.valor_total
      having sum(p.monto) > r.valor_total
   )
    where rownum = 1;

   raise_application_error(
      -20021,
      'RN21: los pagos exitosos de la reserva '
      || v_reserva
      || ' sumarían '
      || v_pagado
      || ' y su valor total es '
      || v_total
      || '.'
   );
exception
   when no_data_found then
      null;
end;
/

create or replace trigger trg_pago_saldo after
   insert or update of monto,estado_pago,id_reserva on pago
begin
   validar_saldos;
end;
/

create or replace trigger trg_reserva_saldo after
   update of valor_total on reserva
begin
   validar_saldos;
end;
/


-- ---------------------------------------------------------------------
-- RN22: solo los pagos con tarjeta o PSE son pagos en línea
-- ---------------------------------------------------------------------
create or replace trigger trg_pago_en_linea_metodo before
   insert or update of id_pago on pago_en_linea
   for each row
declare
   v_metodo pago.metodo_pago%type;
begin
   select metodo_pago
     into v_metodo
     from pago
    where id_pago = :new.id_pago;
   if v_metodo not in ( 'tarjeta de credito',
                        'tarjeta debito',
                        'pse' ) then
      raise_application_error(
         -20022,
         'RN22: un pago por '
         || v_metodo
         || ' no es un pago en línea y no lleva referencia de transacción.'
      );
   end if;
exception
   when no_data_found then
      null;
end;
/


-- ---------------------------------------------------------------------
-- RN06, RN07 y R3: reglas de las notificaciones
-- ---------------------------------------------------------------------
create or replace trigger trg_notificacion_validar before
   insert or update of id_cliente,id_reserva,canal,tipo on notificacion
   for each row
declare
   v_tiene_perfil number;
   v_promos       perfil_cliente.acepta_promociones%type;
   v_cliente_res  reserva.id_cliente%type;
begin
    -- R3: la reserva asociada debe ser del mismo cliente
   if :new.id_reserva is not null then
      select id_cliente
        into v_cliente_res
        from reserva
       where id_reserva = :new.id_reserva;
      if v_cliente_res <> :new.id_cliente then
         raise_application_error(
            -20031,
            'La notificación y su reserva deben ser del mismo cliente.'
         );
      end if;
   end if;

   select count(*),
          max(acepta_promociones)
     into
      v_tiene_perfil,
      v_promos
     from perfil_cliente
    where id_cliente = :new.id_cliente;

    -- RN06: un cliente sin perfil solo recibe notificaciones por correo
   if
      v_tiene_perfil = 0
      and :new.canal <> 'correo'
   then
      raise_application_error(
         -20006,
         'RN06: el cliente no tiene cuenta; solo puede notificarse por correo.'
      );
   end if;

    -- RN07: promociones solo a quien las aceptó
   if
      :new.tipo = 'promocion'
      and nvl(
         v_promos,
         'no'
      ) <> 'si'
   then
      raise_application_error(
         -20007,
         'RN07: el cliente no acepta promociones.'
      );
   end if;
end;
/


-- Verificación: lista los triggers y si quedaron válidos
select trigger_name,
       table_name,
       status
  from user_triggers
 order by table_name,
          trigger_name;
select object_name,
       object_type,
       status
  from user_objects
 where status <> 'VALID';