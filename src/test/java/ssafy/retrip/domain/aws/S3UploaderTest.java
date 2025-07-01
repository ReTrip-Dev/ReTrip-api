//package ssafy.retrip.domain.aws;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.multipart.MultipartFile;
//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.S3Utilities;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.model.PutObjectResponse;
//import ssafy.retrip.aws.S3Uploader;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.Optional;
//import java.util.function.Consumer;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class S3UploaderTest {
//
//    @Mock
//    private S3Client s3Client;
//
//    @Mock
//    private S3Utilities s3Utilities;
//
//    private S3Uploader s3Uploader;
//    private S3Uploader spyUploader;
//
//    private final String BUCKET_NAME = "retrip-photos-ssafy04";
//    private final String DIR_NAME = "test-images";
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // S3Uploader 생성자 파라미터 설정
//        s3Uploader = new S3Uploader(
//                "test-access-key",
//                "test-secret-key",
//                "ap-northeast-2",
//                BUCKET_NAME,
//                DIR_NAME
//        );
//
//        // private 필드 s3Client를 mock 객체로 교체
//        ReflectionTestUtils.setField(s3Uploader, "s3Client", s3Client);
//
//        // mock S3Client가 S3Utilities를 반환하도록 설정 (lenient 모드)
//        lenient().when(s3Client.utilities()).thenReturn(s3Utilities);
//
//        // 테스트용 스파이 객체 생성 (부분 모킹)
//        spyUploader = spy(s3Uploader);
//    }
//
//    @Test
//    void uploadFileSuccess() throws IOException {
//        // Given
//        String fileName = "test-image.jpg";
//        String fileContent = "fake image content";
//
//        MultipartFile multipartFile = new MockMultipartFile(
//                fileName,
//                fileName,
//                "image/jpeg",
//                fileContent.getBytes(StandardCharsets.UTF_8)
//        );
//
//        // S3 업로드 성공 모킹
//        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
//                .thenReturn(PutObjectResponse.builder().build());
//
//        // S3 URL 모킹
//        URL mockUrl = new URL("https://retrip-photos-ssafy04.s3.amazonaws.com/test-images/test-image.jpg");
//        when(s3Utilities.getUrl(any(Consumer.class))).thenReturn(mockUrl);
//
//        // When
//        String result = s3Uploader.upload(multipartFile, DIR_NAME);
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.contains("https://"));
//        assertTrue(result.contains(BUCKET_NAME));
//        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
//        verify(s3Client, times(1)).utilities();
//        verify(s3Utilities, times(1)).getUrl(any(Consumer.class));
//    }
//
//    @Test
//    void uploadFileWithEmptyFile() throws IOException {
//        // Given
//        MultipartFile emptyFile = new MockMultipartFile(
//                "empty.jpg",
//                "empty.jpg",
//                "image/jpeg",
//                new byte[0]
//        );
//
//        // 파일 변환 단계에서 빈 Optional을 반환하도록 spy 설정
//        doReturn(Optional.empty()).when(spyUploader).convert(any(MultipartFile.class));
//
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            spyUploader.upload(emptyFile, DIR_NAME);
//        });
//
//        // 실제로 S3 관련 메서드가 호출되지 않음을 검증
//        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
//        verify(s3Client, never()).utilities();
//    }
//
//    @Test
//    void uploadFileWithNullFile() {
//        // When & Then
//        assertThrows(IllegalArgumentException.class, () -> {
//            s3Uploader.upload(null, DIR_NAME);
//        });
//
//        // 실제로 S3 관련 메서드가 호출되지 않음을 검증
//        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
//        verify(s3Client, never()).utilities();
//    }
//
//    @Test
//    void uploadLargeFile() throws IOException {
//        // Given
//        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
//        MultipartFile largeFile = new MockMultipartFile(
//                "large.jpg",
//                "large.jpg",
//                "image/jpeg",
//                largeContent
//        );
//
//        // S3 업로드 성공 모킹
//        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
//                .thenReturn(PutObjectResponse.builder().build());
//
//        // S3 URL 모킹
//        URL mockUrl = new URL("https://retrip-photos-ssafy04.s3.amazonaws.com/test-images/large.jpg");
//        when(s3Utilities.getUrl(any(Consumer.class))).thenReturn(mockUrl);
//
//        // When
//        String result = s3Uploader.upload(largeFile, DIR_NAME);
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.contains("https://"));
//        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
//        verify(s3Client, times(1)).utilities();
//        verify(s3Utilities, times(1)).getUrl(any(Consumer.class));
//    }
//
//}
