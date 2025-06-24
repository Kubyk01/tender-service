package com.tender_service.utils;

import com.tender_service.core.api.database.entity.UserCwk;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;

public class UserCwkSpecification {
    public static Specification<UserCwk> fieldStartsWith(String fieldName, String value) {
        return (root, query, cb) -> {
            try {
                Field field = UserCwk.class.getDeclaredField(fieldName);
                Class<?> type = field.getType();

                if (type == String.class) {
                    return cb.like(root.get(fieldName), value + "%");
                } else if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
                    return cb.equal(root.get(fieldName), Long.parseLong(value));
                } else if (type == Boolean.class || type == boolean.class) {
                    return cb.equal(root.get(fieldName), Boolean.parseBoolean(value));
                } else {
                    return null;
                }
            } catch (NoSuchFieldException e) {
                return null;
            }
        };
    }
}
