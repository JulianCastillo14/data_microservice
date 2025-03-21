package com.smartuis.module.domian.repository;

import com.smartuis.module.domian.entity.Camera;

import java.util.List;

public interface CameraRepository {

    Camera findById(String idCamera);

    Camera save(Camera camera);

    List<Camera> findAll();

    List<Camera> saveAll(List<Camera> cameras);

    boolean existsByName(String name);

    boolean existsByUrl(String url);
}
