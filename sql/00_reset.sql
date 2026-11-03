-- Borra todos los objetos del usuario QUINDIOSTAY para volver a correr los scripts desde cero.
-- Correr conectado como quindiostay. Orden de uso: 00_reset -> 01_ddl -> 02_triggers -> 03_datos
BEGIN
  FOR t IN (SELECT table_name FROM user_tables) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE "' || t.table_name || '" CASCADE CONSTRAINTS PURGE';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences WHERE sequence_name NOT LIKE 'ISEQ$$%') LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE "' || s.sequence_name || '"';
  END LOOP;
END;
/
