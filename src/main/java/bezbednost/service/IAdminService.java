package bezbednost.service;

import bezbednost.entity.Admin;

import java.util.UUID;

public interface IAdminService {

    Admin findOneById(UUID id);

}
