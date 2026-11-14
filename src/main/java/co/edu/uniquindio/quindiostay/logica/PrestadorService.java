package co.edu.uniquindio.quindiostay.logica;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import co.edu.uniquindio.quindiostay.datos.PrestadorDAO;
import co.edu.uniquindio.quindiostay.modelo.Prestador;

public class PrestadorService {
    public static final List<String> ESTADOS = List.of("activo", "suspendido", "retirado");

    private static final Pattern NIT = Pattern.compile("^[0-9-]{6,15}$");
    private static final Pattern RNT = Pattern.compile("^[0-9]{1,10}$");
    private static final Pattern TELEFONO = Pattern.compile("^(3[0-9]{9}|60[1-8][0-9]{7})$");
    private static final Pattern CORREO = Pattern.compile("^[^@ ]+@[^@ ]+\\.[^@ ]+$");

    private final PrestadorDAO dao = new PrestadorDAO();

    public List<Prestador> consultar(String filtro) throws SQLException {
        return dao.consultar(filtro);
    }

    /** Inserta si es nuevo (id == null) o actualiza si ya existe. */
    public void guardar(Prestador p) throws SQLException, ReglaException {
        exigirAdmin();
        validar(p);
        try {
            if (p.id() == null) {
                dao.insertar(p);
            } else {
                dao.actualizar(p);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {   // UNIQUE: NIT o RNT repetidos
                String texto = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                throw new ReglaException(texto.contains("rnt")
                        ? "Ya existe un prestador con ese número de RNT."
                        : "Ya existe un prestador con ese NIT.");
            }
            throw e;
        }
    }

    public void eliminar(Prestador p) throws SQLException, ReglaException {
        exigirAdmin();
        try {
            dao.eliminar(p.id());
        } catch (SQLException e) {
            if (e.getErrorCode() == 2292) {   // RESTRICT: tiene paquetes, componentes o cotizaciones
                throw new ReglaException("No se puede eliminar \"" + p.nombreComercial()
                        + "\": tiene paquetes, componentes o cotizaciones registrados (RESTRICT). "
                        + "Si ya no opera, cámbiale el estado a retirado para conservar su historial.");
            }
            throw e;
        }
    }

    private void validar(Prestador p) throws ReglaException {
        texto(p.nombreComercial(), "El nombre comercial", 100);
        if (p.nit() == null || !NIT.matcher(p.nit()).matches()) {
            throw new ReglaException("El NIT debe tener entre 6 y 15 caracteres, solo dígitos y guion.");
        }
        if (p.numeroRnt() == null || !RNT.matcher(p.numeroRnt()).matches()) {
            throw new ReglaException("El número de RNT debe tener entre 1 y 10 dígitos.");
        }
        texto(p.direccion(), "La dirección", 120);
        if (p.telefono() == null || !TELEFONO.matcher(p.telefono()).matches()) {
            throw new ReglaException("El teléfono debe ser un celular de 10 dígitos (empieza por 3) "
                    + "o un fijo con indicativo 601 a 608 (10 dígitos).");
        }
        if (p.correo() == null || p.correo().length() > 100 || !CORREO.matcher(p.correo()).matches()) {
            throw new ReglaException("El correo no es válido (máximo 100 caracteres).");
        }
        if (!ESTADOS.contains(p.estado())) {
            throw new ReglaException("Selecciona el estado.");
        }
        if (p.idTipoPrestador() <= 0) {
            throw new ReglaException("Selecciona el tipo de prestador.");
        }
        if (p.idMunicipio() <= 0) {
            throw new ReglaException("Selecciona el municipio.");
        }
    }

    private void texto(String valor, String campo, int max) throws ReglaException {
        if (valor == null || valor.isBlank()) {
            throw new ReglaException(campo + " es obligatorio.");
        }
        if (valor.length() > max) {
            throw new ReglaException(campo + " no puede tener más de " + max + " caracteres.");
        }
    }

    private void exigirAdmin() throws ReglaException {
        if (!Sesion.esAdmin()) {
            throw new ReglaException("Solo el administrador puede modificar los prestadores.");
        }
    }
    
}
