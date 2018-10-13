import com.frozenironsoftware.avocado.data.model.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.UserIdLimitedOffsetRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteSerialiableTests {
    private static int LIMIT = 24;
    private static long OFFSET = 20;
    private static long USER_ID = 18;

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
}
