"""Genera sql/03_datos.sql con los volúmenes mínimos del enunciado y verifica las reglas antes de escribirlo.

Uso (desde la raíz del repo):  pip install bcrypt   y luego   python3 sql/generador/generar_datos.py sql/03_datos.sql
"""
import random, bcrypt, re, sys
from datetime import date, timedelta, datetime
from collections import defaultdict

random.seed(2026)
HOY = date(2026, 11, 10)
SALIDA_SQL = sys.argv[1]
HASH = bcrypt.hashpw(b"Clave123", bcrypt.gensalt(rounds=10, prefix=b"2a")).decode()

def q(s): return "'" + str(s).replace("'", "''") + "'"
def d(x): return f"DATE '{x.isoformat()}'"
def ts(x): return f"TIMESTAMP '{x.strftime('%Y-%m-%d %H:%M:%S')}'"
def nul(v, f=str): return "NULL" if v is None else f(v)
def dt(x, h=8, m=0): return datetime(x.year, x.month, x.day, h, m)

SS = [(date(2025,4,13), date(2025,4,20)), (date(2026,3,29), date(2026,4,5))]
def temporada(x):
    md = x.strftime("%m%d")
    if any(a <= x <= b for a, b in SS) or "0615" <= md <= "0731" or md >= "1215" or md <= "0115":
        return "alta"
    if md[:2] in ("01","06","08","10","11","12"): return "media"
    return "baja"
PESO = {"alta": 3.0, "media": 1.6, "baja": 1.0}

# ===================== catálogos =====================
municipios = [("63001","Armenia"),("63111","Buenavista"),("63130","Calarcá"),("63190","Circasia"),
    ("63212","Córdoba"),("63272","Filandia"),("63302","Génova"),("63401","La Tebaida"),
    ("63470","Montenegro"),("63548","Pijao"),("63594","Quimbaya"),("63690","Salento")]
MUN = {n: i+1 for i, (c, n) in enumerate(municipios)}
tipos_prest = ["alojamiento","agencia de viajes","transporte","gastronomia","guianza","alquiler de vehiculos","atraccion"]
TP = {n: i+1 for i, n in enumerate(tipos_prest)}
roles = ["administrador de la plataforma","encargado de prestador","operador"]
tipos_comp = ["hospedaje","transporte","alimentacion","actividad","seguro"]
TC = {n: i+1 for i, n in enumerate(tipos_comp)}

# nombre, nit, rnt, dirección, tel, correo, estado_final, tipo, municipio
prestadores = [
 ("Café & Montaña Viajes","900123456-7","45821","Calle 21 # 14-35","3157778899","reservas@cafeymontana.co","activo","agencia de viajes","Armenia"),
 ("Quindío Aventura Tours","901234567-1","51207","Carrera 25 # 30-12","3104445566","info@quindioaventura.co","activo","agencia de viajes","Calarcá"),
 ("Hotel Las Palmas","800456789-2","23456","Calle 5 # 3-20","6067591234","reservas@laspalmas.co","activo","alojamiento","Salento"),
 ("Finca Hotel El Mirador","811222333-4","34567","Vereda El Roble km 2","3201112233","contacto@elmirador.co","activo","alojamiento","Filandia"),
 ("Transportes Quindío Express","890333444-5","12890","Terminal de Transportes local 8","6067412345","viajes@quindioexpress.co","activo","transporte","Armenia"),
 ("Restaurante La Fonda Paisa","1094888777","67123","Calle Real # 6-15","3112223344","fondapaisa@gmail.com","activo","gastronomia","Salento"),
 ("Guías del Cocora","1094555666","70456","Plaza principal local 3","3145556677","guiasdelcocora@gmail.com","activo","guianza","Salento"),
 ("Parapente Cielo Abierto","900777888-3","58123","Mirador de Buenavista","3168889900","vuela@cieloabierto.co","activo","atraccion","Buenavista"),
 ("Ecoparque Los Guaduales","900999111-6","61234","Km 6 vía Montenegro","3177776655","info@losguaduales.co","activo","atraccion","Montenegro"),
 ("Viajes Génova Natural","901555666-8","49870","Calle 10 # 8-40","3189990011","hola@genovanatural.co","suspendido","agencia de viajes","Génova"),
 ("Ruta Paisa Travel","901777222-5","52310","Avenida Bolívar # 12N-45","3135557788","ventas@rutapaisa.co","activo","agencia de viajes","Armenia"),
 ("Salento Explora","901888333-9","53902","Carrera 6 # 4-18","3204448899","explora@salentoexplora.co","activo","agencia de viajes","Salento"),
 ("Filandia Colonial Tours","900666555-1","39120","Calle 7 # 5-22","3116667788","tours@filandiacolonial.co","retirado","agencia de viajes","Filandia"),
 ("Hotel Bosques del Quindío","800111999-4","21877","Carrera 14 # 22-50","6067445566","reservas@bosquesquindio.co","activo","alojamiento","Armenia"),
 ("Glamping Valle Verde","901444777-2","68210","Vereda Palestina km 4","3176663322","hola@valleverde.co","activo","alojamiento","Salento"),
 ("Finca Agroturística La Cosecha","900222777-8","30145","Vereda El Laurel","3128885544","lacosecha@gmail.com","activo","alojamiento","Quimbaya"),
 ("Willys Tours Salento","1094777333","71802","Plaza de Bolívar","3159994411","willystours@gmail.com","activo","transporte","Salento"),
 ("Restaurante Helechos","1094321654","66510","Calle 6 # 7-30","3102227799","helechos.filandia@gmail.com","activo","gastronomia","Filandia"),
 ("Asadero El Cafetal","1094666111","65933","Calle 17 # 4-12","3183332211","elcafetal@gmail.com","activo","gastronomia","Montenegro"),
 ("Mariposario Jardín de Calarcá","900345678-0","62001","Km 3 vía al Valle","6067422233","visitas@mariposario.co","activo","atraccion","Calarcá"),
 ("Andrés Marín Guía Natural","1097123456","70988","Calle 8 # 12-04","3011234567","andresmarin.guia@gmail.com","activo","guianza","Circasia"),
]
NP = len(prestadores)
AG = [1, 2, 10, 11, 12, 13]  # agencias

usuarios = [("jpespinosa",1,None),("jjospina",1,None),("mabetancourt",3,None),("operador1",3,None),
            ("cafeymontana",2,1),("quindioaventura",2,2),("rutapaisa",2,11)]

destinos = [
 ("Valle de Cocora","Valle de palmas de cera, árbol nacional de Colombia","Salento"),
 ("Parque del Café","Parque temático de la cultura cafetera","Montenegro"),
 ("Mirador de Filandia","Mirador colonial con vista a tres departamentos","Filandia"),
 ("Reserva Río Gris","Bosque de niebla con más de 300 especies de aves","Génova"),
 ("Cañón del río Barbas","Cañón con bosque andino y puentes colgantes","Filandia"),
 ("Jardín Botánico del Quindío","Jardín botánico con mariposario","Calarcá"),
 ("Pueblo de Salento","Calle Real, artesanías y miradores","Salento"),
 ("Fincas cafeteras de Quimbaya","Haciendas tradicionales y proceso del café","Quimbaya"),
]

# componente: nombre, desc, costo, tipo, prestador
componentes = [
 ("Hospedaje Hotel Las Palmas","Habitación doble con desayuno",300000,"hospedaje",3),
 ("Hospedaje Finca El Mirador","Cabaña para dos con desayuno",250000,"hospedaje",4),
 ("Traslados Quindío Express","Traslados en van con conductor",80000,"transporte",5),
 ("Almuerzo típico La Fonda","Bandeja paisa o trucha con bebida",45000,"alimentacion",6),
 ("Guianza Valle de Cocora","Guía local durante la caminata",60000,"actividad",7),
 ("Vuelo en parapente","Vuelo biplaza de 20 minutos",220000,"actividad",8),
 ("Tour finca cafetera","Proceso del café con degustación",70000,"actividad",7),
 ("Seguro de viaje","Asistencia médica durante el paquete",15000,"seguro",1),
 ("Entrada Ecoparque Los Guaduales","Entrada con atracciones",90000,"actividad",9),
 ("Avistamiento de aves","Guía ornitólogo y binoculares",85000,"actividad",21),
 ("Hospedaje Hotel Bosques","Habitación estándar con desayuno",220000,"hospedaje",14),
 ("Noche de glamping","Domo para dos con desayuno",380000,"hospedaje",15),
 ("Recorrido en Willys","Recorrido en campero Willys",50000,"transporte",17),
 ("Cena en Restaurante Helechos","Menú de la casa con postre",55000,"alimentacion",18),
 ("Almuerzo campestre El Cafetal","Asado con acompañamientos",40000,"alimentacion",19),
 ("Entrada al mariposario","Recorrido guiado por el mariposario",35000,"actividad",20),
 ("Hospedaje Finca La Cosecha","Habitación en finca cafetera",180000,"hospedaje",16),
 ("Seguro de aventura","Cobertura para deportes de aventura",25000,"seguro",2),
 ("Caminata cañón del Barbas","Guía y puentes colgantes",65000,"actividad",21),
 ("Taller de cestería","Taller con artesanos de Filandia",40000,"actividad",13),
 ("Traslado aeropuerto El Edén","Recogida en el aeropuerto",60000,"transporte",5),
 ("Cata de cafés especiales","Cata guiada de tres cafés",45000,"actividad",16),
]

# paquete: nombre, desc, dias, categoria, min, destino, prestador, comps, precio base, frecuencia
paquetes = [
 ("Ruta del café 3 días","Fincas cafeteras de Salento y caminata por el Valle de Cocora",3,"cafetero",4,1,1,[1,3,4,5,7,8],650000,1.6),
 ("Quindío de aventura 5 días","Parapente, cabalgata y día completo en el Parque del Café",5,"aventura",6,2,2,[11,3,6,9,18],1100000,1.0),
 ("Filandia patrimonial","Arquitectura colonial, mirador y taller de cestería",2,"cultural",2,3,1,[2,3,4,20],320000,0.9),
 ("Aves de Génova","Avistamiento de aves con guía especializado en Río Gris",2,"naturaleza",3,4,10,[2,10,3],380000,0.5),
 ("Familia en el Parque del Café","Plan familiar con atracciones, show del café y hospedaje",3,"familiar",4,2,1,[11,3,9,15],520000,1.3),
 ("Salento y Cocora exprés","Pasadía a Salento y caminata corta por el Valle de Cocora",1,"naturaleza",2,1,12,[13,5,4],180000,1.2),
 ("Jardín botánico y mariposas","Recorrido guiado por el jardín botánico y el mariposario",1,"naturaleza",2,6,2,[3,16],150000,0.0),
 ("Cañón del río Barbas","Caminata por el cañón y puentes colgantes",2,"aventura",4,5,11,[2,3,19],420000,0.0),
 ("Glamping y estrellas en Salento","Noche de glamping, fogata y caminata al amanecer",2,"naturaleza",2,7,12,[12,13,5],560000,0.8),
 ("Haciendas cafeteras de Quimbaya","Hospedaje en finca, cata de cafés y tour del grano a la taza",3,"cafetero",2,8,11,[17,22,7,3],590000,0.9),
 ("Quindío mágico 4 días","Cocora, Filandia, Parque del Café y cena típica",4,"cultural",5,1,11,[11,3,5,9,14,21],890000,1.0),
 ("Colonial de Filandia 2025","Recorrido histórico por Filandia (temporada 2025)",2,"cultural",3,3,13,[2,20,14],300000,0.7),
]
NPUB = len(paquetes)
dia_txt = ["Llegada, registro en el hotel y recorrido de bienvenida","Tour por finca cafetera y almuerzo típico",
    "Caminata guiada y actividad principal del paquete","Día libre con actividades opcionales","Cierre del recorrido y regreso"]

# ===================== planes =====================
planes = []   # dict
def add_plan(paq, nombre, desc, tipo, precio, ini, fin, estado_final="activo"):
    planes.append(dict(id=len(planes)+1, nombre=nombre, desc=desc, tipo=tipo, precio=precio, ini=ini, fin=fin,
                       estado_final=estado_final, paq=paq))
for i, p in enumerate(paquetes, start=1):
    base = p[8]
    if i == 12:   # agencia retirada: planes 2025, hoy inactivos
        add_plan(i, "Estándar 2025", "Hospedaje, transporte y guía", "estandar", base, date(2025,1,1), date(2025,12,31), "inactivo")
        add_plan(i, "Premium 2025", "Hotel boutique y guía bilingüe", "premium", round(base*1.35, -4), date(2025,1,1), date(2025,12,31), "inactivo")
        continue
    if i == 4:    # agencia suspendida: solo vendió en 2025
        add_plan(i, "Estándar", "Hospedaje, guía y binoculares", "estandar", base, date(2025,1,1), date(2026,12,31), "activo")
        add_plan(i, "Básico 2025", "Guía y binoculares, sin hospedaje", "basico", round(base*0.7, -4), date(2025,1,1), date(2025,12,31), "inactivo")
        continue
    add_plan(i, "Estándar", "Hospedaje, transporte y actividades", "estandar", base, date(2025,1,1), date(2026,12,31))
    add_plan(i, "Premium", "Hotel 4 estrellas, guía bilingüe y seguro", "premium", round(base*1.4, -4), date(2025,1,1), date(2026,12,31))
    if i in (1, 2, 5, 11):
        add_plan(i, "Todo incluido", "Hospedaje, todas las comidas y todas las actividades", "todo incluido",
                 round(base*1.7, -4), date(2025,1,1), date(2026,12,31))
    if i in (1, 5, 6, 9, 10):
        add_plan(i, "Promo temporada baja 2025", "Precio especial de temporada baja", "basico",
                 round(base*0.8, -4), date(2025,2,1), date(2025,5,31), "inactivo")
    if i in (1, 2, 11):
        add_plan(i, "Navidad 2026", "Precio de temporada navideña con cena especial", "premium",
                 round(base*1.5, -4), date(2026,12,1), date(2027,1,15))
    if i in (3, 8):
        add_plan(i, "Grupo empresarial", "Plan para grupos de empresa", "estandar",
                 round(base*0.9, -4), date(2025,1,1), date(2026,12,31), "inactivo")

# ===================== salidas =====================
salidas = []   # dict id, fecha, cupo, paq
def add_salida(paq, fecha, cupo):
    salidas.append(dict(id=len(salidas)+1, fecha=fecha, cupo=cupo, paq=paq, reservas=[]))
    return salidas[-1]
inicio, fin = date(2025, 1, 10), date(2026, 12, 30)
for i, p in enumerate(paquetes, start=1):
    freq = p[9]
    if freq == 0:   # paquetes nunca vendidos: dos salidas programadas sin reservas
        add_salida(i, date(2026, 11, 28) if i == 7 else date(2026, 12, 12), 15)
        add_salida(i, date(2026, 12, 19) if i == 7 else date(2027, 1, 9) if False else date(2026, 12, 26), 15)
        continue
    x = inicio + timedelta(days=random.randint(0, 20))
    while x <= fin:
        if i == 12 and x > date(2025, 12, 20): break
        if i == 4 and x > date(2025, 12, 20) and len([s for s in salidas if s['paq']==4 and s['fecha']>HOY]) >= 2: break
        if i == 4 and date(2025, 12, 20) < x <= HOY + timedelta(days=12): x += timedelta(days=30); continue
        cupo = random.choice([12, 15, 16, 20, 20, 24, 30]) if p[4] > 2 else random.choice([8, 10, 12, 15])
        add_salida(i, x, cupo)
        gap = 58 / (freq * PESO[temporada(x)] ** 0.9)
        x += timedelta(days=max(6, int(random.gauss(gap, gap * 0.25))))

# salidas extra a finales de noviembre y comienzos de diciembre (reportes 5, 9 y 10 en la sustentación)
for paq, f, cupo in [(1, date(2026, 11, 26), 20), (5, date(2026, 11, 29), 20), (6, date(2026, 12, 2), 12),
                     (11, date(2026, 11, 30), 16), (3, date(2026, 12, 4), 12), (2, date(2026, 12, 5), 20)]:
    add_salida(paq, f, cupo)

# ===================== clientes y pasajeros =====================
nombres_m = ["Juan David","Carlos Andrés","Santiago","Andrés Felipe","Sebastián","Mateo","Julián","Diego","Felipe","Alejandro",
 "Tomás","Samuel","Nicolás","Martín","David","Esteban","Simón","Emilio","Jerónimo","Miguel Ángel","Pablo","Joaquín","Ricardo",
 "Hernán","Fernando","Jorge","Óscar","Luis Carlos","Camilo","Daniel","Gustavo","Mauricio","Germán","Álvaro","Héctor","Iván"]
nombres_f = ["María Fernanda","Laura","Valentina","Daniela","Camila","Paula Andrea","Natalia","Isabella","Manuela","Sara","Sofía",
 "Mariana","Gabriela","Lucía","Ana María","Juliana","Catalina","Luisa","Antonia","Valeria","Salomé","Luciana","Elena","Victoria",
 "Adriana","Gloria","Patricia","Claudia","Beatriz","Diana","Carolina","Marcela","Lina","Ángela","Viviana","Alejandra"]
apellidos = ["Gómez","Rodríguez","Martínez","García","Hernández","López","Ramírez","Torres","Sánchez","Díaz","Restrepo","Moreno",
 "Muñoz","Rojas","Álvarez","Jaramillo","Castaño","Osorio","Marín","Ocampo","Giraldo","Henao","Cardona","Zuluaga","Arango",
 "Valencia","Quintero","Patiño","Londoño","Salazar","Mejía","Vélez","Gil","Ríos","Toro","Duque","Bedoya","Echeverri","Montoya","Agudelo"]
ciudades_co = ["Bogotá"]*14 + ["Medellín"]*12 + ["Cali"]*10 + ["Pereira"]*7 + ["Manizales"]*5 + ["Armenia"]*4 + \
    ["Bucaramanga"]*3 + ["Barranquilla"]*3 + ["Ibagué"]*3 + ["Cartagena"]*2 + ["Neiva"]*2 + ["Villavicencio"]*2 + ["Tuluá", "Calarcá", "Pasto", "Cúcuta"]
extranjeros = [("Emma","Schmidt","Berlín, Alemania"),("John","Miller","Miami, Estados Unidos"),("Sophie","Martin","París, Francia"),
 ("Lucas","Dubois","Lyon, Francia"),("Ana","Fernández Ruiz","Madrid, España"),("Javier","Moreno Soto","Barcelona, España"),
 ("Camila","Rivas","Quito, Ecuador"),("Mateo","Paredes","Lima, Perú"),("Martina","Gutiérrez","Buenos Aires, Argentina"),
 ("Diego","Fuentes","Santiago, Chile"),("Olivia","Brown","Toronto, Canadá"),("Liam","Wilson","Nueva York, Estados Unidos"),
 ("Valeria","Ortega Luna","Ciudad de México, México"),("Hannah","Becker","Múnich, Alemania"),("Giulia","Romano","Roma, Italia"),
 ("Pedro","Almeida","São Paulo, Brasil"),("Chloé","Lefebvre","Montreal, Canadá"),("Daniel","Castillo","Caracas, Venezuela")]
dominios = ["gmail.com"]*6 + ["hotmail.com"]*3 + ["outlook.com"]*2 + ["yahoo.com"]

def slug(s): return re.sub(r"[^a-z]", "", s.lower().translate(str.maketrans("áéíóúñü", "aeiounu")))
correos, docs_cli = set(), set()
def nuevo_correo(nb, ap):
    base = f"{slug(nb.split()[0])}.{slug(ap.split()[0])}"
    c = f"{base}@{random.choice(dominios)}"; n = 1
    while c in correos:
        n += 1; c = f"{base}{n}@{random.choice(dominios)}"
    correos.add(c); return c
def celular(): return "3" + random.choice("0123512") + str(random.randint(10000000, 99999999))

N_CLI = 150
clientes = []
ext_idx = 0
for i in range(N_CLI):
    if i % 9 == 4 and ext_idx < len(extranjeros):
        nb, ap, ciudad = extranjeros[ext_idx]; ext_idx += 1
        td = "pasaporte" if ext_idx % 3 else "CE"
        doc = (f"{random.choice('ABCDEFGHKLMNPRSTXY')}{random.choice('ABCDEFGHKLMNPRSTXY')}{random.randint(1000000, 9999999)}"
               if td == "pasaporte" else str(random.randint(300000, 999999)))
    else:
        mujer = random.random() < 0.52
        nb = random.choice(nombres_f if mujer else nombres_m)
        ap = f"{random.choice(apellidos)} {random.choice(apellidos)}"
        ciudad = random.choice(ciudades_co); td = "CC"
        doc = str(random.choice([random.randint(10000000, 99999999), random.randint(1000000000, 1199999999)]))
    while (td, doc) in docs_cli: doc = str(int(doc) + 7) if doc.isdigit() else doc[:2] + str(int(doc[2:]) + 7)
    docs_cli.add((td, doc))
    nac = date(random.randint(1955, 2004), random.randint(1, 12), random.randint(1, 28))
    clientes.append(dict(id=i+1, nb=nb, ap=ap, td=td, doc=doc, correo=nuevo_correo(nb, ap), tel=celular(), nac=nac,
                         ciudad=ciudad, ext=td != "CC"))
# clientes de prueba fijos (para la guía): el 1 con cuenta, promociones sí
correos.discard(clientes[0]["correo"])
clientes[0].update(nb="María Fernanda", ap="Gómez Ruiz", correo="maria.gomez@gmail.com")
correos.add("maria.gomez@gmail.com")

perfiles = {}
for c in clientes:
    if c["id"] == 1 or random.random() < 0.85:
        perfiles[c["id"]] = dict(idioma="ingles" if c["ext"] and random.random() < 0.6 else "espanol",
            canal=random.choices(["correo", "sms", "whatsapp"], [5, 2, 4])[0], dias=random.choice([1, 3, 3, 7]),
            promos="si" if c["id"] == 1 or random.random() < 0.55 else "no")

pasajeros = []   # dict id, nb, ap, td, doc, nac
docs_pas = set()
def add_pasajero(nb, ap, td, doc, nac):
    while (td, doc) in docs_pas: doc = str(int(doc) + 3) if doc.isdigit() else doc[:2] + str(int(doc[2:]) + 3)
    docs_pas.add((td, doc))
    pasajeros.append(dict(id=len(pasajeros)+1, nb=nb, ap=ap, td=td, doc=doc, nac=nac)); return len(pasajeros)
titular = {}
for c in clientes:
    titular[c["id"]] = add_pasajero(c["nb"], c["ap"], c["td"], c["doc"], c["nac"])
acomp = defaultdict(list)   # acompañantes fijos de cada cliente (familia y amigos)
def acompanantes(cli, n, fecha_viaje):
    """Devuelve n-1 acompañantes del cliente, creándolos si hace falta."""
    c = clientes[cli-1]
    while len(acomp[cli]) < n - 1:
        k = len(acomp[cli])
        ap1 = c["ap"].split()[0]
        if k == 0:   # pareja
            nb = random.choice(nombres_m + nombres_f); ap = f"{random.choice(apellidos)} {random.choice(apellidos)}"
            nac = date(min(2004, c["nac"].year + random.randint(-4, 4)), random.randint(1, 12), random.randint(1, 28)); td = c["td"] if c["ext"] else "CC"
        elif k in (1, 2, 3) and random.random() < 0.6:   # hijos
            nb = random.choice(nombres_m + nombres_f); ap = f"{ap1} {random.choice(apellidos)}"
            nac = date(min(2022, max(c["nac"].year + 22, 2006) + random.randint(0, 12)), random.randint(1, 12), random.randint(1, 28))
            edad = (date(2026, 1, 1) - nac).days / 365.25
            td = "RC" if edad < 7 else "TI" if edad < 18 else "CC"
            if c["ext"]: td = "pasaporte"
        else:        # amigo o familiar adulto
            nb = random.choice(nombres_m + nombres_f); ap = f"{random.choice(apellidos)} {random.choice(apellidos)}"
            nac = date(random.randint(1958, 2003), random.randint(1, 12), random.randint(1, 28)); td = "CC"
        doc = (f"{random.choice('ABCDEFGHKLMNPRSTXY')}{random.choice('ABCDEFGHKLMNPRSTXY')}{random.randint(1000000, 9999999)}"
               if td == "pasaporte" else str(random.randint(1000000000, 1199999999)))
        acomp[cli].append(add_pasajero(nb, ap, td, doc, nac))
    return acomp[cli][:n-1]

# ===================== cotizaciones =====================
# número, fecha, estado, prestador (agencia), cliente, líneas [(componente, días después, personas, valor)]
cotizaciones = []
def add_cot(fecha, estado, prest, cli, lineas):
    n = len(cotizaciones) + 1
    cotizaciones.append(dict(id=n, num=f"COT-{100 + n:06d}", fecha=fecha, estado=estado, prest=prest, cli=cli,
        lineas=[dict(comp=c, fecha=fecha + timedelta(days=dd), pers=pp, valor=vv) for c, dd, pp, vv in lineas]))
    return cotizaciones[-1]
plantillas_cot = [[(1,20,2,600000),(3,20,2,160000),(5,21,2,120000),(7,22,2,140000)],
                  [(6,25,2,440000),(11,25,2,440000),(3,25,2,160000)],
                  [(12,15,2,760000),(13,16,2,100000)],
                  [(17,30,4,720000),(22,31,4,180000),(7,31,4,280000)],
                  [(2,18,3,750000),(20,19,3,120000),(14,19,3,165000)],
                  [(10,22,2,170000),(2,22,2,500000)]]
aceptadas = []
fechas_cot = sorted(date(2025, 1, 15) + timedelta(days=random.randint(0, 640)) for _ in range(30))
for k, f in enumerate(fechas_cot):
    cli = random.randint(2, N_CLI); prest = random.choice([1, 2, 11, 12])
    lin = plantillas_cot[k % len(plantillas_cot)]
    if k % 5 == 0 and f + timedelta(days=20) > date(2025, 2, 1) and len(aceptadas) < 6 and f < date(2026, 9, 1):
        estado = "aceptada"
    else:
        estado = random.choices(["rechazada", "vencida"], [2, 3])[0]
    aceptadas.append(add_cot(f, estado, prest, cli, lin)) if estado == "aceptada" else add_cot(f, estado, prest, cli, lin)
for f, est in [(date(2026, 11, 4), "enviada"), (date(2026, 11, 6), "enviada"), (date(2026, 11, 9), "enviada"),
               (date(2026, 11, 9), "borrador"), (date(2026, 11, 10), "borrador")]:
    add_cot(f, est, random.choice([1, 2, 11, 12]), random.randint(2, N_CLI), random.choice(plantillas_cot))
aceptadas = [c for c in cotizaciones if c["estado"] == "aceptada"]

# paquete privado + plan + salida por cada cotización aceptada (decisión 7)
privados = []
for c in aceptadas:
    pid = len(paquetes) + 1
    personas = c["lineas"][0]["pers"]; total = sum(l["valor"] for l in c["lineas"])
    precio = total // personas; total = precio * personas
    c["valor_reserva"] = total
    fecha_sal = c["lineas"][0]["fecha"]
    dias = max(1, (max(l["fecha"] for l in c["lineas"]) - fecha_sal).days + 1)
    paquetes.append((f"Viaje a la medida {c['num']}", f"Paquete privado creado al aceptar la cotización {c['num']}",
                     dias, "cafetero", 1, 1, c["prest"], sorted({l["comp"] for l in c["lineas"]}), precio, 0))
    add_plan(pid, f"Plan a la medida {c['num']}", "Plan acordado en la cotización", "premium", precio,
             c["fecha"], fecha_sal + timedelta(days=30))
    s = add_salida(pid, fecha_sal, personas)
    privados.append(dict(paq=pid, cot=c, salida=s["id"], plan=len(planes), pers=personas))

# ===================== reservas =====================
canales_peso = (["web"]*5 + ["aplicacion movil"]*3 + ["telefono"]*2 + ["mostrador"]*2 + ["agencia intermediaria"]*2)
reservas = []   # dict
res_pas = []
cli_salidas = defaultdict(set)
pl_por_paq = defaultdict(list)
for p in planes: pl_por_paq[p["paq"]].append(p)

def crear_reserva(s, cli, n, plan, fc, canal, cot=None, valor=None):
    pax = [titular[cli]] + acompanantes(cli, n, s["fecha"])
    r = dict(id=len(reservas)+1, fc=fc, plan=plan["id"], salida=s["id"], cli=cli, cot=cot,
             valor=valor if valor else plan["precio"] * n, canal=canal, pax=pax, n=n, final=None,
             canc=None, reemb=None)
    reservas.append(r); s["reservas"].append(r["id"]); cli_salidas[cli].add(s["id"])
    res_pas.extend((r["id"], p) for p in pax)
    return r

# las privadas primero
for pv in privados:
    s = salidas[pv["salida"]-1]
    c = pv["cot"]
    fc = min(c["fecha"] + timedelta(days=random.randint(1, 6)), HOY - timedelta(days=1))
    crear_reserva(s, c["cli"], pv["pers"], planes[pv["plan"]-1], fc, "agencia intermediaria", c["id"], c["valor_reserva"])

# públicas: reparto proporcional a temporada y frecuencia del paquete hasta llegar a ~520
publicas = [s for s in salidas if s["paq"] <= NPUB and paquetes[s["paq"]-1][9] > 0]
objetivo = 520 - len(reservas)
pesos = [PESO[temporada(s["fecha"])] * (1.0 if s["fecha"] < HOY else 1.6) for s in publicas]
intentos = 0
while len(reservas) < 520 and intentos < 20000:
    intentos += 1
    s = random.choices(publicas, pesos)[0]
    usados = sum(reservas[r-1]["n"] for r in s["reservas"])
    n = random.choices([1, 2, 2, 2, 3, 3, 4, 4, 5, 6], k=1)[0]
    if usados + n > s["cupo"] - (0 if s["fecha"] < HOY else random.choice([0, 1, 2, 4])): continue
    cli = random.randint(1, N_CLI)
    if s["id"] in cli_salidas[cli]: continue
    if cli == 1 and len([1 for r in reservas if r["cli"] == 1]) >= 4: continue
    fc = s["fecha"] - timedelta(days=random.randint(6, 75))
    if fc >= HOY: fc = HOY - timedelta(days=random.randint(1, 5))
    if fc < date(2024, 12, 1): fc = date(2024, 12, 1) + timedelta(days=random.randint(0, 20))
    vigentes = [p for p in pl_por_paq[s["paq"]] if p["ini"] <= s["fecha"] <= p["fin"]]
    if not vigentes: continue
    plan = random.choices(vigentes, [3 if p["tipo"] in ("estandar", "basico") else 1.2 for p in vigentes])[0]
    crear_reserva(s, cli, n, plan, fc, random.choice(canales_peso))

# escenario fijo para la demostración: una salida de "Ruta del café" del 06/11 a 2 cupos de llenarse
# (se arma ajustando su cupo al número vendido + 2) y otras dos bajo el mínimo
futuras = [s for s in salidas if s["fecha"] > HOY and s["paq"] <= NPUB]

# ===================== estados de salidas y reservas =====================
for r in reservas:
    r["cancela"] = r["cot"] is None and random.random() < 0.06
for s in salidas:
    pax = sum(reservas[r-1]["n"] for r in s["reservas"] if not reservas[r-1]["cancela"])
    minimo = paquetes[s["paq"]-1][4]
    if s["fecha"] < HOY:
        if pax >= minimo and pax > 0 and random.random() > 0.03:
            s["final"] = "realizada"
        else:
            s["final"] = "cancelada"
    else:
        dias = (s["fecha"] - HOY).days
        s["final"] = "confirmada" if pax >= minimo and dias <= 25 and random.random() < 0.6 else "programada"

pagos = []   # dict
metodos = ["tarjeta de credito"]*4 + ["pse"]*4 + ["tarjeta debito"]*2 + ["transferencia"]*2 + ["efectivo"]*2
def pago(r, fecha, monto, metodo, estado):
    fecha = min(max(fecha, r["fc"]), HOY)
    pagos.append(dict(id=len(pagos)+1, fecha=fecha, monto=int(monto), metodo=metodo, estado=estado, res=r["id"]))

for r in reservas:
    s = salidas[r["salida"]-1]; v = r["valor"]
    pasada = s["fecha"] < HOY
    cancela = r["cancela"] and s["final"] != "cancelada"
    # pagos
    if random.random() < 0.12:   # pago fallido antes
        pago(r, r["fc"], v * random.choice([3, 5, 10]) // 10, random.choice(["tarjeta de credito", "pse"]), "fallido")
    if pasada and s["final"] == "realizada" and not cancela:
        if random.random() < 0.45:
            a = round(v * random.choice([0.3, 0.4, 0.5]), -3)
            pago(r, r["fc"] + timedelta(days=random.randint(0, 2)), a, random.choice(metodos), "exitoso")
            pago(r, s["fecha"] - timedelta(days=random.randint(0, 3)), v - a, random.choice(metodos), "exitoso")
        else:
            pago(r, r["fc"] + timedelta(days=random.randint(0, 2)), v, random.choice(metodos), "exitoso")
        r["final"] = "completada"
    elif s["final"] == "cancelada":
        if random.random() < 0.7:
            pago(r, r["fc"] + timedelta(days=1), round(v * 0.3, -3), random.choice(metodos), "exitoso")
        r["final"] = "cancelada"
        ultimo = max([p["fecha"] for p in pagos if p["res"] == r["id"]] + [r["fc"]])
        r["canc"] = max(min(s["fecha"] - timedelta(days=random.randint(6, 12)), HOY - timedelta(days=1)), ultimo)
        r["por_agencia"] = True
    else:
        modo = random.choices(["nada", "anticipo", "total"], [2, 4, 4])[0] if not pasada else "total"
        if r["cot"]: modo = "anticipo"
        if modo == "anticipo":
            pago(r, r["fc"] + timedelta(days=random.randint(0, 2)), round(v * random.choice([0.3, 0.4, 0.5]), -3),
                 random.choice(metodos), "exitoso")
        elif modo == "total":
            pago(r, r["fc"] + timedelta(days=random.randint(0, 2)), v, random.choice(metodos), "exitoso")
        elif random.random() < 0.6:
            pago(r, r["fc"], round(v * 0.3, -3), "pse", "pendiente")
        r["final"] = "confirmada" if modo != "nada" else "pendiente"
        if cancela:
            r["final"] = "cancelada"
            lim = min(s["fecha"] - timedelta(days=1), HOY - timedelta(days=1))
            ultimo = max([p["fecha"] for p in pagos if p["res"] == r["id"]] + [r["fc"]])
            if lim > ultimo:
                r["canc"] = ultimo + timedelta(days=random.randint(1, (lim - ultimo).days))
            else:
                r["final"] = "confirmada" if modo != "nada" else "pendiente"
            r["por_agencia"] = False
    if r["final"] == "cancelada":
        pagado = sum(p["monto"] for p in pagos if p["res"] == r["id"] and p["estado"] == "exitoso")
        if r.get("por_agencia"): r["reemb"] = pagado
        else: r["reemb"] = round(pagado * 0.8) if (s["fecha"] - r["canc"]).days > 5 else 0
# reservas canceladas cuyos pagos fueron devueltos por la agencia: estado reembolsado
for r in reservas:
    if r["final"] == "cancelada" and r.get("por_agencia"):
        for p in pagos:
            if p["res"] == r["id"] and p["estado"] == "exitoso": p["estado"] = "reembolsado"
        r["reemb"] = sum(p["monto"] for p in pagos if p["res"] == r["id"] and p["estado"] == "reembolsado")

# completar pagos hasta ≥ 500 con abonos en las futuras con anticipo
pagos.sort(key=lambda p: (p["fecha"], p["res"]))
for i, p in enumerate(pagos, 1): p["id"] = i

# ===================== notificaciones =====================
notifs = []
def notif(tipo, canal, msg, prog, envio, estado, cli, res):
    notifs.append(dict(tipo=tipo, canal=canal, msg=msg[:500], prog=prog, envio=envio, estado=estado, cli=cli, res=res))
def canal_de(cli): return perfiles[cli]["canal"] if cli in perfiles else "correo"
def dias_de(cli): return perfiles[cli]["dias"] if cli in perfiles else 3
def enviado(prog):
    if prog.date() > HOY: return None, "pendiente"
    e = random.random()
    if e < 0.06: return None, "fallida"
    return prog + timedelta(minutes=random.randint(0, 20)), "leida" if random.random() < 0.55 else "enviada"
for r in reservas:
    s = salidas[r["salida"]-1]; p = paquetes[s["paq"]-1]; cli = r["cli"]; c = clientes[cli-1]
    if r["final"] in ("confirmada", "completada") or (r["final"] == "cancelada" and r["id"] % 2):
        prog = dt(r["fc"], random.randint(8, 19), random.randint(0, 59))
        e, est = enviado(prog)
        notif("confirmacion de reserva", canal_de(cli), f"{c['nb']}, tu reserva {r['id']} para \"{p[0]}\" del {s['fecha'].strftime('%d/%m/%Y')} quedó registrada.",
              prog, e, est, cli, r["id"])
    if r["final"] != "cancelada":
        prog = dt(s["fecha"] - timedelta(days=dias_de(cli)))
        e, est = enviado(prog)
        notif("recordatorio de reserva", canal_de(cli), f"Hola {c['nb']}, tu viaje \"{p[0]}\" sale el {s['fecha'].strftime('%d/%m/%Y')}. ¡Te esperamos!",
              prog, e, est, cli, r["id"])
    else:
        prog = dt(r["canc"], 10)
        notif("cambio de estado", canal_de(cli), f"Tu reserva {r['id']} fue cancelada. Reembolso: ${r['reemb']:,}".replace(",", "."),
              prog, prog + timedelta(minutes=2), "enviada", cli, r["id"])
for pg in pagos:
    if pg["estado"] in ("exitoso", "reembolsado") and random.random() < 0.55:
        r = reservas[pg["res"]-1]; prog = dt(pg["fecha"], random.randint(8, 20), random.randint(0, 59))
        e, est = enviado(prog)
        notif("confirmacion de pago", canal_de(r["cli"]), f"Recibimos tu pago de ${pg['monto']:,} para la reserva {r['id']}.".replace(",", "."),
              prog, e, est, r["cli"], r["id"])
campanas = [(date(2025, 3, 1), "Semana Santa en el Quindío: 15 % de descuento en paquetes cafeteros"),
            (date(2025, 5, 20), "Vacaciones de mitad de año: niños gratis en Familia en el Parque del Café"),
            (date(2025, 11, 15), "Diciembre en el Quindío: reserva tu plan navideño"),
            (date(2026, 3, 1), "Semana Santa 2026: paquetes desde $180.000"),
            (date(2026, 5, 25), "Mitad de año: Quindío de aventura con seguro incluido"),
            (date(2026, 11, 15), "Navidad 2026: Ruta del café con cena especial")]
acepta = [cid for cid, p in perfiles.items() if p["promos"] == "si"]
for f, msg in campanas:
    for cli in random.sample(acepta, min(len(acepta), 30)):
        prog = dt(f, 9)
        e, est = enviado(prog)
        notif("promocion", canal_de(cli), msg, prog, e, est, cli, None)

# ===================== verificaciones =====================
ER = []
def chk(c, m):
    if not c: ER.append(m)
cnt = defaultdict(int)
for rid, p in res_pas:
    if reservas[rid-1]["final"] != "cancelada": cnt[reservas[rid-1]["salida"]] += 1
for s in salidas:
    chk(cnt[s["id"]] <= s["cupo"], f"cupo salida {s['id']}")
    if s["final"] in ("realizada", "confirmada"): chk(cnt[s["id"]] >= paquetes[s["paq"]-1][4], f"min salida {s['id']}")
chk(len(set(res_pas)) == len(res_pas), "rp duplicado")
for r in reservas:
    pl = planes[r["plan"]-1]; s = salidas[r["salida"]-1]
    chk(pl["paq"] == s["paq"], f"RN14 {r['id']}")
    chk(pl["ini"] <= s["fecha"] <= pl["fin"], f"RN10 {r['id']}")
    chk(r["fc"] < HOY and r["fc"] < s["fecha"], f"fc {r['id']}")
    chk(r["valor"] == pl["precio"] * r["n"], f"RN19 {r['id']}")
    edades = [(s["fecha"] - pasajeros[p-1]["nac"]).days / 365.25 for p in r["pax"]]
    chk(edades[0] >= 18, f"titular menor {r['id']}")
    if r["final"] == "cancelada": chk(r["canc"] is not None and r["fc"] <= r["canc"] < s["fecha"], f"canc {r['id']}")
pag = defaultdict(int)
for p in pagos:
    r = reservas[p["res"]-1]
    chk(r["fc"] <= p["fecha"] <= HOY, f"fecha pago {p['id']}")
    if r["canc"]: chk(p["fecha"] <= r["canc"], f"pago tras cancelar {p['id']}")
    if p["estado"] == "exitoso": pag[p["res"]] += p["monto"]
    chk(p["monto"] > 0, "monto")
for k, v in pag.items(): chk(v <= reservas[k-1]["valor"], f"RN21 {k}")
for c in cotizaciones:
    for l in c["lineas"]: chk(l["fecha"] > c["fecha"], f"RN25 {c['num']}")
    if c["estado"] in ("enviada", "borrador"): chk((HOY - c["fecha"]).days <= 8, f"vigencia {c['num']}")
for n in notifs:
    if n["canal"] != "correo": chk(n["cli"] in perfiles, "RN06")
    if n["tipo"] == "promocion": chk(perfiles[n["cli"]]["promos"] == "si", "RN07")
    if n["res"]: chk(reservas[n["res"]-1]["cli"] == n["cli"], "R3")
    if n["envio"]: chk(n["envio"] >= n["prog"], "envio")
for p in pasajeros:
    edad = (date(2026, 1, 1) - p["nac"]).days / 365.25
    chk(not (p["td"] == "CC" and edad < 18), f"CC menor {p['id']}")
chk(len(correos) == N_CLI, "correos")
if ER:
    print("ERRORES:", len(ER), ER[:15]); sys.exit(1)

# ===================== SQL =====================
out = []
w = out.append
def ins_all(tabla, cols, filas, lote=80):
    w(f"-- {tabla.upper()} ({len(filas)} filas)")
    for i in range(0, len(filas), lote):
        w("INSERT ALL")
        for f in filas[i:i+lote]:
            w(f"  INTO {tabla} ({', '.join(cols)}) VALUES ({', '.join(f)})")
        w("SELECT 1 FROM dual;")
    w("")
def en_lotes(ids, plantilla):
    ids = list(ids)
    for i in range(0, len(ids), 500):
        w(plantilla.format(", ".join(map(str, ids[i:i+500]))))

w(f"""-- =====================================================================
-- QuindioStay - 03_datos.sql
-- Datos de ejemplo verosímiles y asimétricos (enunciado, sección 4).
-- Correr como QUINDIOSTAY con "Run Script" después de 00_reset, 01_ddl y 02_triggers.
-- Generado con un programa en Python (sql/generador/), con fecha de referencia {HOY.isoformat()}.

-- Cantidades (mínimo del enunciado entre paréntesis):
--   MUNICIPIO {len(municipios)} (12)  TIPO_PRESTADOR {len(tipos_prest)} (4)  PRESTADOR {NP} (20)
--   CLIENTE {N_CLI} (150), {len(perfiles)} con cuenta y {sum(1 for c in clientes if c['ext'])} extranjeros
--   RESERVA {len(reservas)} (500) entre 2025 y 2026, más en temporada alta
--   PAGO {len(pagos)} (500) con anticipos, saldos, fallidos y pendientes
--   PLAN {len(planes)} (40), algunos inactivos o vencidos
--   NOTIFICACION {len(notifs)} (800) de todos los tipos y canales
--   USUARIO_SISTEMA {len(usuarios)} (5)

-- Contraseña de TODOS los usuarios internos y cuentas de cliente: Clave123
--   Internos: jpespinosa, jjospina (admin) · mabetancourt, operador1 (operador)
--             cafeymontana, quindioaventura, rutapaisa (encargados de prestador)
--   Cliente:  maria.gomez@gmail.com

-- Orden de carga: las reservas entran "pendiente" y las salidas "programada";
-- al final se pasan a su estado real respetando las reglas (RN17, RN18, RN28).
-- =====================================================================
SET DEFINE OFF
""")
ins_all("municipio", ["id_municipio","codigo_dane","nombre"], [[str(i+1), q(c), q(n)] for i, (c, n) in enumerate(municipios)])
ins_all("tipo_prestador", ["id_tipo_prestador","nombre"], [[str(i+1), q(n)] for i, n in enumerate(tipos_prest)])
ins_all("rol", ["id_rol","nombre"], [[str(i+1), q(n)] for i, n in enumerate(roles)])
ins_all("tipo_componente", ["id_tipo_componente","nombre"], [[str(i+1), q(n)] for i, n in enumerate(tipos_comp)])
ins_all("prestador", ["id_prestador","nombre_comercial","nit","numero_rnt","direccion","telefono","correo","estado","id_tipo_prestador","id_municipio"],
    [[str(i+1), q(p[0]), q(p[1]), q(p[2]), q(p[3]), q(p[4]), q(p[5]), q("activo"), str(TP[p[7]]), str(MUN[p[8]])] for i, p in enumerate(prestadores)])
ins_all("usuario_sistema", ["id_usuario","nombre_usuario","contrasena_cifrada","estado","id_rol","id_prestador"],
    [[str(i+1), q(u), q(HASH), q("activo"), str(r), nul(p)] for i, (u, r, p) in enumerate(usuarios)])
ins_all("destino", ["id_destino","nombre","descripcion","id_municipio"], [[str(i+1), q(a), q(b), str(MUN[c])] for i, (a, b, c) in enumerate(destinos)])
ins_all("paquete", ["id_paquete","nombre","descripcion","duracion_dias","categoria","minimo_viajeros","visibilidad","id_destino","id_prestador"],
    [[str(i+1), q(p[0]), q(p[1]), str(p[2]), q(p[3]), str(p[4]), q("publico" if i < NPUB else "privado"), str(p[5]), str(p[6])] for i, p in enumerate(paquetes)])
itin = []
for i, p in enumerate(paquetes):
    for dn in range(1, p[2] + 1):
        if p[2] == 1: txt = "Salida en la mañana, actividad principal y regreso en la tarde"
        elif dn == 1: txt = dia_txt[0]
        elif dn == p[2]: txt = dia_txt[4]
        else: txt = dia_txt[min(dn - 1, 3)]
        itin.append([str(i+1), str(dn), q(txt)])
ins_all("itinerario_dia", ["id_paquete","numero_dia","descripcion"], itin)
ins_all("plan", ["id_plan","nombre","descripcion","tipo_plan","precio","fecha_inicio_vigencia","fecha_fin_vigencia","estado","id_paquete"],
    [[str(p["id"]), q(p["nombre"]), q(p["desc"]), q(p["tipo"]), str(int(p["precio"])), d(p["ini"]), d(p["fin"]), q("activo"), str(p["paq"])] for p in planes])
ins_all("componente", ["id_componente","nombre","descripcion","costo","id_tipo_componente","id_prestador"],
    [[str(i+1), q(c[0]), q(c[1]), str(c[2]), str(TC[c[3]]), str(c[4])] for i, c in enumerate(componentes)])
ins_all("paquete_componente", ["id_paquete","id_componente"], [[str(i+1), str(c)] for i, p in enumerate(paquetes) for c in p[7]])
ins_all("salida", ["id_salida","fecha_inicio","cupo_maximo","estado","id_paquete"],
    [[str(s["id"]), d(s["fecha"]), str(s["cupo"]), q("programada"), str(s["paq"])] for s in salidas])
ins_all("cliente", ["id_cliente","nombres","apellidos","tipo_documento","numero_documento","correo","telefono","fecha_nacimiento","ciudad_origen"],
    [[str(c["id"]), q(c["nb"]), q(c["ap"]), q(c["td"]), q(c["doc"]), q(c["correo"]), q(c["tel"]), d(c["nac"]), q(c["ciudad"])] for c in clientes])
ins_all("perfil_cliente", ["id_perfil","contrasena_cifrada","idioma","canal_preferido","dias_anticipacion","acepta_promociones","id_cliente"],
    [[str(i+1), q(HASH), q(p["idioma"]), q(p["canal"]), str(p["dias"]), q(p["promos"]), str(cid)] for i, (cid, p) in enumerate(sorted(perfiles.items()))])
ins_all("pasajero", ["id_pasajero","nombres","apellidos","tipo_documento","numero_documento","fecha_nacimiento"],
    [[str(p["id"]), q(p["nb"]), q(p["ap"]), q(p["td"]), q(p["doc"]), d(p["nac"])] for p in pasajeros])
ins_all("cotizacion", ["id_cotizacion","numero_cotizacion","fecha","valor_total","estado","id_prestador","id_cliente"],
    [[str(c["id"]), q(c["num"]), d(c["fecha"]), str(sum(l["valor"] for l in c["lineas"])), q(c["estado"]), str(c["prest"]), str(c["cli"])] for c in cotizaciones])
ins_all("detalle_cotizacion", ["id_cotizacion","numero_linea","fecha_servicio","cantidad_personas","valor","id_componente"],
    [[str(c["id"]), str(k+1), d(l["fecha"]), str(l["pers"]), str(l["valor"]), str(l["comp"])] for c in cotizaciones for k, l in enumerate(c["lineas"])])
ins_all("reserva", ["id_reserva","fecha_creacion","valor_total","estado","canal","id_plan","id_salida","id_cliente","id_cotizacion"],
    [[str(r["id"]), d(r["fc"]), str(int(r["valor"])), q("pendiente"), q(r["canal"]), str(r["plan"]), str(r["salida"]), str(r["cli"]), nul(r["cot"])] for r in reservas])
w("-- Pasajeros: en cada reserva va primero el titular (adulto), por la regla RN30")
ins_all("reserva_pasajero", ["id_reserva","id_pasajero"], [[str(a), str(b)] for a, b in res_pas])
w("-- Pagos: se cargan como exitosos los que luego la agencia reembolsó (se actualizan al cancelar)")
ins_all("pago", ["id_pago","fecha_pago","monto","metodo_pago","estado_pago","id_reserva"],
    [[str(p["id"]), d(p["fecha"]), str(p["monto"]), q(p["metodo"]), q("exitoso" if p["estado"] == "reembolsado" else p["estado"]), str(p["res"])] for p in pagos])
enl = [p for p in pagos if p["metodo"] in ("tarjeta de credito", "tarjeta debito", "pse")]
refs = set()
filas = []
for p in enl:
    pref = "PSE" if p["metodo"] == "pse" else "TC" if "credito" in p["metodo"] else "TD"
    ref = f"{pref}-{p['fecha'].strftime('%y%m%d')}-{random.randrange(16**6):06X}"
    while ref in refs: ref = f"{pref}-{p['fecha'].strftime('%y%m%d')}-{random.randrange(16**6):06X}"
    refs.add(ref); filas.append([str(p["id"]), q(ref)])
ins_all("pago_en_linea", ["id_pago","referencia_transaccion"], filas)

w("-- ================ Estados finales ================")
w("-- Reservas: pendiente -> confirmada -> completada (RN28)")
en_lotes([r["id"] for r in reservas if r["final"] in ("confirmada", "completada")],
         "UPDATE reserva SET estado = 'confirmada' WHERE id_reserva IN ({});")
en_lotes([r["id"] for r in reservas if r["final"] == "completada"],
         "UPDATE reserva SET estado = 'completada' WHERE id_reserva IN ({});")
w("-- Cancelaciones: reembolso del 80 % si fue con más de 5 días (RN31) o total si canceló la agencia")
for r in reservas:
    if r["final"] == "cancelada":
        w(f"UPDATE reserva SET estado = 'cancelada', fecha_cancelacion = {d(r['canc'])}, valor_reembolso = {r['reemb']} WHERE id_reserva = {r['id']};")
en_lotes([p["id"] for p in pagos if p["estado"] == "reembolsado"],
         "UPDATE pago SET estado_pago = 'reembolsado' WHERE id_pago IN ({});")
w("-- Salidas: programada -> confirmada (si alcanzó el mínimo, RN17) -> realizada; o cancelada")
en_lotes([s["id"] for s in salidas if s["final"] in ("confirmada", "realizada")],
         "UPDATE salida SET estado = 'confirmada' WHERE id_salida IN ({});")
en_lotes([s["id"] for s in salidas if s["final"] == "realizada"],
         "UPDATE salida SET estado = 'realizada' WHERE id_salida IN ({});")
en_lotes([s["id"] for s in salidas if s["final"] == "cancelada"],
         "UPDATE salida SET estado = 'cancelada' WHERE id_salida IN ({});")
w("-- Planes vencidos o retirados de la venta")
en_lotes([p["id"] for p in planes if p["estado_final"] == "inactivo"],
         "UPDATE plan SET estado = 'inactivo' WHERE id_plan IN ({});")
w("-- Prestadores suspendidos o retirados (se conservan con su historial)")
for i, p in enumerate(prestadores, 1):
    if p[6] != "activo": w(f"UPDATE prestador SET estado = '{p[6]}' WHERE id_prestador = {i};")
w("")
ins_all("notificacion", ["id_notificacion","tipo","canal","mensaje","fecha_programada","fecha_envio","estado","id_cliente","id_reserva"],
    [[str(i+1), q(n["tipo"]), q(n["canal"]), q(n["msg"]), ts(n["prog"]), nul(n["envio"], ts), q(n["estado"]), str(n["cli"]), nul(n["res"])]
     for i, n in enumerate(sorted(notifs, key=lambda n: n["prog"]))])

w("-- Los IDENTITY siguen después del último id insertado a mano")
for t, c in [("municipio","id_municipio"),("tipo_prestador","id_tipo_prestador"),("rol","id_rol"),
             ("tipo_componente","id_tipo_componente"),("prestador","id_prestador"),("usuario_sistema","id_usuario"),
             ("destino","id_destino"),("paquete","id_paquete"),("plan","id_plan"),("componente","id_componente"),
             ("salida","id_salida"),("cliente","id_cliente"),("perfil_cliente","id_perfil"),("pasajero","id_pasajero"),
             ("cotizacion","id_cotizacion"),("reserva","id_reserva"),("pago","id_pago"),("notificacion","id_notificacion")]:
    w(f"ALTER TABLE {t} MODIFY {c} GENERATED BY DEFAULT AS IDENTITY (START WITH LIMIT VALUE);")
w("")
w("COMMIT;")
w("")
w("-- Verificación: cantidad de filas por tabla")
tbls = ["municipio","tipo_prestador","rol","tipo_componente","prestador","usuario_sistema","destino","paquete",
        "itinerario_dia","plan","componente","paquete_componente","salida","cliente","perfil_cliente","pasajero",
        "cotizacion","detalle_cotizacion","reserva","reserva_pasajero","pago","pago_en_linea","notificacion"]
w("SELECT table_name, num_rows FROM (\n  " + "\n  UNION ALL ".join(
    f"SELECT '{t.upper()}' AS table_name, COUNT(*) AS num_rows FROM {t}" for t in tbls) + "\n) ORDER BY table_name;")

sql = "\n".join(out) + "\n"
sql = re.sub(r"(?m)^--\s*$", "", sql)
open(SALIDA_SQL, "w").write(sql)

# resumen para revisar
from collections import Counter
print("prestadores", NP, "clientes", N_CLI, "perfiles", len(perfiles), "pasajeros", len(pasajeros))
print("paquetes", len(paquetes), "planes", len(planes), "salidas", len(salidas))
print("reservas", len(reservas), Counter(r["final"] for r in reservas))
print("por temporada", Counter(temporada(salidas[r["salida"]-1]["fecha"]) for r in reservas))
print("por año", Counter(salidas[r["salida"]-1]["fecha"].year for r in reservas))
print("pagos", len(pagos), Counter(p["estado"] for p in pagos), "en linea", len(enl))
print("notifs", len(notifs), Counter(n["tipo"] for n in notifs), Counter(n["estado"] for n in notifs))
print("salidas", Counter(s["final"] for s in salidas))
print("cotizaciones", Counter(c["estado"] for c in cotizaciones))
print("menores pasajeros", sum(1 for p in pasajeros if (date(2026,1,1)-p["nac"]).days/365.25 < 18))
print("municipios prestadores", Counter(p[8] for p in prestadores))

