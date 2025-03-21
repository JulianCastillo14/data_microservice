package com.smartuis.module.application.thread;

import com.smartuis.module.application.exceptions.ConectionStorageException;
import com.smartuis.module.domian.repository.StorageRepository;
import com.smartuis.module.persistence.exceptions.UnitsTimeException;
import com.smartuis.module.persistence.exceptions.UploadFileException;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.concurrent.BlockingQueue;

public class CameraThread extends Thread {

    private StorageRepository storageRepository;
    private String idThread;
    private String urlConnect;
    private Boolean paused;
    private final String extension;
    private BlockingQueue<Exception> exceptionQueue;

    public CameraThread(StorageRepository storageRepository, String idThread, String urlConnect, BlockingQueue<Exception> exceptionQueue){
        this.storageRepository = storageRepository;
        this.idThread = idThread;
        this.urlConnect = urlConnect;
        this.paused = false;
        this.extension = "mp4";
        this.exceptionQueue = exceptionQueue;
    }

    @Override
    public void run()  {

        try {
            startRecord(this.urlConnect);
        } catch (FFmpegFrameRecorder.Exception | FFmpegFrameGrabber.Exception | InterruptedException e) {
            new ConectionStorageException("Hubo un erro con la conexion");
        }
    }

    public void startRecord(String urlConexion) throws FFmpegFrameGrabber.Exception, InterruptedException, FFmpegFrameRecorder.Exception {

            String fileTempName = "application/" + idThread + "." + extension;
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(urlConexion);
            grabber.start();

            while (!isInterrupted()) {
                var recorder = new FFmpegFrameRecorder(fileTempName, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels());
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setFormat(extension);
                recorder.setFrameRate(grabber.getFrameRate());

                recorder.start();
                long lastTime = System.currentTimeMillis();
                long activeTimeElapsed = 0;
                long duration = 1 * 60 * 1000;

                while (activeTimeElapsed < duration && !isInterrupted()) {
                    synchronized (this) {
                        while (paused) {
                            System.out.println("pausado");
                            wait();
                            lastTime = System.currentTimeMillis();
                        }
                    }

                    System.out.println("grabando");
                    Frame frame = grabber.grab();
                    if (frame == null) continue;
                    recorder.record(frame);

                    long now = System.currentTimeMillis();
                    activeTimeElapsed += (now - lastTime);
                    lastTime = now;
                }

                recorder.stop();

                if (isInterrupted()) {
                    break;
                }

                File file = new File(fileTempName);
                String pathname = idThread + "/" + idThread + " - " + Instant.now() + "." + extension;

                storageRepository.saveFile(file, pathname);
                System.out.println(storageRepository.toString());
                System.out.println("guardando");
            }

            File file = new File(fileTempName);
            file.delete();

    }


    public void stopRecord(){
        this.interrupt();
    }


    public String getIdThread() {
        return idThread;
    }


    public synchronized void pauseRecord(){
        paused = true;
    }


    public synchronized void resumeRecord(){
        paused = false;
        this.notify();
    }

}



