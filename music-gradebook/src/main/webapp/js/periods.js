var Periods = {

    startTime: undefined,
    endTime: undefined,
    warnBellMins: undefined,

    update: function () {
        function fmtTime(secs) {
            var s = Math.floor(secs);
            var m = Math.floor(s / 60);
            s -= m * 60;
            var wb = "";
            var wbm = Periods.warnBellMins;
            if (wbm && wbm > 0 && m >= wbm) {
                m -= wbm;
                wb = wbm + " + ";
            }
            if (m < 10) m = "0" + m;
            if (s < 10) s = "0" + s;
            return wb + m + ":" + s
        }

        var nowMs = new Date().getTime();
        if (Periods.startTime && Periods.endTime && nowMs >= Periods.startTime && nowMs <= Periods.endTime) {
            var secs = Math.floor((Periods.endTime - nowMs) / 1000);
            $("#progressMins").html(fmtTime(secs));
            $("#timeRemaining").val(secs);
        }
        setTimeout(Periods.update, 1000 - nowMs % 1000);
    },

    start: function() {
        Periods.update();
    }
};
