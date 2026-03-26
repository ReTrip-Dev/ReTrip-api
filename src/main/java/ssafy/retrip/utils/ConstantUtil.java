package ssafy.retrip.utils;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ConstantUtil {

  public static final String BUCKET_PREFIX = "uploads/";
  public static final String BUCKET_SUFFIX = "/_complete";
  public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

}
