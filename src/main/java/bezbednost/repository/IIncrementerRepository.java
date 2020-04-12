package bezbednost.repository;

import bezbednost.entity.Incrementer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IIncrementerRepository extends JpaRepository<Incrementer, UUID> {
}
