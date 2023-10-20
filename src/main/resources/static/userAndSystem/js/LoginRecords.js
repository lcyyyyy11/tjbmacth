$(document).ready(function () {
    var Address = "/ps/userAndSystem/LoginRecords";
    var gridWidth = Math.floor($(window).width() * 0.975);
    var gridHeight = Math.floor($(window).height()) - 160;

    //时间日期
    $("#sDate").datepicker({
        autoclose:true,
        format:"yyyy-mm-dd",
        orientation:"bottom",
        todayHighlight:true
    }).datepicker("setDate","now");


    $("#eDate").datepicker({
        autoclose:true,
        format:"yyyy-mm-dd",
        orientation:"bottom",
        todayHighlight:true
    }).datepicker("setDate","now");

    $("#Grid").jqGrid({
        url: Address + '/gridSearch',
        styleUI:'Bootstrap',
        datatype:'json',
        //初始获取有效的数据
        postData:{
            startDate: $("#sDate").val(),
            endDate: $("#eDate").val()
        },
        colModel:[
            {label:"登录ID",name:"loginid",width:2, searchoptions: {sopt:['cn']}},
            {label:"登录姓名",name:"username",width:2, searchoptions: {sopt:['cn']}},
            {label:"登录时间",name:"logintime",width:2,stype: "date",sorttype: "date"},
            {label:"登录IP",name:"loginip",width:2, searchoptions: {sopt:['cn']}},
            {label:"浏览器类型",name:"browsertype",width:2, searchoptions: {sopt:['cn']}},
            {label:"登录结果",name:"loginremark",width:1, searchoptions: {sopt:['cn']}},
            {label:"操作系统",name:"screenresolution",width:1, searchoptions: {sopt:['cn']}}
        ],
        rowNum:200,
        autowidth:true,
        width: gridWidth,
        height: gridHeight,
        sortorder:'desc',
        //显示数据库总记录数
        viewrecords:true,
        pager:'#Page',
        loadonce:true,
        loadComplete: function(){
            $("#gs_logintime").attr("type","text");
            parent.changeHeight();
        },
        gridComplete: function() {
            parent.changeHeight();
        }
    });
    //jqgrid编辑
    $("#Grid").navGrid('#Page',{edit:false,add:false,del:false,search:true,searchtext:"查找",refresh:true,refreshtext:"刷新"},        // 查找
        {},{},{},{multipleSearch: true});
    $("#Grid").jqGrid('filterToolbar', {searchOprators: true});

    //刷新按钮
    $("#reload").click(function () {
        $("#Grid").jqGrid("setGridParam",{
            url: Address + '/gridSearch',
            datatype: 'json',
            postData:{
                startDate:$("#sDate").val(),
                endDate:$("#eDate").val()
            }
        }).trigger("reloadGrid");
    });

    //自适应
    $(window).resize(function () {
         gridWidth = Math.floor($(window).width() * 0.975);
         gridHeight = Math.floor($(window).height()) - 160;
        $("#Grid").setGridWidth(gridWidth).setGridHeight(gridHeight);
    });

});



