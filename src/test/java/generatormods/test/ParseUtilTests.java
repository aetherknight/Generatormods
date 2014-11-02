package generatormods.test;

import generatormods.caruins.config.CARule;
// import generatormods.common.BlockAndMeta;
// import generatormods.common.TemplateRule;
import generatormods.common.config.ParseError;
import generatormods.util.ParseUtil;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ParseUtilTests {
    Logger logger;

    @Before
    public void beforeEach() {
        logger = mock(Logger.class);
    }

    private List<CARule> readAutomataRule(String read) throws Exception {
        return ParseUtil.readAutomataList(logger, "=", "some_rule = " + read);
    }

    @Test
    public void testReadAutomataList() throws Exception {
        List<CARule> rulelist;

        // Simple case
        rulelist = readAutomataRule("B3/S23");
        assertThat(rulelist.size(), equalTo(1));
        assertThat(rulelist.get(0).toString(), equalTo("B3/S23"));

        // List case, with interesting whitespace
        rulelist = readAutomataRule("B3/S23, B34/S2356,B36/S125");
        assertThat(rulelist.size(), equalTo(3));
        assertThat(rulelist.get(0).toString(), equalTo("B3/S23"));
        assertThat(rulelist.get(1).toString(), equalTo("B34/S2356"));
        assertThat(rulelist.get(2).toString(), equalTo("B36/S125"));

        // Invalid rule string behavior
        rulelist = readAutomataRule("");
        assertThat(rulelist, equalTo(null));

        // TODO: consider letting the ParseError bubble up instead of
        // suppressing and logging it
        rulelist = readAutomataRule("B3/S23, asdfB34/S2356,B36/S125");
        assertThat(rulelist.get(0).toString(), equalTo("B3/S23"));
        assertThat(rulelist.get(1).toString(), equalTo("B36/S125"));

        verify(logger, times(2)).error(anyString(), any(ParseError.class));

    }

    // @Test(expected=ParseError.class)
    // public void testInvalidReadAutomataList() throws Exception {
    // readAutomataRule("B3/S23, asdfB34/S2356,B36/S125");
    // }

    @Test
    public void testReadBooleanParam() {
        boolean res;

        res = ParseUtil.readBooleanParam(logger, true, "=", "some_rule = true");
        assertThat(res, equalTo(true));

        res = ParseUtil.readBooleanParam(logger, true, "=", "some_rule = false");
        assertThat(res, equalTo(false));

        // Non-true:
        res = ParseUtil.readBooleanParam(logger, true, "=", "some_rule = wtf");
        assertThat(res, equalTo(false));

        // TODO: How we get a null pointer exception? Boolean.parseBoolean
        // returns false for any non-null string that is not case-insensitive
        // matching true. to get a null pointer, we would have to somehow pass
        // in null for the string.
        // res = ParseUtil.readBooleanParam(logger, true, "=", "some_rule = wtfFalsey");
        // assertThat(res, equalTo(true));
        // verify(logger).error(anyString(), any(ParseError.class));
    }

    @Test
    public void testReadFloatParam() {
        float res;

        res = ParseUtil.readFloatParam(logger, 0.1F, "=", "some_rule = 0.20");
        assertThat(res, equalTo(0.20F));

        res = ParseUtil.readFloatParam(logger, 0.1F, "=", "some_rule = 1.0");
        assertThat(res, equalTo(1.0F));

        res = ParseUtil.readFloatParam(logger, 0.1F, "=", "some_rule = 1.blimey");
        assertThat(res, equalTo(0.1F));
        verify(logger).error(anyString(), any(ParseError.class));
    }

    @Test
    public void testReadIntList() {
        Integer[] res;

        res = ParseUtil.readIntList(logger, new Integer[] {1}, "=", "some_rule = 5");
        assertThat(res, equalTo(new Integer[] {5}));

        res = ParseUtil.readIntList(logger, new Integer[] {1}, "=", "some_rule = 1,2,3,4");
        assertThat(res, equalTo(new Integer[] {1, 2, 3, 4}));

        res = ParseUtil.readIntList(logger, new Integer[] {1}, "=", "some_rule = 1,2,3,blue");
        assertThat(res, equalTo(new Integer[] {1}));
        verify(logger).error(anyString(), any(Exception.class));
    }

    @Test
    public void testReadIntParam() {
        int res;

        res = ParseUtil.readIntParam(logger, -10, "=", "some_rule = 5");
        assertThat(res, equalTo(5));

        res = ParseUtil.readIntParam(logger, -10, "=", "some_rule = 3aww");
        assertThat(res, equalTo(-10));
        verify(logger).error(anyString(), any(Exception.class));
    }

    private static String[] choices = new String[] {"onefish", "two fish", "redfish", "bluefish"};

    @Test
    public void testReadNamedCheckList() {
        int[] res;

        // standard case
        res =
                ParseUtil.readNamedCheckList(logger, new int[] {0, 0, 0, 1}, "=",
                        "some_rule = blue fish, one fish, twofish", choices, "all fish");
        assertThat(res, equalTo(new int[] {1, 1, 0, 1}));

        // multiple entries
        res =
                ParseUtil.readNamedCheckList(logger, new int[] {0, 0, 0, 1}, "=",
                        "some_rule = blue fish, bluefish, BlueFish", choices, "all fish");
        assertThat(res, equalTo(new int[] {0, 0, 0, 3}));

        // all case
        res =
                ParseUtil.readNamedCheckList(logger, new int[] {0, 0, 0, 1}, "=",
                        "some_rule = all fish", choices, "all fish");
        assertThat(res, equalTo(new int[] {1, 1, 1, 1}));

        // 1 bad entry
        res =
                ParseUtil.readNamedCheckList(logger, new int[] {0, 0, 0, 1}, "=",
                        "some_rule = nofish, redfish", choices, "all fish");
        assertThat(res, equalTo(new int[] {0, 0, 1, 0}));
        verify(logger).warn(anyString());

        // TODO: exception-causing error
    }

    // private static TemplateRule[] rules = new TemplateRule[]{TemplateRule.AIR_RULE,
    // TemplateRule.STONE_RULE};

    /**
     * TODO: we need to bootstrap Minecraft's blocks in order to test block rules.
     */
    @Test
    public void testReadRuleIdOrRule() throws Exception {
        // TemplateRule res;

        // condition, chance, block, [block...]
        // block is either id[-metadata] or name[-metadata]

        // TODO: just a ruleID
        // TODO: rule list with a special block
        // res = ParseUtil.readRuleIdOrRule("=", "some_rule = 0,100,0,PRESERVE", rules);
        // assertThat(res.condition, equalTo(0));
        // assertThat(res.chance, equalTo(100));
        // assertThat(res.primaryBlock, equalTo(BlockAndMeta.PRESERVE_BLOCK));

        // TODO: rule list with multiple block IDs
        // TODO: rule list with multiple block names
        // TODO: rule list mixing block IDs and name
    }
}
