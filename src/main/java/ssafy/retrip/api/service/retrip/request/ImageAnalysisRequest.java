package ssafy.retrip.api.service.retrip.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisRequest {
    private String memberId;
    private Long retripId;
}
