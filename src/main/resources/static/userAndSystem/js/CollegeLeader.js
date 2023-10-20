$(function () {
    var role = $.trim(getGlobalVar("YRole"));
    var Address = "/ps/userAndSystem/CollegeLeader";
    var gridWidth = Math.floor($(window).width() * 0.975);
    var gridHeight = Math.floor($(window).height()) - 160;
    var utype = {value:":;1:小学;2:中学;3:高中(普高);4:高中(职高)"};

    // setTimeout(function () {
        initGrid();
    // }, 500);

    function initGrid() {
        var cname = getjQGridOption("CityInfo");
        $("#Grid").jqGrid({
            url: Address + '/gridSearch',
            postData:{
                // matid: matid
            },
            styleUI:'Bootstrap',
            datatype:'json',
            colModel:[
                {label:"uid",name:"uid",hidden:true},

                {label:"学校名称",name:"uname",width:180,editable:true,editrules:{required:true},editoptions:{maxlength:256},
                    formoptions: {label: "学校名称 <span class='m_mark' style=''>*</span>"},searchoptions: {sopt:['cn']},
                },

                {label:"所在地", name: 'ulocation', width: 400,editable:true,editrules:{required:true},editoptions:{maxlength:100},
                    formoptions: {label: "所在地 <span class='m_mark' style=''>*</span>"},searchoptions: {sopt:['cn']}
                },
                {label:"学校邮编", name: 'upostcode', width: 110,editable:true,editrules:{required:true},editoptions:{maxlength:6},
                    formoptions: {label: "学校邮编 <span class='m_mark' style=''>*</span>"},searchoptions: {sopt:['cn']}
                },

                {label: "所属市/州", name: 'cid', align:'center', width: 180,editable:true,editrules:{required:true},edittype:'select',editoptions:{value:":;" + cname},stype: 'select',searchoptions:{value:":;" + cname},formatter: 'select',
                    formoptions: {label: "所属市/州 <span class='m_mark' style=''>*</span>"}},
                {label: "学校类型", name: 'utype', align:'center', width: 110,editable:true,edittype:'select',editoptions:utype,stype: 'select',searchoptions:utype,formatter: 'select',
                    formoptions: {label: "学校类型 <span class='m_mark' style=''>*</span>"}},

                {label:"备注",name:'unote',width:220,editable:true,editoptions:{maxlength:200},searchoptions: {sopt:['cn']}},
            ],
            rowNum:500,
            width: gridWidth,
            height: gridHeight,
            sortorder:'desc',
            shrinkToFit:true,
            //是否可以多选

            viewrecords:true,
            pager:'#Page',
            //显示数据库总记录数
            loadonce:true,
            onSelectRow: function (rowid) {
                $("#Grid").saveOldValues(rowid);
            },
            gridComplete: function(){
                $("#Page_center").hide();
                $("#Page_left").css("width", "40%");
            },

        });

        //设置标题头搜索过滤框
        $("#Grid").navGrid('#Page',{edit:false, add:false, del:false,search:true,refresh:true,
                addtext:'添加', edittext:'编辑', deltext:'删除',searchtext:"查找",refreshtext:"刷新"},
            {}, {}, {});
        $("#Grid").jqGrid('filterToolbar', {searchOprators: true});

        $("#Grid").navButtonAdd("#Page", {
            caption: "删除",id: "gridDel", buttonicon: "glyphicon glyphicon-trash", onClickButton: doDelete, position: "first"
        })
            .navButtonAdd("#Page", {
                caption: "编辑",id: "gridEdit", buttonicon: "glyphicon glyphicon-edit", onClickButton: doEdit, position: "first"
            })
            .navButtonAdd("#Page", {
                caption: "新加",id: "gridAdd", buttonicon: "glyphicon glyphicon-plus", onClickButton: doAdd, position: "first"
            });
    }





    // 新加
    function doAdd() {
        var grid = $("#Grid");
        var editParameters = {
            top: 50,
            left: 25,
            url:Address + "/gridAdd",
            reloadAfterSubmit: true,
            closeAfterAdd: true,
            addCaption: "添加中小学信息",
            editData:{
                oldlist: function () {
                    return jqOldValues;
                }
            },
            beforeShowForm: function(){
                $("#TblGrid_Grid input").attr("autocomplete","off");
            },
            afterShowForm: function (formid) {
                $('.EditButton #sData').addClass('btn-info');
                $('.EditButton #cData').addClass('btn-warning');
                $('.jqResize.ui-resizable-handle.ui-resizable-se.glyphicon.glyphicon-import').css("display","none");

                $("#TblGrid_Grid").css("margin-left","-30px");
            },

            afterSubmit:function (response,postdata) {
                if ($.parseJSON(response.responseText).msg === "成功") {
                    $(this).jqGrid('setGridParam', { datatype: 'json' }).trigger('reloadGrid');
                    return [true, '']
                } else {
                    $(this).jqGrid('setGridParam', { datatype: 'json' }).trigger('reloadGrid');
                    return [false, $.parseJSON(response.responseText).data]
                }
            }
        };
        grid.editGridRow('new', editParameters);
    }

    function doGridAddReal(){

    }


    // 编辑
    function doEdit() {
        var sel_id = $("#Grid").jqGrid('getGridParam', 'selrow');
        if (isNullOrEmpty(sel_id)) {
            RunPop("提示信息", "", "请勾选数据！", "警告", 0.5, 0.3);
            return;
        }

        doGridEditReal(sel_id);
    }

    function doGridEditReal(sel_id) {
        var editParameters = {
            top: 50,
            left: 25,
            url:Address + "/gridEdit",
            reloadAfterSubmit: true,
            closeAfterEdit: true,
            editCaption: "编辑中小学信息",
            editData: {
                uid: function () {
                    var sel_id = $("#Grid").jqGrid('getGridParam', 'selrow');
                    var value = $("#Grid").jqGrid('getCell', sel_id, 'uid');
                    return value;
                },

                oldlist: function () {
                    return jqOldValues;
                }
            },
            beforeShowForm: function(){
                $("#TblGrid_Grid input").attr("autocomplete","off");
            },
            afterShowForm: function (formid) {
                $('.EditButton #sData').addClass('btn-info');
                $('.EditButton #cData').addClass('btn-warning');



                $("#TblGrid_Grid").css("margin-left","-30px");
            },


            afterSubmit:function (response,postdata) {
                if ($.parseJSON(response.responseText).msg === "成功") {
                    $(this).jqGrid('setGridParam', { datatype: 'json' }).trigger('reloadGrid');
                    return [true, '']
                } else {
                    $(this).jqGrid('setGridParam', { datatype: 'json' }).trigger('reloadGrid');
                    return [false, $.parseJSON(response.responseText).data]
                }
            }
        };
        $("#Grid").editGridRow(sel_id, editParameters);
    }

    //移除
    function doDelete() {
        var sel_id = $("#Grid").jqGrid('getGridParam', 'selrow');
        var muid = $("#Grid").jqGrid('getCell', sel_id, 'muid');
        if (isNullOrEmpty(muid)) {
            RunPop("提示信息", "", "请勾选记录！", "警告", 0.5, 0.3);
            return;
        }
        var uid = $("#Grid").jqGrid('getCell', sel_id, 'uid');
        if (isNullOrEmpty(uid)) {
            RunPop("提示信息", "", "请勾选记录！", "警告", 0.5, 0.3);
            return;
        }
        var mspid = $("#Grid").jqGrid('getCell', sel_id, 'mspid');
        if (isNullOrEmpty(mspid)) {
            RunPop("提示信息", "", "请勾选记录！", "警告", 0.5, 0.3);
            return;
        }

        doGridDelReal(sel_id,muid,uid,mspid);
    }

    function doGridDelReal(sel_id,muid,uid,mspid) {
        var msg = "是否删除中小学【"+$("#Grid").jqGrid('getCell',sel_id,'uname') +"】？" ;
        RunPop("提示", "", msg, "提示", 0.5, 0.3, function () {
            $.ax(Address + "/gridDelete", {matid: matid, muid: muid,uid:uid, mspid: mspid}, true, "post", "json", "", function (d, textStatus, resObj) { // success
                if (d.msg === "成功") {
                    reloadGrid();
                    RunPop("提示", "", "删除成功！", "成功", 0.5, 0.3,"", "U1");
                } else {
                    RunPop("提示信息", "", d.data, "提示", 0.5, 0.3, "", "U1");
                }
            });
        },'deleteModal');
    }
    //自适应
    $(window).resize(function () {
        gridWidth = Math.floor($(window).width() * 0.96);
        gridHeight = Math.floor($(window).height()) - 150;
        $("#Grid").setGridWidth(gridWidth).setGridHeight(gridHeight);
    });




    $("#refresh").click(function () {
        reloadGrid();
    });


    function reloadGrid() {
        $("#Grid").jqGrid("setGridParam", {
            datatype:'json',
            postData:{
            }
        }).trigger("reloadGrid");
    }


})