package com.tender_service.utils;

import com.tender_service.core.api.database.entity.TenderCwk;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Path;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TenderCwkSpecification {
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;


    public static Specification<TenderCwk> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<TenderCwk> hasTendererId(Long tendererId) {
        return (root, query, cb) -> cb.equal(root.get("tenderer").get("id"), tendererId);
    }

    public static Specification<TenderCwk> hasSupplierId(Long supplierId) {
        return (root, query, cb) -> cb.equal(root.get("supplier").get("id"), supplierId);
    }

    public static Specification<TenderCwk> fieldStartsWith(String fieldName, String value) {
        return (root, query, cb) -> {
            try {
                Path<?> path;

                switch (fieldName) {
                    case "userId":
                        path = root.get("user").get("id");
                        return cb.equal(path, Long.parseLong(value));
                    case "tendererId":
                        path = root.get("tenderer").get("id");
                        return cb.equal(path, Long.parseLong(value));
                    case "supplierId":
                        path = root.get("supplier").get("id");
                        return cb.equal(path, Long.parseLong(value));
                    default:
                        Field field = TenderCwk.class.getDeclaredField(fieldName);
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
                }
            } catch (NoSuchFieldException | NumberFormatException e) {
                return null;
            }
        };
    }

    public static Specification<TenderCwk> fieldInRange(String fieldName, String start, String stop) {
        return (root, query, cb) -> {
            try {
                Field field = TenderCwk.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Class<?> type = field.getType();
                Path<?> path = root.get(fieldName);

                if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
                    if (type == Double.class || type == double.class) {
                        Double startVal = start != null ? Double.parseDouble(start) : null;
                        Double stopVal = stop != null ? Double.parseDouble(stop) : null;

                        if (startVal != null && stopVal != null)
                            return cb.between(path.as(Double.class), startVal, stopVal);
                        else if (startVal != null)
                            return cb.greaterThanOrEqualTo(path.as(Double.class), startVal);
                        else if (stopVal != null)
                            return cb.lessThanOrEqualTo(path.as(Double.class), stopVal);
                    } else {
                        Long startVal = start != null ? Long.parseLong(start) : null;
                        Long stopVal = stop != null ? Long.parseLong(stop) : null;

                        if (startVal != null && stopVal != null)
                            return cb.between(path.as(Long.class), startVal, stopVal);
                        else if (startVal != null)
                            return cb.greaterThanOrEqualTo(path.as(Long.class), startVal);
                        else if (stopVal != null)
                            return cb.lessThanOrEqualTo(path.as(Long.class), stopVal);
                    }
                }

                if (type == LocalDateTime.class) {
                    LocalDateTime startVal = start != null
                            ? LocalDateTime.parse(ensureDateTimeFormat(start), ISO_DATE_TIME)
                            : null;
                    LocalDateTime stopVal = stop != null
                            ? LocalDateTime.parse(ensureDateTimeFormat(stop), ISO_DATE_TIME)
                            : null;

                    if (startVal != null && stopVal != null)
                        return cb.between(path.as(LocalDateTime.class), startVal, stopVal);
                    else if (startVal != null)
                        return cb.greaterThanOrEqualTo(path.as(LocalDateTime.class), startVal);
                    else if (stopVal != null)
                        return cb.lessThanOrEqualTo(path.as(LocalDateTime.class), stopVal);
                }

                if (type == LocalDate.class) {
                    LocalDate startVal = start != null ? LocalDate.parse(start, ISO_DATE) : null;
                    LocalDate stopVal = stop != null ? LocalDate.parse(stop, ISO_DATE) : null;

                    if (startVal != null && stopVal != null)
                        return cb.between(path.as(LocalDate.class), startVal, stopVal);
                    else if (startVal != null)
                        return cb.greaterThanOrEqualTo(path.as(LocalDate.class), startVal);
                    else if (stopVal != null)
                        return cb.lessThanOrEqualTo(path.as(LocalDate.class), stopVal);
                }

                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    private static String ensureDateTimeFormat(String input) {
        return input.length() == 10 ? input + "T00:00:00" : input;
    }
}
