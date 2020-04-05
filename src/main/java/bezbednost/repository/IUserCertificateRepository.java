package bezbednost.repository;

import bezbednost.entity.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IUserCertificateRepository extends JpaRepository<UserCertificate, UUID> {

    UserCertificate findOneById(UUID id);
}
