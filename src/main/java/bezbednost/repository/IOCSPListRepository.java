package bezbednost.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import bezbednost.entity.OCSPEntity;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public interface IOCSPListRepository extends JpaRepository<OCSPEntity, UUID> {

    OCSPEntity findOneById(UUID id);
    OCSPEntity findOneBySerialNum(BigInteger serial_num);
    List<OCSPEntity> findAllByRevoker(UUID id);
    List<OCSPEntity> findAll();
}
