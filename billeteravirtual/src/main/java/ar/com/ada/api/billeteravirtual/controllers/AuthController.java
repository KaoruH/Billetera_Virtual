package ar.com.ada.api.billeteravirtual.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import ar.com.ada.api.billeteravirtual.entities.Usuario;
import ar.com.ada.api.billeteravirtual.models.request.*;
import ar.com.ada.api.billeteravirtual.models.response.*;
import ar.com.ada.api.billeteravirtual.security.jwt.JWTTokenUtil;
import ar.com.ada.api.billeteravirtual.services.*;

/**
 * AuthController
 */
@RestController
public class AuthController {

    @Autowired
    UsuarioService usuarioService;

    /*
     * @Autowired private AuthenticationManager authenticationManager;
     */
    @Autowired
    private JWTTokenUtil jwtTokenUtil;

    @Autowired
    private JWTUserDetailsService userDetailsService;

    // Auth : authentication -> / crea un usuario
    @PostMapping("auth/register")
    public ResponseEntity<RegistrationResponse> postRegisterUser(@RequestBody RegistrationRequest req) {
        RegistrationResponse r = new RegistrationResponse();
        // aca creamos la persona y el usuario a traves del service.
        // usuarioService.crearUsuario(parametros de req);

        usuarioService.crearUsuario(req.fullName, req.country, req.identificationType, req.identification,
                req.birthDate, req.email, req.password);

        r.isOk = true;
        r.message = "Te registraste con exitoooo!!!!!!!";
        r.userId = 0; // <-- AQUI ponemos el numerito de id para darle a front!
        return ResponseEntity.ok(r);

    }

    @PostMapping("auth/login") // probando nuestro login / a partir de un usuario ya existente, crea un token
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest authenticationRequest)
            throws Exception {

        usuarioService.login(authenticationRequest.username, authenticationRequest.password);

        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username);

        String token = jwtTokenUtil.generateToken(userDetails);

        // Cambio para que devuelva el full perfil
        Usuario u = usuarioService.buscarPorUsername(authenticationRequest.username);

        LoginResponse r = new LoginResponse();
        r.id = u.getUsuarioId();
        r.billeteraId = u.getPersona().getBilletera().getBilleteraId();
        r.username = authenticationRequest.username;
        r.email = u.getEmail();
        r.token = token;

        return ResponseEntity.ok(r);
        // return ResponseEntity.ok(new JwtResponse(token));

    }

}