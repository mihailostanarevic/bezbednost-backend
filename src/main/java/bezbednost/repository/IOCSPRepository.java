package bezbednost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import bezbednost.entity.OCSP;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public interface IOCSPRepository extends JpaRepository<OCSP, UUID> {

    OCSP findOneById(UUID id);
    OCSP findOneBySerialNum(BigInteger serial_num);
    List<OCSP> findAllByRevoker(UUID id);
}
