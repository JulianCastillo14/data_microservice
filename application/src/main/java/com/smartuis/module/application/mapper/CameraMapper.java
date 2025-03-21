package com.smartuis.module.application.mapper;

import com.smartuis.module.domian.entity.Camera;
import com.smartuis.module.domian.entity.CameraDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CameraMapper {

    public CameraDTO mapCameraToCameraDTO(Camera camera){
        return new CameraDTO(camera.getId(), camera.getName(), camera.getState());
    }

    public List<CameraDTO> mapCameraToCameraDTO(List<Camera> cameras){
        return cameras.stream().map(camera->
                        new CameraDTO(camera.getId(), camera.getName(), camera.getState()))
                .toList();
    }
}
