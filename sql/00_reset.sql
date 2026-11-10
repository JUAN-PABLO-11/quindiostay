-- Borra todos los objetos del usuario QUINDIOSTAY para volver a correr los scripts desde cero.
-- Correr conectado como quindiostay con "Run Script" (F5).
-- Orden de uso: 00_reset -> 01_ddl -> 02_triggers -> 03_datos
BEGIN
  -- Al borrar las tablas se borran también sus triggers e índices
  FOR t IN (SELECT table_name FROM user_tables) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE "' || t.table_name || '" CASCADE CONSTRAINTS PURGE';
  END LOOP;
  FOR o IN (SELECT object_name, object_type FROM user_objects
             WHERE object_type IN ('PROCEDURE', 'FUNCTION', 'VIEW', 'PACKAGE')) LOOP
    EXECUTE IMMEDIATE 'DROP ' || o.object_type || ' "' || o.object_name || '"';
  END LOOP;
  FOR s IN (SELECT sequence_name FROM user_sequences WHERE sequence_name NOT LIKE 'ISEQ$$%') LOOP
    EXECUTE IMMEDIATE 'DROP SEQUENCE "' || s.sequence_name || '"';
  END LOOP;
END;
/
