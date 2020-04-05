package bezbednost.repository;

import bezbednost.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IAdminRepository extends JpaRepository<Admin, UUID> {

    Admin findOneById(UUID id);
}
