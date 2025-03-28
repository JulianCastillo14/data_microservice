package com.smartuis.module.application.controller;

import com.smartuis.module.application.thread.ListCameraThread;
import com.smartuis.module.application.thread.CameraThread;
import com.smartuis.module.application.exceptions.ConectionStorageException;
import com.smartuis.module.application.mapper.CameraMapper;
import com.smartuis.module.domian.entity.Camera;
import com.smartuis.module.domian.entity.CameraDTO;
import com.smartuis.module.domian.entity.StateCamera;
import com.smartuis.module.domian.repository.CameraRepository;
import com.smartuis.module.domian.repository.StorageRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/camera")
public class CameraController {

    private StorageRepository storageRepository;
    private ListCameraThread listCameraThread;
    private CameraRepository cameraRepository;

    public CameraController(StorageRepository storageRepository, CameraRepository cameraRepository) {
        this.storageRepository = storageRepository;
        this.listCameraThread = listCameraThread.getInstance();
        this.cameraRepository = cameraRepository;
    }


    @GetMapping(value = "/stream", produces = "multipart/x-mixed-replace;boundary=frame" )
    public void startStream(HttpServletResponse response, @RequestParam(value = "idCamera") String idCamera) throws Exception {
        Camera camera = cameraRepository.findById(idCamera);

        response.setContentType("multipart/x-mixed-replace;boundary=frame");

        String rtspUrl = camera.getUrl();
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl);
        grabber.start();

        Java2DFrameConverter converter = new Java2DFrameConverter();

        while (true) {
            Frame frame = grabber.grab();
            if (frame == null) {
                break;
            }

            BufferedImage bufferedImage = converter.convert(frame);
            if (bufferedImage == null) {
                continue;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            response.getOutputStream().write(("--frame\r\n" +
                    "Content-Type: image/jpeg\r\n" +
                    "Content-Length: " + imageBytes.length + "\r\n\r\n").getBytes());
            response.getOutputStream().write(imageBytes);
            response.getOutputStream().write("\r\n".getBytes());
            response.getOutputStream().flush();

        }
        grabber.stop();
    }

    @GetMapping("/start")
    public ResponseEntity startStream(@RequestParam(value = "idCamera") String idCamera)   {
        Camera camera = cameraRepository.findById(idCamera);


        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if (existsHilo){
            return ResponseEntity.badRequest().body("Esta camara ya esta grabando");
        }

        BlockingQueue<Exception> exceptionQueue = new LinkedBlockingQueue<>();
        CameraThread cameraThread = new CameraThread(storageRepository, camera.getName(), camera.getUrl(), exceptionQueue);
        cameraThread.start();

        try {
            Exception exceptionHilo = exceptionQueue.poll(1,TimeUnit.SECONDS);
            if (exceptionHilo != null) {
                throw exceptionHilo;
            }

            listCameraThread.getThreads().add(cameraThread);
            camera.setState(StateCamera.Recording);
            cameraRepository.save(camera);
            CameraDTO cameraDTO = CameraMapper.mapCameraToCameraDTO(camera);
            return ResponseEntity.ok(cameraDTO);
        } catch (Exception e) {
            throw new ConectionStorageException("Hubo un erro con la conexion");
        }


    }

    @GetMapping("/stop")
    public ResponseEntity stopStream(@RequestParam(value = "idCamera") String idCamera)   {
        Camera camera = cameraRepository.findById(idCamera);

        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if(!existsHilo){
            return ResponseEntity.badRequest().body("Esa camara ya esta parada");
        }

        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.stopRecord();
        listCameraThread.getThreads().remove(reproductor);

        camera.setState(StateCamera.Stopped);
        cameraRepository.save(camera);
        CameraDTO cameraDTO = CameraMapper.mapCameraToCameraDTO(camera);
        return ResponseEntity.ok().body(cameraDTO);
    }

    @GetMapping("/pause")
    public ResponseEntity pauseStream(@RequestParam(value = "idCamera") String idCamera)   {

        Camera camera = cameraRepository.findById(idCamera);

        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if(!existsHilo){
            return ResponseEntity.badRequest().body("Esa camara ya esta parada");
        }

        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.pauseRecord();
        camera.setState(StateCamera.Paused);
        cameraRepository.save(camera);
        CameraDTO cameraDTO = CameraMapper.mapCameraToCameraDTO(camera);
        return ResponseEntity.ok().body(cameraDTO);
    }

    @GetMapping("/resume")
    public ResponseEntity resumeStream(@RequestParam(value = "idCamera") String idCamera)   {

        Camera camera = cameraRepository.findById(idCamera);

        if (camera == null){
            return ResponseEntity.badRequest().body("Esta camara no existe");
        }

        boolean existsHilo = listCameraThread.existHilo(camera.getName());
        if(!existsHilo){
            return ResponseEntity.badRequest().body("Esa camara  esta parada y no pausada. Dale Start.");
        }

        CameraThread reproductor = listCameraThread.findThread(camera.getName());
        reproductor.resumeRecord();
        camera.setState(StateCamera.Recording);
        cameraRepository.save(camera);
        CameraDTO cameraDTO = CameraMapper.mapCameraToCameraDTO(camera);
        return ResponseEntity.ok(cameraDTO);
    }


    @PostMapping("/add")
    public ResponseEntity addCamera(@RequestBody @Valid Camera camera){

        boolean existNombre = cameraRepository.existsByName(camera.getName());

        if (existNombre){
            return ResponseEntity.badRequest().body("Ya existe una camara con ese nombre");
        }

        boolean existUrl = cameraRepository.existsByUrl(camera.getUrl());

        if (existUrl){
            return ResponseEntity.badRequest().body("Ya existe una camara con esa url");
        }

        camera.setState(StateCamera.Stopped);
        return ResponseEntity.ok(cameraRepository.save(camera));
    }

    @GetMapping("/list")
    public List<CameraDTO> listAllCamera(){
        List<Camera> cameras = cameraRepository.findAll();
        if(listCameraThread.getThreads().isEmpty()){
            cameras.stream().forEach(camera -> camera.setState(StateCamera.Stopped));
            cameraRepository.saveAll(cameras);
        }

        return CameraMapper.mapCameraToCameraDTO(cameras);
    }


    
}
