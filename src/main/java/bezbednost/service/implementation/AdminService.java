package bezbednost.service.implementation;

import bezbednost.entity.Admin;
import bezbednost.repository.IAdminRepository;
import bezbednost.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService implements IAdminService {

    @Autowired
    IAdminRepository _adminRepository;

    @Override
    public Admin findOneById(UUID id) {
        return _adminRepository.findOneById(id);
    }
}
