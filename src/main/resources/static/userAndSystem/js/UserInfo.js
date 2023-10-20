$(function () {
    var role = $.trim(getGlobalVar("YRole"));
    var Address = "/ps/userAndSystem/userInfo";
    var gridWidth = Math.floor($(window).width() * 0.975);
    var gridHeight = "";

    var userrole = ":;" + getjQGridOption("UserRoleSponsor");

    var oper = "";

    if (parseInt(role) > 2){
        $("#btnDiv").hide();
        gridHeight = Math.floor($(window).height()) - 130;
    } else {
        gridHeight = Math.floor($(window).height()) - 160;
    }

    $("#Grid").jqGrid({
        url: Address + '/gridSearch',
        styleUI: 'Bootstrap',
        colModel:[
            {label:"用户账号",name:"userno",width:170,editable:true,align:"left",
                editrules:{required:true,custom:true,custom_func:u_check},editoptions:{maxlength:18},
                formoptions: {label: "用户账号 <span class='m_mark' style=''>*</span>"}, searchoptions: {sopt:['cn']}
            },
            {label:"用户名",name:"username",width:160,editable:true,
                editrules:{required:true},editoptions:{maxlength:30},
                formoptions: {label: "用户名 <span class='m_mark' style=''>*</span>"}, searchoptions: {sopt:['cn']}
            },
            {label:"身份证号",name:"pid",width:170,editable:true,editoptions:{maxlength:18},editrules:{required:true,custom:true,custom_func:p_check},
                formoptions: {label: "身份证号码 <span class='m_mark'>*</span>"}, searchoptions: {sopt:['cn']}
            },
            {label: "手机号",name:"phone",width: 110,editable:true,editrules:{required:true},
                editoptions:{maxlength:11},searchoptions: {sopt:['cn']},
                formoptions: {label: "手机号 <span class='m_mark' style=''>*</span>"}
            },
            {label:"是否启用",name:"ifactive",width:80,editable:true,align:"center",
                stype: 'select', searchoptions:{value: ":;1:是;0:否"},editrules:{required:true},
                edittype:'select',editoptions:{value:":;1:是;0:否"},formatter:signSelected,
                formoptions: {label: "是否启用 <span class='m_mark'>*</span>"}},
            {label:"用户角色",name:"userrole",width:120,formatter:'select',stype:'select',searchoptions:{value:userrole},
                editable:true,edittype:'select',editoptions:{value:userrole},editrules:{required:true},
                formoptions: {label: "用户角色 <span class='m_mark'>*</span>"}},
            {label:"上次在线时间",name:"lastonline",width:150,editable:false,stype: "date",sorttype: "date", searchoptions: {sopt:['cn']}},
            {label:"是否在线",name:"onlinemark",width:80,editable:false,align:"center",stype: 'select',
                searchoptions: {value: ":;1:是;0:否"}, formatter:signSelected},
            {label:"备注",name:"unote",width:220,editable:true,editoptions:{maxlength: 49}, searchoptions: {sopt:['cn']}},
            {label:"",name:"activemark",hidden:true}
        ],
        datatype: 'json',
        rowNum: 200,
        autowidth:true,
        width: gridWidth,
        height: gridHeight,
        sortorder:'desc',
        //显示数据库总记录数
        viewrecords:true,
        pager:'#Page',
        //是否可以多选
        multiselect:true,
        multiboxonly:true,
        //水平铺满屏幕
        shrinkToFit:true,
        loadonce:true,
        loadComplete: function(){
            $("#gs_lastonline").attr("type","text");
            parent.changeHeight();
        },
        onSelectRow: function (rowid) {
            $("#Grid").saveOldValues(rowid);
        }
    });

    //设置标题头搜索过滤框
    $("#Grid").jqGrid('filterToolbar', {searchOprators: true});

    //jqgrid编辑
    $("#Grid").navGrid("#Page",
        {
            add:(parseInt(role) <= 2), edit:(parseInt(role) <= 2), del:false,
            search:true, refresh:true,
            addtext:'添加', edittext:'编辑', deltext:'删除', searchtext:'查找', refreshtext:'刷新'
        },
        //编辑
        {
            top: 130,
            left: 475,
            editCaption: "编辑所选记录",
            url:Address+"/gridEdit",
            reloadAfterSubmit: true,
            closeAfterEdit: true,
            editData: {
                userno: function () {
                    var sel_id = $("#Grid").jqGrid('getGridParam', 'selrow');
                    return $("#Grid").jqGrid('getCell', sel_id, 'userno');
                },
                oldlist: function(){
                    return jqOldValues;
                }
            },
            afterShowForm: function (formid) {
                oper = "edit";
                $("#userno,#pid").attr("readonly","readonly");
                $('.EditButton #sData').addClass('btn-info');
                $('.EditButton #cData').addClass('btn-warning');
                $(".navButton").css("display","none");
                $('#ifactive').css("padding", "0.5em 0.3em");

                $("#TblGrid_Grid_2").css("margin-top","30px");
                $("#TblGrid_Grid .DataTD").css("padding-right","30px");

                var sel_id = $("#Grid").jqGrid('getGridParam', 'selrow');
                var ifactive = $("#Grid").jqGrid('getCell', sel_id, 'activemark');
                if (nonNullAndEmpty(ifactive) && parseInt(ifactive) == 1){
                    $("#ifactive").val("1");
                }
                else {
                    $("#ifactive").val("0");
                }
            },
            beforeSubmit:function (postdata, formid) {
                if (!(/^1[0-9]{10}$/.test(postdata.phone))){
                    return [false,'请输入合法的手机号码！'];
                }else {
                    return [true,''];
                }
            },
            afterSubmit:function (response,postdata) {
                if ($.parseJSON(response.responseText).msg == "成功"){
                    $(this).jqGrid("setGridParam",{datatype: 'json'}).trigger('reloadGrid');
                    return [true,''];
                }else {
                    $(this).jqGrid("setGridParam",{datatype:'json'}).trigger('reloadGrid');
                    return [false,$.parseJSON(response.responseText).data];
                }
            }
        },
        //添加
        {
            top: 130,
            left: 475,
            editCaption: "添加记录",
            url:Address + "/gridAdd",
            reloadAfterSubmit: true,
            closeAfterAdd: true,
            beforeShowForm: function(){
                $("#TblGrid_Grid input").attr("autocomplete","off");
            },
            beforeSubmit:function (postdata, formid) {
                if (!(/^1[0-9]{10}$/.test(postdata.phone))){
                    return [false,'请输入合法的手机号码！'];
                }else {
                    return [true,''];
                }
            },
            afterShowForm: function (formid) {
                oper = "add";
                $('.EditButton #sData').addClass('btn-info');
                $('.EditButton #cData').addClass('btn-warning');
                $('#ifactive').css("padding", "0.5em 0.3em");
                $('.jqResize.ui-resizable-handle.ui-resizable-se.glyphicon.glyphicon-import').css("display","none");

                $("#TblGrid_Grid_2").css("margin-top","30px");
                $("#TblGrid_Grid .DataTD").css("padding-right","30px");

                $("#userno").on("change",function () {
                    var check = checkId(Address + "/selUserNo?userno=" + $(this).val());
                    if (!check) {
                        RunPop('提示', '', "系统已有该用户账号！请重新输入。", '提示', 0.4, 0.2);
                    }
                });
                $("#Pid").on("change",function () {
                    var check = checkId(Address + "/selPid?pid=" + $.trim($(this).val())) ;
                    if (!check) {
                        RunPop('提示', '', "系统已有该身份证号！请重新输入。", '提示', 0.4, 0.2);
                    }
                });
            },
            afterSubmit:function (response,postdata) {
                if ($.parseJSON(response.responseText).msg == "成功"){
                    $(this).jqGrid("setGridParam",{datatype: 'json'}).trigger('reloadGrid');
                    return [true,''];
                }else {
                    $(this).jqGrid("setGridParam",{datatype:'json'}).trigger('reloadGrid');
                    return [false,$.parseJSON(response.responseText).data];
                }
            }
        },
        //删除
        {},
        // 查找
        {multipleSearch: true}
    );

    //批量启用
    $("#btnValid").click(function () {
        setValid("valid");
    });

    //批量停用
    $("#btnInvalid").click(function () {
        setValid("invalid");
    });

    //批量设置离线
    $("#btnOffline").click(function () {
        setValid("offline");
    });

    //判断用户账号
    function u_check(value,colName){
        if (oper === "add"){
            var reg = /^\d{1,18}|^\d{1,18}[X]$/g;

            //判断用户账号只能是数字和X
            if (!reg.test(value)){
                return [false,"用户账号只能是数字或身份证号！"]
            }
            // 判断用户账号不能重复
            var check = checkId(Address + "/selUserNo?UserNo="+value) ;
            if(!check) {
                return [false,"系统已有该账号,请重新输入!"];
            }
            else{
                return [true,""];
            }
        } else
            return [true,""];
    }

    //判断身份证号格式
    function p_check(value,colName) {
        if ($.trim(value)){
            if (!IdCardValidate($.trim(value))){
                return [false,"身份证格式有误，请重新输入！"];
            }
        }
        return [true,""];
    }

    function checkId(url){
        var t ;
        $.ajax({
            type:"get",
            url:url,
            async: false,
            dataType: "text",
            success:function (data) {
                t = (data == "") ? true : false;
            }
        });
        return t;
    }

    function reloadGrid() {
        $("#Grid").jqGrid("setGridParam", {
            url: Address + '/gridSearch',
            datatype: 'json'
        }).trigger("reloadGrid");
    }

    function setValid(flag){
        var sel_id = $("#Grid").jqGrid("getGridParam","selarrrow");
        if (sel_id.length == 0){
            RunPop("提示","","请勾选数据！","提示",0.4,0.2);
            return;
        }

        var userNoArr = [];
        if (sel_id.length > 0){
            for (var i = 0; i < sel_id.length ; i++) {
                //得到UserNo值
                var value = $("#Grid").jqGrid('getCell',sel_id[i],'userno');
                userNoArr.push(value);
            }
        }

        var url = "";
        var text = "";
        switch (flag) {
            case "valid":
                url = "/setValid";
                text = "启用";
                break;
            case "invalid":
                url = "/setInvalid";
                text = "停用";
                break;
            case "offline":
                url = "/setOffline";
                text = "离线";
                break;
        }
        RunPop("提示","","是否" + text + "勾选人员账号?","提示",0.3,0.2,function () {
            $.ax(Address + url,{usernos:userNoArr.toString()},false,"POST","json","",function (d) {
                if (d.msg.indexOf("成功") > -1){
                    RunPop("成功","","设置成功！","成功",0.3,0.2);
                } else {
                    RunPop("提示","","设置失败，记录未作改动！","提示",0.3,0.2);
                }
                reloadGrid();
            });
        },"bql");
    }

    //自适应
    $(window).resize(function () {
        gridWidth = Math.floor($(window).width()*0.975);
        if (parseInt(role) > 2){
            gridHeight = Math.floor($(window).height()) - 130;
        } else {
            gridHeight = Math.floor($(window).height()) - 160;
        }
        $("#Grid").setGridWidth(gridWidth).setGridHeight(gridHeight);
    });
});
