-- Borra todos los objetos del usuario QUINDIOSTAY para volver a correr los scripts desde cero.
-- Correr conectado como quindiostay con "Run Script" (F5).
-- Orden de uso: 00_reset -> 01_ddl -> 02_triggers -> 03_datos
begin
  -- Al borrar las tablas se borran también sus triggers e índices
   for t in (
      select table_name
        from user_tables
   ) loop
      execute immediate 'DROP TABLE "'
                        || t.table_name
                        || '" CASCADE CONSTRAINTS PURGE';
   end loop;
   for o in (
      select object_name,
             object_type
        from user_objects
       where object_type in ( 'PROCEDURE',
                              'FUNCTION',
                              'VIEW',
                              'PACKAGE' )
   ) loop
      execute immediate 'DROP '
                        || o.object_type
                        || ' "'
                        || o.object_name
                        || '"';
   end loop;
   for s in (
      select sequence_name
        from user_sequences
       where sequence_name not like 'ISEQ$$%'
   ) loop
      execute immediate 'DROP SEQUENCE "'
                        || s.sequence_name
                        || '"';
   end loop;
end;
/