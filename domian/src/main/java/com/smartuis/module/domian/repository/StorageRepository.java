package com.smartuis.module.domian.repository;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;

public interface StorageRepository {
    void saveFile(File file, String pathname);
    void saveFile(MultipartFile file, String pathname);
}
