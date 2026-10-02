import java.lang.annotation.*;
import java.lang.reflect.Field;

/** DEMO 1: annotations and reflection. Run: java Demo1Annotations.java
 *  Expected output: select full_name, email_id from student */
public class Demo1Annotations {
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    @interface Table { String name(); }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface Column { String name(); }

    @Table(name = "student")
    static class Student {
        @Column(name = "full_name") String name;
        @Column(name = "email_id")  String email;
        String notMapped;           // no annotation, so it is ignored
    }

    public static void main(String[] args) {
        Class<?> c = Student.class;
        StringBuilder sql = new StringBuilder("select ");
        for (Field f : c.getDeclaredFields()) {
            Column col = f.getAnnotation(Column.class);
            if (col != null) sql.append(col.name()).append(", ");
        }
        sql.setLength(sql.length() - 2);
        sql.append(" from ").append(c.getAnnotation(Table.class).name());
        System.out.println(sql);
    }
}
