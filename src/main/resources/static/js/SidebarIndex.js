$(function () {
    $(".uname").html(getGlobalVar("YUserName"));
    var funcid = getUrlParameter("funcid");
    var matid = getUrlParameter("matid");

    createMenu();

    function createMenu() {
        // 查找打开的页面
        $.ax("/ps/searchPage", {"funcid": funcid}, false, "POST", "json",'',function(res){
            if (res.length == 0) {
                RunPop('提示', '', "加载页面失败，请稍后再试！", '失败', 0.4, 0.2,'','error');
            }
            else {
                var sIcon = ["glyphicon glyphicon-edit","glyphicon glyphicon-pencil","glyphicon glyphicon-random","glyphicon glyphicon-bullhorn","glyphicon glyphicon-cloud-download",
                    "glyphicon glyphicon-cloud-upload", "glyphicon glyphicon-calendar","glyphicon glyphicon-lock","glyphicon glyphicon-check","glyphicon glyphicon-sort"];
                var sHtml =""
                $("#moduleName").text(res[0].functitle);
                $(document).attr("title",res[0].functitle);
                var sHtml = "<ul>";
                $.each(res,function (index,item) {
                    var i = index;
                    sHtml += "<div class='menuA' role='tab' id='heading" + i + "'>" ;
                    sHtml += "<li>";
                    sHtml += "<a href='#' id = 'menu" + i + "' class='mBtn1' onclick=\"fillpage(event," + i + ",'"+$.trim(item["functitle"])+"','"+$.trim(item["funcsubtitle"])+"','" + $.trim(item["funcpage"]) + "?matid="+matid + "','"+$.trim(item["funcid"])+"')\">";
                    sHtml += "<span class='"+sIcon[i % 10]+" icon'></span>";
                    sHtml += "<span>" + $.trim(item["funcsubtitle"]) + "</span></a></li>";
                    sHtml += "</div>";
                });
                sHtml += "</ul>";

                $('#menu').html(sHtml);
                $(".mBtn1").first().addClass("on");
                $(".mBtn1").first().click();
            }
        },function(){
            RunPop('提示', '', "加载页面失败，请稍后再试！", '失败', 0.4, 0.2,'','error');
        },'');

    }



    window.onload = function() {
        //定时获取和设置高度，是为了高度发生变化及时更新，0是毫秒的单位，可以根据需要调整
        // var intervalChangeHeight = setInterval("changeHeight()",0);
    };


    function GetPageScroll()
    {
        var x, y;
        if(window.pageYOffset)
        {    // all except IE
            y =
                window.pageYOffset;
            x = window.pageXOffset;
        } else
        if(document.documentElement && document.documentElement.scrollTop)

        {    // IE 6 Strict
            y = document.documentElement.scrollTop;
            x
                = document.documentElement.scrollLeft;
        } else if(document.body) {    // all
            // other IE
            y = document.body.scrollTop;
            x =
                document.body.scrollLeft;
        }
        return {X:x,
            Y:y};

    }


    function indexcallback(fn) {
        if($.isFunction(fn)){
            fn();
        }
    }
    
});


//添加页面
function fillpage(event, a,title,subtitle, url,funcid) {
    event = event || window.event;
    if ((navigator.userAgent.indexOf('MSIE') >= 0) && (navigator.userAgent.indexOf('Opera') < 0)) {
        event.cancelBubble = true;//停止冒泡
    } else {
        event.stopPropagation(); // 阻止事件的传播
    }
    change(a);
    produce(url);
}

//tab和list出现
//tab和list出现
function change(c){
    var $menuA = $(".mBtn1");
    $menuA.removeClass('on');
    $("#menu"+c).addClass('on');
    $(".menuA").removeClass("panelCur");
    $("#menu" + c).parents(".titleName").prev().addClass("panelCur");
}

//添加div.frame和#ulcontrol.li
function produce(url) {
    $("#indexIframe").attr("src",url);
}

/*iframe高度随子页面内容改变*/
function changeHeight() {
    //获取子页面内容的高度
    var iframeHeight = $("#indexIframe").contents().find("body").height();
    //设置iframe的高度为子页面内容的高度
    $("#indexIframe").height(iframeHeight);
}