package ssafy.retrip.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class S3Uploader {

    private final S3Client s3Client;
    private final String bucket;
    private final String dirName;

    public S3Uploader(
            @Value("${spring.cloud.aws.credentials.access-key}") String accessKey,
            @Value("${spring.cloud.aws.credentials.secret-key}") String secretKey,
            @Value("${spring.cloud.aws.region.static}") String region,
            @Value("${spring.cloud.aws.s3.bucket}") String bucket,
            @Value("${spring.cloud.aws.s3.dir-name}") String dirName) {
        
        this.bucket = bucket;
        this.dirName = dirName;
        
        // 명시적으로 UrlConnectionHttpClient 지정
        this.s3Client = S3Client.builder()
                .httpClient(UrlConnectionHttpClient.builder().build())
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 변환 실패"));
        
        return upload(uploadFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);
        
        // 로컬에 생성된 파일 삭제
        removeNewFile(uploadFile);
        
        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build(), 
                RequestBody.fromFile(uploadFile));
        
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileName)).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            System.out.println("파일이 삭제되었습니다.");
        } else {
            System.out.println("파일이 삭제되지 못했습니다.");
        }
    }

    // 테스트를 위해 protected로 변경
    public Optional<File> convert(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Optional.empty();
        }

        // 1. 원본 파일명 가져오기
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unnamed-file";
        }

        // 2. 안전한 임시 파일 생성 (UUID 사용으로 중복 방지)
        String fileExtension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        File tempFile = File.createTempFile(
                UUID.randomUUID().toString(),
                fileExtension,
                new File(System.getProperty("java.io.tmpdir"))
        );

        // 3. 파일 내용 쓰기
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            // 오류 발생 시 파일 삭제 시도
            if (!tempFile.delete()) {
                tempFile.deleteOnExit();
            }
            throw new IOException("파일 변환 중 오류 발생: " + e.getMessage(), e);
        }

        return Optional.of(tempFile);
    }
}
