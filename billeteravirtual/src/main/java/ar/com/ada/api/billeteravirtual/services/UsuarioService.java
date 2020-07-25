package ar.com.ada.api.billeteravirtual.services;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.stereotype.Service;

import ar.com.ada.api.billeteravirtual.entities.*;
import ar.com.ada.api.billeteravirtual.repos.UsuarioRepository;
import ar.com.ada.api.billeteravirtual.security.Crypto;
import ar.com.ada.api.billeteravirtual.sistema.comm.EmailService;

@Service
public class UsuarioService {

  @Autowired
  PersonaService personaService;
  @Autowired
  BilleteraService billeteraService;
  @Autowired
  UsuarioRepository usuarioRepository;
  @Autowired
  EmailService emailService;

	public Usuario buscarPorUsername(String username) {
		return usuarioRepository.findByUsername(username);
	}

	public void login(String username, String password) {

        /**
     * Metodo IniciarSesion recibe usuario y contraseña validar usuario y contraseña
     */

    Usuario u = buscarPorUsername(username);

    if (u == null || !u.getPassword().equals(Crypto.encrypt(password, u.getUsername()))) {

      throw new BadCredentialsException("Usuario o contraseña invalida");
    }
	}
     
    /*1.Metodo: Crear Usuario
    1.1-->Crear una Persona(setearle un usuario)
    1.2-->crear un usuario
    1.3-->Crear una billetera(setearle una persona)
    1.4-->Crear una cuenta en pesos y otra en dolares
    
         * Metodo para crearUsuario 1 crear persona (se le settea un usuario) 2 crear
     * usuario 3 crear billetera 4 crear cuenta por moneda(ARS y/o USD?)*/

    public Usuario crearUsuario(String nombre, int pais, int tipoDocumento, String documento, Date fechaNacimiento,
      String email, String password) {

    Persona persona = new Persona();
    persona.setNombre(nombre);
    persona.setPaisId(pais);
    persona.setTipoDocumentoId(tipoDocumento);
    persona.setDocumento(documento);
    persona.setFechaNacimiento(fechaNacimiento);

    Usuario usuario = new Usuario();
    usuario.setUsername(email);
    usuario.setEmail(email);
    usuario.setPassword(Crypto.encrypt(password, email));

    persona.setUsuario(usuario);

    personaService.grabar(persona);

    Billetera billetera = new Billetera();

    Cuenta pesos = new Cuenta();

    pesos.setSaldo(new BigDecimal(0));
    pesos.setMoneda("ARS");

    Cuenta dolares = new Cuenta();

    dolares.setSaldo(new BigDecimal(0));
    dolares.setMoneda("USD");

    billetera.agregarCuenta(pesos);
    billetera.agregarCuenta(dolares);

    persona.setBilletera(billetera);

    billeteraService.grabar(billetera);

    billeteraService.cargarSaldo(new BigDecimal(500), "ARS", billetera, "regalo", "Bienvenida por creacion de usuario");

    emailService.SendEmail(usuario.getEmail(), "Bienvenido a La Billetera Virtual de ADA",
        "Felicidades! Te regalamos 500 ARS como bienvenida a Billetera Virtual! :D Saludos");

    return usuario;
  }

  public Usuario buscarPorEmail(String email) {

    return usuarioRepository.findByEmail(email);
  }

  public Usuario buscarPor(Integer id) {
    Optional<Usuario> usuarioOp = usuarioRepository.findById(id);

    if (usuarioOp.isPresent()) {
      return usuarioOp.get();
    }

    return null;
  }


    /* 2. Metodo: Iniciar Sesion 
    2.1-- recibe el username y la password
    2.2-- vamos a validar los datos
    2.3-- devolver un verdadero o falso
    */

}