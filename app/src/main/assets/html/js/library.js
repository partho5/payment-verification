function milisToDateTime(milis){
    var d = new Date(milis);
    var date = d.toJSON().slice(0,10);
    var time = (d+"").slice(16,25);
    return date+" "+time;
}

function milisToDate(milis){
    var d = new Date(milis);
    var date = d.toJSON().slice(0,10);
    return date;
}

function milisToTime(milis){
    var d = new Date(milis);
    var time = (d+"").slice(16,25);
    return time;
}

function todayDate() {
	//return "2021-02-15";

	var d = new Date();
	var date = d.toJSON().slice(0,10);
	return date;
}


function showSecondsAsClock(totalSeconds){
    var h=0, m=0, s=0;
    h = Math.floor(totalSeconds / 3600);
    totalSeconds = totalSeconds % 3600;
    m = Math.floor(totalSeconds / 60);
    s = totalSeconds % 60;
    h = h > 0 ? h+" hour " : "";
    m = m > 0 ? m+" min " : "";
    s = s > 0 ? s+" sec " : "";
    //s="";//don't show sec
    return h+m+s;
}//timeDuration()