package tools.jdk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Making sure that java toolchain resolves to a proper version of JRE.
 *
 * <p>See details <a href="https://fivetran.height.app/T-373420">here</a>
 */
public class VerifyJRE17Spec {

    @Test
    public void verifyJavaVersion() {
        final Integer[] expectedJavaVersion = {17, 0, 7};

        assertThat(Runtime.version().version()).containsExactly(expectedJavaVersion);
    }
}
