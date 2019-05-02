package com.autonomoussystemserver.server.repository;

import com.autonomoussystemserver.server.domainModel.Devices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DevicesRepository extends JpaRepository<Devices, UUID>{
}