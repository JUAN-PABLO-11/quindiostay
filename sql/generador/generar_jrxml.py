"""Genera los .jrxml de los 10 reportes y las 5 plantillas (src/main/resources/reportes).

Uso (desde la raíz del repo):  python3 sql/generador/generar_jrxml.py src/main/resources/reportes
Si se cambia un diseño a mano en Jaspersoft Studio, NO volver a correr este script (lo sobrescribe).
"""
import sys, os
from xml.sax.saxutils import escape

DEST = sys.argv[1]
os.makedirs(DEST, exist_ok=True)

VERDE = "#2E5E3A"      # verde café
VERDE_CLARO = "#E8F0EA"
CAFE = "#7A4E2D"
GRIS = "#5F6368"
LINEA = "#C9D3CB"

def cdata(t): return f"<![CDATA[{t}]]>"

def cabecera(nombre, ancho, alto, margen=20, horizontal=False, sin_datos="AllSectionsNoDetail"):
    col = ancho - 2 * margen
    o = ' orientation="Landscape"' if horizontal else ""
    return f'''<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
              name="{nombre}" language="java" pageWidth="{ancho}" pageHeight="{alto}"{o} columnWidth="{col}"
              leftMargin="{margen}" rightMargin="{margen}" topMargin="{margen}" bottomMargin="{margen}"
              whenNoDataType="{sin_datos}" uuid="{uuid(nombre)}">
	<property name="net.sf.jasperreports.export.pdf.metadata.title" value="QuindioStay"/>
	<style name="Base" isDefault="true" fontName="SansSerif" fontSize="9" forecolor="#202124"/>
	<style name="Fila" mode="Transparent">
		<conditionalStyle>
			<conditionExpression>{cdata("$V{REPORT_COUNT} % 2 == 0")}</conditionExpression>
			<style mode="Opaque" backcolor="#F4F7F5"/>
		</conditionalStyle>
	</style>
'''

_n = [0]
def uuid(semilla):
    import hashlib
    h = hashlib.md5(semilla.encode()).hexdigest()
    return f"{h[:8]}-{h[8:12]}-{h[12:16]}-{h[16:20]}-{h[20:32]}"

def re_(x, y, w, h, extra=""):
    _n[0] += 1
    return f'<reportElement {extra}x="{x}" y="{y}" width="{w}" height="{h}" uuid="{uuid(str(_n[0]) + str(x) + str(y))}"/>'

def texto(x, y, w, h, t, size=9, bold=False, align="Left", color=None, fondo=None, valign="Middle", italic=False):
    modo = f'mode="Opaque" backcolor="{fondo}" ' if fondo else ""
    fc = f'forecolor="{color}" ' if color else ""
    return (f'<staticText>{re_(x, y, w, h, modo + fc)}'
            f'<box leftPadding="4" rightPadding="4"/>'
            f'<textElement textAlignment="{align}" verticalAlignment="{valign}">'
            f'<font size="{size}" isBold="{str(bold).lower()}" isItalic="{str(italic).lower()}"/></textElement>'
            f'<text>{cdata(t)}</text></staticText>')

def campo(x, y, w, h, expr, size=9, bold=False, align="Left", patron=None, color=None, fondo=None,
          estira=False, valign="Middle", nulo_vacio=True, italic=False, style=None):
    modo = f'mode="Opaque" backcolor="{fondo}" ' if fondo else ""
    fc = f'forecolor="{color}" ' if color else ""
    st = f'style="{style}" ' if style else ""
    pat = f' pattern="{patron}"' if patron else ""
    est = ' textAdjust="StretchHeight"' if estira else ""
    pos = ' positionType="Float" stretchType="ContainerHeight"' if False else ""
    return (f'<textField{pat}{est} isBlankWhenNull="{str(nulo_vacio).lower()}">{re_(x, y, w, h, st + modo + fc)}'
            f'<box leftPadding="4" rightPadding="4"/>'
            f'<textElement textAlignment="{align}" verticalAlignment="{valign}">'
            f'<font size="{size}" isBold="{str(bold).lower()}" isItalic="{str(italic).lower()}"/></textElement>'
            f'<textFieldExpression>{cdata(expr)}</textFieldExpression></textField>')

def rect(x, y, w, h, color, extra=""):
    return (f'<rectangle>{re_(x, y, w, h, extra + f"mode=\"Opaque\" backcolor=\"{color}\" forecolor=\"{color}\" ")}'
            f'<graphicElement><pen lineWidth="0.0"/></graphicElement></rectangle>')

def linea(x, y, w, color=LINEA, grosor=0.75):
    return (f'<line>{re_(x, y, w, 1, f"forecolor=\"{color}\" ")}'
            f'<graphicElement><pen lineWidth="{grosor}"/></graphicElement></line>')

def band(h, *els, split=None):
    s = f' splitType="{split}"' if split else ""
    return f'<band height="{h}"{s}>' + "".join(els) + "</band>"

# ======================================================================
#                              REPORTES
# ======================================================================
# columna: (campo, título, ancho, clase, patrón, alineación, sumar)
N = "java.math.BigDecimal"; S = "java.lang.String"

REPORTES = [
 dict(n=1, nivel="Simple", titulo="Salidas por estado",
      pregunta="¿Cuántas salidas hay en cada estado (programada, confirmada, cancelada, realizada)?",
      tablas="SALIDA", sql="""SELECT estado, COUNT(*) AS cantidad
  FROM salida
 GROUP BY estado
 ORDER BY cantidad DESC""",
      cols=[("ESTADO", "Estado", 300, S, None, "Left", False), ("CANTIDAD", "Salidas", 140, N, "#,##0", "Right", True)],
      grafico=dict(tipo="pie", clave="$F{ESTADO}", valor="$F{CANTIDAD}", titulo="Salidas por estado")),
 dict(n=2, nivel="Simple", titulo="Planes activos y vigentes",
      pregunta="¿Qué planes están activos y vigentes hoy, ordenados de menor a mayor precio?",
      tablas="PLAN", sql="""SELECT id_paquete, nombre, tipo_plan, precio,
       TO_CHAR(fecha_inicio_vigencia, 'DD/MM/YYYY') AS vigente_desde,
       TO_CHAR(fecha_fin_vigencia, 'DD/MM/YYYY')    AS vigente_hasta
  FROM plan
 WHERE estado = 'activo'
   AND TRUNC(SYSDATE) BETWEEN fecha_inicio_vigencia AND fecha_fin_vigencia
 ORDER BY precio ASC""",
      cols=[("ID_PAQUETE", "Paquete #", 70, N, "#0", "Center", False), ("NOMBRE", "Plan", 250, S, None, "Left", False),
            ("TIPO_PLAN", "Tipo", 110, S, None, "Left", False), ("PRECIO", "Precio por persona", 130, N, "$ #,##0", "Right", False),
            ("VIGENTE_DESDE", "Vigente desde", 110, S, None, "Center", False), ("VIGENTE_HASTA", "Vigente hasta", 110, S, None, "Center", False)]),
 dict(n=3, nivel="Simple", titulo="Cotizaciones por estado",
      pregunta="¿Qué porcentaje de las cotizaciones termina aceptada (convertida en reserva), rechazada o vencida?",
      tablas="COTIZACION", sql="""SELECT estado,
       COUNT(*) AS cantidad,
       ROUND(100 * COUNT(*) / SUM(COUNT(*)) OVER (), 1) AS porcentaje
  FROM cotizacion
 GROUP BY estado
 ORDER BY cantidad DESC""",
      cols=[("ESTADO", "Estado", 260, S, None, "Left", False), ("CANTIDAD", "Cotizaciones", 130, N, "#,##0", "Right", True),
            ("PORCENTAJE", "% del total", 130, N, "#,##0.0 '%'", "Right", False)]),
 dict(n=4, nivel="Intermedio", titulo="Paquetes más vendidos por temporada",
      pregunta="¿Cuáles son los paquetes más vendidos en cada temporada (alta, media, baja)?",
      tablas="RESERVA, SALIDA, PAQUETE, RESERVA_PASAJERO", sql="""SELECT temporada, paquete, pasajeros, reservas,
       RANK() OVER (PARTITION BY temporada ORDER BY pasajeros DESC) AS puesto
  FROM (SELECT temporada(s.fecha_inicio)    AS temporada,
               p.nombre                     AS paquete,
               COUNT(rp.id_pasajero)        AS pasajeros,
               COUNT(DISTINCT r.id_reserva) AS reservas
          FROM reserva r
          JOIN salida s            ON s.id_salida   = r.id_salida
          JOIN paquete p           ON p.id_paquete  = s.id_paquete
          JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
         WHERE r.estado <> 'cancelada'
           AND p.visibilidad = 'publico'
         GROUP BY temporada(s.fecha_inicio), p.nombre)
 ORDER BY CASE temporada WHEN 'alta' THEN 1 WHEN 'media' THEN 2 ELSE 3 END, puesto""",
      cols=[("TEMPORADA", "Temporada", 90, S, None, "Left", False), ("PUESTO", "Puesto", 60, N, "#0", "Center", False),
            ("PAQUETE", "Paquete", 330, S, None, "Left", False), ("PASAJEROS", "Pasajeros", 110, N, "#,##0", "Right", True),
            ("RESERVAS", "Reservas", 110, N, "#,##0", "Right", True)],
      grafico=dict(tipo="barras", serie="$F{TEMPORADA}", categoria="$F{PAQUETE}", valor="$F{PASAJEROS}",
                   titulo="Pasajeros por paquete y temporada", eje="Pasajeros")),
 dict(n=5, nivel="Intermedio", titulo="Ocupación de las salidas programadas",
      pregunta="¿Qué porcentaje del cupo lleva vendido cada salida programada?",
      tablas="SALIDA, PAQUETE, RESERVA, RESERVA_PASAJERO", sql="""SELECT p.nombre AS paquete,
       TO_CHAR(s.fecha_inicio, 'DD/MM/YYYY') AS fecha,
       COUNT(rp.id_pasajero) AS vendidos,
       s.cupo_maximo,
       ROUND(100 * COUNT(rp.id_pasajero) / s.cupo_maximo) AS porcentaje,
       p.minimo_viajeros,
       CASE WHEN COUNT(rp.id_pasajero) >= p.minimo_viajeros
            THEN 'Alcanza el mínimo' ELSE 'Bajo el mínimo' END AS situacion
  FROM salida s
  JOIN paquete p ON p.id_paquete = s.id_paquete
  LEFT JOIN reserva r ON r.id_salida = s.id_salida AND r.estado <> 'cancelada'
  LEFT JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
 WHERE s.estado = 'programada'
   AND p.visibilidad = 'publico'
 GROUP BY s.id_salida, p.nombre, s.fecha_inicio, s.cupo_maximo, p.minimo_viajeros
 ORDER BY s.fecha_inicio""",
      cols=[("PAQUETE", "Paquete", 240, S, None, "Left", False), ("FECHA", "Salida", 80, S, None, "Center", False),
            ("VENDIDOS", "Vendidos", 70, N, "#,##0", "Right", True), ("CUPO_MAXIMO", "Cupo", 60, N, "#,##0", "Right", True),
            ("PORCENTAJE", "Ocupación", 80, N, "#0 '%'", "Right", False), ("MINIMO_VIAJEROS", "Mínimo", 60, N, "#0", "Right", False),
            ("SITUACION", "Situación", 130, S, None, "Left", False)],
      grafico=dict(tipo="barras", serie='"Ocupación %"', categoria='$F{PAQUETE} + " " + $F{FECHA}', valor="$F{PORCENTAJE}",
                   titulo="Porcentaje de ocupación por salida", eje="% del cupo", rotar=True)),
 dict(n=6, nivel="Intermedio", titulo="Prestadores que más participan en paquetes",
      pregunta="¿Qué prestadores aportan componentes a 3 o más paquetes?",
      tablas="PRESTADOR, TIPO_PRESTADOR, COMPONENTE, PAQUETE_COMPONENTE", sql="""SELECT pr.nombre_comercial             AS prestador,
       tp.nombre                       AS tipo_prestador,
       COUNT(DISTINCT c.id_componente) AS componentes,
       COUNT(DISTINCT pc.id_paquete)   AS paquetes
  FROM prestador pr
  JOIN tipo_prestador tp     ON tp.id_tipo_prestador = pr.id_tipo_prestador
  JOIN componente c          ON c.id_prestador       = pr.id_prestador
  JOIN paquete_componente pc ON pc.id_componente     = c.id_componente
 GROUP BY pr.nombre_comercial, tp.nombre
HAVING COUNT(DISTINCT pc.id_paquete) >= 3
 ORDER BY paquetes DESC, prestador""",
      cols=[("PRESTADOR", "Prestador", 280, S, None, "Left", False), ("TIPO_PRESTADOR", "Tipo", 160, S, None, "Left", False),
            ("COMPONENTES", "Componentes", 110, N, "#,##0", "Right", False), ("PAQUETES", "Paquetes", 110, N, "#,##0", "Right", False)]),
 dict(n=7, nivel="Intermedio", titulo="Recaudo mensual por canal de venta",
      pregunta="¿Cuánto se recauda mes a mes por cada canal de venta?",
      tablas="PAGO, RESERVA", sql="""SELECT TO_CHAR(pg.fecha_pago, 'YYYY-MM') AS mes,
       r.canal,
       SUM(pg.monto) AS recaudo,
       COUNT(*)      AS pagos
  FROM pago pg
  JOIN reserva r ON r.id_reserva = pg.id_reserva
 WHERE pg.estado_pago = 'exitoso'
 GROUP BY TO_CHAR(pg.fecha_pago, 'YYYY-MM'), r.canal
 ORDER BY mes, r.canal""",
      cols=[("MES", "Mes", 100, S, None, "Center", False), ("CANAL", "Canal", 220, S, None, "Left", False),
            ("PAGOS", "Pagos", 90, N, "#,##0", "Right", True), ("RECAUDO", "Recaudo", 160, N, "$ #,##0", "Right", True)],
      grafico=dict(tipo="lineas", serie="$F{CANAL}", categoria="$F{MES}", valor="$F{RECAUDO}",
                   titulo="Recaudo mensual por canal", eje="Pesos", rotar=True)),
 dict(n=8, nivel="Avanzado", titulo="Paquetes públicos que nunca se han vendido",
      pregunta="¿Qué paquetes públicos no tienen ninguna reserva vigente?",
      tablas="PAQUETE, DESTINO, SALIDA, RESERVA (subconsulta NOT EXISTS)", sql="""SELECT p.id_paquete, p.nombre, p.categoria, d.nombre AS destino
  FROM paquete p
  JOIN destino d ON d.id_destino = p.id_destino
 WHERE p.visibilidad = 'publico'
   AND NOT EXISTS (SELECT 1
                     FROM salida s
                     JOIN reserva r ON r.id_salida = s.id_salida
                    WHERE s.id_paquete = p.id_paquete
                      AND r.estado <> 'cancelada')
 ORDER BY p.nombre""",
      cols=[("ID_PAQUETE", "#", 50, N, "#0", "Center", False), ("NOMBRE", "Paquete", 300, S, None, "Left", False),
            ("CATEGORIA", "Categoría", 130, S, None, "Left", False), ("DESTINO", "Destino", 230, S, None, "Left", False)]),
 dict(n=9, nivel="Avanzado", titulo="Salidas que no alcanzan el mínimo de viajeros",
      pregunta="¿Qué salidas programadas todavía no alcanzan el mínimo de viajeros de su paquete?",
      tablas="SALIDA, PAQUETE, RESERVA, RESERVA_PASAJERO (subconsulta escalar)", sql="""SELECT paquete, fecha, minimo_viajeros, vendidos,
       minimo_viajeros - vendidos AS faltan
  FROM (SELECT p.nombre AS paquete,
               s.fecha_inicio,
               TO_CHAR(s.fecha_inicio, 'DD/MM/YYYY') AS fecha,
               p.minimo_viajeros,
               (SELECT COUNT(*)
                  FROM reserva r
                  JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
                 WHERE r.id_salida = s.id_salida
                   AND r.estado <> 'cancelada') AS vendidos
          FROM salida s
          JOIN paquete p ON p.id_paquete = s.id_paquete
         WHERE s.estado = 'programada'
           AND p.visibilidad = 'publico')
 WHERE vendidos < minimo_viajeros
 ORDER BY fecha_inicio""",
      cols=[("PAQUETE", "Paquete", 300, S, None, "Left", False), ("FECHA", "Salida", 100, S, None, "Center", False),
            ("MINIMO_VIAJEROS", "Mínimo", 90, N, "#0", "Right", False), ("VENDIDOS", "Vendidos", 90, N, "#0", "Right", False),
            ("FALTAN", "Faltan", 90, N, "#0", "Right", True)]),
 dict(n=10, nivel="Avanzado", titulo="Clientes con saldo pendiente y salida próxima",
      pregunta="¿Qué clientes tienen saldo pendiente y su salida es en los próximos 15 días?",
      tablas="CLIENTE, RESERVA, SALIDA, PAQUETE, PAGO (subconsulta correlacionada)", sql="""SELECT c.nombres || ' ' || c.apellidos AS cliente,
       c.telefono,
       c.correo,
       p.nombre AS paquete,
       TO_CHAR(s.fecha_inicio, 'DD/MM/YYYY') AS fecha_salida,
       r.valor_total,
       NVL((SELECT SUM(pg.monto) FROM pago pg
             WHERE pg.id_reserva = r.id_reserva AND pg.estado_pago = 'exitoso'), 0) AS pagado,
       r.valor_total - NVL((SELECT SUM(pg.monto) FROM pago pg
                             WHERE pg.id_reserva = r.id_reserva AND pg.estado_pago = 'exitoso'), 0) AS saldo
  FROM reserva r
  JOIN cliente c ON c.id_cliente = r.id_cliente
  JOIN salida s  ON s.id_salida  = r.id_salida
  JOIN paquete p ON p.id_paquete = s.id_paquete
 WHERE r.estado <> 'cancelada'
   AND s.fecha_inicio BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 15
   AND r.valor_total > NVL((SELECT SUM(pg.monto) FROM pago pg
                             WHERE pg.id_reserva = r.id_reserva AND pg.estado_pago = 'exitoso'), 0)
 ORDER BY s.fecha_inicio, saldo DESC""",
      cols=[("CLIENTE", "Cliente", 150, S, None, "Left", False), ("TELEFONO", "Teléfono", 75, S, None, "Left", False),
            ("CORREO", "Correo", 155, S, None, "Left", False), ("PAQUETE", "Paquete", 150, S, None, "Left", False),
            ("FECHA_SALIDA", "Salida", 62, S, None, "Center", False), ("VALOR_TOTAL", "Valor", 70, N, "#,##0", "Right", True),
            ("PAGADO", "Pagado", 70, N, "#,##0", "Right", True), ("SALDO", "Saldo", 70, N, "#,##0", "Right", True)]),
]

def grafico_xml(g, y, ancho, alto):
    titulo = f'<chartTitle position="Top"><font size="10" isBold="true"/><titleExpression>{cdata(chr(34) + g["titulo"] + chr(34))}</titleExpression></chartTitle>'
    chart = (f'<chart isShowLegend="true" evaluationTime="Report">{re_(0, y, ancho, alto)}'
             f'{titulo}<chartSubtitle/><chartLegend position="Bottom"><font size="8"/></chartLegend></chart>')
    if g["tipo"] == "pie":
        return (f'<pieChart>{chart}<pieDataset><keyExpression>{cdata(g["clave"])}</keyExpression>'
                f'<valueExpression>{cdata(g["valor"])}</valueExpression></pieDataset>'
                f'<piePlot labelFormat="{{0}}: {{1}} ({{2}})"><plot><seriesColor seriesOrder="0" color="#2E5E3A"/><seriesColor seriesOrder="1" color="#C08A3E"/><seriesColor seriesOrder="2" color="#7FA88A"/><seriesColor seriesOrder="3" color="#A23B3B"/></plot><itemLabel><font size="8"/></itemLabel></piePlot></pieChart>')
    rot = ' labelRotation="-45.0"' if g.get("rotar") else ""
    dataset = (f'<categoryDataset><categorySeries><seriesExpression>{cdata(g["serie"])}</seriesExpression>'
               f'<categoryExpression>{cdata(g["categoria"])}</categoryExpression>'
               f'<valueExpression>{cdata(g["valor"])}</valueExpression></categorySeries></categoryDataset>')
    ejes = (f'<categoryAxisFormat labelRotation="{-45.0 if g.get("rotar") else 0.0}"><axisFormat><labelFont><font size="7"/></labelFont>'
            f'<tickLabelFont><font size="7"/></tickLabelFont></axisFormat></categoryAxisFormat>'
            f'<valueAxisLabelExpression>{cdata(chr(34) + g["eje"] + chr(34))}</valueAxisLabelExpression>'
            f'<valueAxisFormat><axisFormat><labelFont><font size="8"/></labelFont><tickLabelFont><font size="7"/></tickLabelFont></axisFormat></valueAxisFormat>')
    if g["tipo"] == "barras":
        return (f'<barChart>{chart}{dataset}<barPlot isShowLabels="false"><plot{rot}>'
                f'<seriesColor seriesOrder="0" color="#2E5E3A"/><seriesColor seriesOrder="1" color="#C08A3E"/>'
                f'<seriesColor seriesOrder="2" color="#7FA88A"/></plot>'
                f'<itemLabel/>{ejes}</barPlot></barChart>')
    return (f'<lineChart>{chart}{dataset}<linePlot isShowShapes="true"><plot{rot}>'
            f'<seriesColor seriesOrder="0" color="#2E5E3A"/><seriesColor seriesOrder="1" color="#C08A3E"/>'
            f'<seriesColor seriesOrder="2" color="#3B6EA8"/><seriesColor seriesOrder="3" color="#A23B3B"/>'
            f'<seriesColor seriesOrder="4" color="#7A7A7A"/></plot>{ejes}</linePlot></lineChart>')

def encabezado_marca(ancho, etiqueta):
    return (rect(0, 0, ancho, 34, VERDE)
            + texto(10, 0, 300, 34, "QuindioStay", size=16, bold=True, color="#FFFFFF")
            + texto(ancho - 330, 0, 320, 34, etiqueta, size=9, align="Right", color="#FFFFFF"))

def reporte(r):
    _n[0] = 0
    horiz = True
    ancho = 842 - 40
    nombre = f"reporte{r['n']:02d}"
    x = cabecera(nombre, 842, 595, horizontal=horiz)
    x += f"\t<queryString language=\"SQL\">{cdata(r['sql'])}</queryString>\n"
    for c in r["cols"]:
        x += f'\t<field name="{c[0]}" class="{c[3]}"/>\n'
    for c in r["cols"]:
        if c[6]:
            x += f'\t<variable name="TOTAL_{c[0]}" class="java.math.BigDecimal" calculation="Sum"><variableExpression>{cdata("$F{" + c[0] + "}")}</variableExpression></variable>\n'
    tabla_ancho = sum(c[2] for c in r["cols"])
    # título
    t = band(96,
        encabezado_marca(ancho, f"Reporte {r['n']} de 10 · Nivel {r['nivel'].lower()}"),
        texto(0, 40, ancho, 22, r["titulo"], size=15, bold=True, color=VERDE),
        texto(0, 62, ancho, 16, r["pregunta"], size=10, italic=True, color=GRIS),
        texto(0, 78, 70, 14, "Tablas:", size=8, bold=True, color=GRIS),
        texto(70, 78, ancho - 260, 14, r["tablas"], size=8, color=GRIS),
        campo(ancho - 190, 78, 190, 14, '"Generado el " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())',
              size=8, align="Right", color=GRIS))
    # encabezado de columnas
    xs, ch = 0, []
    for c in r["cols"]:
        ch.append(texto(xs, 0, c[2], 20, c[1], size=9, bold=True, align=c[5], color="#FFFFFF", fondo=CAFE))
        xs += c[2]
    cabeza = band(20, *ch)
    # detalle
    xs, de = 0, [f'<frame>{re_(0, 0, tabla_ancho, 16, "style=\"Fila\" ")}</frame>']
    for c in r["cols"]:
        de.append(campo(xs, 0, c[2], 16, "$F{" + c[0] + "}", align=c[5], patron=c[4]))
        xs += c[2]
    de.append(linea(0, 16, tabla_ancho, color="#E3E8E4", grosor=0.5))
    detalle = band(17, *de)
    pie = band(18,
        linea(0, 2, ancho),
        texto(0, 4, 400, 14, "QuindioStay · Bases de Datos I · Universidad del Quindío", size=7, color=GRIS),
        campo(ancho - 200, 4, 150, 14, '"Página " + $V{PAGE_NUMBER} + " de"', size=7, align="Right", color=GRIS),
        f'<textField evaluationTime="Report">{re_(ancho - 50, 4, 50, 14)}<textElement><font size="7"/></textElement>'
        f'<textFieldExpression>{cdata(chr(34) + " " + chr(34) + " + $V{PAGE_NUMBER}")}</textFieldExpression></textField>')
    # totales en el pie de un grupo único (se imprimen justo debajo de la tabla)
    res = [linea(0, 0, tabla_ancho, color=CAFE, grosor=1.0)]
    xs = 0
    hay_total = any(c[6] for c in r["cols"])
    for i, c in enumerate(r["cols"]):
        if c[6]:
            res.append(campo(xs, 2, c[2], 16, "$V{TOTAL_" + c[0] + "}", bold=True, align=c[5], patron=c[4]))
        elif i == 0 and hay_total:
            res.append(texto(xs, 2, c[2], 16, "Total", bold=True))
        xs += c[2]
    res.append(campo(0, 20, 300, 14, '$V{REPORT_COUNT} + " filas"', size=8, color=GRIS, italic=True))
    grupo = (f'\t<group name="Todo"><groupExpression>{cdata(chr(34) + "todo" + chr(34))}</groupExpression>'
             f'<groupFooter>{band(36, *res)}</groupFooter></group>\n')
    # el gráfico va en el resumen: si no cabe en la página, pasa entero a la siguiente
    resumen = band(480, grafico_xml(r["grafico"], 6, ancho, 470)) if r.get("grafico") else None
    sin_datos = band(60, encabezado_marca(ancho, f"Reporte {r['n']} de 10"),
                     texto(0, 40, ancho, 20, "No hay datos para este reporte en este momento.", size=11, italic=True, color=GRIS))
    x += grupo
    x += f"\t<title>{t}</title>\n\t<columnHeader>{cabeza}</columnHeader>\n\t<detail>{detalle}</detail>\n"
    x += f"\t<pageFooter>{pie}</pageFooter>\n"
    if resumen:
        x += f"\t<summary>{resumen}</summary>\n"
    x += f"\t<noData>{sin_datos}</noData>\n</jasperReport>\n"
    return nombre, x.replace('whenNoDataType="AllSectionsNoDetail"', 'whenNoDataType="NoDataSection"')

for r in REPORTES:
    nombre, xml = reporte(r)
    open(os.path.join(DEST, nombre + ".jrxml"), "w").write(xml)

# índice para la aplicación
with open(os.path.join(DEST, "reportes.properties"), "w", encoding="utf-8") as f:
    f.write("# número = nivel | título | pregunta de negocio (lo lee ReportesPanel)\n")
    for r in REPORTES:
        f.write(f"reporte{r['n']:02d}={r['nivel']}|{r['titulo']}|{r['pregunta']}\n")

# ======================================================================
#                              PLANTILLAS
# ======================================================================
CARTA_W, CARTA_H, M = 612, 792, 36
CW = CARTA_W - 2 * M

def marca_doc(titulo, sub_expr):
    return (rect(0, 0, CW, 46, VERDE)
            + texto(12, 4, 260, 24, "QuindioStay", size=18, bold=True, color="#FFFFFF")
            + texto(12, 26, 300, 16, "Plataforma de turismo del Quindío", size=8, color="#DDE8DF")
            + texto(CW - 280, 4, 270, 22, titulo, size=13, bold=True, align="Right", color="#FFFFFF")
            + campo(CW - 280, 26, 270, 16, sub_expr, size=9, align="Right", color="#FFFFFF"))

def etiqueta_valor(x, y, w, etiqueta, expr, patron=None, we=90, bold=False):
    return (texto(x, y, we, 15, etiqueta, size=8, bold=True, color=GRIS)
            + campo(x + we, y, w - we, 15, expr, patron=patron, bold=bold))

def seccion(y, t):
    return texto(0, y, CW, 18, t.upper(), size=9, bold=True, color=VERDE) + linea(0, y + 18, CW, color=VERDE, grosor=1.0)

def pie_doc():
    return band(26, linea(0, 2, CW),
        texto(0, 5, CW - 120, 20, "Documento generado por QuindioStay, proyecto académico de Bases de Datos I - Universidad del Quindío.",
              size=7, color=GRIS, italic=True),
        campo(CW - 120, 5, 120, 20, '"Generado " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())',
              size=7, align="Right", color=GRIS))

def firma(x, y, w, nombre_expr, rol):
    return (linea(x, y, w, color="#202124", grosor=0.75)
            + campo(x, y + 2, w, 14, nombre_expr, size=8, bold=True, align="Center")
            + texto(x, y + 15, w, 13, rol, size=8, align="Center", color=GRIS))

def plantilla(nombre, param, param_clase, sql, campos, titulo, sub_expr, cuerpo_titulo, cabeza_cols, detalle_els, alto_detalle,
              resumen_els, alto_resumen, alto_titulo, print_detalle=None, sin_datos_msg="No se encontró el registro solicitado."):
    _n[0] = 0
    x = cabecera(nombre, CARTA_W, CARTA_H, margen=M, sin_datos="NoDataSection")
    x += f'\t<parameter name="{param}" class="{param_clase}"/>\n'
    x += f"\t<queryString language=\"SQL\">{cdata(sql)}</queryString>\n"
    for c, cl in campos:
        x += f'\t<field name="{c}" class="{cl}"/>\n'
    x += f'\t<title>{band(alto_titulo, marca_doc(titulo, sub_expr), *cuerpo_titulo)}</title>\n'
    if cabeza_cols:
        x += f'\t<columnHeader>{band(18, *cabeza_cols)}</columnHeader>\n'
    if detalle_els:
        pw = f'<printWhenExpression>{cdata(print_detalle)}</printWhenExpression>' if print_detalle else ""
        x += f'\t<detail><band height="{alto_detalle}">{pw}{"".join(detalle_els)}</band></detail>\n'
    x += f'\t<pageFooter>{pie_doc()}</pageFooter>\n'
    if resumen_els:
        x += f'\t<summary>{band(alto_resumen, *resumen_els)}</summary>\n'
    x += f'\t<noData>{band(80, marca_doc(titulo, chr(34) + chr(34)), texto(0, 60, CW, 20, sin_datos_msg, size=11, italic=True, color=GRIS))}</noData>\n'
    x += "</jasperReport>\n"
    open(os.path.join(DEST, nombre + ".jrxml"), "w").write(x)

BASE_RESERVA = """FROM reserva r
  JOIN cliente c    ON c.id_cliente    = r.id_cliente
  JOIN salida s     ON s.id_salida     = r.id_salida
  JOIN paquete p    ON p.id_paquete    = s.id_paquete
  JOIN destino d    ON d.id_destino    = p.id_destino
  JOIN municipio m  ON m.id_municipio  = d.id_municipio
  JOIN plan pl      ON pl.id_plan      = r.id_plan
  JOIN prestador pr ON pr.id_prestador = p.id_prestador"""
COLS_RESERVA = """r.id_reserva,
       TO_CHAR(r.fecha_creacion, 'DD/MM/YYYY') AS fecha_reserva,
       r.estado, r.canal, r.valor_total,
       NVL((SELECT SUM(pg.monto) FROM pago pg
             WHERE pg.id_reserva = r.id_reserva AND pg.estado_pago = 'exitoso'), 0) AS pagado,
       c.nombres || ' ' || c.apellidos AS cliente,
       c.tipo_documento || ' ' || c.numero_documento AS doc_cliente,
       c.correo, c.telefono, c.ciudad_origen,
       p.nombre AS paquete, p.duracion_dias,
       d.nombre || ' (' || m.nombre || ')' AS destino,
       pl.nombre AS plan, pl.precio,
       TO_CHAR(s.fecha_inicio, 'DD/MM/YYYY') AS fecha_salida,
       TO_CHAR(s.fecha_inicio + p.duracion_dias - 1, 'DD/MM/YYYY') AS fecha_regreso,
       pr.nombre_comercial AS agencia, pr.nit, pr.numero_rnt, pr.direccion AS dir_agencia,
       pr.telefono AS tel_agencia, pr.correo AS correo_agencia"""
CAMPOS_RESERVA = [("ID_RESERVA", N), ("FECHA_RESERVA", S), ("ESTADO", S), ("CANAL", S), ("VALOR_TOTAL", N), ("PAGADO", N),
    ("CLIENTE", S), ("DOC_CLIENTE", S), ("CORREO", S), ("TELEFONO", S), ("CIUDAD_ORIGEN", S), ("PAQUETE", S),
    ("DURACION_DIAS", N), ("DESTINO", S), ("PLAN", S), ("PRECIO", N), ("FECHA_SALIDA", S), ("FECHA_REGRESO", S),
    ("AGENCIA", S), ("NIT", S), ("NUMERO_RNT", S), ("DIR_AGENCIA", S), ("TEL_AGENCIA", S), ("CORREO_AGENCIA", S)]
H = CW // 2

def bloque_agencia(y):
    return [seccion(y, "Agencia"),
            etiqueta_valor(0, y + 22, H, "Agencia", "$F{AGENCIA}", bold=True),
            etiqueta_valor(0, y + 37, H, "NIT", "$F{NIT}"),
            etiqueta_valor(0, y + 52, H, "RNT", "$F{NUMERO_RNT}"),
            etiqueta_valor(H, y + 22, H, "Dirección", "$F{DIR_AGENCIA}"),
            etiqueta_valor(H, y + 37, H, "Teléfono", "$F{TEL_AGENCIA}"),
            etiqueta_valor(H, y + 52, H, "Correo", "$F{CORREO_AGENCIA}")]

def bloque_cliente(y, titulo="Cliente"):
    return [seccion(y, titulo),
            etiqueta_valor(0, y + 22, H, "Nombre", "$F{CLIENTE}", bold=True),
            etiqueta_valor(0, y + 37, H, "Documento", "$F{DOC_CLIENTE}"),
            etiqueta_valor(H, y + 22, H, "Correo", "$F{CORREO}"),
            etiqueta_valor(H, y + 37, H, "Teléfono", "$F{TELEFONO}")]

def bloque_viaje(y):
    return [seccion(y, "Viaje"),
            etiqueta_valor(0, y + 22, H, "Paquete", "$F{PAQUETE}", bold=True),
            etiqueta_valor(0, y + 37, H, "Destino", "$F{DESTINO}"),
            etiqueta_valor(0, y + 52, H, "Plan", "$F{PLAN}"),
            etiqueta_valor(H, y + 22, H, "Salida", "$F{FECHA_SALIDA}", bold=True),
            etiqueta_valor(H, y + 37, H, "Regreso", "$F{FECHA_REGRESO}"),
            etiqueta_valor(H, y + 52, H, "Duración", '$F{DURACION_DIAS} + " días"')]

# ---------- 1. Comprobante de reserva (con pasajeros e itinerario: voucher del paquete) ----------
plantilla("comprobante_reserva", "ID_RESERVA", "java.lang.Integer",
    f"""SELECT {COLS_RESERVA},
       pa.nombres || ' ' || pa.apellidos AS pasajero,
       pa.tipo_documento || ' ' || pa.numero_documento AS doc_pasajero,
       TRUNC(MONTHS_BETWEEN(s.fecha_inicio, pa.fecha_nacimiento) / 12) AS edad,
       (SELECT LISTAGG('Día ' || i.numero_dia || ': ' || i.descripcion, CHR(10))
               WITHIN GROUP (ORDER BY i.numero_dia)
          FROM itinerario_dia i WHERE i.id_paquete = p.id_paquete) AS itinerario
  {BASE_RESERVA}
  LEFT JOIN reserva_pasajero rp ON rp.id_reserva = r.id_reserva
  LEFT JOIN pasajero pa         ON pa.id_pasajero = rp.id_pasajero
 WHERE r.id_reserva = $P{{ID_RESERVA}}
 ORDER BY edad DESC""",
    CAMPOS_RESERVA + [("PASAJERO", S), ("DOC_PASAJERO", S), ("EDAD", N), ("ITINERARIO", S)],
    "Comprobante de reserva", '"Reserva N.º " + $F{ID_RESERVA}',
    [etiqueta_valor(0, 56, H, "Fecha de reserva", "$F{FECHA_RESERVA}", we=95),
     etiqueta_valor(H, 56, H, "Estado", "$F{ESTADO}.toUpperCase()", bold=True),
     etiqueta_valor(0, 71, H, "Canal", "$F{CANAL}", we=95)]
    + bloque_cliente(92) + bloque_viaje(150) + [seccion(225, "Pasajeros")],
    [texto(0, 0, 260, 18, "Pasajero", bold=True, color="#FFFFFF", fondo=CAFE),
     texto(260, 0, 170, 18, "Documento", bold=True, color="#FFFFFF", fondo=CAFE),
     texto(430, 0, CW - 430, 18, "Edad al viajar", bold=True, align="Right", color="#FFFFFF", fondo=CAFE)],
    [f'<frame>{re_(0, 0, CW, 16, "style=\"Fila\" ")}</frame>',
     campo(0, 0, 260, 16, "$F{PASAJERO}"), campo(260, 0, 170, 16, "$F{DOC_PASAJERO}"),
     campo(430, 0, CW - 430, 16, '$F{EDAD} == null ? "" : $F{EDAD} + " años" + ($F{EDAD}.intValue() < 18 ? " (menor)" : "")', align="Right")],
    16,
    [seccion(8, "Valores"),
     etiqueta_valor(0, 30, H, "Precio por persona", "$F{PRECIO}", patron="$ #,##0", we=110),
     etiqueta_valor(0, 45, H, "Pasajeros", "$V{REPORT_COUNT}", we=110),
     etiqueta_valor(H, 30, H, "Valor total", "$F{VALOR_TOTAL}", patron="$ #,##0", bold=True),
     etiqueta_valor(H, 45, H, "Pagado", "$F{PAGADO}", patron="$ #,##0"),
     etiqueta_valor(H, 60, H, "Saldo", "$F{VALOR_TOTAL}.subtract($F{PAGADO})", patron="$ #,##0", bold=True),
     seccion(84, "Itinerario"),
     f'<textField textAdjust="StretchHeight" isBlankWhenNull="true">{re_(0, 106, CW, 15, "positionType=\"Float\" ")}'
     f'<textElement><font size="9"/><paragraph lineSpacing="1_1_2"/></textElement><textFieldExpression>{cdata("$F{ITINERARIO}")}</textFieldExpression></textField>']
    + [f'<frame>{re_(0, 130, CW, 1, "positionType=\"Float\" ")}</frame>']
    + [e.replace("<reportElement ", '<reportElement positionType="Float" ') for e in bloque_agencia(132)]
    + [texto(0, 206, CW, 28, "Presenta este comprobante el día de la salida. Cancelaciones con más de 5 días de anticipación: "
                              "se reembolsa el 80 % de lo pagado; con 5 días o menos no hay reembolso.", size=8, italic=True, color=GRIS)
       .replace("<reportElement ", '<reportElement positionType="Float" ', 1)],
    240, 248)

# ---------- 2. Comprobante de pago ----------
plantilla("comprobante_pago", "ID_PAGO", "java.lang.Integer",
    f"""SELECT pg.id_pago, TO_CHAR(pg.fecha_pago, 'DD/MM/YYYY') AS fecha_pago, pg.monto, pg.metodo_pago, pg.estado_pago,
       pel.referencia_transaccion,
       {COLS_RESERVA}
  FROM pago pg
  JOIN reserva r ON r.id_reserva = pg.id_reserva
  LEFT JOIN pago_en_linea pel ON pel.id_pago = pg.id_pago
  JOIN cliente c    ON c.id_cliente    = r.id_cliente
  JOIN salida s     ON s.id_salida     = r.id_salida
  JOIN paquete p    ON p.id_paquete    = s.id_paquete
  JOIN destino d    ON d.id_destino    = p.id_destino
  JOIN municipio m  ON m.id_municipio  = d.id_municipio
  JOIN plan pl      ON pl.id_plan      = r.id_plan
  JOIN prestador pr ON pr.id_prestador = p.id_prestador
 WHERE pg.id_pago = $P{{ID_PAGO}}""",
    [("ID_PAGO", N), ("FECHA_PAGO", S), ("MONTO", N), ("METODO_PAGO", S), ("ESTADO_PAGO", S), ("REFERENCIA_TRANSACCION", S)] + CAMPOS_RESERVA,
    "Comprobante de pago", '"Pago N.º " + $F{ID_PAGO}',
    [rect(0, 58, CW, 62, VERDE_CLARO),
     texto(12, 62, 200, 16, "Valor pagado", size=9, color=GRIS),
     campo(12, 78, 260, 34, "$F{MONTO}", size=22, bold=True, patron="$ #,##0", color=VERDE),
     etiqueta_valor(290, 64, 240, "Fecha", "$F{FECHA_PAGO}", we=80),
     etiqueta_valor(290, 79, 240, "Método", "$F{METODO_PAGO}", we=80),
     etiqueta_valor(290, 94, 240, "Estado", "$F{ESTADO_PAGO}.toUpperCase()", we=80, bold=True),
     etiqueta_valor(0, 128, CW, "Referencia", '$F{REFERENCIA_TRANSACCION} == null ? "No aplica (pago presencial)" : $F{REFERENCIA_TRANSACCION}', we=90)]
    + bloque_cliente(150) + bloque_viaje(208)
    + [seccion(284, "Estado de cuenta de la reserva N.º"),
       campo(200, 284, 100, 18, "$F{ID_RESERVA}", bold=True, color=VERDE),
       etiqueta_valor(0, 306, H, "Valor de la reserva", "$F{VALOR_TOTAL}", patron="$ #,##0", we=120),
       etiqueta_valor(0, 321, H, "Total pagado", "$F{PAGADO}", patron="$ #,##0", we=120),
       etiqueta_valor(H, 306, H, "Saldo pendiente", "$F{VALOR_TOTAL}.subtract($F{PAGADO})", patron="$ #,##0", bold=True, we=110),
       etiqueta_valor(H, 321, H, "Situación", '$F{VALOR_TOTAL}.compareTo($F{PAGADO}) <= 0 ? "PAGADA" : "CON SALDO"', bold=True, we=110)]
    + bloque_agencia(346),
    None, None, 0, None, 0, 420, sin_datos_msg="No se encontró el pago solicitado.")

# ---------- 3. Factura ----------
plantilla("factura", "ID_RESERVA", "java.lang.Integer",
    f"""SELECT {COLS_RESERVA},
       'FV-' || LPAD(r.id_reserva, 6, '0') AS numero_factura,
       (SELECT COUNT(*) FROM reserva_pasajero rp WHERE rp.id_reserva = r.id_reserva) AS cantidad,
       ROUND(r.valor_total / 1.19) AS base,
       r.valor_total - ROUND(r.valor_total / 1.19) AS iva
  {BASE_RESERVA}
 WHERE r.id_reserva = $P{{ID_RESERVA}}""",
    CAMPOS_RESERVA + [("NUMERO_FACTURA", S), ("CANTIDAD", N), ("BASE", N), ("IVA", N)],
    "Factura de venta", '"N.º " + $F{NUMERO_FACTURA}',
    [etiqueta_valor(0, 56, H, "Fecha de emisión", "$F{FECHA_RESERVA}", we=100),
     etiqueta_valor(H, 56, H, "Forma de pago", '$F{PAGADO}.compareTo($F{VALOR_TOTAL}) >= 0 ? "Contado" : "Abonos"', we=100)]
    + bloque_agencia(78) + bloque_cliente(150, "Adquiriente")
    + [etiqueta_valor(0, 202, H, "Ciudad", "$F{CIUDAD_ORIGEN}"),
       texto(0, 226, 40, 18, "Cant.", bold=True, align="Center", color="#FFFFFF", fondo=CAFE),
       texto(40, 226, 310, 18, "Descripción", bold=True, color="#FFFFFF", fondo=CAFE),
       texto(350, 226, 90, 18, "Valor unitario", bold=True, align="Right", color="#FFFFFF", fondo=CAFE),
       texto(440, 226, CW - 440, 18, "Total", bold=True, align="Right", color="#FFFFFF", fondo=CAFE),
       campo(0, 246, 40, 30, "$F{CANTIDAD}", align="Center", valign="Top"),
       campo(40, 246, 310, 30, '"Paquete turístico " + $F{PAQUETE} + " - plan " + $F{PLAN} + ". Salida del " + $F{FECHA_SALIDA} + " (" + $F{DURACION_DIAS} + " días)."',
             valign="Top"),
       campo(350, 246, 90, 30, "$F{PRECIO}", align="Right", patron="#,##0", valign="Top"),
       campo(440, 246, CW - 440, 30, "$F{VALOR_TOTAL}", align="Right", patron="#,##0", valign="Top"),
       linea(0, 278, CW),
       etiqueta_valor(330, 284, CW - 330, "Subtotal", "$F{BASE}", patron="$ #,##0", we=80),
       etiqueta_valor(330, 299, CW - 330, "IVA (19 %)", "$F{IVA}", patron="$ #,##0", we=80),
       rect(330, 315, CW - 330, 20, VERDE_CLARO),
       etiqueta_valor(330, 317, CW - 330, "TOTAL", "$F{VALOR_TOTAL}", patron="$ #,##0", we=80, bold=True),
       etiqueta_valor(0, 284, 320, "Pagado a la fecha", "$F{PAGADO}", patron="$ #,##0", we=110),
       etiqueta_valor(0, 299, 320, "Saldo", "$F{VALOR_TOTAL}.subtract($F{PAGADO})", patron="$ #,##0", we=110),
       texto(0, 348, CW, 40, "Factura de ejemplo generada desde el sistema para el proyecto académico. Los valores incluyen IVA. "
             "Prestador inscrito en el Registro Nacional de Turismo con el número indicado.", size=8, italic=True, color=GRIS),
       firma(0, 432, 220, "$F{AGENCIA}", "Firma y sello del vendedor"),
       firma(CW - 220, 432, 220, "$F{CLIENTE}", "Recibido por el adquiriente")],
    None, None, 0, None, 0, 464, sin_datos_msg="No se encontró la reserva solicitada.")

# ---------- 4. Autorización de viaje de menores ----------
plantilla("autorizacion_menores", "ID_RESERVA", "java.lang.Integer",
    f"""SELECT {COLS_RESERVA},
       mn.menor, mn.doc_menor, mn.edad_menor, mn.nacimiento_menor
  {BASE_RESERVA}
  LEFT JOIN (SELECT rp.id_reserva,
                    pa.nombres || ' ' || pa.apellidos AS menor,
                    pa.tipo_documento || ' ' || pa.numero_documento AS doc_menor,
                    TO_CHAR(pa.fecha_nacimiento, 'DD/MM/YYYY') AS nacimiento_menor,
                    TRUNC(MONTHS_BETWEEN(s2.fecha_inicio, pa.fecha_nacimiento) / 12) AS edad_menor
               FROM reserva_pasajero rp
               JOIN pasajero pa ON pa.id_pasajero = rp.id_pasajero
               JOIN reserva r2  ON r2.id_reserva  = rp.id_reserva
               JOIN salida s2   ON s2.id_salida   = r2.id_salida
              WHERE MONTHS_BETWEEN(s2.fecha_inicio, pa.fecha_nacimiento) < 216) mn
         ON mn.id_reserva = r.id_reserva
 WHERE r.id_reserva = $P{{ID_RESERVA}}
 ORDER BY mn.edad_menor""",
    CAMPOS_RESERVA + [("MENOR", S), ("DOC_MENOR", S), ("EDAD_MENOR", N), ("NACIMIENTO_MENOR", S)],
    "Autorización de viaje", '"Menores de edad · Reserva N.º " + $F{ID_RESERVA}',
    [campo(0, 60, CW, 92,
        '"Yo, " + $F{CLIENTE} + ", identificado(a) con " + $F{DOC_CLIENTE} + ", en calidad de padre, madre o representante legal, "'
        ' + "AUTORIZO a los menores de edad relacionados a continuación a viajar en el paquete turístico \\"" + $F{PAQUETE} + "\\" '
        '(plan " + $F{PLAN} + "), con destino " + $F{DESTINO} + ", entre el " + $F{FECHA_SALIDA} + " y el " + $F{FECHA_REGRESO} + ", '
        'organizado por " + $F{AGENCIA} + " (NIT " + $F{NIT} + ", RNT " + $F{NUMERO_RNT} + "). Declaro que viajarán acompañados por al menos '
        'un adulto registrado en la misma reserva, que conozco el itinerario y las actividades, y que la información suministrada es verídica."',
        size=10, estira=True, valign="Top"),
     texto(0, 158, 240, 18, "Menor", bold=True, color="#FFFFFF", fondo=CAFE)
        .replace("<reportElement ", '<reportElement positionType="Float" ', 1),
     texto(240, 158, 140, 18, "Documento", bold=True, color="#FFFFFF", fondo=CAFE)
        .replace("<reportElement ", '<reportElement positionType="Float" ', 1),
     texto(380, 158, 90, 18, "Nacimiento", bold=True, align="Center", color="#FFFFFF", fondo=CAFE)
        .replace("<reportElement ", '<reportElement positionType="Float" ', 1),
     texto(470, 158, CW - 470, 18, "Edad", bold=True, align="Right", color="#FFFFFF", fondo=CAFE)
        .replace("<reportElement ", '<reportElement positionType="Float" ', 1)],
    None,
    [f'<frame>{re_(0, 0, CW, 16, "style=\"Fila\" ")}</frame>',
     campo(0, 0, 240, 16, "$F{MENOR}"), campo(240, 0, 140, 16, "$F{DOC_MENOR}"),
     campo(380, 0, 90, 16, "$F{NACIMIENTO_MENOR}", align="Center"),
     campo(470, 0, CW - 470, 16, '$F{EDAD_MENOR} + " años"', align="Right")],
    16,
    [campo(0, 6, CW, 18, '$V{REPORT_COUNT} == 0 || $F{MENOR} == null ? "Esta reserva no tiene menores de edad: no requiere autorización." : ""',
           italic=True, color=CAFE),
     texto(0, 30, CW, 30, "Esta autorización se presenta junto con el documento de identidad de cada menor y del adulto responsable. "
           "Para viajes fuera del país se requiere además el permiso notarial y los requisitos de Migración Colombia.", size=8, italic=True, color=GRIS),
     campo(0, 70, CW, 15, '"Firmado en ______________________ el día ______ de ______________ de ________."'),
     firma(0, 150, 230, "$F{CLIENTE}", "Firma del padre, madre o representante legal"),
     campo(0, 180, 230, 13, '"C.C. / documento: " + $F{DOC_CLIENTE}', size=8, align="Center", color=GRIS),
     firma(CW - 230, 150, 230, "$F{AGENCIA}", "Recibido por la agencia")],
    200, 180, print_detalle="$F{MENOR} != null", sin_datos_msg="No se encontró la reserva solicitada.")

# ---------- 5. Autorización de tratamiento de datos personales ----------
plantilla("autorizacion_datos", "ID_CLIENTE", "java.lang.Integer",
    """SELECT c.id_cliente,
       c.nombres || ' ' || c.apellidos AS cliente,
       c.tipo_documento || ' ' || c.numero_documento AS doc_cliente,
       c.correo, c.telefono, c.ciudad_origen,
       NVL(pc.canal_preferido, 'correo') AS canal,
       CASE WHEN pc.acepta_promociones = 'si' THEN 'SÍ' ELSE 'NO' END AS promociones,
       CASE WHEN pc.id_perfil IS NULL THEN 'Sin cuenta en línea' ELSE 'Con cuenta en línea' END AS cuenta
  FROM cliente c
  LEFT JOIN perfil_cliente pc ON pc.id_cliente = c.id_cliente
 WHERE c.id_cliente = $P{ID_CLIENTE}""",
    [("ID_CLIENTE", N), ("CLIENTE", S), ("DOC_CLIENTE", S), ("CORREO", S), ("TELEFONO", S), ("CIUDAD_ORIGEN", S),
     ("CANAL", S), ("PROMOCIONES", S), ("CUENTA", S)],
    "Tratamiento de datos", '"Autorización · Cliente N.º " + $F{ID_CLIENTE}',
    [seccion(58, "Titular de los datos"),
     etiqueta_valor(0, 80, H, "Nombre", "$F{CLIENTE}", bold=True),
     etiqueta_valor(0, 95, H, "Documento", "$F{DOC_CLIENTE}"),
     etiqueta_valor(0, 110, H, "Ciudad", "$F{CIUDAD_ORIGEN}"),
     etiqueta_valor(H, 80, H, "Correo", "$F{CORREO}"),
     etiqueta_valor(H, 95, H, "Teléfono", "$F{TELEFONO}"),
     etiqueta_valor(H, 110, H, "Cuenta", "$F{CUENTA}"),
     seccion(136, "Autorización"),
     campo(0, 160, CW, 250,
        '"De acuerdo con la Ley 1581 de 2012 y el Decreto 1074 de 2015, yo, " + $F{CLIENTE} + ", identificado(a) con " + $F{DOC_CLIENTE} + ", '
        'autorizo de manera previa, expresa e informada a QuindioStay y a los prestadores turísticos con los que haga reservas para recolectar, '
        'almacenar, usar y actualizar mis datos personales (nombre, documento, correo, teléfono, fecha de nacimiento y ciudad de origen) y los de '
        'los pasajeros que registre, con las siguientes finalidades:\\n\\n'
        '1. Gestionar mis reservas, pagos, cotizaciones y la prestación de los servicios turísticos contratados.\\n'
        '2. Enviarme confirmaciones, recordatorios y cambios de estado por el canal que elegí (" + $F{CANAL} + ").\\n'
        '3. Cumplir las obligaciones legales del sector turismo, como el Registro Nacional de Turismo y la facturación.\\n'
        '4. Enviarme promociones y novedades: " + $F{PROMOCIONES} + " lo autorizo.\\n\\n'
        'Como titular tengo derecho a conocer, actualizar, rectificar y suprimir mis datos, a solicitar prueba de esta autorización y a '
        'revocarla cuando lo desee, escribiendo a datos@quindiostay.co o desde la opción Mi perfil de la plataforma. Mis datos no se venderán ni '
        'se compartirán con terceros distintos de los prestadores que atienden mis reservas. Las contraseñas se guardan cifradas."',
        size=10, estira=True, valign="Top"),
     campo(0, 420, CW, 15, '"Ciudad y fecha: ______________________, " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date())')
        .replace("<reportElement ", '<reportElement positionType="Float" ', 1),
     firma(0, 500, 250, "$F{CLIENTE}", "Firma del titular").replace("<reportElement ", '<reportElement positionType="Float" ')],
    None, None, 0, None, 0, 540, sin_datos_msg="No se encontró el cliente solicitado.")

print("ok", len(REPORTES), "reportes + 5 plantillas")
