package testers.util;

import static testers.util.SdkConverters.SYS_CLOCK;
import static org.junit.Assert.assertEquals;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import org.junit.Test;

public class SdkConvertersSpec {
    @Test
    public void instantToTimestamp_usec() {
        Instant usec = Instant.parse("2007-12-03T10:15:30.123456Z");
        Timestamp ts = SdkConverters.instantToTimestamp(usec);

        String result = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toString();
        assertEquals(result, "2007-12-03T10:15:30.123Z");
    }

    @Test
    public void instantToTimestamp_msec() {
        Instant now = SYS_CLOCK.instant();
        Timestamp ts = SdkConverters.instantToTimestamp(now);

        String result = Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos()).toString();
        assertEquals(result, now.toString());
    }
}
