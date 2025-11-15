package esfe.skyfly.Repositorios;

import esfe.skyfly.Modelos.Destino;
import groovyjarjarantlr4.v4.parse.ANTLRParser.parserRule_return;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IDestinoRepository extends JpaRepository<Destino, Integer> {
    Page<Destino> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
        String nombre, String descripcion, Pageable pageable);
@Query("select d.imagen from Destino d where d.id = :id")
byte[] findImagenById(@Param("id") Integer id);
}
