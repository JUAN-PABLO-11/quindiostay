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
SELECT estado,
       COUNT(*) AS cantidad
  FROM salida
 GROUP BY estado
 ORDER BY cantidad DESC;


-- =====================================================================
-- R2 (simple) ¿Qué planes están activos y vigentes hoy, de menor a mayor precio?
-- Una sola tabla (PLAN), como exige el nivel simple.
-- =====================================================================
SELECT nombre,
       tipo_plan,
       precio,
       fecha_inicio_vigencia AS vigente_desde,
       fecha_fin_vigencia    AS vigente_hasta
  FROM plan
 WHERE estado = 'activo'
   AND TRUNC(SYSDATE) BETWEEN fecha_inicio_vigencia AND fecha_fin_vigencia
 ORDER BY precio ASC;


-- =====================================================================
-- R3 (simple) ¿Qué porcentaje de las cotizaciones está en cada estado?
-- =====================================================================
SELECT estado,
       COUNT(*) AS cantidad,
       ROUND(100 * COUNT(*) / SUM(COUNT(*)) OVER (), 1) AS porcentaje
  FROM cotizacion
 GROUP BY estado
 ORDER BY cantidad DESC;


-- =====================================================================
-- R4 (intermedio) ¿Cuáles son los paquetes más vendidos en cada temporada?
-- Temporada (alta, media, baja) de la fecha del servicio, con la función
-- temporada() de 02_triggers.sql. Ventas = pasajeros de reservas no canceladas.
-- puesto = 1 marca el más vendido de la temporada (gráfico de barras).
-- =====================================================================
SELECT temporada,
       paquete,
       pasajeros,
       reservas,
       RANK() OVER (PARTITION BY temporada ORDER BY pasajeros DESC) AS puesto
  FROM (SELECT temporada(s.fecha_inicio)      AS temporada,
               p.nombre                       AS paquete,
               COUNT(rp.id_pasajero)          AS pasajeros,
               COUNT(DISTINCT r.id_reserva)   AS reservas
          FROM reserva r
          JOIN salida s            ON s.id_salida   = r.id_salida
          JOIN paquete p           ON p.id_paquete  = s.id_paquete
          JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
         WHERE r.estado <> 'cancelada'
           AND p.visibilidad = 'publico'
         GROUP BY temporada(s.fecha_inicio), p.nombre)
 ORDER BY CASE temporada WHEN 'alta' THEN 1 WHEN 'media' THEN 2 ELSE 3 END, puesto;


-- =====================================================================
-- R5 (intermedio) ¿Qué porcentaje del cupo lleva vendido cada salida programada?
-- (gráfico de barras; en Jasper se agregan filtros de fechas y paquete)
-- =====================================================================
SELECT s.id_salida,
       p.nombre                      AS paquete,
       s.fecha_inicio,
       COUNT(rp.id_pasajero)         AS vendidos,
       s.cupo_maximo,
       ROUND(100 * COUNT(rp.id_pasajero) / s.cupo_maximo) AS porcentaje,
       p.minimo_viajeros,
       CASE WHEN COUNT(rp.id_pasajero) >= p.minimo_viajeros
            THEN 'alcanza el minimo' ELSE 'bajo el minimo' END AS situacion
  FROM salida s
  JOIN paquete p ON p.id_paquete = s.id_paquete
  LEFT JOIN reserva r
         ON r.id_salida = s.id_salida AND r.estado <> 'cancelada'
  LEFT JOIN reserva_pasajero rp
         ON rp.id_reserva = r.id_reserva
 WHERE s.estado = 'programada'
   AND p.visibilidad = 'publico'
 GROUP BY s.id_salida, p.nombre, s.fecha_inicio, s.cupo_maximo, p.minimo_viajeros
 ORDER BY s.fecha_inicio;


-- =====================================================================
-- R6 (intermedio) ¿Qué prestadores aportan componentes a 3 o más paquetes?
-- =====================================================================
SELECT pr.nombre_comercial              AS prestador,
       tp.nombre                        AS tipo_prestador,
       COUNT(DISTINCT c.id_componente)  AS componentes,
       COUNT(DISTINCT pc.id_paquete)    AS paquetes
  FROM prestador pr
  JOIN tipo_prestador tp     ON tp.id_tipo_prestador = pr.id_tipo_prestador
  JOIN componente c          ON c.id_prestador       = pr.id_prestador
  JOIN paquete_componente pc ON pc.id_componente     = c.id_componente
 GROUP BY pr.nombre_comercial, tp.nombre
HAVING COUNT(DISTINCT pc.id_paquete) >= 3
 ORDER BY paquetes DESC, prestador;


-- =====================================================================
-- R7 (intermedio) ¿Cuánto se recauda mes a mes por canal de venta?
-- Solo pagos exitosos (gráfico de líneas: una línea por canal).
-- =====================================================================
SELECT TO_CHAR(pg.fecha_pago, 'YYYY-MM') AS mes,
       r.canal,
       SUM(pg.monto)                     AS recaudo,
       COUNT(*)                          AS pagos
  FROM pago pg
  JOIN reserva r ON r.id_reserva = pg.id_reserva
 WHERE pg.estado_pago = 'exitoso'
 GROUP BY TO_CHAR(pg.fecha_pago, 'YYYY-MM'), r.canal
 ORDER BY mes, r.canal;


-- =====================================================================
-- R8 (avanzado) ¿Qué paquetes públicos nunca se han vendido?
-- =====================================================================
SELECT p.id_paquete,
       p.nombre,
       p.categoria,
       d.nombre AS destino
  FROM paquete p
  JOIN destino d ON d.id_destino = p.id_destino
 WHERE p.visibilidad = 'publico'
   AND NOT EXISTS (SELECT 1
                     FROM salida s
                     JOIN reserva r ON r.id_salida = s.id_salida
                    WHERE s.id_paquete = p.id_paquete
                      AND r.estado <> 'cancelada')
 ORDER BY p.nombre;


-- =====================================================================
-- R9 (avanzado) ¿Qué salidas programadas todavía no alcanzan el mínimo
-- de viajeros de su paquete?
-- =====================================================================
SELECT s.id_salida,
       p.nombre          AS paquete,
       s.fecha_inicio,
       p.minimo_viajeros,
       (SELECT COUNT(*)
          FROM reserva r
          JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
         WHERE r.id_salida = s.id_salida
           AND r.estado <> 'cancelada') AS vendidos
  FROM salida s
  JOIN paquete p ON p.id_paquete = s.id_paquete
 WHERE s.estado = 'programada'
   AND p.visibilidad = 'publico'
   AND (SELECT COUNT(*)
          FROM reserva r
          JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
         WHERE r.id_salida = s.id_salida
           AND r.estado <> 'cancelada') < p.minimo_viajeros
 ORDER BY s.fecha_inicio;


-- =====================================================================
-- R10 (avanzado) ¿Qué clientes tienen saldo pendiente y su salida es en
-- los próximos 15 días?
-- =====================================================================
SELECT c.nombres || ' ' || c.apellidos AS cliente,
       c.telefono,
       c.correo,
       p.nombre                        AS paquete,
       s.fecha_inicio                  AS fecha_salida,
       r.valor_total,
       r.valor_total - NVL((SELECT SUM(pg.monto)
                              FROM pago pg
                             WHERE pg.id_reserva = r.id_reserva
                               AND pg.estado_pago = 'exitoso'), 0) AS saldo_pendiente
  FROM reserva r
  JOIN cliente c ON c.id_cliente = r.id_cliente
  JOIN salida s  ON s.id_salida  = r.id_salida
  JOIN paquete p ON p.id_paquete = s.id_paquete
 WHERE r.estado <> 'cancelada'
   AND s.fecha_inicio BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 15
   AND r.valor_total > NVL((SELECT SUM(pg.monto)
                              FROM pago pg
                             WHERE pg.id_reserva = r.id_reserva
                               AND pg.estado_pago = 'exitoso'), 0)
 ORDER BY s.fecha_inicio, saldo_pendiente DESC;
