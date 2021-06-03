




window.baseDomain = "http://wordmas.com";
window.version = baseDomain+"/version";



/************** simple popup start **********/



function showSimplePopup(title, body) {
    $(".darken-screen").show();
    $("#simple-popup .popup-title-bar .title").text(title);
    $("#simple-popup .popup-body").html(body);
    $("#simple-popup").show();
    var popupHeight = $("#simple-popup").height();
    $("#simple-popup").css('width', '10%').css('height', '10%');
    $(".popup-title-bar, .popup-body").hide();
    $("#simple-popup").animate({
        width : '90%',
        height : popupHeight,
        opacity : 1
    }, 300);

    setTimeout(function () {
        $(".popup-title-bar, .popup-body").fadeIn();
    }, 200);
}


function hideSimplePopup() {
    var popupHeight = $("#simple-popup").height();
    $("#simple-popup").animate({
        width : '10%',
        height : '10%',
        opacity : 0
    }, 100, function () {
        $("#simple-popup").css('width', '90%').css('height', popupHeight).hide();
        $(".darken-screen").hide();
    });
}


$("#simple-popup .cross-btn").click(function () {
    hideSimplePopup();
});

/************** simple popup end **********/



/************** custom popup start **********/

$('#custom-popup-holder').html(
    '<div class="darken-screen"></div>\n' +
    '    <div id="custom-popup" class="col-xs-12 no-padding">\n' +
    '        <div class="popup-title-bar">\n' +
    '            <span class="title"></span>\n' +
    '            <span class="cross-btn"><img src="images/icon/cross_icon.png" alt="x"></span>\n' +
    '        </div>\n' +
    '        <div class="popup-body"></div>\n' +
    '        <div class="bottom-line"></div>\n' +
    '        <div class="col-xs-12 no-padding action-btn-wrapper text-center">\n' +
    '            <div class="col-xs-6 no-padding positive-btn">Yes</div>\n' +
    '            <div class="col-xs-6 no-padding negative-btn">No</div>\n' +
    '        </div>\n' +
    '    </div>'
);




function showCustomPopup(params) {
    //p('entered in showCustomPopup()');
    var title = params['title'];
    var body = params['body'];
    var bottomLine = params['bottomLine'];
    var positiveBtn = params['positiveBtn'];
    var negativeBtn = params['negativeBtn'];
    if(bottomLine && positiveBtn && negativeBtn){
        //these properties are mandatory

        $(".darken-screen").show();
        if(! title){
            $("#custom-popup .popup-title-bar, .cross-btn").hide();
            $("#custom-popup .popup-title-bar").css('padding', '0');
        }else{
            $("#custom-popup .popup-title-bar .title").text(title);
        }
        $("#custom-popup .popup-body").html(body);
        $("#custom-popup .bottom-line").html(bottomLine);
        $('.positive-btn').html(positiveBtn);
        $('.negative-btn').html(negativeBtn);
        $("#custom-popup").show();
        var popupHeight = $("#custom-popup").height();
        $("#custom-popup").css('width', '10%').css('height', '10%');
        $("#custom-popup .popup-title-bar, #custom-popup .popup-body, #custom-popup .bottom-line").hide();

        $("#custom-popup").animate({
            width : '90%',
            height : popupHeight,
            opacity : 1
        }, 300);

        setTimeout(function () {
            $("#custom-popup .popup-title-bar, #custom-popup .popup-body, #custom-popup .bottom-line, .positive-btn, .negative-btn").fadeIn();
        }, 200);
    }
}


function hideCustomPopup() {
    var popupHeight = $("#custom-popup").height();
    $("#custom-popup").animate({
        width : '10%',
        height : '10%',
        opacity : 0
    }, 100, function () {
        $("#custom-popup").css('width', '90%').css('height', popupHeight).hide();
        $(".darken-screen").hide();
    });
}


$("#custom-popup .cross-btn").click(function () {
    hideCustomPopup();
});

/*************** custom popup end ***************/



function p(data){
    console.log(data);
}