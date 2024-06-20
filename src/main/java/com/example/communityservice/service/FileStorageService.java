package com.example.communityservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service // 이 클래스가 스프링 서비스 빈임을 나타냅니다.
public class FileStorageService {

    private final Path fileStorageLocation; // 파일 저장 경로를 저장하는 변수

    // 생성자: 파일 업로드 디렉토리 경로를 설정하고 해당 디렉토리를 생성합니다.
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize(); // 파일 저장 경로를 절대 경로로 설정하고 정규화합니다.

        try {
            Files.createDirectories(this.fileStorageLocation); // 파일 저장 디렉토리를 생성합니다.
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex); // 디렉토리를 생성할 수 없는 경우 예외를 발생시킵니다.
        }
    }

    // 파일을 저장하는 메서드
    public String storeFile(MultipartFile file) {
        // 파일 이름을 정규화합니다.
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // 파일 이름에 잘못된 문자가 포함되어 있는지 확인합니다.
            if(fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName); // 파일 이름에 잘못된 문자가 포함된 경우 예외를 발생시킵니다.
            }

            // 파일을 대상 위치로 복사합니다. (같은 이름의 기존 파일을 대체합니다.)
            Path targetLocation = this.fileStorageLocation.resolve(fileName); // 대상 위치를 설정합니다.
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING); // 파일을 복사합니다.

            return fileName; // 저장된 파일 이름을 반환합니다.
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex); // 파일을 저장할 수 없는 경우 예외를 발생시킵니다.
        }
    }
}
