-- =====================================================================
-- QuindioStay - 03_datos.sql
-- Datos de prueba. Correr como QUINDIOSTAY con "Run Script" (F5) después
-- de 00_reset, 01_ddl y 02_triggers.
--
-- Fechas pensadas para hoy = 2026-11-03 (y siguen sirviendo para la
-- sustentación del 20/11): hay salidas realizadas (jul-oct), programadas
-- (nov-dic), una confirmada y dos canceladas.
--
-- Contraseña de TODOS los usuarios y cuentas de cliente: Clave123
--   Usuarios internos: jpespinosa, jjospina (admin), mabetancourt (operador),
--   cafeymontana, quindioaventura, laspalmas (encargados de prestador)
--
-- Qué muestra cada reporte con estos datos:
--   R1  salidas en los 4 estados
--   R2  planes vigentes; hay uno de temporada futura y dos inactivos que no salen
--   R3  cotizaciones aceptadas, enviadas, rechazadas, vencidas y en borrador
--   R4  ventas repartidas entre junio y noviembre
--   R5  7 salidas programadas públicas con distinto % de ocupación
--   R6  prestadores que aportan componentes a 3 o más paquetes
--   R7  pagos exitosos en varios meses y canales
--   R8  "Jardín botánico y mariposas" y "Cañón del río Barbas" sin ventas
--   R9  "Aves de Génova" (04/12) y "Salento y Cocora exprés" (11/12) bajo el mínimo
--   R10 clientes con saldo y salida en los próximos 15 días
-- =====================================================================
   SET DEFINE OFF

-- MUNICIPIO (12)
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 1,
     '63001',
     'Armenia' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 2,
     '63111',
     'Buenavista' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 3,
     '63130',
     'Calarcá' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 4,
     '63190',
     'Circasia' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 5,
     '63212',
     'Córdoba' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 6,
     '63272',
     'Filandia' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 7,
     '63302',
     'Génova' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 8,
     '63401',
     'La Tebaida' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 9,
     '63470',
     'Montenegro' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 10,
     '63548',
     'Pijao' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 11,
     '63594',
     'Quimbaya' );
insert into municipio (
   id_municipio,
   codigo_dane,
   nombre
) values
   ( 12,
     '63690',
     'Salento' );

-- TIPO_PRESTADOR (7)
insert into tipo_prestador (
   id_tipo_prestador,
   nombre
) values
   ( 1,
     'alojamiento' );
insert into tipo_prestador (
   id_tipo_prestador,
   nombre
) values
   ( 2,
     'agencia de viajes' );
insert into tipo_prestador (
   id_tipo_prestador,
   nombre
) values
   ( 3,
     'transporte' );
insert into tipo_prestador (
   id_tipo_prestador,
   nombre
) values
   ( 4,
     'gastronomia' );
insert into tipo_prestador (
   id_tipo_prestador,
   nombre
) values
   ( 5,
     'guianza' );
insert into tipo_prestador (
   id_tipo_prestador,
   nombre
) values
   ( 6,
     'alquiler de vehiculos' );
insert into tipo_prestador (
   id_tipo_prestador,
   nombre
) values
   ( 7,
     'atraccion' );

-- ROL (3)
insert into rol (
   id_rol,
   nombre
) values
   ( 1,
     'administrador de la plataforma' );
insert into rol (
   id_rol,
   nombre
) values
   ( 2,
     'encargado de prestador' );
insert into rol (
   id_rol,
   nombre
) values
   ( 3,
     'operador' );

-- TIPO_COMPONENTE (5)
insert into tipo_componente (
   id_tipo_componente,
   nombre
) values
   ( 1,
     'hospedaje' );
insert into tipo_componente (
   id_tipo_componente,
   nombre
) values
   ( 2,
     'transporte' );
insert into tipo_componente (
   id_tipo_componente,
   nombre
) values
   ( 3,
     'alimentacion' );
insert into tipo_componente (
   id_tipo_componente,
   nombre
) values
   ( 4,
     'actividad' );
insert into tipo_componente (
   id_tipo_componente,
   nombre
) values
   ( 5,
     'seguro' );

-- PRESTADOR (10)
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 1,
     'Café & Montaña Viajes',
     '900123456-7',
     '45821',
     'Calle 21 # 14-35',
     '3157778899',
     'reservas@cafeymontana.co',
     'activo',
     2,
     1 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 2,
     'Quindío Aventura Tours',
     '901234567-1',
     '51207',
     'Carrera 25 # 30-12',
     '3104445566',
     'info@quindioaventura.co',
     'activo',
     2,
     3 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 3,
     'Hotel Las Palmas',
     '800456789-2',
     '23456',
     'Calle 5 # 3-20',
     '6067591234',
     'reservas@laspalmas.co',
     'activo',
     1,
     12 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 4,
     'Finca Hotel El Mirador',
     '811222333-4',
     '34567',
     'Vereda El Roble km 2',
     '3201112233',
     'contacto@elmirador.co',
     'activo',
     1,
     6 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 5,
     'Transportes Quindío Express',
     '890333444-5',
     '12890',
     'Terminal de Transportes local 8',
     '6067412345',
     'viajes@quindioexpress.co',
     'activo',
     3,
     1 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 6,
     'Restaurante La Fonda Paisa',
     '1094888777',
     '67123',
     'Calle Real # 6-15',
     '3112223344',
     'fondapaisa@correo.co',
     'activo',
     4,
     12 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 7,
     'Guías del Cocora',
     '1094555666',
     '70456',
     'Plaza principal',
     '3145556677',
     'guiasdelcocora@correo.co',
     'activo',
     5,
     12 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 8,
     'Parapente Cielo Abierto',
     '900777888-3',
     '58123',
     'Mirador de Buenavista',
     '3168889900',
     'vuela@cieloabierto.co',
     'activo',
     7,
     2 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 9,
     'Ecoparque Los Guaduales',
     '900999111-6',
     '61234',
     'Km 6 vía Montenegro',
     '3177776655',
     'info@losguaduales.co',
     'activo',
     7,
     9 );
insert into prestador (
   id_prestador,
   nombre_comercial,
   nit,
   numero_rnt,
   direccion,
   telefono,
   correo,
   estado,
   id_tipo_prestador,
   id_municipio
) values
   ( 10,
     'Viajes Génova Natural',
     '901555666-8',
     '49870',
     'Calle 10 # 8-40',
     '3189990011',
     'hola@genovanatural.co',
     'suspendido',
     2,
     7 );

-- USUARIO_SISTEMA (7)
insert into usuario_sistema (
   id_usuario,
   nombre_usuario,
   contrasena_cifrada,
   estado,
   id_rol,
   id_prestador
) values
   ( 1,
     'jpespinosa',
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'activo',
     1,
     null );
insert into usuario_sistema (
   id_usuario,
   nombre_usuario,
   contrasena_cifrada,
   estado,
   id_rol,
   id_prestador
) values
   ( 2,
     'jjospina',
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'activo',
     1,
     null );
insert into usuario_sistema (
   id_usuario,
   nombre_usuario,
   contrasena_cifrada,
   estado,
   id_rol,
   id_prestador
) values
   ( 3,
     'mabetancourt',
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'activo',
     3,
     null );
insert into usuario_sistema (
   id_usuario,
   nombre_usuario,
   contrasena_cifrada,
   estado,
   id_rol,
   id_prestador
) values
   ( 4,
     'cafeymontana',
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'activo',
     2,
     1 );
insert into usuario_sistema (
   id_usuario,
   nombre_usuario,
   contrasena_cifrada,
   estado,
   id_rol,
   id_prestador
) values
   ( 5,
     'quindioaventura',
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'activo',
     2,
     2 );
insert into usuario_sistema (
   id_usuario,
   nombre_usuario,
   contrasena_cifrada,
   estado,
   id_rol,
   id_prestador
) values
   ( 6,
     'laspalmas',
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'activo',
     2,
     3 );
insert into usuario_sistema (
   id_usuario,
   nombre_usuario,
   contrasena_cifrada,
   estado,
   id_rol,
   id_prestador
) values
   ( 7,
     'operador1',
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'activo',
     3,
     null );

-- DESTINO (6)
insert into destino (
   id_destino,
   nombre,
   descripcion,
   id_municipio
) values
   ( 1,
     'Valle de Cocora',
     'Valle de palmas de cera, árbol nacional de Colombia',
     12 );
insert into destino (
   id_destino,
   nombre,
   descripcion,
   id_municipio
) values
   ( 2,
     'Parque del Café',
     'Parque temático de la cultura cafetera',
     9 );
insert into destino (
   id_destino,
   nombre,
   descripcion,
   id_municipio
) values
   ( 3,
     'Mirador de Filandia',
     'Mirador colonial con vista a los tres departamentos',
     6 );
insert into destino (
   id_destino,
   nombre,
   descripcion,
   id_municipio
) values
   ( 4,
     'Reserva Río Gris',
     'Bosque de niebla con más de 300 especies de aves',
     7 );
insert into destino (
   id_destino,
   nombre,
   descripcion,
   id_municipio
) values
   ( 5,
     'Cañón del río Barbas',
     'Cañón con bosque andino y puentes colgantes',
     6 );
insert into destino (
   id_destino,
   nombre,
   descripcion,
   id_municipio
) values
   ( 6,
     'Jardín Botánico del Quindío',
     'Jardín botánico con mariposario',
     3 );

-- PAQUETE (9)
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 1,
     'Ruta del café 3 días',
     'Recorrido por fincas cafeteras de Salento y caminata por el Valle de Cocora',
     3,
     'cafetero',
     4,
     'publico',
     1,
     1 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 2,
     'Quindío de aventura 5 días',
     'Parapente en Buenavista, cabalgata y día completo en el Parque del Café',
     5,
     'aventura',
     6,
     'publico',
     2,
     2 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 3,
     'Filandia patrimonial',
     'Arquitectura colonial, mirador y talleres de cestería',
     2,
     'cultural',
     2,
     'publico',
     3,
     1 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 4,
     'Aves de Génova',
     'Avistamiento de aves con guía especializado en la Reserva Río Gris',
     2,
     'naturaleza',
     3,
     'publico',
     4,
     2 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 5,
     'Familia en el Parque del Café',
     'Plan familiar con atracciones, show del café y hospedaje',
     3,
     'familiar',
     4,
     'publico',
     2,
     1 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 6,
     'Salento y Cocora exprés',
     'Pasadía a Salento y caminata corta por el Valle de Cocora',
     1,
     'naturaleza',
     2,
     'publico',
     1,
     1 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 7,
     'Jardín botánico y mariposas',
     'Recorrido guiado por el jardín botánico y el mariposario',
     1,
     'naturaleza',
     2,
     'publico',
     6,
     2 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 8,
     'Cañón del río Barbas',
     'Caminata por el cañón y puentes colgantes',
     2,
     'aventura',
     4,
     'publico',
     5,
     2 );
insert into paquete (
   id_paquete,
   nombre,
   descripcion,
   duracion_dias,
   categoria,
   minimo_viajeros,
   visibilidad,
   id_destino,
   id_prestador
) values
   ( 9,
     'Viaje a la medida COT-000187',
     'Paquete privado creado al aceptar la cotización COT-000187',
     4,
     'cafetero',
     1,
     'privado',
     1,
     1 );

-- ITINERARIO_DIA (23)
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 1,
     1,
     'Llegada, registro en el hotel y recorrido de bienvenida' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 1,
     2,
     'Tour por finca cafetera y almuerzo típico' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 1,
     3,
     'Cierre del recorrido y regreso' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 2,
     1,
     'Llegada, registro en el hotel y recorrido de bienvenida' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 2,
     2,
     'Tour por finca cafetera y almuerzo típico' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 2,
     3,
     'Caminata guiada y actividad principal del paquete' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 2,
     4,
     'Día libre con actividades opcionales' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 2,
     5,
     'Cierre del recorrido y regreso' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 3,
     1,
     'Llegada, registro en el hotel y recorrido de bienvenida' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 3,
     2,
     'Cierre del recorrido y regreso' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 4,
     1,
     'Llegada, registro en el hotel y recorrido de bienvenida' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 4,
     2,
     'Cierre del recorrido y regreso' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 5,
     1,
     'Llegada, registro en el hotel y recorrido de bienvenida' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 5,
     2,
     'Tour por finca cafetera y almuerzo típico' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 5,
     3,
     'Cierre del recorrido y regreso' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 6,
     1,
     'Salida en la mañana, actividad principal y regreso en la tarde' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 7,
     1,
     'Salida en la mañana, actividad principal y regreso en la tarde' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 8,
     1,
     'Llegada, registro en el hotel y recorrido de bienvenida' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 8,
     2,
     'Cierre del recorrido y regreso' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 9,
     1,
     'Llegada, registro en el hotel y recorrido de bienvenida' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 9,
     2,
     'Tour por finca cafetera y almuerzo típico' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 9,
     3,
     'Caminata guiada y actividad principal del paquete' );
insert into itinerario_dia (
   id_paquete,
   numero_dia,
   descripcion
) values
   ( 9,
     4,
     'Cierre del recorrido y regreso' );

-- PLAN (14)
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 1,
     'Estándar',
     'Hotel 3 estrellas, transporte y guía',
     'estandar',
     650000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     1 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 2,
     'Premium',
     'Hotel 4 estrellas y guía bilingüe',
     'premium',
     900000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     1 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 3,
     'Estándar',
     'Hospedaje, transporte y actividades',
     'estandar',
     1100000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     2 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 4,
     'Todo incluido',
     'Hospedaje, alimentación completa y todas las actividades',
     'todo incluido',
     1450000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     2 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 5,
     'Básico',
     'Transporte, guía y almuerzo',
     'basico',
     320000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     3 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 6,
     'Estándar',
     'Hospedaje, guía y binoculares',
     'estandar',
     380000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     4 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 7,
     'Familiar',
     'Hospedaje familiar y entradas al parque',
     'estandar',
     520000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     5 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 8,
     'Básico',
     'Transporte y guía',
     'basico',
     180000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     6 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 9,
     'Básico',
     'Entrada y guía',
     'basico',
     150000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     7 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 10,
     'Estándar',
     'Hospedaje, transporte y guía',
     'estandar',
     420000,
     date '2026-01-01',
     date '2027-06-30',
     'activo',
     8 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 11,
     'Privado COT-000187',
     'Plan del viaje a la medida',
     'premium',
     1400000,
     date '2026-10-09',
     date '2026-11-30',
     'activo',
     9 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 12,
     'Temporada diciembre',
     'Precio de temporada alta',
     'premium',
     980000,
     date '2026-12-01',
     date '2027-01-31',
     'activo',
     1 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 13,
     'Plan 2025',
     'Plan de la temporada anterior',
     'basico',
     290000,
     date '2025-01-01',
     date '2025-12-31',
     'inactivo',
     3 );
insert into plan (
   id_plan,
   nombre,
   descripcion,
   tipo_plan,
   precio,
   fecha_inicio_vigencia,
   fecha_fin_vigencia,
   estado,
   id_paquete
) values
   ( 14,
     'Grupo empresarial',
     'Plan para empresas, suspendido',
     'estandar',
     160000,
     date '2026-01-01',
     date '2027-06-30',
     'inactivo',
     6 );

-- COMPONENTE (10)
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 1,
     'Hospedaje Hotel Las Palmas',
     'Habitación doble con desayuno',
     300000,
     1,
     3 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 2,
     'Hospedaje Finca El Mirador',
     'Cabaña para dos personas con desayuno',
     250000,
     1,
     4 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 3,
     'Transporte Quindío Express',
     'Traslados en van con conductor',
     80000,
     2,
     5 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 4,
     'Almuerzo típico La Fonda',
     'Bandeja paisa o trucha con bebida',
     45000,
     3,
     6 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 5,
     'Guianza Valle de Cocora',
     'Guía local durante la caminata',
     60000,
     4,
     7 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 6,
     'Parapente Cielo Abierto',
     'Vuelo biplaza de 20 minutos',
     220000,
     4,
     8 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 7,
     'Tour finca cafetera',
     'Recorrido por el proceso del café con degustación',
     70000,
     4,
     7 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 8,
     'Seguro de viaje',
     'Asistencia médica durante el paquete',
     15000,
     5,
     1 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 9,
     'Entrada Ecoparque Los Guaduales',
     'Entrada con atracciones incluidas',
     90000,
     4,
     9 );
insert into componente (
   id_componente,
   nombre,
   descripcion,
   costo,
   id_tipo_componente,
   id_prestador
) values
   ( 10,
     'Avistamiento de aves',
     'Guía ornitólogo y binoculares',
     85000,
     4,
     7 );

-- PAQUETE_COMPONENTE (33)
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 1,
     1 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 1,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 1,
     4 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 1,
     5 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 1,
     7 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 1,
     8 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 2,
     1 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 2,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 2,
     6 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 2,
     9 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 2,
     8 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 3,
     2 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 3,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 3,
     4 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 4,
     2 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 4,
     10 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 4,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 5,
     1 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 5,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 5,
     9 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 5,
     4 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 6,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 6,
     5 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 6,
     4 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 7,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 7,
     4 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 8,
     2 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 8,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 8,
     5 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 9,
     1 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 9,
     3 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 9,
     5 );
insert into paquete_componente (
   id_paquete,
   id_componente
) values
   ( 9,
     7 );

-- SALIDA (16)
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 1,
     date '2026-07-10',
     20,
     'programada',
     1 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 2,
     date '2026-08-14',
     16,
     'programada',
     2 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 3,
     date '2026-09-05',
     10,
     'programada',
     3 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 4,
     date '2026-10-09',
     20,
     'programada',
     1 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 5,
     date '2026-10-23',
     24,
     'programada',
     5 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 6,
     date '2026-08-22',
     12,
     'programada',
     4 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 7,
     date '2026-11-06',
     20,
     'programada',
     1 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 8,
     date '2026-11-13',
     16,
     'programada',
     2 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 9,
     date '2026-11-20',
     10,
     'programada',
     3 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 10,
     date '2026-11-27',
     24,
     'programada',
     5 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 11,
     date '2026-12-04',
     12,
     'programada',
     4 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 12,
     date '2026-12-11',
     8,
     'programada',
     6 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 13,
     date '2026-12-18',
     20,
     'programada',
     1 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 14,
     date '2026-11-08',
     8,
     'programada',
     6 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 15,
     date '2026-11-14',
     2,
     'programada',
     9 );
insert into salida (
   id_salida,
   fecha_inicio,
   cupo_maximo,
   estado,
   id_paquete
) values
   ( 16,
     date '2026-12-20',
     16,
     'programada',
     2 );

-- CLIENTE (20)
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 1,
     'María Fernanda',
     'Gómez Ruiz',
     'CE',
     '1094000057',
     'maria.gomez1@correo.com',
     '3100000000',
     date '1965-01-01',
     'Medellín' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 2,
     'Carlos Andrés',
     'Rodríguez López',
     'CC',
     '1094001428',
     'carlos.rodriguez2@correo.com',
     '3104137731',
     date '1968-02-08',
     'Bogotá' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 3,
     'Laura',
     'Martínez Castro',
     'CC',
     '1094002799',
     'laura.martinez3@correo.com',
     '3108275462',
     date '1971-03-15',
     'Cali' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 4,
     'Santiago',
     'García Henao',
     'CC',
     '1094004170',
     'santiago.garcia4@correo.com',
     '3112413193',
     date '1974-04-22',
     'Pereira' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 5,
     'Valentina',
     'Hernández Ríos',
     'CC',
     '1094005541',
     'valentina.hernandez5@correo.com',
     '3116550924',
     date '1977-05-01',
     'Manizales' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 6,
     'Juan David',
     'López Gil',
     'CC',
     '1094006912',
     'juan.lopez6@correo.com',
     '3120688655',
     date '1980-06-08',
     'Armenia' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 7,
     'Daniela',
     'Ramírez Ospina',
     'CC',
     '1094008283',
     'daniela.ramirez7@correo.com',
     '3124826386',
     date '1983-07-15',
     'Bucaramanga' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 8,
     'Andrés Felipe',
     'Torres Arango',
     'CC',
     '1094009654',
     'andres.torres8@correo.com',
     '3128964117',
     date '1986-08-22',
     'Barranquilla' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 9,
     'Camila',
     'Sánchez Mejía',
     'CC',
     '1094011025',
     'camila.sanchez9@correo.com',
     '3133101848',
     date '1989-09-01',
     'Ibagué' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 10,
     'Sebastián',
     'Díaz Cardona',
     'CE',
     '1094012396',
     'sebastian.diaz10@correo.com',
     '3137239579',
     date '1992-10-08',
     'Cartagena' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 11,
     'Paula Andrea',
     'Restrepo Vélez',
     'CC',
     '1094013767',
     'paula.restrepo11@correo.com',
     '3141377310',
     date '1995-11-15',
     'Medellín' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 12,
     'Mateo',
     'Moreno Quintero',
     'CC',
     '1094015138',
     'mateo.moreno12@correo.com',
     '3145515041',
     date '1998-12-22',
     'Bogotá' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 13,
     'Natalia',
     'Muñoz Giraldo',
     'CC',
     '1094016509',
     'natalia.munoz13@correo.com',
     '3149652772',
     date '1966-01-01',
     'Cali' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 14,
     'Julián',
     'Rojas Valencia',
     'CC',
     '1094017880',
     'julian.rojas14@correo.com',
     '3153790503',
     date '1969-02-08',
     'Pereira' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 15,
     'Isabella',
     'Álvarez Zuluaga',
     'CC',
     '1094019251',
     'isabella.alvarez15@correo.com',
     '3157928234',
     date '1972-03-15',
     'Manizales' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 16,
     'Diego',
     'Jaramillo Toro',
     'CC',
     '1094020622',
     'diego.jaramillo16@correo.com',
     '3162065965',
     date '1975-04-22',
     'Armenia' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 17,
     'Manuela',
     'Castaño Duque',
     'CC',
     '1094021993',
     'manuela.castano17@correo.com',
     '3166203696',
     date '1978-05-01',
     'Bucaramanga' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 18,
     'Felipe',
     'Osorio Patiño',
     'CC',
     '1094023364',
     'felipe.osorio18@correo.com',
     '3170341427',
     date '1981-06-08',
     'Barranquilla' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 19,
     'Sara',
     'Marín Londoño',
     'CE',
     '1094024735',
     'sara.marin19@correo.com',
     '3174479158',
     date '1984-07-15',
     'Ibagué' );
insert into cliente (
   id_cliente,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   correo,
   telefono,
   fecha_nacimiento,
   ciudad_origen
) values
   ( 20,
     'Alejandro',
     'Ocampo Salazar',
     'CC',
     '1094026106',
     'alejandro.ocampo20@correo.com',
     '3178616889',
     date '1987-08-22',
     'Cartagena' );

-- PERFIL_CLIENTE (14)
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 1,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'correo',
     1,
     'si',
     1 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 2,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'sms',
     3,
     'no',
     2 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 3,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'whatsapp',
     7,
     'si',
     3 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 4,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'ingles',
     'correo',
     1,
     'no',
     4 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 5,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'sms',
     3,
     'si',
     5 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 6,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'whatsapp',
     7,
     'no',
     6 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 7,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'correo',
     1,
     'si',
     7 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 8,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'sms',
     3,
     'no',
     8 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 9,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'whatsapp',
     7,
     'si',
     9 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 10,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'ingles',
     'correo',
     1,
     'no',
     10 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 11,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'sms',
     3,
     'si',
     11 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 12,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'whatsapp',
     7,
     'no',
     12 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 13,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'correo',
     1,
     'si',
     13 );
insert into perfil_cliente (
   id_perfil,
   contrasena_cifrada,
   idioma,
   canal_preferido,
   dias_anticipacion,
   acepta_promociones,
   id_cliente
) values
   ( 14,
     '$2a$10$Yxuy45LXIysQFb73LnhLne54kljEXjJ54IFiOYo1RzrNy2wlR33tS',
     'espanol',
     'sms',
     3,
     'no',
     14 );

-- PASAJERO (70)
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 1,
     'María Fernanda',
     'Gómez Ruiz',
     'CE',
     '1094000057',
     date '1965-01-01' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 2,
     'Carlos Andrés',
     'Rodríguez López',
     'CC',
     '1094001428',
     date '1968-02-08' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 3,
     'Laura',
     'Martínez Castro',
     'CC',
     '1094002799',
     date '1971-03-15' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 4,
     'Santiago',
     'García Henao',
     'CC',
     '1094004170',
     date '1974-04-22' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 5,
     'Valentina',
     'Hernández Ríos',
     'CC',
     '1094005541',
     date '1977-05-01' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 6,
     'Juan David',
     'López Gil',
     'CC',
     '1094006912',
     date '1980-06-08' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 7,
     'Daniela',
     'Ramírez Ospina',
     'CC',
     '1094008283',
     date '1983-07-15' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 8,
     'Andrés Felipe',
     'Torres Arango',
     'CC',
     '1094009654',
     date '1986-08-22' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 9,
     'Camila',
     'Sánchez Mejía',
     'CC',
     '1094011025',
     date '1989-09-01' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 10,
     'Sebastián',
     'Díaz Cardona',
     'CE',
     '1094012396',
     date '1992-10-08' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 11,
     'Paula Andrea',
     'Restrepo Vélez',
     'CC',
     '1094013767',
     date '1995-11-15' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 12,
     'Mateo',
     'Moreno Quintero',
     'CC',
     '1094015138',
     date '1998-12-22' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 13,
     'Natalia',
     'Muñoz Giraldo',
     'CC',
     '1094016509',
     date '1966-01-01' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 14,
     'Julián',
     'Rojas Valencia',
     'CC',
     '1094017880',
     date '1969-02-08' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 15,
     'Isabella',
     'Álvarez Zuluaga',
     'CC',
     '1094019251',
     date '1972-03-15' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 16,
     'Diego',
     'Jaramillo Toro',
     'CC',
     '1094020622',
     date '1975-04-22' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 17,
     'Manuela',
     'Castaño Duque',
     'CC',
     '1094021993',
     date '1978-05-01' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 18,
     'Felipe',
     'Osorio Patiño',
     'CC',
     '1094023364',
     date '1981-06-08' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 19,
     'Sara',
     'Marín Londoño',
     'CE',
     '1094024735',
     date '1984-07-15' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 20,
     'Alejandro',
     'Ocampo Salazar',
     'CC',
     '1094026106',
     date '1987-08-22' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 21,
     'Sofía',
     'García Henao',
     'TI',
     '1100000013',
     date '2012-01-01' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 22,
     'Tomás',
     'Restrepo Vélez',
     'CC',
     '1100002754',
     date '1965-04-12' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 23,
     'Mariana',
     'Osorio Patiño',
     'CC',
     '1100005495',
     date '1970-07-23' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 24,
     'Samuel',
     'Hernández Ríos',
     'CC',
     '1100008236',
     date '1975-10-06' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 25,
     'Gabriela',
     'Moreno Quintero',
     'CC',
     '1100010977',
     date '1980-01-17' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 26,
     'Nicolás',
     'Marín Londoño',
     'TI',
     '1100013718',
     date '2017-06-06' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 27,
     'Lucía',
     'López Gil',
     'CC',
     '1100016459',
     date '1990-07-11' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 28,
     'Martín',
     'Muñoz Giraldo',
     'CC',
     '1100019200',
     date '1995-10-22' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 29,
     'Ana María',
     'Ocampo Salazar',
     'CC',
     '1100021941',
     date '1960-01-05' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 30,
     'David',
     'Ramírez Ospina',
     'CC',
     '1100024682',
     date '1965-04-16' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 31,
     'Juliana',
     'Rojas Valencia',
     'TI',
     '1100027423',
     date '2012-11-11' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 32,
     'Esteban',
     'Gómez Ruiz',
     'CC',
     '1100030164',
     date '1975-10-10' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 33,
     'Catalina',
     'Torres Arango',
     'CC',
     '1100032905',
     date '1980-01-21' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 34,
     'Simón',
     'Álvarez Zuluaga',
     'pasaporte',
     'AB500481',
     date '1985-04-04' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 35,
     'Luisa',
     'Rodríguez López',
     'CC',
     '1100038387',
     date '1990-07-15' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 36,
     'Emilio',
     'Sánchez Mejía',
     'TI',
     '1100041128',
     date '2017-04-16' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 37,
     'Antonia',
     'Jaramillo Toro',
     'CC',
     '1100043869',
     date '1960-01-09' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 38,
     'Jerónimo',
     'Martínez Castro',
     'CC',
     '1100046610',
     date '1965-04-20' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 39,
     'Valeria',
     'Díaz Cardona',
     'CC',
     '1100049351',
     date '1970-07-03' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 40,
     'Miguel Ángel',
     'Castaño Duque',
     'CC',
     '1100052092',
     date '1975-10-14' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 41,
     'Salomé',
     'García Henao',
     'TI',
     '1100054833',
     date '2012-09-21' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 42,
     'Emmanuel',
     'Restrepo Vélez',
     'CC',
     '1100057574',
     date '1985-04-08' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 43,
     'Luciana',
     'Osorio Patiño',
     'CC',
     '1100060315',
     date '1990-07-19' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 44,
     'Juan José',
     'Hernández Ríos',
     'CC',
     '1100063056',
     date '1995-10-02' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 45,
     'Mariángel',
     'Moreno Quintero',
     'CC',
     '1100065797',
     date '1960-01-13' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 46,
     'Pablo',
     'Marín Londoño',
     'TI',
     '1100068538',
     date '2017-02-26' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 47,
     'Elena',
     'López Gil',
     'pasaporte',
     'AB500962',
     date '1970-07-07' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 48,
     'Joaquín',
     'Muñoz Giraldo',
     'CC',
     '1100074020',
     date '1975-10-18' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 49,
     'Victoria',
     'Ocampo Salazar',
     'CC',
     '1100076761',
     date '1980-01-01' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 50,
     'Ricardo',
     'Ramírez Ospina',
     'CC',
     '1100079502',
     date '1985-04-12' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 51,
     'Adriana',
     'Rojas Valencia',
     'TI',
     '1100082243',
     date '2012-07-04' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 52,
     'Hernán',
     'Gómez Ruiz',
     'CC',
     '1100084984',
     date '1995-10-06' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 53,
     'Gloria',
     'Torres Arango',
     'CC',
     '1100087725',
     date '1960-01-17' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 54,
     'Fernando',
     'Álvarez Zuluaga',
     'CC',
     '1100090466',
     date '1965-04-28' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 55,
     'Patricia',
     'Rodríguez López',
     'CC',
     '1100093207',
     date '1970-07-11' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 56,
     'Jorge',
     'Sánchez Mejía',
     'TI',
     '1100095948',
     date '2017-12-09' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 57,
     'Claudia',
     'Jaramillo Toro',
     'CC',
     '1100098689',
     date '1980-01-05' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 58,
     'Óscar',
     'Martínez Castro',
     'CC',
     '1100101430',
     date '1985-04-16' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 59,
     'Beatriz',
     'Díaz Cardona',
     'CC',
     '1100104171',
     date '1990-07-27' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 60,
     'Luis Carlos',
     'Castaño Duque',
     'pasaporte',
     'AB501443',
     date '1995-10-10' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 61,
     'María Fernanda',
     'García Henao',
     'TI',
     '1100109653',
     date '2012-05-14' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 62,
     'Carlos Andrés',
     'Restrepo Vélez',
     'CC',
     '1100112394',
     date '1965-04-04' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 63,
     'Laura',
     'Osorio Patiño',
     'CC',
     '1100115135',
     date '1970-07-15' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 64,
     'Santiago',
     'Hernández Ríos',
     'CC',
     '1100117876',
     date '1975-10-26' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 65,
     'Valentina',
     'Moreno Quintero',
     'CC',
     '1100120617',
     date '1980-01-09' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 66,
     'Juan David',
     'Marín Londoño',
     'TI',
     '1100123358',
     date '2017-10-19' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 67,
     'Daniela',
     'López Gil',
     'CC',
     '1100126099',
     date '1990-07-03' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 68,
     'Andrés Felipe',
     'Muñoz Giraldo',
     'CC',
     '1100128840',
     date '1995-10-14' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 69,
     'Camila',
     'Ocampo Salazar',
     'CC',
     '1100131581',
     date '1960-01-25' );
insert into pasajero (
   id_pasajero,
   nombres,
   apellidos,
   tipo_documento,
   numero_documento,
   fecha_nacimiento
) values
   ( 70,
     'Sebastián',
     'Ramírez Ospina',
     'CC',
     '1100134322',
     date '1965-04-08' );

-- COTIZACION (9)
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 1,
     'COT-000187',
     date '2026-10-05',
     2800000,
     'aceptada',
     1,
     5 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 2,
     'COT-000188',
     date '2026-10-08',
     1800000,
     'aceptada',
     2,
     8 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 3,
     'COT-000189',
     date '2026-10-29',
     1640000,
     'enviada',
     1,
     3 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 4,
     'COT-000190',
     date '2026-11-01',
     420000,
     'enviada',
     2,
     15 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 5,
     'COT-000191',
     date '2026-09-12',
     1500000,
     'rechazada',
     1,
     11 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 6,
     'COT-000192',
     date '2026-09-20',
     400000,
     'rechazada',
     2,
     17 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 7,
     'COT-000193',
     date '2026-08-30',
     450000,
     'vencida',
     1,
     2 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 8,
     'COT-000194',
     date '2026-09-25',
     820000,
     'vencida',
     2,
     19 );
insert into cotizacion (
   id_cotizacion,
   numero_cotizacion,
   fecha,
   valor_total,
   estado,
   id_prestador,
   id_cliente
) values
   ( 9,
     'COT-000195',
     date '2026-11-02',
     360000,
     'borrador',
     1,
     9 );

-- DETALLE_COTIZACION (18)
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 1,
     1,
     date '2026-11-14',
     2,
     1300000,
     1 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 1,
     2,
     date '2026-11-14',
     2,
     300000,
     3 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 1,
     3,
     date '2026-11-15',
     2,
     500000,
     5 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 1,
     4,
     date '2026-11-16',
     2,
     700000,
     7 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 2,
     1,
     date '2026-12-06',
     2,
     600000,
     6 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 2,
     2,
     date '2026-12-06',
     2,
     1200000,
     1 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 3,
     1,
     date '2026-12-10',
     4,
     1400000,
     2 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 3,
     2,
     date '2026-12-11',
     4,
     240000,
     4 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 4,
     1,
     date '2026-12-02',
     2,
     220000,
     10 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 4,
     2,
     date '2026-12-02',
     2,
     200000,
     3 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 5,
     1,
     date '2026-10-01',
     3,
     1500000,
     1 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 6,
     1,
     date '2026-10-10',
     1,
     280000,
     6 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 6,
     2,
     date '2026-10-10',
     1,
     120000,
     9 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 7,
     1,
     date '2026-09-20',
     5,
     450000,
     7 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 8,
     1,
     date '2026-10-20',
     2,
     700000,
     2 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 8,
     2,
     date '2026-10-21',
     2,
     120000,
     4 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 9,
     1,
     date '2026-12-20',
     3,
     60000,
     8 );
insert into detalle_cotizacion (
   id_cotizacion,
   numero_linea,
   fecha_servicio,
   cantidad_personas,
   valor,
   id_componente
) values
   ( 9,
     2,
     date '2026-12-20',
     3,
     300000,
     3 );

-- RESERVA (45)
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 1,
     date '2026-05-30',
     2600000,
     'completada',
     'web',
     1,
     1,
     1,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 2,
     date '2026-05-22',
     2700000,
     'completada',
     'aplicacion movil',
     2,
     1,
     2,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 3,
     date '2026-06-17',
     3250000,
     'completada',
     'telefono',
     1,
     1,
     3,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 4,
     date '2026-06-19',
     1800000,
     'completada',
     'mostrador',
     2,
     1,
     4,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 5,
     date '2026-06-03',
     1950000,
     'completada',
     'agencia intermediaria',
     1,
     1,
     5,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 6,
     date '2026-07-03',
     4400000,
     'completada',
     'aplicacion movil',
     3,
     2,
     6,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 7,
     date '2026-07-09',
     2900000,
     'completada',
     'telefono',
     4,
     2,
     7,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 8,
     date '2026-07-26',
     4400000,
     'completada',
     'mostrador',
     3,
     2,
     8,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 9,
     date '2026-07-28',
     640000,
     'completada',
     'telefono',
     5,
     3,
     9,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 10,
     date '2026-08-14',
     960000,
     'completada',
     'mostrador',
     5,
     3,
     10,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 11,
     date '2026-07-24',
     960000,
     'completada',
     'agencia intermediaria',
     5,
     3,
     11,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 12,
     date '2026-08-28',
     3250000,
     'completada',
     'mostrador',
     1,
     4,
     12,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 13,
     date '2026-09-16',
     3600000,
     'completada',
     'agencia intermediaria',
     2,
     4,
     13,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 14,
     date '2026-09-17',
     1950000,
     'completada',
     'web',
     1,
     4,
     14,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 15,
     date '2026-09-12',
     1800000,
     'completada',
     'web',
     2,
     4,
     15,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 16,
     date '2026-10-02',
     2080000,
     'completada',
     'agencia intermediaria',
     7,
     5,
     16,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 17,
     date '2026-09-28',
     2600000,
     'completada',
     'web',
     7,
     5,
     17,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 18,
     date '2026-09-27',
     2080000,
     'completada',
     'web',
     7,
     5,
     18,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 19,
     date '2026-10-05',
     1560000,
     'completada',
     'aplicacion movil',
     7,
     5,
     19,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 20,
     date '2026-07-08',
     1140000,
     'pendiente',
     'web',
     6,
     6,
     20,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 21,
     date '2026-10-22',
     2600000,
     'confirmada',
     'web',
     1,
     7,
     1,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 22,
     date '2026-10-25',
     2700000,
     'confirmada',
     'aplicacion movil',
     2,
     7,
     2,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 23,
     date '2026-09-07',
     1300000,
     'pendiente',
     'telefono',
     1,
     7,
     3,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 24,
     date '2026-10-15',
     4500000,
     'confirmada',
     'mostrador',
     2,
     7,
     4,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 25,
     date '2026-09-08',
     2600000,
     'confirmada',
     'agencia intermediaria',
     1,
     7,
     5,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 26,
     date '2026-10-08',
     4400000,
     'confirmada',
     'aplicacion movil',
     3,
     8,
     6,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 27,
     date '2026-10-26',
     4350000,
     'confirmada',
     'telefono',
     4,
     8,
     7,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 28,
     date '2026-10-23',
     5500000,
     'pendiente',
     'mostrador',
     3,
     8,
     8,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 29,
     date '2026-10-31',
     640000,
     'confirmada',
     'telefono',
     5,
     9,
     9,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 30,
     date '2026-10-27',
     960000,
     'confirmada',
     'mostrador',
     5,
     9,
     10,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 31,
     date '2026-11-01',
     640000,
     'pendiente',
     'agencia intermediaria',
     5,
     9,
     11,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 32,
     date '2026-10-02',
     2080000,
     'confirmada',
     'mostrador',
     7,
     10,
     12,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 33,
     date '2026-10-24',
     2080000,
     'confirmada',
     'agencia intermediaria',
     7,
     10,
     13,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 34,
     date '2026-11-01',
     2600000,
     'pendiente',
     'web',
     7,
     10,
     14,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 35,
     date '2026-10-03',
     1560000,
     'confirmada',
     'web',
     7,
     10,
     15,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 36,
     date '2026-11-01',
     760000,
     'pendiente',
     'agencia intermediaria',
     6,
     11,
     16,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 37,
     date '2026-10-28',
     180000,
     'pendiente',
     'web',
     8,
     12,
     17,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 38,
     date '2026-10-28',
     2600000,
     'confirmada',
     'web',
     1,
     13,
     18,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 39,
     date '2026-10-29',
     1800000,
     'confirmada',
     'aplicacion movil',
     2,
     13,
     19,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 40,
     date '2026-10-19',
     1950000,
     'pendiente',
     'telefono',
     1,
     13,
     20,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 41,
     date '2026-10-20',
     2700000,
     'confirmada',
     'mostrador',
     2,
     13,
     1,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 42,
     date '2026-09-16',
     360000,
     'confirmada',
     'aplicacion movil',
     8,
     14,
     2,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 43,
     date '2026-09-09',
     720000,
     'confirmada',
     'telefono',
     8,
     14,
     3,
     null );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 44,
     date '2026-10-09',
     2800000,
     'confirmada',
     'telefono',
     11,
     15,
     5,
     1 );
insert into reserva (
   id_reserva,
   fecha_creacion,
   valor_total,
   estado,
   canal,
   id_plan,
   id_salida,
   id_cliente,
   id_cotizacion
) values
   ( 45,
     date '2026-10-12',
     2200000,
     'pendiente',
     'web',
     3,
     8,
     12,
     null );

-- RESERVA_PASAJERO (146)
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 1,
     1 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 1,
     45 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 1,
     56 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 1,
     35 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 2,
     2 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 2,
     29 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 2,
     51 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 3,
     3 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 3,
     47 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 3,
     36 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 3,
     21 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 3,
     61 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 4,
     4 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 4,
     24 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 5,
     5 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 5,
     50 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 5,
     43 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 6,
     6 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 6,
     59 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 6,
     64 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 6,
     25 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 7,
     7 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 7,
     53 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 8,
     8 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 8,
     43 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 8,
     62 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 8,
     40 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 9,
     9 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 9,
     21 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 10,
     10 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 10,
     61 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 10,
     49 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 11,
     11 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 11,
     26 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 11,
     45 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 12,
     12 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 12,
     68 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 12,
     54 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 12,
     64 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 12,
     62 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 13,
     13 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 13,
     22 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 13,
     24 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 13,
     47 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 14,
     14 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 14,
     59 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 14,
     28 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 15,
     15 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 15,
     29 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 16,
     16 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 16,
     40 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 16,
     64 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 16,
     51 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 17,
     17 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 17,
     21 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 17,
     58 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 17,
     44 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 17,
     43 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 18,
     18 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 18,
     61 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 18,
     23 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 18,
     34 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 19,
     19 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 19,
     46 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 19,
     70 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 20,
     20 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 20,
     69 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 20,
     55 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 21,
     1 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 21,
     32 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 21,
     41 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 21,
     37 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 22,
     2 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 22,
     42 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 22,
     25 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 23,
     3 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 23,
     56 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 24,
     4 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 24,
     21 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 24,
     68 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 24,
     47 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 24,
     64 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 25,
     5 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 25,
     34 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 25,
     28 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 25,
     29 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 26,
     6 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 26,
     53 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 26,
     46 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 26,
     25 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 27,
     7 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 27,
     55 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 27,
     42 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 28,
     8 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 28,
     21 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 28,
     40 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 28,
     37 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 28,
     51 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 29,
     9 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 29,
     38 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 30,
     10 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 30,
     62 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 30,
     28 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 31,
     11 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 31,
     32 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 32,
     12 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 32,
     40 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 32,
     32 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 32,
     34 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 33,
     13 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 33,
     43 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 33,
     35 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 33,
     31 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 34,
     14 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 34,
     27 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 34,
     23 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 34,
     28 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 34,
     50 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 35,
     15 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 35,
     67 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 35,
     68 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 36,
     16 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 36,
     35 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 37,
     17 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 38,
     18 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 38,
     56 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 38,
     54 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 38,
     39 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 39,
     19 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 39,
     25 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 40,
     20 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 40,
     60 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 40,
     44 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 41,
     1 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 41,
     53 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 41,
     28 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 42,
     2 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 42,
     64 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 43,
     3 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 43,
     48 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 43,
     63 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 43,
     43 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 44,
     5 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 44,
     43 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 45,
     12 );
insert into reserva_pasajero (
   id_reserva,
   id_pasajero
) values
   ( 45,
     60 );

-- PAGO (63)
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 1,
     date '2026-05-31',
     2600000,
     'transferencia',
     'exitoso',
     1 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 2,
     date '2026-05-23',
     1350000,
     'efectivo',
     'exitoso',
     2 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 3,
     date '2026-06-11',
     1350000,
     'transferencia',
     'exitoso',
     2 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 4,
     date '2026-06-18',
     3250000,
     'pse',
     'exitoso',
     3 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 5,
     date '2026-06-20',
     900000,
     'tarjeta de credito',
     'exitoso',
     4 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 6,
     date '2026-06-27',
     900000,
     'pse',
     'exitoso',
     4 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 7,
     date '2026-06-04',
     1950000,
     'pse',
     'exitoso',
     5 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 8,
     date '2026-07-04',
     4400000,
     'tarjeta de credito',
     'exitoso',
     6 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 9,
     date '2026-07-10',
     1450000,
     'pse',
     'exitoso',
     7 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 10,
     date '2026-07-20',
     1450000,
     'efectivo',
     'exitoso',
     7 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 11,
     date '2026-07-27',
     4400000,
     'tarjeta debito',
     'exitoso',
     8 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 12,
     date '2026-07-29',
     640000,
     'transferencia',
     'exitoso',
     9 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 13,
     date '2026-08-15',
     480000,
     'efectivo',
     'exitoso',
     10 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 14,
     date '2026-08-23',
     480000,
     'pse',
     'exitoso',
     10 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 15,
     date '2026-07-25',
     960000,
     'pse',
     'exitoso',
     11 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 16,
     date '2026-08-29',
     3250000,
     'tarjeta de credito',
     'exitoso',
     12 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 17,
     date '2026-09-17',
     1800000,
     'pse',
     'exitoso',
     13 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 18,
     date '2026-09-24',
     1800000,
     'tarjeta de credito',
     'exitoso',
     13 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 19,
     date '2026-09-18',
     1950000,
     'tarjeta debito',
     'exitoso',
     14 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 20,
     date '2026-09-13',
     900000,
     'transferencia',
     'exitoso',
     15 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 21,
     date '2026-09-28',
     900000,
     'tarjeta debito',
     'exitoso',
     15 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 22,
     date '2026-10-03',
     2080000,
     'transferencia',
     'exitoso',
     16 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 23,
     date '2026-09-29',
     1300000,
     'efectivo',
     'exitoso',
     17 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 24,
     date '2026-10-16',
     1300000,
     'pse',
     'exitoso',
     17 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 25,
     date '2026-09-28',
     2080000,
     'pse',
     'exitoso',
     18 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 26,
     date '2026-10-06',
     780000,
     'tarjeta de credito',
     'exitoso',
     19 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 27,
     date '2026-10-16',
     780000,
     'transferencia',
     'exitoso',
     19 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 28,
     date '2026-07-09',
     570000,
     'tarjeta de credito',
     'exitoso',
     20 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 29,
     date '2026-10-23',
     2600000,
     'transferencia',
     'exitoso',
     21 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 30,
     date '2026-10-26',
     1350000,
     'tarjeta de credito',
     'fallido',
     22 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 31,
     date '2026-10-27',
     1350000,
     'efectivo',
     'exitoso',
     22 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 32,
     date '2026-09-09',
     390000,
     'pse',
     'exitoso',
     23 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 33,
     date '2026-10-16',
     2250000,
     'tarjeta de credito',
     'exitoso',
     24 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 34,
     date '2026-10-26',
     2250000,
     'pse',
     'exitoso',
     24 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 35,
     date '2026-09-09',
     1300000,
     'tarjeta de credito',
     'fallido',
     25 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 36,
     date '2026-09-10',
     1300000,
     'pse',
     'exitoso',
     25 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 37,
     date '2026-10-09',
     4400000,
     'tarjeta de credito',
     'exitoso',
     26 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 38,
     date '2026-10-27',
     2175000,
     'tarjeta de credito',
     'fallido',
     27 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 39,
     date '2026-10-28',
     2175000,
     'pse',
     'exitoso',
     27 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 40,
     date '2026-10-25',
     1650000,
     'tarjeta debito',
     'exitoso',
     28 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 41,
     date '2026-11-01',
     640000,
     'transferencia',
     'exitoso',
     29 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 42,
     date '2026-10-28',
     480000,
     'tarjeta de credito',
     'fallido',
     30 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 43,
     date '2026-10-29',
     480000,
     'efectivo',
     'exitoso',
     30 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 44,
     date '2026-11-03',
     192000,
     'pse',
     'exitoso',
     31 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 45,
     date '2026-10-03',
     2080000,
     'tarjeta de credito',
     'exitoso',
     32 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 46,
     date '2026-10-25',
     1040000,
     'tarjeta de credito',
     'fallido',
     33 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 47,
     date '2026-10-26',
     1040000,
     'pse',
     'exitoso',
     33 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 48,
     date '2026-11-03',
     780000,
     'tarjeta debito',
     'exitoso',
     34 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 49,
     date '2026-10-04',
     780000,
     'transferencia',
     'exitoso',
     35 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 50,
     date '2026-10-19',
     780000,
     'tarjeta debito',
     'exitoso',
     35 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 51,
     date '2026-11-02',
     228000,
     'transferencia',
     'exitoso',
     36 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 52,
     date '2026-10-29',
     54000,
     'tarjeta de credito',
     'exitoso',
     37 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 53,
     date '2026-10-29',
     2600000,
     'transferencia',
     'exitoso',
     38 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 54,
     date '2026-10-30',
     900000,
     'tarjeta de credito',
     'fallido',
     39 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 55,
     date '2026-10-31',
     900000,
     'efectivo',
     'exitoso',
     39 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 56,
     date '2026-10-21',
     585000,
     'pse',
     'exitoso',
     40 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 57,
     date '2026-10-21',
     1350000,
     'tarjeta de credito',
     'exitoso',
     41 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 58,
     date '2026-11-02',
     1350000,
     'pse',
     'exitoso',
     41 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 59,
     date '2026-09-17',
     360000,
     'tarjeta de credito',
     'exitoso',
     42 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 60,
     date '2026-09-10',
     360000,
     'pse',
     'exitoso',
     43 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 61,
     date '2026-09-22',
     360000,
     'efectivo',
     'exitoso',
     43 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 62,
     date '2026-10-11',
     1400000,
     'transferencia',
     'exitoso',
     44 );
insert into pago (
   id_pago,
   fecha_pago,
   monto,
   metodo_pago,
   estado_pago,
   id_reserva
) values
   ( 63,
     date '2026-10-13',
     1100000,
     'pse',
     'exitoso',
     45 );

-- PAGO_EN_LINEA (43)
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 4,
     'TX-4D5284B503' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 5,
     'TX-6BFA153504' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 6,
     'TX-5187B6EC05' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 7,
     'TX-0DFB6F3A06' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 8,
     'TX-CF71E7F507' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 9,
     'TX-EB2B50B508' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 11,
     'TX-724BF80B10' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 14,
     'TX-FB14B19513' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 15,
     'TX-1A1F80D114' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 16,
     'TX-A4FE556115' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 17,
     'TX-03F9C73E16' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 18,
     'TX-01397A2917' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 19,
     'TX-16904BEB18' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 21,
     'TX-DE9AC5EE20' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 24,
     'TX-2104142823' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 25,
     'TX-048D09C824' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 26,
     'TX-3E056E8025' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 28,
     'TX-0CD5E3E327' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 30,
     'TX-DB01B9F229' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 32,
     'TX-1594011E31' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 33,
     'TX-B5906F5732' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 34,
     'TX-F6DD601533' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 35,
     'TX-B79B14F334' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 36,
     'TX-02EB2C8635' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 37,
     'TX-1465F23336' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 38,
     'TX-4FA1CC6F37' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 39,
     'TX-0F4DAD8838' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 40,
     'TX-7050330839' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 42,
     'TX-AD47F8FA41' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 44,
     'TX-CC1FD5C743' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 45,
     'TX-A5176DA044' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 46,
     'TX-6AFFBC9A45' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 47,
     'TX-C9472C5946' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 48,
     'TX-F1E6679547' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 50,
     'TX-911AE38D49' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 52,
     'TX-4AD9F59851' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 54,
     'TX-FA3A077653' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 56,
     'TX-D4CF50A755' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 57,
     'TX-E35C18A056' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 58,
     'TX-606DE4EB57' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 59,
     'TX-AF507DE358' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 60,
     'TX-E567DABB59' );
insert into pago_en_linea (
   id_pago,
   referencia_transaccion
) values
   ( 63,
     'TX-CEB71A8F62' );

-- NOTIFICACION (25)
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 1,
     'recordatorio de reserva',
     'correo',
     'Tu salida del 06/11 es en 1 días',
     timestamp '2026-11-05 08:00:00',
     null,
     'pendiente',
     1,
     21 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 2,
     'recordatorio de reserva',
     'sms',
     'Tu salida del 06/11 es en 3 días',
     timestamp '2026-11-03 08:00:00',
     timestamp '2026-11-03 08:02:00',
     'enviada',
     2,
     22 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 3,
     'recordatorio de reserva',
     'whatsapp',
     'Tu salida del 06/11 es en 7 días',
     timestamp '2026-10-30 08:00:00',
     timestamp '2026-10-30 08:02:00',
     'enviada',
     3,
     23 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 4,
     'recordatorio de reserva',
     'correo',
     'Tu salida del 06/11 es en 1 días',
     timestamp '2026-11-05 08:00:00',
     null,
     'pendiente',
     4,
     24 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 5,
     'recordatorio de reserva',
     'sms',
     'Tu salida del 06/11 es en 3 días',
     timestamp '2026-11-03 08:00:00',
     timestamp '2026-11-03 08:02:00',
     'enviada',
     5,
     25 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 6,
     'recordatorio de reserva',
     'whatsapp',
     'Tu salida del 13/11 es en 7 días',
     timestamp '2026-11-06 08:00:00',
     null,
     'pendiente',
     6,
     26 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 7,
     'recordatorio de reserva',
     'correo',
     'Tu salida del 13/11 es en 1 días',
     timestamp '2026-11-12 08:00:00',
     null,
     'pendiente',
     7,
     27 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 8,
     'recordatorio de reserva',
     'sms',
     'Tu salida del 13/11 es en 3 días',
     timestamp '2026-11-10 08:00:00',
     null,
     'pendiente',
     8,
     28 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 9,
     'recordatorio de reserva',
     'whatsapp',
     'Tu salida del 20/11 es en 7 días',
     timestamp '2026-11-13 08:00:00',
     null,
     'pendiente',
     9,
     29 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 10,
     'recordatorio de reserva',
     'correo',
     'Tu salida del 20/11 es en 1 días',
     timestamp '2026-11-19 08:00:00',
     null,
     'pendiente',
     10,
     30 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 11,
     'recordatorio de reserva',
     'sms',
     'Tu salida del 20/11 es en 3 días',
     timestamp '2026-11-17 08:00:00',
     null,
     'pendiente',
     11,
     31 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 12,
     'recordatorio de reserva',
     'whatsapp',
     'Tu salida del 27/11 es en 7 días',
     timestamp '2026-11-20 08:00:00',
     null,
     'pendiente',
     12,
     32 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 13,
     'recordatorio de reserva',
     'correo',
     'Tu salida del 27/11 es en 1 días',
     timestamp '2026-11-26 08:00:00',
     null,
     'pendiente',
     13,
     33 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 14,
     'recordatorio de reserva',
     'sms',
     'Tu salida del 27/11 es en 3 días',
     timestamp '2026-11-24 08:00:00',
     null,
     'pendiente',
     14,
     34 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 15,
     'confirmacion de pago',
     'correo',
     'Recibimos tu pago de $2.600.000',
     timestamp '2026-05-31 10:00:00',
     timestamp '2026-05-31 10:01:00',
     'leida',
     1,
     1 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 16,
     'confirmacion de pago',
     'sms',
     'Recibimos tu pago de $1.350.000',
     timestamp '2026-05-23 10:00:00',
     timestamp '2026-05-23 10:01:00',
     'leida',
     2,
     2 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 17,
     'confirmacion de pago',
     'sms',
     'Recibimos tu pago de $1.350.000',
     timestamp '2026-06-11 10:00:00',
     timestamp '2026-06-11 10:01:00',
     'leida',
     2,
     2 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 18,
     'confirmacion de pago',
     'whatsapp',
     'Recibimos tu pago de $3.250.000',
     timestamp '2026-06-18 10:00:00',
     timestamp '2026-06-18 10:01:00',
     'leida',
     3,
     3 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 19,
     'confirmacion de pago',
     'correo',
     'Recibimos tu pago de $900.000',
     timestamp '2026-06-20 10:00:00',
     timestamp '2026-06-20 10:01:00',
     'leida',
     4,
     4 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 20,
     'confirmacion de pago',
     'correo',
     'Recibimos tu pago de $900.000',
     timestamp '2026-06-27 10:00:00',
     timestamp '2026-06-27 10:01:00',
     'leida',
     4,
     4 );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 21,
     'promocion',
     'correo',
     'Temporada diciembre: Ruta del café con hotel 4 estrellas',
     timestamp '2026-11-01 09:00:00',
     timestamp '2026-11-01 09:05:00',
     'enviada',
     1,
     null );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 22,
     'promocion',
     'whatsapp',
     'Temporada diciembre: Ruta del café con hotel 4 estrellas',
     timestamp '2026-11-01 09:00:00',
     timestamp '2026-11-01 09:05:00',
     'enviada',
     3,
     null );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 23,
     'promocion',
     'sms',
     'Temporada diciembre: Ruta del café con hotel 4 estrellas',
     timestamp '2026-11-01 09:00:00',
     timestamp '2026-11-01 09:05:00',
     'enviada',
     5,
     null );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 24,
     'promocion',
     'correo',
     'Temporada diciembre: Ruta del café con hotel 4 estrellas',
     timestamp '2026-11-01 09:00:00',
     timestamp '2026-11-01 09:05:00',
     'enviada',
     7,
     null );
insert into notificacion (
   id_notificacion,
   tipo,
   canal,
   mensaje,
   fecha_programada,
   fecha_envio,
   estado,
   id_cliente,
   id_reserva
) values
   ( 25,
     'cambio de estado',
     'correo',
     'Tu salida a Génova del 22/08 fue cancelada; te devolvemos el abono',
     timestamp '2026-08-15 09:00:00',
     timestamp '2026-08-15 09:03:00',
     'fallida',
     20,
     20 );

-- Estados finales: se aplican después de insertar las reservas, porque RN18
-- no deja reservar en salidas canceladas o realizadas.
update reserva
   set
   estado = 'cancelada'
 where id_reserva = 20;
update reserva
   set
   estado = 'cancelada'
 where id_reserva = 45;
update pago
   set
   estado_pago = 'reembolsado'
 where id_reserva = 20;
update pago
   set
   estado_pago = 'reembolsado'
 where id_reserva = 45;
update salida
   set
   estado = 'realizada'
 where id_salida = 1;
update salida
   set
   estado = 'realizada'
 where id_salida = 2;
update salida
   set
   estado = 'realizada'
 where id_salida = 3;
update salida
   set
   estado = 'realizada'
 where id_salida = 4;
update salida
   set
   estado = 'realizada'
 where id_salida = 5;
update salida
   set
   estado = 'cancelada'
 where id_salida = 6;
update salida
   set
   estado = 'confirmada'
 where id_salida = 14;
update salida
   set
   estado = 'cancelada'
 where id_salida = 16;

-- Los IDENTITY siguen después del último id insertado a mano
alter table municipio modify
   id_municipio generated by default as identity ( start with limit value );
alter table tipo_prestador modify
   id_tipo_prestador generated by default as identity ( start with limit value );
alter table rol modify
   id_rol generated by default as identity ( start with limit value );
alter table tipo_componente modify
   id_tipo_componente generated by default as identity ( start with limit value );
alter table prestador modify
   id_prestador generated by default as identity ( start with limit value );
alter table usuario_sistema modify
   id_usuario generated by default as identity ( start with limit value );
alter table destino modify
   id_destino generated by default as identity ( start with limit value );
alter table paquete modify
   id_paquete generated by default as identity ( start with limit value );
alter table plan modify
   id_plan generated by default as identity ( start with limit value );
alter table componente modify
   id_componente generated by default as identity ( start with limit value );
alter table salida modify
   id_salida generated by default as identity ( start with limit value );
alter table cliente modify
   id_cliente generated by default as identity ( start with limit value );
alter table perfil_cliente modify
   id_perfil generated by default as identity ( start with limit value );
alter table pasajero modify
   id_pasajero generated by default as identity ( start with limit value );
alter table cotizacion modify
   id_cotizacion generated by default as identity ( start with limit value );
alter table reserva modify
   id_reserva generated by default as identity ( start with limit value );
alter table pago modify
   id_pago generated by default as identity ( start with limit value );
alter table notificacion modify
   id_notificacion generated by default as identity ( start with limit value );

commit;

-- Verificación: cantidad de filas por tabla
select table_name,
       num_rows
  from (
   select 'MUNICIPIO' as table_name,
          count(*) as num_rows
     from municipio
   union all
   select 'TIPO_PRESTADOR' as table_name,
          count(*) as num_rows
     from tipo_prestador
   union all
   select 'ROL' as table_name,
          count(*) as num_rows
     from rol
   union all
   select 'TIPO_COMPONENTE' as table_name,
          count(*) as num_rows
     from tipo_componente
   union all
   select 'PRESTADOR' as table_name,
          count(*) as num_rows
     from prestador
   union all
   select 'USUARIO_SISTEMA' as table_name,
          count(*) as num_rows
     from usuario_sistema
   union all
   select 'DESTINO' as table_name,
          count(*) as num_rows
     from destino
   union all
   select 'PAQUETE' as table_name,
          count(*) as num_rows
     from paquete
   union all
   select 'ITINERARIO_DIA' as table_name,
          count(*) as num_rows
     from itinerario_dia
   union all
   select 'PLAN' as table_name,
          count(*) as num_rows
     from plan
   union all
   select 'COMPONENTE' as table_name,
          count(*) as num_rows
     from componente
   union all
   select 'PAQUETE_COMPONENTE' as table_name,
          count(*) as num_rows
     from paquete_componente
   union all
   select 'SALIDA' as table_name,
          count(*) as num_rows
     from salida
   union all
   select 'CLIENTE' as table_name,
          count(*) as num_rows
     from cliente
   union all
   select 'PERFIL_CLIENTE' as table_name,
          count(*) as num_rows
     from perfil_cliente
   union all
   select 'PASAJERO' as table_name,
          count(*) as num_rows
     from pasajero
   union all
   select 'COTIZACION' as table_name,
          count(*) as num_rows
     from cotizacion
   union all
   select 'DETALLE_COTIZACION' as table_name,
          count(*) as num_rows
     from detalle_cotizacion
   union all
   select 'RESERVA' as table_name,
          count(*) as num_rows
     from reserva
   union all
   select 'RESERVA_PASAJERO' as table_name,
          count(*) as num_rows
     from reserva_pasajero
   union all
   select 'PAGO' as table_name,
          count(*) as num_rows
     from pago
   union all
   select 'PAGO_EN_LINEA' as table_name,
          count(*) as num_rows
     from pago_en_linea
   union all
   select 'NOTIFICACION' as table_name,
          count(*) as num_rows
     from notificacion
)
 order by table_name;