$(function () {
    var role = $.trim(getGlobalVar("YRole"));
    var Address = "/ps/userAndSystem/CityInfo";
    var gridWidth = Math.floor($(window).width() * 0.975);
    var gridHeight = Math.floor($(window).height()) - 160;
    var sex = {value:":;0:女;1:男"};

    // setTimeout(function () {
        initGrid();
    // }, 500);

    function initGrid() {
        var cname = getjQGridOption("CityInfo");
        $("#Grid").jqGrid({
            url: Address + '/gridSearch',
            postData:{
            },
            styleUI:'Bootstrap',
            datatype:'json',
            colModel:[
                {label:"uid",name:"uid",hidden:true},
                {label: "所属市/州", name: 'cid', align:'center', width: 180,editable:true,editrules:{required:true},edittype:'select',editoptions:{value:":;" + cname},stype: 'select',searchoptions:{value:":;" + cname},formatter: 'select',
                    formoptions: {label: "所属市/州 <span class='m_mark' style=''>*</span>"}},
                {label:"联络人姓名", name: 'managername', width: 135,editable:true,editrules:{required:true},editoptions:{maxlength:32},
                    formoptions: {label: "联络人姓名 <span class='m_mark' style=''>*</span>"},searchoptions: {sopt:['cn']}
                },
                {label:"联络人身份证号", name: 'managerid', width: 180,editable:true,editrules:{required:true},editoptions:{maxlength:18},
                    formoptions: {label: "联络人身份证号 <span class='m_mark' style=''>*</span>"},searchoptions: {sopt:['cn']}
                },
                {label: "联络人性别", name: 'managersex', align:'center', width: 70,editable:true,edittype:'select',editoptions:sex,stype: 'select',searchoptions:sex,formatter: 'select',
                    formoptions: {label: "性别 <span class='m_mark' style=''>*</span>"}},
                {label:"联络人出生日期", name: 'managerbirth', width: 135,editable:true,editrules:{required:true},editoptions:{maxlength:32},
                    formoptions: {label: "出生日期 <span class='m_mark' style=''>*</span>"},searchoptions: {sopt:['cn']}
                },
                {label:"联络人电话", name: 'managerphone', width: 135,editable:true,editrules:{required:true},editoptions:{maxlength:18},
                    formoptions: {label: "联络人电话 <span class='m_mark' style=''>*</span>"},searchoptions: {sopt:['cn']}
                },
                {label:"联络人邮箱",name:'manageremail',width:180,editable:true,editoptions:{maxlength:20},searchoptions: {sopt:['cn']}},
                {label:"备注",name:'unote',width:220,editable:true,editoptions:{maxlength:200},searchoptions: {sopt:['cn']}},
            ],
            rowNum:500,
            width: gridWidth,
            height: gridHeight,
            sortorder:'desc',
            shrinkToFit:true,
            //是否可以多选
            // multiselect:true,
            // multiboxonly:true,
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
                caption: "编辑",id: "gridEdit", buttonicon: "glyphicon glyphicon-edit", onClickButton: doEdit, position: "first"
            });
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
            editCaption: "编辑参与高校信息",
            editData: {
                cid: function () {
                    var sel_id = $("#Grid").jqGrid('getGridParam', 'selrow');
                    var value = $("#Grid").jqGrid('getCell', sel_id, 'cid');
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

                $('#managersex').attr("disabled","true");
                $('#managerbirth').attr("disabled","true");
                $("#TblGrid_Grid").css("margin-left","-30px");

                $("input[name='managerid']").on("keyup",function () {
                    if ($(this).val().length == 18){
                        var managerid = $(this).val();

                        tcsexAndBirthday(managerid)//根据身份证号识别性别和出生年月
                    }
                });
            },
            beforeSubmit:function (postdata, formid) {
                if (!IdCardValidate(postdata.managerid)){
                    return [false, '请输入合法的身份证号码'];
                }else if (!(/^1[0-9]{10}$/.test(postdata.managerphone))){
                    return [false, '请输入合法的手机号码'];
                }else if (!CheckMail(postdata.manageremail)&&!isNullOrEmpty(postdata.manageremail)){
                    return [false, '请输入合法的邮箱'];
                }
                else {
                    return [true, ''];
                }
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


    //自适应
    $(window).resize(function () {
        gridWidth = Math.floor($(window).width() * 0.96);
        gridHeight = Math.floor($(window).height()) - 150;
        $("#Grid").setGridWidth(gridWidth).setGridHeight(gridHeight);
    });


    $("#refresh").click(function () {
        reloadGrid();
    });


    function tcsexAndBirthday (t){
        if(t && t.length == 18 && IdCardValidate(t)){
            var sex = t.substr(16,1)%2;
            if (sex == 1){
                $("#managersex").val("1");
            }else if (sex == 0){
                $("#managersex").val("0");
            }
            var birth = t.substring(6, 10) + "-" + t.substring(10, 12) + "-" + t.substring(12, 14);
            $("#managerbirth").val(birth);
        }
    }


    function reloadGrid() {
        $("#Grid").jqGrid("setGridParam", {
            datatype:'json',
            postData:{
            }
        }).trigger("reloadGrid");
    }


})