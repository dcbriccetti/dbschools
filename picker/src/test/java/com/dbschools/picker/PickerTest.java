package com.dbschools.picker;

import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;
import org.junit.Test;

public class PickerTest {
    private static Logger log = Logger.getLogger(PickerTest.class.getName());
    
    @Test
    public void testPick() {
        log.info("pick");
        final List<String> names = Arrays.asList("Dave", "Mary", "Sue");
        Picker p = new Picker();
        p.setNames(names);
        p.setFanfare(true);
        p.addListener(new Listener() {
            public void itemSelected(int itemIndex, boolean isFinal) {
                log.info(names.get(itemIndex) + (isFinal ? " selected" : " passed"));
            }
        });
        for (String n : names) {
            p.pick();
        }
    }

}