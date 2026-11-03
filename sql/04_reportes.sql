-- =====================================================================
-- QuindioStay - 04_reportes.sql
-- Las 10 consultas de los reportes (3 simples, 4 intermedias, 3 avanzadas).
-- Se prueban aquí y luego se copian a Jasper (cada una es un .jrxml).
-- Para probar una sola: cursor dentro de la consulta y Cmd + Enter.
-- Regla RN13: los paquetes privados no aparecen en reportes de paquetes.
-- =====================================================================


-- =====================================================================
-- R1 (simple) ¿Cuántas salidas hay en cada estado?
-- =====================================================================
select estado,
       count(*) as cantidad
  from salida
 group by estado
 order by cantidad desc;


-- =====================================================================
-- R2 (simple) ¿Qué planes están activos y vigentes hoy, de menor a mayor precio?
-- =====================================================================
select p.nombre as paquete,
       pl.nombre as plan,
       pl.tipo_plan,
       pl.precio,
       pl.fecha_fin_vigencia as vigente_hasta
  from plan pl
  join paquete p
on p.id_paquete = pl.id_paquete
 where pl.estado = 'activo'
   and trunc(sysdate) between pl.fecha_inicio_vigencia and pl.fecha_fin_vigencia
   and p.visibilidad = 'publico'
 order by pl.precio asc;


-- =====================================================================
-- R3 (simple) ¿Qué porcentaje de las cotizaciones está en cada estado?
-- =====================================================================
select estado,
       count(*) as cantidad,
       round(
          100 * count(*) / sum(count(*))
                           over(),
          1
       ) as porcentaje
  from cotizacion
 group by estado
 order by cantidad desc;


-- =====================================================================
-- R4 (intermedio) ¿Cuáles son los paquetes más vendidos en cada mes?
-- Ventas = pasajeros de reservas no canceladas, por mes de la reserva.
-- puesto = 1 marca el más vendido del mes (gráfico de barras).
-- =====================================================================
select mes,
       paquete,
       pasajeros,
       reservas,
       rank()
       over(partition by mes
            order by pasajeros desc
       ) as puesto
  from (
   select to_char(
      r.fecha_creacion,
      'YYYY-MM'
   ) as mes,
          p.nombre as paquete,
          count(rp.id_pasajero) as pasajeros,
          count(distinct r.id_reserva) as reservas
     from reserva r
     join salida s
   on s.id_salida = r.id_salida
     join paquete p
   on p.id_paquete = s.id_paquete
     join reserva_pasajero rp
   on rp.id_reserva = r.id_reserva
    where r.estado <> 'cancelada'
      and p.visibilidad = 'publico'
    group by to_char(
      r.fecha_creacion,
      'YYYY-MM'
   ),
             p.nombre
)
 order by mes,
          puesto;


-- =====================================================================
-- R5 (intermedio) ¿Qué porcentaje del cupo lleva vendido cada salida programada?
-- (gráfico de barras; en Jasper se agregan filtros de fechas y paquete)
-- =====================================================================
select s.id_salida,
       p.nombre as paquete,
       s.fecha_inicio,
       count(rp.id_pasajero) as vendidos,
       s.cupo_maximo,
       round(100 * count(rp.id_pasajero) / s.cupo_maximo) as porcentaje,
       p.minimo_viajeros,
       case
          when count(rp.id_pasajero) >= p.minimo_viajeros then
             'alcanza el minimo'
          else
             'bajo el minimo'
       end as situacion
  from salida s
  join paquete p
on p.id_paquete = s.id_paquete
  left join reserva r
on r.id_salida = s.id_salida
   and r.estado <> 'cancelada'
  left join reserva_pasajero rp
on rp.id_reserva = r.id_reserva
 where s.estado = 'programada'
   and p.visibilidad = 'publico'
 group by s.id_salida,
          p.nombre,
          s.fecha_inicio,
          s.cupo_maximo,
          p.minimo_viajeros
 order by s.fecha_inicio;


-- =====================================================================
-- R6 (intermedio) ¿Qué prestadores aportan componentes a 3 o más paquetes?
-- =====================================================================
select pr.nombre_comercial as prestador,
       tp.nombre as tipo_prestador,
       count(distinct c.id_componente) as componentes,
       count(distinct pc.id_paquete) as paquetes
  from prestador pr
  join tipo_prestador tp
on tp.id_tipo_prestador = pr.id_tipo_prestador
  join componente c
on c.id_prestador = pr.id_prestador
  join paquete_componente pc
on pc.id_componente = c.id_componente
 group by pr.nombre_comercial,
          tp.nombre
having count(distinct pc.id_paquete) >= 3
 order by paquetes desc,
          prestador;


-- =====================================================================
-- R7 (intermedio) ¿Cuánto se recauda mes a mes por canal de venta?
-- Solo pagos exitosos (gráfico de líneas: una línea por canal).
-- =====================================================================
select to_char(
   pg.fecha_pago,
   'YYYY-MM'
) as mes,
       r.canal,
       sum(pg.monto) as recaudo,
       count(*) as pagos
  from pago pg
  join reserva r
on r.id_reserva = pg.id_reserva
 where pg.estado_pago = 'exitoso'
 group by to_char(
   pg.fecha_pago,
   'YYYY-MM'
),
          r.canal
 order by mes,
          r.canal;


-- =====================================================================
-- R8 (avanzado) ¿Qué paquetes públicos nunca se han vendido?
-- =====================================================================
select p.id_paquete,
       p.nombre,
       p.categoria,
       d.nombre as destino
  from paquete p
  join destino d
on d.id_destino = p.id_destino
 where p.visibilidad = 'publico'
   and not exists (
   select 1
     from salida s
     join reserva r
   on r.id_salida = s.id_salida
    where s.id_paquete = p.id_paquete
      and r.estado <> 'cancelada'
)
 order by p.nombre;


-- =====================================================================
-- R9 (avanzado) ¿Qué salidas programadas todavía no alcanzan el mínimo
-- de viajeros de su paquete?
-- =====================================================================
select s.id_salida,
       p.nombre as paquete,
       s.fecha_inicio,
       p.minimo_viajeros,
       (
          select count(*)
            from reserva r
            join reserva_pasajero rp
          on rp.id_reserva = r.id_reserva
           where r.id_salida = s.id_salida
             and r.estado <> 'cancelada'
       ) as vendidos
  from salida s
  join paquete p
on p.id_paquete = s.id_paquete
 where s.estado = 'programada'
   and p.visibilidad = 'publico'
   and (
   select count(*)
     from reserva r
     join reserva_pasajero rp
   on rp.id_reserva = r.id_reserva
    where r.id_salida = s.id_salida
      and r.estado <> 'cancelada'
) < p.minimo_viajeros
 order by s.fecha_inicio;


-- =====================================================================
-- R10 (avanzado) ¿Qué clientes tienen saldo pendiente y su salida es en
-- los próximos 15 días?
-- =====================================================================
select c.nombres
       || ' '
       || c.apellidos as cliente,
       c.telefono,
       c.correo,
       p.nombre as paquete,
       s.fecha_inicio as fecha_salida,
       r.valor_total,
       r.valor_total - nvl(
          (
             select sum(pg.monto)
               from pago pg
              where pg.id_reserva = r.id_reserva
                and pg.estado_pago = 'exitoso'
          ),
          0
       ) as saldo_pendiente
  from reserva r
  join cliente c
on c.id_cliente = r.id_cliente
  join salida s
on s.id_salida = r.id_salida
  join paquete p
on p.id_paquete = s.id_paquete
 where r.estado <> 'cancelada'
   and s.fecha_inicio between trunc(sysdate) and trunc(sysdate) + 15
   and r.valor_total > nvl(
   (
      select sum(pg.monto)
        from pago pg
       where pg.id_reserva = r.id_reserva
         and pg.estado_pago = 'exitoso'
   ),
   0
)
 order by s.fecha_inicio,
          saldo_pendiente desc;