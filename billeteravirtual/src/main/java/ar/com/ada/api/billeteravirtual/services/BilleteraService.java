package ar.com.ada.api.billeteravirtual.services;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.ada.api.billeteravirtual.entities.*;
import ar.com.ada.api.billeteravirtual.entities.Transaccion.*;
import ar.com.ada.api.billeteravirtual.repos.BilleteraRepository;
import ar.com.ada.api.billeteravirtual.sistema.comm.EmailService;

@Service
public class BilleteraService {

    @Autowired
    BilleteraRepository billeteraRepo;

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    EmailService emailService;

    public void grabar(Billetera billetera) {
        billeteraRepo.save(billetera);
    }

    public Billetera buscarPorId(Integer id) {

        return billeteraRepo.findByBilleteraId(id);
    }

    /*
     * 1.Metodo: Cargar saldo 1.1-- Recibir un importe, se busca una billetera por
     * id, se busca una cuenta por la moneda 1.2-- hacer transaccion 1.3--
     * actualizar el saldo de la billetera
     * 
     * Metodo cargarSaldo buscar billetera por id se identifica cuenta por moneda
     * determinar importe a cargar hacer transaccion
     * 
     * ver delegaciones sobre entidades
     */

     // Este método busca la billetera por el ID y llama al método cargarSaldo pasando todos los parametros pero en lugar del Id de la billetera, pasa la billetera en si.
    public void cargarSaldo(BigDecimal saldo, String moneda, Integer billeteraId, String conceptoOperacion,
            String detalle) {

        Billetera billetera = this.buscarPorId(billeteraId);

        cargarSaldo(saldo, moneda, billetera, conceptoOperacion, detalle);
    }

    // Este método primero trae la cuenta de la billetera de la moneda especificada, despues genera una transaccion del usuario al usuario, la salva en la cuenta, graba la billetera con las actualizaciones y envia un email de confirmacion.
    public void cargarSaldo(BigDecimal saldo, String moneda, Billetera billetera, String conceptoOperacion,
            String detalle) {

        Cuenta cuenta = billetera.getCuenta(moneda);

        Transaccion transaccion = cuenta.generarTransaccion(conceptoOperacion, detalle, saldo, TipoTransaccionEnum.ENTRANTE);

        transaccion.setDeCuentaId(cuenta.getCuentaId());
        transaccion.setDeUsuarioId(billetera.getPersona().getUsuario().getUsuarioId());

        // Transaccion transaccion = new Transaccion();
        // // transaccion.setCuenta(cuenta);
        // transaccion.setMoneda(moneda);
        // transaccion.setFecha(new Date());
        // transaccion.setConceptoOperacion(conceptoOperacion);
        // transaccion.setDetalle(detalle);
        // transaccion.setImporte(saldo);
        // transaccion.setTipoOperacion(TipoTransaccionEnum.ENTRANTE);// 1 Entrada, 0 Salida
        // transaccion.setEstadoId(2);// -1 Rechazada 0 Pendiente 2 Aprobada
        // transaccion.setDeCuentaId(cuenta.getCuentaId());
        // transaccion.setDeUsuarioId(billetera.getPersona().getUsuario().getUsuarioId());
        // transaccion.setaUsuarioId(billetera.getPersona().getUsuario().getUsuarioId());
        // transaccion.setaCuentaId(cuenta.getCuentaId());

        cuenta.agregarTransaccion(transaccion);

        this.grabar(billetera);

        emailService.SendEmail(billetera.getPersona().getUsuario().getEmail(), "Carga Saldo",
                "Tu carga fue exitosa. Saldo: " + saldo);

    }

    /*
     * 3. Metodo: consultar saldo 3.1-- recibir el id de la billetera y la moneda en
     * la que esta la cuenta
     * 
     * Metodo consultarSaldo buscar billetera por id se identifica cuenta por moneda
     * traer saldo
     */

     // El método busca la billetera con el Id indicado, trae la cuenta de la moneda indicada desde la billetera y devuelve el saldo de esta cuenta

    public BigDecimal consultarSaldo(Integer billeteraId, String moneda) {

        Billetera billetera = billeteraRepo.findByBilleteraId(billeteraId);

        Cuenta cuenta = billetera.getCuenta(moneda);

        return cuenta.getSaldo();

    }

    /*
     * 2. Metodo: enviar plata 2.1-- recibir un importe, la moneda en la que va a
     * estar ese importe recibir una billetera de origen y otra de destino 2.2--
     * actualizar los saldos de las cuentas (a una se le suma y a la otra se le
     * resta) 2.3-- generar dos transacciones
     * 
     * Metodo enviarSaldo buscar billetera por id se identifica cuenta por moneda
     * determinar importe a transferir billetera de origen y billetera destino
     * actualizar los saldos de las cuentas (resta en la origen y suma en la
     * destino) generar 2 transacciones
     * 
     * ver delegaciones sobre entidades
     * 
     */

    public ResultadoTransaccionEnum enviarSaldo(BigDecimal importe, String moneda, Integer billeteraOrigenId,
            Integer billeteraDestinoId, String concepto, String detalle) {

        if (importe.compareTo(new BigDecimal(0)) == -1)
            return ResultadoTransaccionEnum.ERROR_IMPORTE_NEGATIVO;

        Billetera billeteraSaliente = this.buscarPorId(billeteraOrigenId);

        if (billeteraSaliente == null)
            return ResultadoTransaccionEnum.BILLETERA_ORIGEN_NO_ENCONTRADA;

        Billetera billeteraEntrante = this.buscarPorId(billeteraDestinoId);

        if (billeteraEntrante == null)
            return ResultadoTransaccionEnum.BILLETERA_DESTINO_NO_ENCONTRADA;

        Cuenta cuentaSaliente = billeteraSaliente.getCuenta(moneda);

        if (cuentaSaliente == null)
            return ResultadoTransaccionEnum.CUENTA_ORIGEN_INEXISTENTE;

        Cuenta cuentaEntrante = billeteraEntrante.getCuenta(moneda);

        if (cuentaEntrante == null)
            return ResultadoTransaccionEnum.CUENTA_DESTINO_INEXITENTE;

        if (cuentaSaliente.getSaldo().compareTo(importe) == -1)
            return ResultadoTransaccionEnum.SALDO_INSUFICIENTE;

        Transaccion tSaliente = new Transaccion();
        Transaccion tEntrante = new Transaccion();

        tSaliente = cuentaSaliente.generarTransaccion(concepto, detalle, importe, TipoTransaccionEnum.SALIENTE);
        tSaliente.setaCuentaId(cuentaEntrante.getCuentaId());
        tSaliente.setaUsuarioId(billeteraEntrante.getPersona().getUsuario().getUsuarioId());

        tEntrante = cuentaEntrante.generarTransaccion(concepto, detalle, importe, TipoTransaccionEnum.ENTRANTE);
        tEntrante.setDeCuentaId(cuentaSaliente.getCuentaId());
        tEntrante.setDeUsuarioId(billeteraSaliente.getPersona().getUsuario().getUsuarioId());

        cuentaSaliente.agregarTransaccion(tSaliente);
        cuentaEntrante.agregarTransaccion(tEntrante);

        this.grabar(billeteraSaliente);
        this.grabar(billeteraEntrante);

        emailService.SendEmail(billeteraEntrante.getPersona().getUsuario().getEmail(), "Transferencia",
                "Recibio " + importe + " de el usuario " + billeteraSaliente.getPersona().getUsuario().getEmail());
        emailService.SendEmail(billeteraSaliente.getPersona().getUsuario().getEmail(), "Transferencia",
                "Se realizo la transferencia con exito a " + billeteraEntrante.getPersona().getUsuario().getEmail()
                        + " y recibio " + importe);

        return ResultadoTransaccionEnum.INICIADA;

    }

    public ResultadoTransaccionEnum enviarSaldo(BigDecimal importe, String moneda, Integer billeteraOrigenId,
            String email, String concepto, String detalle) {

        Usuario usuarioDestino = usuarioService.buscarPorEmail(email);

        if (usuarioDestino == null)
            return ResultadoTransaccionEnum.EMAIL_DESTINO_INEXISTENTE;
        return this.enviarSaldo(importe, moneda, billeteraOrigenId,
                usuarioDestino.getPersona().getBilletera().getBilleteraId(), concepto, detalle);

    }

    public List<Transaccion> listarTransacciones(Billetera billetera, String moneda) {

        List<Transaccion> movimientos = new ArrayList<>();

        Cuenta cuenta = billetera.getCuenta(moneda);

        for (Transaccion transaccion : cuenta.getTransacciones()) {

            movimientos.add(transaccion);
        }

        return movimientos;
    }

    public List<Transaccion> listarTransacciones(Billetera billetera) {

        List<Transaccion> movimientos = new ArrayList<>();

        for (Cuenta cuenta : billetera.getCuentas()) {

            for (Transaccion transaccion : cuenta.getTransacciones()) {

                movimientos.add(transaccion);
            }
        }
        return movimientos;
    }
}