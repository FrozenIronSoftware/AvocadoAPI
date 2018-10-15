import com.frozenironsoftware.avocado.data.model.bytes.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.bytes.StringArrayRequest;
import com.frozenironsoftware.avocado.data.model.bytes.UserIdLimitedOffsetRequest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ByteSerialiableTests {
    private static int LIMIT = 24;
    private static long OFFSET = 20;
    private static long USER_ID = 18;
    private static List<String> STRING_LIST;

    public ByteSerialiableTests() {
        STRING_LIST = Arrays.asList("ONE", "TWO", "", "THREE", ".", "FOUR", "\uD83D\uDE42");
    }

    @Test
    public void testLimitedOffsetRequest() {
        LimitedOffsetRequest limitedOffsetRequest = new LimitedOffsetRequest(LIMIT, OFFSET);
        byte[] bytes = limitedOffsetRequest.toBytes();
        LimitedOffsetRequest reconstructed = new LimitedOffsetRequest(bytes);
        assertEquals(reconstructed.getLimit(), LIMIT);
        assertEquals(reconstructed.getOffset(), OFFSET);
    }

    @Test
    public void testUserIdLimitedOffsetRequest() {
        UserIdLimitedOffsetRequest userIdLimitedOffsetRequest = new UserIdLimitedOffsetRequest(USER_ID, LIMIT, OFFSET);
        byte[] bytes = userIdLimitedOffsetRequest.toBytes();
        UserIdLimitedOffsetRequest reconstructed = new UserIdLimitedOffsetRequest(bytes);
        assertEquals(reconstructed.getUserId(), USER_ID);
        assertEquals(reconstructed.getLimit(), LIMIT);
        assertEquals(reconstructed.getOffset(), OFFSET);
    }

    @Test
    public void testStringArrayRequest() {
        StringArrayRequest stringArrayRequest = new StringArrayRequest(STRING_LIST);
        byte[] bytes = stringArrayRequest.toBytes();
        StringArrayRequest reconstructed = new StringArrayRequest(bytes);
        assertEquals(reconstructed.getStrings().size(), STRING_LIST.size());
        assertEquals(stringArrayRequest.getStrings(), reconstructed.getStrings());
    }
}
