package com.smartuis.module.application.controller;

import com.smartuis.module.domian.repository.StorageRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    private StorageRepository storageRepository;

    public FileController(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @PostMapping("/save")
    void saveFile(@RequestBody MultipartFile file)  {
        System.out.println(file.getOriginalFilename());

        storageRepository.saveFile(file, file.getOriginalFilename());
    }
}
