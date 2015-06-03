var TestingPage = {

    desktopNotify: false,
    studentCalls: [],
    notifications: {},

    activateTips: function () {
        $(".queueRow img").tooltip({'placement': 'right', 'html': true});
        $(".sessionRow img").tooltip({'placement': 'right', 'html': true});
    },

    update: function () {
        $('tr.queueRow').removeClass('selected');
        $('.qrtime progress').addClass('hidden');
        var now = new Date().getTime();

        for (var n = 0; n < TestingPage.studentCalls.length; ++n) {
            var sc = TestingPage.studentCalls[n];
            if (sc.time > now) {
                var progSel = '#' + sc.rowId + ' .qrtime progress';
                $(progSel).removeClass('hidden');
                $(progSel).val((sc.time - now) / 1000);
            }
            if (now >= sc.time) {
                if (TestingPage.desktopNotify && ! TestingPage.notifications.hasOwnProperty(sc.name)) {
                    var ntf = sendNotification(sc.name + ", it’s time to test");
                    TestingPage.notifications[sc.name] = now;
                }
                $('#' + sc.rowId).addClass('selected');
            }
        }
        setTimeout(TestingPage.update, 1000);
    },

    start: function() {
        TestingPage.update();
        $(document).ready(TestingPage.activateTips);
    }
};
