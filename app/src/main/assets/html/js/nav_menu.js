

var windowWidth = $(window).width();
var leftMenuSlidingTime = 500;
$("#left-menu-wrapper").css('left', '-'+windowWidth+'px');


$("#menu-icon").click(function () {
    showLeftMenu();
});

function showLeftMenu() {
    $("#left-menu-wrapper").animate({
        left : 0,
        opacity : 1
    }, leftMenuSlidingTime);

    window.activeScreen = "leftMenu";
}

$("#left-menu-wrapper").click(function (e) {
    if( $(e.target).hasClass('nav-menu') ){
        hideLeftMenu();
    }
});


$("#menu-close-icon").click(function (e) {
    hideLeftMenu();
});



function hideLeftMenu() {
    $("#left-menu-wrapper").animate({
        left : '-'+windowWidth+'px',
        opacity : 0
    }, leftMenuSlidingTime/3);

    window.activeScreen = "main";
}


/* each menu item-group may contain a dot, which are inline-block. so the cann't display:none by default. Hence using jquery they have to be made hide */
$('.item-group .dot').hide();