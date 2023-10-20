$(function () {
    $(".uname").html(getGlobalVar("YUserName"));
    var Address = "/ps/MainFrm";

    $(".stageBtn").removeClass("btn-white").addClass("notClickBtn");
    $(".stageBtn span:first-child").removeClass("btnlogo").addClass("notclickLogo");
    $("#MaterialScoreBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");
    $("#SceneScoreBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");
    $("#ReflectScoreBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");
    $("#MaterialJudgeBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");
    $("#SceneJudgeBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");

    var role = getGlobalVar("YRole");
    if (role <= 2) { // 主办方的角色使用
        // 所有赛事
        GetOptions("MatchInfo", "matid",function() {
            var matchName = $("#matid").find("option:selected").text();
            if (matchName.indexOf("rgb(242, 114, 94)") >= 0) {
                // clearStamp();
                RunPop('提示', '', "请点击右上角的退出系统，清除缓存后，再进入系统！", '提示', 0.4, 0.2);
                return;
            }
            if (isNullOrEmpty(matchName)) {
                $(".matchName").text("青年教师教学竞赛");
            }
            else {
                $(".matchName").text(matchName);
            }
            if (isNullOrEmpty($("#matid").val())) {
                $.ax("/ps/getUnitNumber", {}, "true", 'GET', "text", "", function (d,textStatus, resObj) {
                    if (parseInt(d) === 0) {
                        $("#MatchActivityBtn").removeClass("notClickBtn").addClass("btn-white");
                        $("#MatchActivityBtn").find("span").first().removeClass("notclickLogo").addClass("btnlogo");
                    }
                    else {
                        RunPop('提示', '', "获取赛事信息失败！", '提示', 0.4, 0.2);
                    }
                });
            } else {
                $("#selectedMatch").val($("#matid").val());
                getRights($("#matid").val(), 1);
            }
        });
    }
    else { // 除开主办方的角色使用
        // 所有赛事
        GetOptions("JoinMatch", "matid",function(){
            // 禁掉系统设置按钮
            $("#SystemManageBtn").hide();
            var matchName = $("#matid").find("option:selected").text();
            if (isNullOrEmpty(matchName)) {
                $(".matchName").text("青年教师教学竞赛");
            }
            else {
                $(".matchName").text(matchName);
            }
            if (isNullOrEmpty($("#matid").val())) {
                RunPop('提示', '', "获取赛事信息失败！", '提示', 0.4, 0.2);
                return;
            }
            $("#selectedMatch").val($("#matid").val());
            getRights($("#matid").val(), 1);
        });
    }

    function clearStamp() {
        $.ax("/ps/user/clearStamp", {}, "true", 'GET', "json", "", function (d,textStatus, resObj) {
            if (d.msg === "成功") {
                window.localStorage.clear();
                window.document.location = GLoginHtml;
            }
            else {
                RunPop('提示', '', "请点击右上角的退出系统，清除缓存后，再进入系统！", '提示', 0.4, 0.2);
            }
        },function(){
            RunPop('提示', '', "请点击右上角的退出系统，清除缓存后，再进入系统！", '提示', 0.4, 0.2);
        });
    }

    function getRights(matid, type) {
        $("#downloadDiv").hide();
        $("#previewDiv").hide();
        $.ax(Address + "/getRights", {"matid": matid}, "true", 'GET', "json", "", function (d) {
            if (d.rows) {
                var index = -1;
                for (var i = 0, len = d.rows.length; i < len; i++) {
                    $("#"+d.rows[i].fname+"Span").text(d.rows[i].functitle);
                    if (d.rows[i].funcid === "502") {
                        $("#"+d.rows[i].fname+"Span").html(d.rows[i].functitle.substring(0,4)+"<br/>"+d.rows[i].functitle.substring(4,d.rows[i].functitle.length));
                    }
                    if (d.rows[i].funcid === "503") {
                        $("#"+d.rows[i].fname+"Span").html(d.rows[i].functitle.substring(0,8)+"<br/>"+d.rows[i].functitle.substring(8,d.rows[i].functitle.length));
                    }
                    $("#"+d.rows[i].fname+"Lable").text("");
                    if (nonNullAndEmpty(d.rows[i].starttime) && nonNullAndEmpty(d.rows[i].endtime)) {
                        $("#"+d.rows[i].fname+"Lable").text(d.rows[i].starttime + "~" + d.rows[i].endtime);
                        if ((new Date(d.rows[i].starttime.replace("年", "/").replace("月", "/").replace("日", "/")) < new Date()) &&
                            new Date(d.rows[i].endtime.replace("年", "/").replace("月", "/").replace("日", "/")) > new Date()) {
                            index = d.rows[i].funcid.substring(0,1);
                        }
                    }
                    if (d.rows[i].mark === "1" && d.rows[i].wmark === "1") { // 可点击的按钮
                        $("#"+d.rows[i].fname+"Btn").removeClass("notClickBtn").addClass("btn-white");
                        $("#"+d.rows[i].fname+"Btn").find("span").first().removeClass("notclickLogo").addClass("btnlogo");
                    }
                }
                if (type === 1 && index !== -1) {
                    if (parseInt(role) === 7) { // 评审 直接定位在【现场比赛(评比)阶段】
                        index = 5;
                    }
                    else if (parseInt(role) === 6) { // 高校领队 直接定位在【活动报名阶段】
                        index = 2;
                    }
                    $(".ystep-container-steps").find("li").eq(index - 1).click();
                }
                else if (type === 3 && index !== -1) {
                    index = 1;
                    $(".ystep-container-steps").find("li").eq(index - 1).click();
                }
                $("#changeMatchModal").modal("hide");

                // 判断是否展示预览和下载按钮
                if (parseInt(role) <= 4 || parseInt(role) === 6 || parseInt(role) === 7) { // 展示预览 和 下载
                    $("#downloadDiv").show();
                    $("#previewDiv").show();
                }
                else  { // 不展示
                    $("#downloadDiv").hide();
                    $("#previewDiv").hide();
                }
            }
            else {
                RunPop('提示', '', d.data, '提示', 0.4, 0.2);
                $(".stageBtn").removeClass("btn-white").addClass("notClickBtn");
                $(".stageBtn span:first-child").removeClass("btnlogo").addClass("notclickLogo");
                $("#MaterialScoreBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");
                $("#SceneScoreBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");
                $("#ReflectScoreBtn").find("span").first().removeClass("btnlogo").addClass("notclickLogo");
            }
        });
    }

    // 当前激活的赛事
    GetOptions("ActiveMatchInfo", "activeMatch");

    //确定
    $("#btnSave").click(function () {
        var matid = $("#matid").val();
        if (isNullOrEmpty(matid)) {
            RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
            return;
        }
        if ($("#selectedMatch").val() === matid) {
            $("#changeMatchModal").modal("hide");
            return;
        }
        $("#selectedMatch").val(matid);
        var matchName = $("#matid").find("option:selected").text();
        if (isNullOrEmpty(matchName)) {
            $(".matchName").text("青年教师教学竞赛");
        }
        else {
            $(".matchName").text(matchName);
        }
       if (parseInt(role) > 2) { // 这里才需要换角色
           $.ax(Address + "/getRole", {"matid": matid}, "true", 'GET', "json", "", function (d,textStatus, resObj) {
               if (d.msg === "成功") {
                   setGlobalVar("YUserName",decodeURI(resObj.getResponseHeader("YUserName")));
                   setGlobalVar("YLoginId",resObj.getResponseHeader("YLoginId"));
                   setGlobalVar("YRole",resObj.getResponseHeader("YRole"));
                   setGlobalVar("YRoleName",decodeURI(resObj.getResponseHeader("YRoleName")));
                   setGlobalVar("YMatid",decodeURI(resObj.getResponseHeader("YMatid")));
                   $(".uname").html(getGlobalVar("YUserName"));
                   getRights(matid, 1);
               }
               else {
                   RunPop('提示', '', d.data, '提示', 0.4, 0.2);
               }
           });
       }
       else {
           getRights(matid, 1);
       }
    });

    // 下载用户手册
    $("#download").click(function(){
        event.preventDefault();
        var url = Address + '/download';
        ExcelExport(url);
    });

    //预览用户使用手册
    $("#preview").click(function(){
        var url = "/ps/funcPublic/PdfView/previewUserManualPDF";
        url = encodeURIComponent(url);
        window.open("../PDFViewer/web/viewer.html?file="+ url,"PreviewUserManual");
    });

    //关闭模态框
    $("#btnCancel").click(function () {
        $("#changeMatchModal").modal("hide");
    });

    // 切换赛事按钮
    $("#ChangeMatchBtn").click(function(){
        $("#changeMatchModal").modal('show');
    });

    // 修改密码按钮
    $("#ChangeMatchPwdBtn").click(function(){
        console.log("$('#changeFlag').val():"+$('#changeFlag').val())
        RunPop("修改密码", "../ChangePwd.html?matid="+$("#matid").val(), '', '', 0.7, 0.8, function(){
            console.log("$('#changeFlag').val():"+$('#changeFlag').val())
            if ($('#changeFlag').val() == '1'){
                doFunUser("GOEXIT");
            }

        },"ChangePwdModel");

    });


    // 赛事活动
    $("#MatchActivityBtn").click(function(){
        if (notClickWord(this)) {
            RunPop("赛事活动", "funcPrepare/MatchActivity.html", '', '', 0.95, 0.98, function(){
                var matid = $("#matid").val();
                $("#matid").empty();
                if (role <= 2) { // 主办方的角色使用
                    // 所有赛事
                    GetOptions("MatchInfo", "matid",function(){
                        var flag = false;
                        $('#matid option').each(function(){
                            if($(this).val() == matid){
                                flag = true;
                                $("#matid").val(matid);
                            }
                        });
                        if (!flag) {
                            $("#matid").val($('#matid option:first').val());
                            getRights($("#matid").val(), 1);
                        }
                        else {
                            getRights($("#matid").val(), 3);
                        }
                    });
                }
                else { // 除开主办方的角色使用
                    // 所有赛事
                    GetOptions("JoinMatch", "matid",function(){
                        var flag = false;
                        $('#matid option').each(function(){
                            if( $(this).val() === matid){
                                flag = true;
                                $("#matid").val(matid);
                            }
                        });
                        if (!flag) {
                            $("#matid").val($('#matid option:first').val());
                            getRights($("#matid").val(), 1);
                        }
                        else {
                            getRights($("#matid").val(), 3);
                        }
                    });
                }
            },"MatchActivity");
            var sHpx = Math.ceil(0.98 * $(window).height() - 45);
            if (sHpx < 50) sHpx = 200;
            $("#MatchActivity .modal-dialog .modal-body").css("max-height", sHpx+"px");
        }
    });

    // 赛事项目分类
    $("#MatchClassifyBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            RunPop("赛事项目分类", "funcPrepare/MatchClassify.html?matid="+matid, '', '', 0.7, 0.8, function(){
            },"MatchClassify");
            var sHpx = Math.ceil(0.98 * $(window).height() - 45);
            if (sHpx < 50) sHpx = 200;
            $("#MatchClassify .modal-dialog .modal-body").css("max-height", sHpx+"px");
        }
    });

    // 赛事日程信息
    $("#MatchScheduleBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            RunPop("赛事日程信息", "funcPrepare/MatchSchedule.html?matid="+matid, '', '', 0.95, 0.98, function(){
                getRights($("#matid").val(), 2);
            },"MatchSchedule");
            var sHpx = Math.ceil(0.98 * $(window).height() - 45);
            if (sHpx < 50) sHpx = 200;
            $("#MatchSchedule .modal-dialog .modal-body").css("max-height", sHpx+"px");
        }
    });

    // 赛事设置
    $("#MatchSetBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            RunPop("赛事设置", "funcPrepare/MatchSet.html?matid="+matid, '', '', 0.5, 0.6, function(){
            },"MatchSet");
            var sHpx = Math.ceil(0.9 * $(window).height() - 45);
            if (sHpx < 50) sHpx = 200;
            $("#MatchSet .modal-dialog .modal-body").css("max-height", sHpx+"px");
        }
    });



    // 评分标准
    $("#GradeStandardBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=106&matid="+matid,"GradeStandardPage");
        }
    });

    // 赛事人员
    $("#PersonManageBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("SidebarIndex.html?funcid=107&matid="+matid,"PersonManagePage");
        }
    });

    // 前台系统管理
    $("#FrontManageBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("SidebarIndex.html?funcid=108&matid="+matid,"FrontManagePage");
        }
    });

    // 报名
    $("#SignUpBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=201&matid="+matid,"SignUpPage");
        }
    });

    // 报名审核
    $("#SignUpCheckBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=202&matid="+matid,"SignUpCheckPage");
        }
    });

    // 高校报名汇总
    $("#UnitSummaryBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=203&matid="+matid,"UnitSummaryPage");
        }
    });

    // 上传材料
    $("#UploadMaterialBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=301&matid="+matid,"UploadMaterialPage");
        }
    });

    // 材料审核
    $("#MaterialCheckBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=302&matid="+matid,"MaterialCheckPage");
        }
    });


    // 竞赛题目
    $("#MatchTitleBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("MatchTitle.html?funcid=211&matid="+matid,"MatchTitlePage");
        }
    });

    // 抽取竞赛题目
    $("#MatchTitleBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("BallotMatchTitle.html?funcid=214&matid="+matid,"BallotMatchTitlePage");
        }
    });

    // 竞赛评审
    $("#AssessSectionBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=401&matid="+matid,"AssessSectionPage");
        }
    });




    // 答辩序号抽签
    $("#LoftNumberBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=402&matid="+matid,"LoftNumberPage");
        }
    });

    // 现场校验
    $("#SceneVerifyBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=501&matid="+matid,"SceneVerifyPage");
        }
    });



    // 抽取演讲节段
    $("#SampleSpeechListBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=502&matid="+matid,"SampleSpeechPage");
        }
    });

    // 下载选手PPT/演讲节段
    $("#DownloadPPTBtn").click(function(){
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=504&matid="+matid,"DownloadPPTPage");
        }
    });

    // 现场控制
    $("#SceneControlBtn").click(function(){
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("funcMatch/SceneControl.html?matid="+matid,"SceneControlPage");
        }
    });

    // 直播二维码
    $("#LiveControlBtn").click(function(){
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=503&matid="+matid,"LiveControlPage");
        }
    });

    // 材料打分
    $("#MaterialJudgeBtn").click(function(){
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("funcMatch/MaterialJudge.html?matid="+matid,"MaterialJudgePage");
        }
    });

    // 现场评审
    $("#SceneJudgeBtn").click(function(){
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("funcMatch/SceneJudge.html?matid="+matid,"SceneJudgePage");
        }
    });



     //综合评分
    $("#CompositeScoreBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=508&matid="+matid,"CompositeScoreListPage");
        }
    });
    // 专家评分表
    $("#JudgeScoreListBtn").click(function(){
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=601&matid="+matid,"JudgeScoreListPage");
        }
    });

    // 评分确认与锁定
    $("#ScoreConfirmBtn").click(function(){
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=602&matid="+matid,"ScoreConfirmPage");
        }
    });


    // 扣罚处理
    $("#DeductScoreBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=603&matid="+matid,"DeductScorePage");
        }
    });

    // 成绩与排名
    $("#ScoreRankBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=604&matid="+matid,"ScoreRankPage");
        }
    });


    //评分监测
    $("#ScoreMonitorBtn").click(function() {
        if (notClickWord(this)) {
            var matid = $("#matid").val();
            if (isNullOrEmpty(matid)) {
                RunPop('提示', '', "请选择赛事！", '提示', 0.4, 0.2);
                return;
            }
            window.open("NoSidebarIndex.html?funcid=605&matid="+matid,"ScoreMonitorPage");
        }
    });


    // 系统设置
    $("#SystemManageBtn").click(function() {
        window.open("SidebarIndex.html?funcid=001&matid=","SystemManagePage");
    });


    function notClickWord(obj) {
        if ($(obj).hasClass("notClickBtn")) {
            RunPop('提示', '', "抱歉！当前不能点击该模块！如需进入该模块，请联系主办方！", '提示', 0.4, 0.2);
            return false;
        }
        return true;
    }

})
function extend(obj1, obj2) {
    for(var attr in obj2) {
        obj1[attr] = obj2[attr];
    }
}

function SetStep(arg) {
    this.body = document.body;
    this.opt = {
        skin: 1,
        show: false,
        content: '.stepCont',
        pageCont: '.pageCont',
        imgWidth: 20,
        stepContainerMar: 20,
        nextBtn: '.nextBtn',
        prevBtn: '.prevBtn',
        steps: ['1', '2', '3', '4'],
        descriptionHeader: ['步骤一', '步骤二', '步骤三', '步骤四'],
        description: ['', '', '', '', '',''],
        //pageClass:'',//分页的类或则id
        stepCounts: 4, //总共的步骤数
        curStep: 1, //当前显示第几页
        animating: false,
        showBtn: true, //是否生成上一步下一步操作按钮
        clickAble: true, //是否可以通过点击进度条的节点操作进度
        onLoad: function() {

        }
    };

    var options = $.extend({}, this.opt, arg);

    switch(options.skin) {
        case 1:
            this.init(arg);
            break;
        case 2:
            $(options.content).addClass("stepY");
            this.initY(arg);
            break;
        case 3:
            $(options.content).addClass("jiantou");
            this.initJanTou(arg);
            break;
        default:
            this.init(arg);
    }

}

//初始化 生成页面的进度条和按钮  箭头
SetStep.prototype.initJanTou = function(arg) {
    var _that = this;
    extend(this.opt, arg);
    this.opt.stepCounts = this.opt.steps.length;
    this.content = $(this.opt.content);
    this.pageCont = this.content.find(this.opt.pageCont);
    var w_con = $(this.content).width();
    var w_li = (w_con - this.opt.stepContainerMar * 2) / this.opt.stepCounts / 2;

    var stepContainer = this.content.find('.ystep-container');

    this.stepContainer = stepContainer;
    var stepsHtml = $("<ul class='ystep-container-steps'></ul>");
    var stepDisc = "<li class='ystep-step ystep-step-undone'></li>";
    var stepP = $("<div class='ystep-progress'>" +
        "<p class='ystep-progress-bar'><span class='ystep-progress-highlight' style='width:0%'></span></p>" +
        "</div>");
    var stepButtonHtml = $("<div class='step-button'><button type='button' class='btn btn-default prevBtn' id='prevBtn' class='prevBtn'>上一步</button>" +
        "<button type='button' class='btn btn-default nextBtn' id='nextBtn' class='nextBtn'>下一步</button></div>");
    stepP.css('width', w_li * 2 * (this.opt.stepCounts - 1));
    stepP.find('.ystep-progress-bar').css('width', w_li * 2 * (this.opt.stepCounts - 1))
    for(var i = 0; i < this.opt.stepCounts; i++) {
        if(i == 0) {
            var _s = $(stepDisc).html('<span class="stepIcon">' + this.opt.steps[i] + '</span><span class="descriptionHeader">' + this.opt.descriptionHeader[i] + '<i class="fa fa-angle-right tubiao"></i></span>').addClass('')
        } else {
            var _s = $(stepDisc).html('<span class="stepIcon">' + this.opt.steps[i] + '</span><span class="descriptionHeader">' + this.opt.descriptionHeader[i] + '<i class="fa fa-angle-right tubiao"></i></span>')
        }
        stepsHtml.append(_s);
    }
    stepsHtml.find('li').css('width', w_li * 2).css("padding-left", "60px")
    stepContainer.append(stepsHtml).append(stepP);

    stepContainer.css('left', (w_con - stepP.width() - this.opt.imgWidth - 10 - this.opt.stepContainerMar * 2) / 2 - 120)
    stepContainer.css('height', stepsHtml.height() - 20);

    this.content.css('overflow', 'hidden')
    this.setProgress(this.stepContainer, this.opt.curStep, this.opt.stepCounts)
    //判断参数 是否显示按钮 并绑定点击事件
    if(this.opt.showBtn) {
        this.content.append(stepButtonHtml)
        this.prevBtn = this.content.find(this.opt.prevBtn)
        this.nextBtn = this.content.find(this.opt.nextBtn)
        this.prevBtn.on('click', function() {
            // if($(this).hasClass('handleAble')){
            if($(_that).attr('disabled') || _that.opt.animating) {
                return false;
            } else {
                _that.opt.animating = true;
                _that.opt.curStep--;
                _that.setProgress(_that.stepContainer, _that.opt.curStep, _that.opt.stepCounts)
            }
        })
        this.nextBtn.on('click', function() {
            // if($(this).hasClass('handleAble')){
            if($(_that).attr('disabled') || _that.opt.animating) {
                return false;
            } else {
                _that.opt.animating = true;
                _that.opt.curStep++;
                _that.setProgress(_that.stepContainer, _that.opt.curStep, _that.opt.stepCounts)
            }
        })
    }
    //判断时候可点击进度条 并绑定点击事件
    if(this.opt.clickAble) {
        stepsHtml.find('li').on('click', function() {
            _that.opt.curStep = $(this).index() + 1;
            _that.setProgress(_that.stepContainer, _that.opt.curStep, _that.opt.stepCounts)
        })
    }
    $(window).resize(function() {
        var w_con = $(_that.content).width();
        var w_li = w_con / _that.opt.stepCounts / 2;
        stepP.css('width', w_li * 2 * (_that.opt.stepCounts - 1));
        stepP.find('.ystep-progress-bar').css('width', w_li * 2 * (_that.opt.stepCounts - 1))
        stepsHtml.find('li').css('width', w_li * 2).css("padding-left", "60px")
        // stepContainer.css('left',(w_con-stepP.width()-_that.opt.imgWidth-10-_that.opt.stepContainerMar*2)/2)
        stepContainer.css('left', (w_con - stepP.width() - _that.opt.imgWidth - 10 - _that.opt.stepContainerMar * 2) / 2 - 120)
    })
}

//初始化 生成页面的进度条和按钮x
SetStep.prototype.init = function(arg) {
    var _that = this;
    extend(this.opt, arg);
    this.opt.stepCounts = this.opt.steps.length;
    this.content = $(this.opt.content);
    this.pageCont = this.content.find(this.opt.pageCont);
    var w_con = $(this.content).width();
    var w_li = (w_con - this.opt.stepContainerMar * 2) / this.opt.stepCounts / 2;

    var stepContainer = this.content.find('.ystep-container');

    this.stepContainer = stepContainer;
    var stepsHtml = $("<ul class='ystep-container-steps'></ul>");
    var stepDisc = "<li class='ystep-step ystep-step-undone'></li>";
    var stepP = $("<div class='ystep-progress'>" +
        "<p class='ystep-progress-bar'><span class='ystep-progress-highlight' style='width:0%'></span></p>" +
        "</div>");
    var stepButtonHtml = $("<div class='step-button'><button type='button' class='btn prevBtn' id='prevBtn' class='prevBtn' style='outline:none;color:white'>上一步</button>" +
        "<button type='button' class='btn  nextBtn' id='nextBtn' class='nextBtn' style='outline:none;color:white'>下一步</button></div>");
    stepP.css('width', w_li * 2 * (this.opt.stepCounts - 1));
    stepP.find('.ystep-progress-bar').css('width', w_li * 2 * (this.opt.stepCounts - 1))
    for(var i = 0; i < this.opt.stepCounts; i++) {
        if(i == 0) {
            var _s = $(stepDisc).html('<span class="stepIcon">' + this.opt.steps[i] + '</span><p class="descriptionHeader">' + this.opt.descriptionHeader[i] + '</p><div class="description">' + this.opt.description[i] + '</div>').addClass('')
        } else {
            var _s = $(stepDisc).html('<span class="stepIcon">' + this.opt.steps[i] + '</span><p class="descriptionHeader">' + this.opt.descriptionHeader[i] + '</p><div class="description">' + this.opt.description[i] + '</div>')
        }
        stepsHtml.append(_s);
    }
    stepsHtml.find('li').css('width', '30px').css('marginRight', w_li * 2 - 30)
    stepContainer.append(stepsHtml).append(stepP);

    stepContainer.css('left', (w_con - stepP.width() - this.opt.imgWidth - 10 - this.opt.stepContainerMar * 2) / 2)
    stepContainer.css('height', stepsHtml.height() - 25);

    this.content.css('overflow', 'hidden')
    this.setProgress(this.stepContainer, this.opt.curStep, this.opt.stepCounts)
    //判断参数 是否显示按钮 并绑定点击事件
    if(this.opt.showBtn) {
        this.content.append(stepButtonHtml)
        this.prevBtn = this.content.find(this.opt.prevBtn)
        this.nextBtn = this.content.find(this.opt.nextBtn)
        this.prevBtn.on('click', function() {
            // if($(this).hasClass('handleAble')){
            if($(_that).attr('disabled') || _that.opt.animating) {
                return false;
            } else {
                _that.opt.animating = true;
                _that.opt.curStep--;
                _that.setProgress(_that.stepContainer, _that.opt.curStep, _that.opt.stepCounts)
            }
        })
        this.nextBtn.on('click', function() {
            // if($(this).hasClass('handleAble')){
            if($(_that).attr('disabled') || _that.opt.animating) {
                return false;
            } else {
                _that.opt.animating = true;
                _that.opt.curStep++;
                _that.setProgress(_that.stepContainer, _that.opt.curStep, _that.opt.stepCounts)
            }
        })
    }
    //判断时候可点击进度条 并绑定点击事件
    if(this.opt.clickAble) {
        stepsHtml.find('li').on('click', function() {
            _that.opt.curStep = $(this).index() + 1;
            _that.setProgress(_that.stepContainer, _that.opt.curStep, _that.opt.stepCounts)
        })
    }
    $(window).resize(function() {
        var w_con = $(_that.content).width();
        var w_li = w_con / _that.opt.stepCounts / 2;
        stepP.css('width', w_li * 2 * (_that.opt.stepCounts - 1));
        stepP.find('.ystep-progress-bar').css('width', w_li * 2 * (_that.opt.stepCounts - 1))
        stepsHtml.find('li').css('width', '30px').css('marginRight', w_li * 2 - 30)
        stepContainer.css('left', (w_con - stepP.width() - _that.opt.imgWidth - 10 - _that.opt.stepContainerMar * 2) / 2)
    })
}

//设置进度条x
SetStep.prototype.setProgress = function(n, curIndex, stepsLen) {
    var _that = this;
    //获取当前容器下所有的步骤
    var $steps = $(n).find("li");
    var $progress = $(n).find(".ystep-progress-highlight");
    //判断当前步骤是否在范围内
    if(1 <= curIndex && curIndex <= $steps.length) {
        //更新进度
        var scale = "%";
        scale = Math.round((curIndex - 1) * 100 / ($steps.length - 1)) + scale;
        $progress.animate({
            width: scale
        }, {
            speed: 1000,
            done: function() {
                //移动节点
                $steps.each(function(j, m) {
                    var _$m = $(m);
                    var _j = j + 1;
                    if(_j < curIndex) {
                        _$m.attr("class", "ystep-step-done");
                    } else if(_j === curIndex) {
                        _$m.attr("class", "ystep-step-active");
                    } else if(_j > curIndex) {
                        _$m.attr("class", "ystep-step-undone");
                    }
                })
                if(_that.opt.showBtn) {
                    if(curIndex == 1) {
                        _that.prevBtn.attr('disabled', 'true')
                        _that.nextBtn.removeAttr('disabled')
                    } else if(curIndex == stepsLen) {
                        _that.prevBtn.removeAttr('disabled')
                        _that.nextBtn.attr('disabled', 'true')
                    } else if(1 < curIndex < stepsLen) {
                        _that.prevBtn.removeAttr('disabled')
                        _that.nextBtn.removeAttr('disabled')
                    }
                }
                _that.checkPage(_that.pageCont, _that.opt.curStep, _that.opt.stepCounts)
                _that.opt.animating = false;
            }
        });
    } else {
        return false;
    }
}
//改变 分页显示
SetStep.prototype.checkPage = function(pageCont, curStep, steps) {
    for(var i = 1; i <= steps; i++) {
        if(i === curStep) {
            pageCont.find('#page' + i).css("display", "block");
        } else {
            pageCont.find('#page' + i).css("display", "none");
        }
    }
}
