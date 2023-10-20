var role = $.trim(getGlobalVar("YRole"));
var Address = "/ps/userAndSystem/UserRoleSet/";
var matid = getUrlParameter("matid");//赛事活动主键
var mspids = getUrlParameter("mspids");
var oprType = "";
$(function () {
    var gridWidth = Math.floor($(window).width() * 0.96);
    var gridHeight = Math.floor(parent.window.innerHeight - 370);

    //===============================================  第一面板Grid ================================================
    $("#Grid0").jqGrid({
        url: Address + "gridSearch",
        styleUI: 'Bootstrap',
        datatype: "json",
        colModel: [
            {label: "角色编码", name: 'roleno', width: 1, editable: true,edittype: "text",
                editrules: {required: true,integer:true,minValue:0,maxValue:50,custom:true,custom_func:m_check },
                sortable: true,sorttype: "number",formoptions: {label: "角色编码 <span class='m_mark' style=''>*</span>"}},
            {label: "角色名称", name: 'rolename',  width: 2,editable: true,edittype: "text",
                editoptions: { maxlength: 9 },editrules: {required: true},searchoptions: { sopt: ['cn']},
                formoptions: {label: "角色名称 <span class='m_mark' style=''>*</span>"}},
            {label: "备注", name: 'rolemark', width: 4, editable: true,edittype: "text",
                editoptions: { maxlength: 9 }, searchoptions: { sopt: ['cn']}},
            {label: "人员数", name: 'num', width: 1, editable: false }
            // {label: "操作", name:"op",width: 1,editable : false,formatter: operation}
        ],
        rowNum: 200,
        loadonce: true,
        shrinkToFit: true,
        width: gridWidth,
        height: gridHeight,
        sortorder: 'desc',
        viewrecords: true,
        pager: "#Page0",
        onSelectRow: function (id) {
            $("#Grid0").saveOldValues(id);
        }
    });
    $("#Grid0").jqGrid('filterToolbar', {searchOprators: true});

    $("#Grid0").navGrid("#Page0", {
            edit: (parseInt(role) == 1), add: (parseInt(role) == 1), del: (parseInt(role) == 1),
            search: true, refresh: true,
            searchtext: "查找", addtext: "添加", edittext: "编辑", deltext: "删除", refreshtext: "刷新"
        },
        {
            top: 150,
            left: 500,
            reloadAfterSubmit: true,
            closeAfterEdit: true,
            url:Address + 'gridEdit',
            editData: {
                roleno: function () {
                    var sel_id = $("#Grid0").jqGrid('getGridParam', 'selrow');
                    return $("#Grid0").jqGrid('getCell', sel_id, 'roleno');
                },
                oldlist: function () {
                    return jqOldValues;
                }
            },
            beforeShowForm:function(){
                $("#TblGrid_Grid0 input").attr("autocomplete","off");
            },
            afterShowForm: function (formid) {
                oprType = "edit";
                $("#roleno").attr("readonly","readonly");
                $('.EditButton #sData').addClass('btn-info');
                $('.EditButton #cData').addClass('btn-warning');
            },
            afterSubmit: function (response, postdata) {
                if ($.parseJSON(response.responseText).msg.indexOf("成功") > -1) {
                    // RunPop('成功', '', "修改成功！", '成功', 0.3, 0.2,'edit');
                    reloadGrid0();
                    return [true, '']
                } else {
                    reloadGrid0();
                    return [false, $.parseJSON(response.responseText).data]
                }
            }
        },
        //add  roleid可编辑
        {
            top: 150,
            left: 500,
            reloadAfterSubmit: true,
            closeAfterAdd: true,
            url:Address + 'gridAdd',
            beforeShowForm:function(){
                $("#TblGrid_Grid0 input").attr("autocomplete","off");
            },
            afterShowForm: function (formid) {
                oprType = "add";
                $('.EditButton #sData').addClass('btn-info');
                $('.EditButton #cData').addClass('btn-warning');
            },
            afterSubmit: function (response, postdata) {
                if ($.parseJSON(response.responseText).msg.indexOf("成功") > -1) {
                    // RunPop('成功', '', "添加成功！", '成功', 0.3, 0.2,'zc');
                    reloadGrid0();
                    return [true, '']
                } else {
                    reloadGrid0();
                    return [false, $.parseJSON(response.responseText).data]
                }
            }
        },
        //delete
        {
            top: 165,
            left: 565,
            url:Address + "gridDelete",
            reloadAfterSubmit:true,
            closeAfterEdit:true,
            delData:{
                roleno:function () {
                    var sel_id = $("#Grid0").jqGrid('getGridParam', 'selrow');
                    return $("#Grid0").jqGrid('getCell', sel_id, 'roleno');
                }
            },
            //设置按钮样式
            afterShowForm: function (formid) {
                $('.EditButton #dData').addClass('btn-info');
                $('.EditButton #eData').addClass('btn-warning');
            },
            beforeSubmit:function(postdata, formid){
                var sel_id = $("#Grid0").jqGrid('getGridParam', 'selrow');
                var num = $("#Grid0").jqGrid('getCell', sel_id, 'num');
                if (parseInt(num) > 0){
                    return[false,"抱歉，该角色下已有用户，不能删除！"];
                }
                return[true,""];
            },
            afterSubmit: function (response, postdata) {
                if ($.parseJSON(response.responseText).msg == "成功") {
                    $(this).jqGrid('setGridParam', {datatype: 'json'}).trigger('reloadGrid');
                    return [true, ''];
                } else {
                    $(this).jqGrid('setGridParam', {datatype: 'json'}).trigger('reloadGrid');
                    return [false, $.parseJSON(response.responseText).data];
                }
            }
        },
        //search
        {}
    );

    function m_check(value, colname) {
        if (oprType === "add"){
            // 判断角色编号只能为数字
            if(!onlyNum(value))
                return [false,"角色编码只能包含数字！"];
            // 判断编码不能重复
            var check = checkId(value,Address + "selRole?roleno=" + value + "&oprType="+oprType);
            if(!check)
                return [false,"系统已有该编号，请重新输入！"];
            else
                return [true,""];
        } else
            return [true,""];
    }

    function checkId (value,url){
        var t;
        $.ajax({
            type: 'POST',
            url: url,
            async: false,
            dataType: "text",
            success: function (d) {
                t = (d == "") ? true : false;
            }
        });
        return t;
    }

    function reloadGrid0(){
        $("#Grid0").jqGrid("setGridParam", {
            url: Address + "gridSearch",
            datatype:'json',
            loadComplete:function () {
                reloadGrid1();
            }
        }).trigger("reloadGrid");
    }

    //===============================================  第二面板Grid ================================================
    var lastSelection1;
    $("#Grid1").jqGrid({
        url: Address + "gridSearch",
        styleUI: 'Bootstrap',
        datatype: "json",
        colModel: [
            {label: "角色编号", name: 'roleno', width: 1, sortable: true, sorttype: "number"},
            {label: "角色名称", name: 'rolename', width: 2,searchoptions: { sopt: ['cn']}},
            {label: "备注", name: 'rolemark', width: 2, searchoptions: { sopt: ['cn']}}
        ],
        rowNum: 200,
        loadonce: true,
        shrinkToFit: true,
        width: gridWidth * 0.58,
        height: gridHeight - 70,
        sortorder: 'desc',
        viewrecords: true,
        pager: "#Page1",
        caption:"角色名称",
        loadComplete: function () {
            //选中第一行
            $("#Grid1").jqGrid('setSelection', 1, true);
            $("#Page1_center").remove();
            // loadGrid2();
        },
        onSelectRow: function (id) {
            lastSelection1 = id;
            loadGrid2();
        }
    });
    $("#Grid1").jqGrid('filterToolbar', {searchOprators: true});

    function loadGrid2(){
        var parent = $("#table_list_2");
        $("#table_list_2").empty();// 清空表格内容
        $("<table id='Grid2'></table>").appendTo(parent);
        // $("#Grid2").jqGrid("clearGridData");
        var rowid = $("#Grid1").jqGrid('getGridParam', 'selrow');
        var RoleNo1 = $("#Grid1").jqGrid('getCell', rowid, 'roleno');

        //==============================gird1初始化===========================
        $("#Grid2").jqGrid({
            url: Address + "grid2Search?roleno=" + RoleNo1,
            styleUI:'Bootstrap',
            datatype:'json',
            colModel:[
                { label: "catno", name: 'catno', hidden: true},
                { label: "funcid", name: 'funcid', hidden: true, stype: 'text', sortable: true},
                { label: "业务类别", name: 'functitle', width: 1, searchoptions: { sopt: ['cn']} },
                { label: "服务名称", name: 'servicenote', width: 1, searchoptions: { sopt: ['cn']} },
                { label: "fid", name: 'fid', hidden:true }
            ],
            rowNum:200,
            //显示行数据顺序号
            autowidth:true,
            width: Math.floor($(window).width() * 0.96) * 0.4,
            height: gridHeight - 35,
            sortorder:'desc',
            //显示数据库总记录数
            viewrecords:true,
            //是否可以多选
            multiselect:true,
            multiboxonly:true,
            pager:'#Page2',
            grouping: true,
            groupingView: {
                groupField: ["servicenote"],
                groupColumnShow: [false,false],
                groupText: ["<b style='color: green;'>{0}</b>","<b>{0}</b>"],
                groupOrder: ["asc","asc"],
                groupSummary: [false,false],
                groupCollapse: false
            },
            caption:"角色权限",
            loadonce:true,
            onSelectRow: function (rowid) {
            },
            loadComplete: function () {
                doSelect();
            }
        });
        $("#Grid2").jqGrid('filterToolbar', {searchOprators: true});
        // jqgrid编辑
        $("#Grid2").navGrid("#Page2",
            {
                add:false,
                edit:false,
                del:false,
                search:true, refresh:true,
                addtext:'添加', edittext:'编辑', deltext:'删除', searchtext:'查找', refreshtext:'刷新'
            },
            //编辑
            {},
            //添加
            {},
            //删除
            {}
        );
    }


    function doSelect() {
        var allJQGridData = $('#Grid2').jqGrid('getGridParam', 'data');
        for (var i = 0; i < allJQGridData.length; i++) {
            if (allJQGridData[i]['fid'] != null)
                $("#Grid2").jqGrid('setSelection', i+1, true);
        }
    }

    // ================================ 功能按钮
    // 刷新
    $("#A1").on("click", function () {
        reloadGrid1();
    });

    function reloadGrid1(){
        $("#Grid1").jqGrid("setGridParam", {
            url: Address + "gridSearch",
            datatype:'json'
        }).trigger("reloadGrid");
    }

    // 权限修改
    $("#A2").on("click", function () {
        saveSet($(this));
    });

    function saveSet($this){
        var sRolId = $('#Grid1').jqGrid('getCell', lastSelection1, 'roleno');
        var grid = $("#Grid2");
        var rowKey = grid.getGridParam("selrow");
        if (!rowKey)
            RunPop("提示","","请选择功能项！","提示",0.3,0.2);
        else {
            var selectedIDs = grid.getGridParam("selarrrow");
            var arrMenu = new Array();
            for (var i = 0; i < selectedIDs.length; i++) {
                var objMenu = new Object();
                objMenu.funcid = grid.jqGrid('getCell', selectedIDs[i], 'funcid');
                objMenu.catno = grid.jqGrid('getCell', selectedIDs[i], 'catno');
                arrMenu.push(objMenu);
            }
            $.ax(Address + "roleFuncEdit", {roleno: sRolId, myJson: JSON.stringify(arrMenu)}, true, 'POST', "json",function () {
                if($this){
                    $this.addClass("disabled").prop('disabled', true);
                }
            }, function (d,textStatus, resObj) { // success
                if($this){
                    $this.removeClass("disabled").prop('disabled', false);
                }
                RunPop("提示","",d.data,"成功",0.3,0.2);
            }, function (resObj, textStatus, errorThrown) { // error
                if($this){
                    $this.removeClass("disabled").prop('disabled', false);
                }
                alert(resObj.responseText);
            },function (resObj, textStatus) { // complete
                if($this){
                    $this.removeClass("disabled").prop('disabled', false);
                }
            });
        }
    }

    //================================================== jqGrid大小随窗口变化 ====================================================
    $(window).resize(function () {
        gridWidth = Math.floor($(window).width() * 0.96);
        gridHeight = Math.floor(parent.window.innerHeight - 370);
        $("#Grid0").setGridWidth(gridWidth).setGridHeight(gridHeight);
        $("#Grid1").setGridWidth(gridWidth*0.58).setGridHeight(gridHeight-70);
        $("#Grid2").setGridWidth(gridWidth*0.4).setGridHeight(gridHeight-35);
    });

});





