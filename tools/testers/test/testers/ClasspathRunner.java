package testers;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.TestClass;

public class ClasspathRunner extends Suite {

    /**
     * Called reflectively on classes annotated with <code>@RunWith(Suite.class)</code>
     *
     * @param klass the root class
     * @param builder builds runners for classes in the suite
     */
    public ClasspathRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(builder, klass, new DefaultTestClassLocator().findTestClasses(klass.getClassLoader()));
    }
}

class DefaultTestClassLocator {
    Class<?>[] findTestClasses(ClassLoader classLoader) {
        return findTestClassInfos(classLoader)
                .map(ClassPath.ClassInfo::load)
                .filter(this::include)
                .toArray(Class[]::new);
    }

    protected Stream<ClassPath.ClassInfo> findTestClassInfos(ClassLoader classLoader) {
        try {
            return ClassPath.from(classLoader).getAllClasses().stream().filter(info -> info.getName().endsWith("Spec"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean include(Class<?> testClass) {
        return !isSuite(testClass)
                && isTestClass(testClass)
                && Modifier.isPublic(testClass.getModifiers())
                && !Modifier.isAbstract(testClass.getModifiers());
    }

    private boolean isSuite(Class<?> testClass) {
        RunWith ann = testClass.getAnnotation(RunWith.class);

        return ann != null && Suite.class.isAssignableFrom(ann.value());
    }

    private boolean isTestClass(Class<?> testClass) {
        return !hasMultipleConstructors(testClass) && hasTestMethods(testClass);
    }

    private boolean hasTestMethods(Class<?> testClass) {
        boolean annotated = !new TestClass(testClass).getAnnotatedMethods(Test.class).isEmpty();
        boolean startsWithTest =
                Arrays.stream(testClass.getMethods()).anyMatch(method -> method.getName().startsWith("test"));

        return annotated || startsWithTest;
    }

    private boolean hasMultipleConstructors(Class<?> testClass) {
        return testClass.getConstructors().length > 1;
    }
}
