import com.frozenironsoftware.avocado.data.SortOrder;
import com.frozenironsoftware.avocado.data.model.bytes.EpisodesRequest;
import com.frozenironsoftware.avocado.data.model.bytes.LimitedOffsetRequest;
import com.frozenironsoftware.avocado.data.model.bytes.StringArrayRequest;
import com.frozenironsoftware.avocado.data.model.bytes.UserIdLimitedOffsetRequest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ByteSerialiableTests {
    private static final int LIMIT = 24;
    private static final long OFFSET = 20;
    private static final long USER_ID = 18;
    private static final List<String> STRING_LIST =
            Arrays.asList("ONE", "TWO", "", "THREE", ".", "FOUR", "\uD83D\uDE42");
    private static final long ID = 2;
    private static final SortOrder SORT_ORDER = SortOrder.DESC;

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

    @Test
    public void testEpisodesRequest() {
        EpisodesRequest episodesRequest = new EpisodesRequest(USER_ID, LIMIT, OFFSET, ID, SORT_ORDER, ID);
        byte[] bytes = episodesRequest.toBytes();
        EpisodesRequest reconstructed = new EpisodesRequest(bytes);
        assertEquals(reconstructed.getUserId(), USER_ID);
        assertEquals(reconstructed.getLimit(), LIMIT);
        assertEquals(reconstructed.getOffset(), OFFSET);
        assertEquals(reconstructed.getPodcastId(), ID);
        assertEquals(reconstructed.getSortOrder(), SORT_ORDER);
    }
}
