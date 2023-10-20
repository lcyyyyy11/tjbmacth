$(document).ready(function () {
    var Address = "/ps/userAndSystem/SafeRecords";
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
        editurl: null,
        styleUI:'Bootstrap',
        datatype:'json',
        //初始获取有效的数据
        postData:{
            startDate: $("#sDate").val(),
            endDate: $("#eDate").val()
        },
        colModel:[
            {label: "账号", name: 'personno', width: 180, searchoptions: {sopt:['cn']}},
            {label: "姓名", name: 'personname', width: 100, searchoptions: {sopt:['cn']}},
            {label: "操作代码", name: 'opclass', width: 90, align: "center", searchoptions: {sopt:['cn']}},
            {label: "操作类型", name: 'optype', width: 90, align: "center", searchoptions: {sopt:['cn']}},
            {label: "操作内容", name: 'opcontent', width: 800, searchoptions: {sopt:['cn']}},
            {label: "操作时间", name: 'optime',width: 180,stype: "date",sorttype: "date", searchoptions: {sopt:['cn']}},
            {label: "标记值", name: 'markid', width: 200, searchoptions: {sopt:['cn']}},
            {label: "地址", name: 'romoteaddr', width: 180, searchoptions: {sopt:['cn']}}
        ],
        rowNum:200,
        //显示行数据顺序号
        // rownumbers:true,
        // rownumWidth: 50,
        autowidth: true,
        width: gridWidth,
        height: gridHeight,
        sortorder: 'desc',
        //显示数据库总记录数
        viewrecords: true,
        pager: '#Page',
        loadonce: true,
        shrinkToFit: false,
        loadComplete: function(){
            $("#gs_optime").attr("type","text");
        }
    });
    //jqgrid编辑
    $("#Grid").navGrid('#Page',{edit:false,add:false,del:false,search:true,searchtext:"查找",refresh:true,refreshtext:"刷新"},{},{},{},{multipleSearch: true});
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

    //系统安全记录Excel导出
    $("#btnExport").click(function () {
        var ids =  $("#Grid").getDataIDs();
        if (ids.length == 0){
            //jqgrid表无数据直接返回
            RunPop("提示信息", "", "该表无数据,不支持导出！", "警告", 0.3, 0.2);
            return;
        }

        //$('#btnExport').attr('href', Address+'/Export?startDate='+$("#sDate").val()+"&endDate="+$("#eDate").val());

        event.preventDefault();
        var url = Address + '/Export?startDate='+$("#sDate").val()+"&endDate="+$("#eDate").val();
        ExcelExport(url);
    });

    //自适应
    $(window).resize(function () {
         gridWidth = Math.floor($(window).width() * 0.975);
         gridHeight = Math.floor($(window).height()) - 160;
        $("#Grid").setGridWidth(gridWidth).setGridHeight(gridHeight);
    });

});



