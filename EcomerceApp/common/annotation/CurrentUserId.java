package com.tmt.ecommerce.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @Target báo cho Java biết Annotation này chỉ được dùng ở các tham số (Parameter) của hàm
@Target(ElementType.PARAMETER)
// @Retention báo cho Java biết giữ Annotation này lại lúc chương trình đang chạy (RUNTIME)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUserId {
}