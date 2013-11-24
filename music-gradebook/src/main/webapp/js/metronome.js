var go = false;
var tempoBpm = 60;
var metroSoundNum = 1;

function metro() {
    var metronome = $('#metronome');
    if (go) {
        metronome.removeClass('active');
        go = false;
    } else {
        metronome.addClass('active');
        go = true;
        beep();
    }
}
function beep() {
    if (go) {
        var startTime = new Date().getTime();
        document.getElementById('audioControl' + metroSoundNum).play();
        var elapsed = new Date().getTime() - startTime;
        var bps = tempoBpm == 0 ? 1 : tempoBpm / 60.0;
        var msDelay = 1000 / bps;
        setTimeout("beep()", msDelay - elapsed);
    }
}
