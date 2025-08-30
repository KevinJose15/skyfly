package esfe.skyfly.Repositorios;

import org.springframework.data.jpa.repository.JpaRepository;

import esfe.skyfly.Modelos.MetodoPago;

public interface IMetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

}