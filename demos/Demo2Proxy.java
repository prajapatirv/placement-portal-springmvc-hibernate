import java.lang.reflect.*;

/** DEMO 2: dynamic proxy. Run: java Demo2Proxy.java
 *  Expected output: BEGIN transaction / "  saving Asha" / COMMIT
 *  This is what Spring does around a @Transactional method, and what Hibernate does for a lazy proxy. */
public class Demo2Proxy {
    interface StudentService { void register(String name); }

    static class RealService implements StudentService {
        public void register(String name) { System.out.println("  saving " + name); }
    }

    public static void main(String[] args) {
        StudentService real = new RealService();
        StudentService proxy = (StudentService) Proxy.newProxyInstance(
            StudentService.class.getClassLoader(),
            new Class<?>[] { StudentService.class },
            (p, method, a) -> {
                System.out.println("BEGIN transaction");
                try {
                    Object result = method.invoke(real, a);
                    System.out.println("COMMIT");
                    return result;
                } catch (InvocationTargetException e) {
                    System.out.println("ROLLBACK");
                    throw e.getCause();
                }
            });
        proxy.register("Asha");
    }
}
