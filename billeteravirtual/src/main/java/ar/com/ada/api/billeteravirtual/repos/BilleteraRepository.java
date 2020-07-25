package ar.com.ada.api.billeteravirtual.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ar.com.ada.api.billeteravirtual.entities.Billetera;

@Repository
public interface BilleteraRepository extends JpaRepository<Billetera, Integer> {

    public Billetera findByBilleteraId(Integer id);

}