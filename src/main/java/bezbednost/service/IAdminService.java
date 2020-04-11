package bezbednost.service;

import bezbednost.entity.Admin;

import java.util.List;
import java.util.UUID;

public interface IAdminService {

    Admin findOneById(UUID id);

    List<Admin> findAll();

}
