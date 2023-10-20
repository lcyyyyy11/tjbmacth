$(function () {
    $(".uname").html(getGlobalVar("YUserName"));
    var funcid = getUrlParameter("funcid");
    var matid = getUrlParameter("matid");

    var height = Math.floor($(window).height()) - $(".top-info").height() - 30;
    $("#iframeP").css("height", height + "px");
    $("#indexIframe").css("height", height + "px");

    // 查找打开的页面
    $.ax("/ps/searchPage", {"funcid": funcid}, false, "POST", "json",'',function(res){
        if (res.length == 0) {
            RunPop('提示', '', "加载页面失败，请稍后再试！", '失败', 0.4, 0.2,'','error');
        }
        else {
            var data = res[0];
            $("#moduleName").text(data.functitle);
            $("#indexIframe").attr("src",data.funcpage+"?matid="+matid);
            $(document).attr("title",data.functitle);
        }
    },function(){
        RunPop('提示', '', "加载页面失败，请稍后再试！", '失败', 0.4, 0.2,'','error');
    },'');


    //自适应
    $(window).resize(function () {
        height = Math.floor($(window).height()) - $(".top-info").height() - 30;
        $("#iframeP").css("height", height + "px");
        $("#indexIframe").css("height", height + "px");
    });

})