package com.ina.dao;


import com.ina.dao.entity.EMVParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EMVParametersRepository extends JpaRepository<EMVParameters,Long> {
    EMVParameters findByDeviceId(String deviceId);

}
