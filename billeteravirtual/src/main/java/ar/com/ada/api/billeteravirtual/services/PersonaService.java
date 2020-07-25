package ar.com.ada.api.billeteravirtual.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.ada.api.billeteravirtual.entities.Persona;
import ar.com.ada.api.billeteravirtual.repos.PersonaRepository;

@Service
public class PersonaService {
    
    @Autowired
    PersonaRepository personaRepo;

    public void grabar(Persona persona){
        personaRepo.save(persona);
    }
    
}