package net.alhazmy13.hijridatepicker.date.hijri;

import com.github.msarhan.ummalqura.calendar.DateTimeException;
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;
import org.junit.Test;

import static org.junit.Assert.*;

public class HijriDatePickerFixTest {

    private static final int[][] THIRTIETH_DAYS = new int[][]{
            // year, month (0-based), day
            {1448, 3, 30}, // 11 Oct 2026 (30 Rabi al-Thani 1448)
            {1448, 4, 30}, // 10 Nov 2026 (30 Jumada al-Ula 1448)
            {1448, 6, 30}, // 8 Jan 2027
            {1448, 7, 30}, // 7 Feb 2027
            {1448, 9, 30}, // 7 Apr 2027
            {1448, 11, 30}, // 5 Jun 2027
            {1449, 2, 30}, // 1 Sep 2027
    };

    @Test
    public void testDateTransitionsStartingFromThirtiethDay() {
        for (int[] thirtiethDay : THIRTIETH_DAYS) {
            int currentYear = thirtiethDay[0];
            int currentMonth = thirtiethDay[1];
            int currentDay = thirtiethDay[2];

            // Verify that this starting day actually has 30 days
            int startingMonthLength = UmmalquraCalendar.lengthOfMonth(currentYear, currentMonth);
            assertEquals("Month should have 30 days for this test case", 30, startingMonthLength);

            // Construct a calendar representing this 30th day
            UmmalquraCalendar cal = new UmmalquraCalendar();
            cal.set(UmmalquraCalendar.DAY_OF_MONTH, 1);
            cal.set(UmmalquraCalendar.YEAR, currentYear);
            cal.set(UmmalquraCalendar.MONTH, currentMonth);
            cal.set(UmmalquraCalendar.DAY_OF_MONTH, currentDay);

            assertEquals(currentYear, cal.get(UmmalquraCalendar.YEAR));
            assertEquals(currentMonth, cal.get(UmmalquraCalendar.MONTH));
            assertEquals(30, cal.get(UmmalquraCalendar.DAY_OF_MONTH));

            // Verify the bug reproduces without the fix, and succeeds with the fix
            for (int targetYear : new int[]{1408, 1428}) {
                for (int targetMonth = 0; targetMonth < 12; targetMonth++) {
                    int targetLength = UmmalquraCalendar.lengthOfMonth(targetYear, targetMonth);

                    // Reset cal to 30th
                    cal.set(UmmalquraCalendar.DAY_OF_MONTH, 1);
                    cal.set(UmmalquraCalendar.YEAR, currentYear);
                    cal.set(UmmalquraCalendar.MONTH, currentMonth);
                    cal.set(UmmalquraCalendar.DAY_OF_MONTH, currentDay);

                    // If target year/month only has 29 days, directly changing year while day=30 throws DateTimeException
                    int monthInTargetYear = UmmalquraCalendar.lengthOfMonth(targetYear, currentMonth);
                    if (monthInTargetYear == 29) {
                        try {
                            UmmalquraCalendar brokenCal = (UmmalquraCalendar) cal.clone();
                            brokenCal.set(UmmalquraCalendar.YEAR, targetYear);
                            fail("Directly setting year from 30th to 29-day month should throw DateTimeException");
                        } catch (DateTimeException expected) {
                            // Verifies that the flaw reproduces without day=1
                        }
                    }

                    // With the fix: set DAY=1 first, then YEAR, then MONTH, then capped DAY
                    cal.set(UmmalquraCalendar.DAY_OF_MONTH, 1);
                    cal.set(UmmalquraCalendar.YEAR, targetYear);
                    cal.set(UmmalquraCalendar.MONTH, targetMonth);
                    cal.set(UmmalquraCalendar.DAY_OF_MONTH, Math.min(30, targetLength));

                    assertEquals(targetYear, cal.get(UmmalquraCalendar.YEAR));
                    assertEquals(targetMonth, cal.get(UmmalquraCalendar.MONTH));
                    assertEquals(Math.min(30, targetLength), cal.get(UmmalquraCalendar.DAY_OF_MONTH));
                }
            }
        }
    }

    @Test
    public void testHijriDatePickerDialogMethodsDoNotThrowOn30th() {
        HijriDatePickerDialog dialog = new HijriDatePickerDialog();

        // Test isOutOfRange for various 29-day and 30-day combinations
        assertFalse(dialog.isOutOfRange(1408, 3, 29));
        assertFalse(dialog.isOutOfRange(1408, 3, 30)); // Capped at 29, should not throw
        assertFalse(dialog.isOutOfRange(1428, 4, 30));
        assertFalse(dialog.isOutOfRange(1448, 3, 30));

        // Test isHighlighted
        assertFalse(dialog.isHighlighted(1408, 3, 30));
        assertFalse(dialog.isHighlighted(1428, 4, 30));
        assertFalse(dialog.isHighlighted(1448, 3, 30));

        // Test getStartDate and getEndDate
        dialog.setMinYear(1300);
        dialog.setMaxYear(1595);
        UmmalquraCalendar startDate = dialog.getStartDate();
        assertNotNull(startDate);
        assertEquals(1300, startDate.get(UmmalquraCalendar.YEAR));
        assertEquals(UmmalquraCalendar.JANUARY, startDate.get(UmmalquraCalendar.MONTH));
        assertEquals(1, startDate.get(UmmalquraCalendar.DAY_OF_MONTH));

        UmmalquraCalendar endDate = dialog.getEndDate();
        assertNotNull(endDate);
        assertEquals(1595, endDate.get(UmmalquraCalendar.YEAR));
        assertEquals(UmmalquraCalendar.DECEMBER, endDate.get(UmmalquraCalendar.MONTH));
        int maxDaysInLastMonth = UmmalquraCalendar.lengthOfMonth(1595, UmmalquraCalendar.DECEMBER);
        assertEquals(maxDaysInLastMonth, endDate.get(UmmalquraCalendar.DAY_OF_MONTH));
    }
}
