///后台返回的JSON日期数据转化 ParseJSONDate & ParseJSONDateS
var SelectValues = "";
var sOldValues = "";
var jqOldValues = '';

//判断是否为空
var isNullOrEmpty = function (obj) { return (obj === undefined || obj === null || obj === ""); }

/***start 字符**********************************************/
//去除字符串开始空格
String.prototype.ltrim = function () { return this.replace(/^\s*/g, ""); }
//去除字符串结尾空格
String.prototype.rtrim = function () { return this.replace(/\s*$/g, ""); }
//去除字符串中间空格
String.prototype.ctrim = function () { return this.replace(/\s/g, ""); }
//去除字符串的首尾的空格
String.prototype.trim = function () { return this.replace(/(^\s*)|(\s*$)/g, ""); }

//str2替换所有的str1
String.prototype.replaceAll = function (str1, str2) {
    var _r = new RegExp(str1, "gm");
    var _s = this;
    while (_s.indexOf(str1) > -1)
        _s = _s.replace(_r, str2);
    return _s;
}

//字符串以str开始
String.prototype.startWith = function (str) {
    return (new RegExp("^" + str)).test(this);
}
//字符串以str结束
String.prototype.endWith = function (str) {
    return (new RegExp(str + "$")).test(this);
}

//获取文件后缀，如xx.doc返回doc
String.prototype.getExtension = function () {
    var ret = this.match(/\.([^\.]+)$/i);
    return (ret == null ? '' : ret[1]);
}
//字符串倒序
String.prototype.reverse = function () {
    var temp = new Array();
    for (var i = this.length - 1; i > -1; i--) {
        temp.push(this.charAt(i));
    }
    return temp.join("").toString();
}
//从字符串左边截取 n 个字符，并支持全角半角字符的区分
//mode:是否计算全角
String.prototype.left = function (len, mode) {
    //截取字符串长度len不是数字，就返回
    if (!/\d+/.test(len)) return (this);
    //先截取字符串（不区分全半角），避免长字符串影响效率
    var str = this.substr(0, len);
    if (!mode) return str;

    //a预期计数：中文2字节，英文1字节；//temp临时字串
    var a = 0, temp = '';
    for (var i = 0; i < str.length; i++) {
        if (str.charCodeAt(i) > 255) { a += 2; }
        else { a++; }

        //如果增加计数后长度大于限定长度，就直接返回临时字符串
        if (a > len) { return temp; }
        //将当前内容加到临时字符串
        temp += str.charAt(i);
    }
    //如果全部是单字节字符，就直接返回源字符串
    return str;
}
//从字符串右边截取 n 个字符，并支持全角半角字符的区分
//mode:是否计算全角
String.prototype.right = function (len, mode) {
    return this.reverse().left(len, mode).reverse();
}

//判断汉字
function isChinese(str) {
  var reg = /^[\u4E00-\u9FA5]+$/;
  if (reg.test(str))
      return true;
  return false;
}

//判断数字
function isNumber(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

/***end 字符**********************************************/

function ParseJSONDate(value) {
    var a;
    if (typeof value === 'string') {
        a = /\/Date\((\d*)\)\//.exec(value);
        if (a) {
            return new Date(+a[1]);
        }
    }
    return value;
}
function ParseJSONDateS(value) {
    return new Date(parseInt(value.substr(6)));
}

//clear all cookies

function clearListCookies() {
    var cookies = document.cookie.split(";");
    for (var i = 0; i < cookies.length; i++) {
        var spcook = cookies[i].split("=");
        document.cookie = spcook[0] + "=;expires=Thu, 21 Sep 1979 00:00:01 UTC;";
    }
}

function getUrlParameter(name) {
    var href = window.location.href;
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var val = window.location.search.match(new RegExp("[\?\&]" + name + "=([^\&]*)(\&?)", "i"));
    return val ? decodeURIComponent(val[1].replace(/\+/g, " ")) : val;
}

function timeLimit(matid, funcid) {
    return new Promise((resolve, reject) => {
        // myAxios("/center/getModuleSchedule?matid=" + matid + "&funcid=" + funcid + "&now=" + Date.parse(new Date), "", "GET", "json")
        //     .then(res => {
        //         let data = res.data.data;
        //         // data.starttime 模块的开始时间
        //         // data.endtime 模块的结束时间
        //         // data.plantime 赛事筹划准备时间
        //         // data.closetime 赛事关闭时间
        //         if (res.data.msg != "成功" || isNullOrEmpty(data.endtime) || isNullOrEmpty(data.starttime)) {
        //             resolve(false);
        //         }
        //         if (compareDateEqual(new Date(), data.endtime) && compareDateEqual(data.starttime,
        //             new Date())) {
        //             resolve(true);
        //             return;
        //         } else {
        //             resolve(false);
        //         }
        //     }).catch(err => {
        //     console.log(err);
        //     resolve(false);
        // })
        $.ax("/getModuleSchedule",{matid:matid,funcid:funcid,now:Date.parse(new Date)},false,"GET","json","",function (res, textStatus, resObj) {
                    // data.starttime 模块的开始时间
                    // data.endtime 模块的结束时间
                    // data.plantime 赛事筹划准备时间
                    // data.closetime 赛事关闭时间
                    if (res.msg != "成功" || isNullOrEmpty(res.data.endtime) || isNullOrEmpty(res.data.starttime)) {
                        resolve(false);
                    }
                    if (compareDateEqual(new Date(), res.data.endtime) && compareDateEqual(res.data.starttime,
                        new Date())) {
                        resolve(true);
                        return;
                    } else {
                        resolve(false);
                    }
        },function (resObj, textStatus, errorThrown) {
            console.log(err);// error
            resolve(false);
        });
    })
}
        // .catch(err => {
    //     //     console.log(err);
    //     //     resolve(false);
// })

function num(id) {
    var oId = document.getElementById(id);
    if (/\D/.test(oId.value)) {
        RunPop('只能输入数字');
        oId.value = '';
    }
}

function GetOptions(sType, optionID, fn) {
    var request = new XMLHttpRequest();
    request.open('GET', '/ps/GetOptions?gType=' + sType, false);
    request.onreadystatechange = function () {
        if (request.readyState == 4) {
            if (request.status == 200) {
                fillOption(request.responseText, optionID, fn);
            }
            else
                alert('err:' + request.status);
        }
    };
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send();
}

function GetSingleVal(sType, async, fn) {
    var request = new XMLHttpRequest();
    request.open('GET', '/searchSingleVal?gType=' + sType, async);
    request.onreadystatechange = function () {
        if (request.readyState == 4) {
            if (request.status == 200) {
                if (isFunction(fn)) {
                    fn(request.responseText);
                }
            }
            else
                fn(request.responseText);
        }
    };
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send();
}

function fillOption(arr, optID, fn) {
    var Narr = arr.split(';');
    var opID = document.getElementById(optID);
    for (var i = 0; i < Narr.length; i++) {
        try {
            var sItem = Narr[i].split(':');
            opID.options.add(new Option(sItem[1], sItem[0]));
        } catch (e) { continue; }
    }
    if(isFunction(fn)){
        fn();
    }
}


$.fn.addSmartList = function (sType,minlen) {
    var obj = this;
    var sFlt = $(obj).val();
    var sRtn = "";
    var listID = $(obj).attr('id') + '_dl';
    if (sFlt.length == 0) {
        $('#' + listID).remove();
    }
    else if (sFlt.length >= minlen && sFlt.charCodeAt(0) > 255) {
        $.ajax({
            url: '/GetOptions',
            type: "POST",
            async: true,
            data: { gType: sType, sFltStr: sFlt },
            success: function (d) {
                sRtn = d;
            }
        });
        if (sRtn != "") {
            $('#' + listID).remove();
            var dl_obj = $('<datalist/>', { id: listID });
            var Narr = sRtn.split(';');
            for (var i = 0; i < Narr.length; i++) {
                try {
                    var sItem = Narr[i].split(':');
                    var opt = $("<option></option>").attr("value", sItem[1]);
                    $(dl_obj).append(opt);

                } catch (e) { continue; }
            }
            $(dl_obj).appendTo("body");
            $('#' + $(obj).attr('id')).attr('list', listID );
        }
    }
}

function getjQGridOption(sType) {
    var sRtn = "";
    $.ajax({
        url: '/ps/GetOptions?gType='+sType,
        type: 'GET',
        dataType:"text",
        async: false,
        success: function (d) {
            sRtn = d;
        },error:function (e) {
        }
    });
    if (sRtn != "")
        return sRtn;
}

function GetSelectValues(urlParas) {
    var request = new XMLHttpRequest();
    request.open('Post', '/GetSelectValueStr', true);
    request.onreadystatechange = function () {
        if (request.readyState == 4) {
            if (request.status == 200) {
                SelectValues = request.responseText;
            }
            else
                alert('err:' + request.status);
        }
    };
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.send(urlParas);
}

function getOption(id) {
    var myselect = document.getElementById(id);
    var index = myselect.selectedIndex;
    return { val: myselect.options[index].value, index: myselect.selectedIndex };
}

///获取网站cookie
function getCookie(cname){
	var name = cname + "=";
	var ca = document.cookie.split(';');
	for (var i=0;i<ca.length;i++){
		var c = ca[i];
		while(c.charAt(0)==' ') c = c.substring(1);
		if (c.indexOf(name) != -1) return decodeURI(c.substring(name.length, c.length));
	}
	return "";
}
///有效邮箱
function CheckMail(mail) {
    var filter = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    if (filter.test(mail)) return true;
    else {
        return false;
    }
}
///身份证验证
function idCardNo(obj) {
  var vcity = { 11: "北京", 12: "天津", 13: "河北", 14: "山西", 15: "内蒙古", 21: "辽宁", 22: "吉林", 23: "黑龙江", 31: "上海", 32: "江苏", 33: "浙江", 34: "安徽", 35: "福建", 36: "江西", 37: "山东", 41: "河南", 42: "湖北", 43: "湖南", 44: "广东", 45: "广西", 46: "海南", 50: "重庆", 51: "四川", 52: "贵州", 53: "云南", 54: "西藏", 61: "陕西", 62: "甘肃", 63: "青海", 64: "宁夏", 65: "新疆", 71: "台湾", 81: "香港", 82: "澳门", 91: "国外"};

  //是否为空  
  if (obj === '') {
      return false;
  }
  //校验长度，类型  
  if (isCardNo(obj) === false) {
      return false;
  }
  //检查省份  
  if (checkProvince(obj) === false) {
      return false;
  }
  //校验生日  
  if (checkBirthday(obj) === false) {
      return false;
  }
  //检验位的检测  
  if (checkParity(obj) === false) {
      return false;
  }
  return true;

  //检查号码是否符合规范，包括长度，类型  
  function isCardNo (obj) {
      //身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X  
      var reg = /(^\d{15}$)|(^\d{17}(\d|X)$)/;
      if (reg.test(obj) === false) {
          return false;
      }
      return true;
  };
  //取身份证前两位,校验省份  
  function checkProvince(obj) {
      var province = obj.substr(0, 2);
      if (vcity[province] == undefined) {
          return false;
      }
      return true;
  };
  //检查生日是否正确  
  function checkBirthday(obj) {
      var len = obj.length;
      //身份证15位时，次序为省（3位）市（3位）年（2位）月（2位）日（2位）校验位（3位），皆为数字  
      if (len == '15') {
          var re_fifteen = /^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/;
          var arr_data = obj.match(re_fifteen);
          var year = arr_data[2];
          var month = arr_data[3];
          var day = arr_data[4];
          var birthday = new Date('19' + year + '/' + month + '/' + day);
          return verifyBirthday('19' + year, month, day, birthday);
      }
      //身份证18位时，次序为省（3位）市（3位）年（4位）月（2位）日（2位）校验位（4位），校验位末尾可能为X  
      if (len == '18') {
          var re_eighteen = /^(\d{6})(\d{4})(\d{2})(\d{2})(\d{3})([0-9]|X)$/;
          var arr_data = obj.match(re_eighteen);
          var year = arr_data[2];
          var month = arr_data[3];
          var day = arr_data[4];
          var birthday = new Date(year + '/' + month + '/' + day);
          return verifyBirthday(year, month, day, birthday);
      }
      return false;
  };
  //校验日期  
  function verifyBirthday(year, month, day, birthday) {
      var now = new Date();
      var now_year = now.getFullYear();
      //年月日是否合理  
      if (birthday.getFullYear() == year && (birthday.getMonth() + 1) == month && birthday.getDate() == day) {
          //判断年份的范围（3岁到100岁之间)  
          var time = now_year - year;
          if (time >= 0 && time <= 130) {
              return true;
          }
          return false;
      }
      return false;
  };
  //校验位的检测  
  function checkParity(obj) {
      //15位转18位  
      obj = changeFivteenToEighteen(obj);
      var len = obj.length;
      if (len == '18') {
          var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
          var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
          var cardTemp = 0, i, valnum;
          for (i = 0; i < 17; i++) {
              cardTemp += obj.substr(i, 1) * arrInt[i];
          }
          valnum = arrCh[cardTemp % 11];
          if (valnum == obj.substr(17, 1)) {
              return true;
          }
          return false;
      }
      return false;
  };
}

//15位转18位身份证号  
function changeFivteenToEighteen(obj) {
  if (obj.length == '15') {
      var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2);
      var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2');
      var cardTemp = 0, i;
      obj = obj.substr(0, 6) + '19' + obj.substr(6, obj.length - 6);
      for (i = 0; i < 17; i++) {
          cardTemp += obj.substr(i, 1) * arrInt[i];
      }
      obj += arrCh[cardTemp % 11];
      return obj;
  }
  return obj;
}

///zzs 2017-05-15 for pop both iframe and 4 kind of message
function RunPop(title, url, sTxt, alertType, sWidth, sHeight, mcallback,insName) { //alertType:'成功'；'提示'；'警告'；'危险'  zzs 2017-05-20 
    if (url == null && sTxt == null) {
        sTxt = title;
        title = '';
    }
    if (sWidth == null && sHeight == null) {
        sWidth = 0.4;
        sHeight = 0.15;
    }
    var popID = 'zzsModal';
    var popConfirmID = 'zzsPopConfirm';
    if (insName != null) {
        popID = insName;
        popConfirmID = insName.trim() + 'Confirm';
    }
    if (alertType == null) alertType = '提示';
    var sMark = (alertType == '成功' ? 'success' : (alertType == '警告' ? 'warning' : (alertType == '危险' ? 'danger' : 'info')));
    var sBody = null, showBack = "false",sHead = '',sHeadTitle='';
    var sWpx = Math.ceil(sWidth * $(document).width());
    var sHpx = Math.ceil(sHeight * $(document).height() - 45);
    if (sWpx < 100) sWpx = 450;
    if (sHpx < 50) sHpx = 200;
    var sLeftPos = Math.ceil((1 - sWidth) * $(document).width() / 2);
    var sTopPos = Math.ceil(((1 - sHeight) * $(document).height() - 10) / 2);
    var sFooter = '';
    if (url != null && url != "")
    {
        sBody = '<iframe src=' + url + ' seamless frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="yes" allowTransparency="true" style="width:100%;height:100%;" ></iframe>';
        showBack = "true";
        sHead = '<div class="modal-header" style="padding:0 10px;">';
        sHeadTitle = ('<h6 class="modal-title">' + title + '</h6></div>');
        sHpx = Math.ceil(sHeight * $(document).height() - 17);
    }
    else {
        sTopPos = Math.ceil(((1 - sHeight) * $(document).height() - 10) / 4);
        sBody = '<div class="alert alert-' + sMark + '">';
        sBody = sBody + '<strong>' + alertType + '!</strong> ' + sTxt + '</div>';
        sHead = '<div class="modal-header">';
        sHeadTitle = ('<h4 class="modal-title">' + title + '</h4></div>');
        sFooter = '<div class="modal-footer">';
        sFooter += '<button id=' + popConfirmID + ' type="button" class="btn btn-primary">确定</button>';
        sFooter += '<button type="button" class="btn btn-primary" data-dismiss="modal">取消</button></div>';
    }
    var sHtml = '<div id=' + popID + ' class="modal fade" data-backdrop=' + showBack + ' role="dialog">';
    sHtml += '<div class="modal-dialog" style="margin:0 auto;position:absolute;top:' + sTopPos + 'px;left:' + sLeftPos + 'px;width:' + sWpx + 'px;">';
    sHtml += '<div class="modal-content">' + sHead;
    sHtml +='<button type="button" class="close" data-dismiss="modal">&times;</button>';
    sHtml += sHeadTitle;
    sHtml += ('<div class="modal-body" style="padding:0;margin:0;height:' + sHpx + 'px; max-height:' + sHpx + 'px;">');
    sHtml += sBody;
    sHtml += '</div>';
    sHtml += sFooter;
    sHtml += '</div></div></div>';
    $(sHtml).appendTo("body");
    $("#"+popID).modal();
    $("#" + popConfirmID).on("click", function () {
        $("#" + popID).modal("hide");
        try{
            mcallback();
        }catch(e){;}
    })
    $("#" + popID).on('hidden.bs.modal', function () {
        $("#" + popID).remove();
        if (url != null && url != "")
            try {
                mcallback();
            } catch (e) {; }
    });
}


//下载文件，直接使用a标签下载，配后后台零拷贝下载方法使用
let ExportFileProgress = url => {
    let a = document.createElement("a");
    a.href = url;
    $("body").append(a);
    a.click();
    $(a).remove();
};
function RunUploadImage_Size(title, btnName, upurl, isMulti, acceptType, sWidth, mcallback, maxSize) {

    var AjaxUpload = null;
    var retValue = '';
    var sWpx = Math.ceil(sWidth * $(document).width());
    var gfiles = null,
        btnTitle = null;
    if (btnName == '')
        btnTitle = '上传...';
    else
        btnTitle = btnName;
    if (isMulti == true || isMulti == 'true')
        sBody = '<input type="file" id="zzsFileUp" multiple="multiple" accept=' + acceptType +
            ' class="btn btn-info" style="width:' + Math.floor(sWpx * 0.94) + 'px;"/>';
    else
        sBody = '<input type="file" id="zzsFileUp" accept=' + acceptType + ' class="btn btn-info"/>';
    sBody += '<div id="zzsFileHolder"></div>';
    var sHtml = '<div id="zzsUploadModal" class="modal fade" data-backdrop="false" role="dialog">';
    sHtml += '<div class="modal-dialog" style="width:' + sWpx + 'px;">';
    sHtml += '<div class="modal-content"><div class="modal-header">'
    sHtml += '<button type="button" class="close" data-dismiss="modal">&times;</button>';
    sHtml += ('<h4 class="modal-title">' + title + '</h4></div>');
    sHtml += ('<div class="modal-body" style="height:170px; max-height:170px;">');
    sHtml += sBody;
    sHtml += '<div class="modal-footer">';
    sHtml +=
        '<div id="zzsUpProgressBar" class="progress-bar" role="progressbar" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100" style="width:0%;height:20px;">0%</div><br/>';
    sHtml += '<button type="button" class="btn btn-primary" id="zzsDoUpload" style="margin-top:5px;">' + btnTitle +
        '</button></div>';
    sHtml += '</div></div></div></div>';
    $(sHtml).appendTo("body");
    $("#zzsFileUp").on('change', function() {
        try{
            AjaxUpload.abort();
        }catch(e){
            // console.log(e);
        }
        $("#zzsDoUpload").removeAttr("disabled");
        $("#zzsFileHolder").html('<br/>');
        $('#zzsUpProgressBar').css('width', '0%');
        var subHtml = "";
        gfiles = this.files;
        for (var i = 0; i < gfiles.length; i++) {
            var file = gfiles[i];
            if (i % 2 == 0)
                if (i == gfiles.length - 1)
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span><br/><br/>';
                else
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span>';
            else
                subHtml += '<span style="position:absolute;left:55%;"> ' + file.name + ' (大小: ' + file.size +
                    ') </span><br/><br/>';
        }
        $("#zzsUploadModal .modal-body").css("height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $("#zzsUploadModal .modal-body").css("max-height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $(subHtml).appendTo("#zzsFileHolder");
    });
    $("#zzsDoUpload").on('click', function() {
        $(this).attr('disabled', "true");
        var max = maxSize * 1024 * 1024;
        if(isNullOrEmpty(gfiles)) return;
        for (var i = 0; i < gfiles.length; i++) {
            // console.log(maxSize * 1024 * 1024, gfiles[i].size)
            if (gfiles[i].size > max) {
                $("#zzsUploadModal").remove();
                RunPop_4("提示信息", "", "上传文件的大小不能超过" + maxSize + "M!", "警告", 0.4, 0.2, "", "UpLoad1");
                return;
            }
            zzsUploadFile(gfiles[i], i); // call the zzs function to upload the file
        }
    });
    $("#zzsUploadModal").modal();
    $("#zzsUploadModal").on('hidden.bs.modal', function() {
        $("#zzsUploadModal").remove();
        try {
            AjaxUpload.abort();
            mcallback(retValue);
        } catch (e) {
            // console.log(e);
        }
    });

    function zzsUploadFile(file, num) {
        var fd = new FormData();
        fd.append("upload", 1);
        fd.append("file", file); //$("#zzsFileUp").get(0).files[0]
        AjaxUpload = $.ajax({
            url: upurl,
            type: "POST",
            async: true,
            processData: false,
            contentType: false,
            data: fd,
            xhr: function() {
                var xhr = new XMLHttpRequest();
                //使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
                xhr.upload.addEventListener('progress', function(e) {
                    //loaded代表上传了多少
                    //total代表总数为多少
                    let rate = new Number(e.loaded / e.total);
                    var progressRate = ( rate * 100).toFixed(2) + '%';
                    //通过设置进度条的宽度达到效果
                    $('#zzsUpProgressBar').css('width', progressRate);
                    $('#zzsUpProgressBar').html(progressRate);
                })
                return xhr;
            },
            success: function(d) {
                retValue = d; //$.parseJSON(d).data;
                ++num;
                // $('#zzsUpProgressBar').css('width', num / gfiles.length * 100 + '%');
                if (num < gfiles.length)
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%');
                else {
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%  上传成功!');
                    $("#zzsUploadModal").modal('hide');
                }
            }
        });
    }
}

//上传视频文件，限制文件格式和大小
function RunUploadVideo_Size(title, btnName, upurl, isMulti, acceptType, sWidth, mcallback, maxSize) {
    var AjaxUpload = null;
    var retValue = '';
    var sWpx = Math.ceil(sWidth * $(document).width());
    var gfiles = null,
        btnTitle = null;
    if (btnName == '')
        btnTitle = '上传...';
    else
        btnTitle = btnName;
    if (isMulti == true || isMulti == 'true')
        sBody = '<input type="file" id="zzsFileUp" multiple="multiple" accept=' + acceptType +
            ' class="btn btn-info" style="width:' + Math.floor(sWpx * 0.94) + 'px;"/>';
    else
        sBody = '<input type="file" id="zzsFileUp" accept=' + acceptType + ' class="btn btn-info"/>';
    sBody += '<div id="zzsFileHolder"></div>';
    var sHtml = '<div id="zzsUploadModal" class="modal fade" data-backdrop="false" role="dialog">';
    sHtml += '<div class="modal-dialog" style="width:' + sWpx + 'px;">';
    sHtml += '<div class="modal-content"><div class="modal-header">'
    sHtml += '<button type="button" class="close" data-dismiss="modal">&times;</button>';
    sHtml += ('<h4 class="modal-title">' + title + '</h4></div>');
    sHtml += ('<div class="modal-body" style="height:170px; max-height:170px;">');
    sHtml += sBody;
    sHtml += '<div class="modal-footer">';
    sHtml +=
        '<div id="zzsUpProgressBar" class="progress-bar" role="progressbar" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100" style="width:0%;height:20px;">0%</div><br/>';
    sHtml += '<button type="button" class="btn btn-primary" id="zzsDoUpload" style="margin-top:5px;">' + btnTitle +
        '</button></div>';
    sHtml += '</div></div></div></div>';
    $(sHtml).appendTo("body");
    $("#zzsFileUp").on('change', function() {
        try{
            AjaxUpload.abort();
        }catch(e){
            // console.log(e);
        }
        $("#zzsDoUpload").removeAttr("disabled");
        $("#zzsFileHolder").html('<br/>');
        $('#zzsUpProgressBar').css('width', '0%');
        var subHtml = "";
        gfiles = this.files;
        for (var i = 0; i < gfiles.length; i++) {
            var file = gfiles[i];
            try{
                const mp4boxfile = MP4Box.createFile();
                if(file){
                    var reader = new FileReader();
                    var buffer = reader.readAsArrayBuffer(file);
                    reader.onload = function(e) {
                        var arrayBuffer = e.target.result
                        arrayBuffer.fileStart = 0
                        console.log(arrayBuffer)
                        mp4boxfile.appendBuffer(arrayBuffer)
                    }
                }
                let is_transcode = 0;
                mp4boxfile.onReady = function(info){//判断视频文件编码格式
                    console.log(info)
                    let mime = info.mime
                    let codec = mime.match(/codecs="(\S*),/)[1];
                    if (codec.indexOf('avc') === -1) {
                        is_transcode = 1;
                    }
                    if(is_transcode){
                        $("#zzsUploadModal").modal('hide');
                        let mytext = `抱歉，您提供的视频由于编码格式问题无法在线播放以及在线评审。
                        推荐您从以下链接下载转换工具，转换为浏览器识别的H.264编码格式(转换器默认的输出格式)的mp4视频。
                        请上传转换后的mp4视频。谢谢！<br/>
                        <a style="color: red;" title="点击下载转换工具，请耐心等待" target="_blank" href='https://www.any-video-converter.com/cn/any-video-converter-free.php'>
                           转换工具链接
                        </a>`;
                        RunPop_4("提示", "", mytext, "提示", 0.4, 0.2, "", "upvideo");
                    }else {
                        $("#zzsDoUpload").attr("disabled",false)
                    }
                }
            }catch (e) {
                console.log(e)
                $("#zzsUploadModal").modal('hide');
                let mytext = `抱歉，您提供的视频由于编码格式问题无法在线播放以及在线评审。
                        推荐您从以下链接下载转换工具，转换为浏览器识别的H.264编码格式(转换器默认的输出格式)的mp4视频。
                        请上传转换后的mp4视频。谢谢！<br/>
                        <a style="color: red;" title="点击下载转换工具，请耐心等待" target="_blank" href='https://www.any-video-converter.com/cn/any-video-converter-free.php'>
                           转换工具链接
                        </a>`;
                RunPop_4("提示", "", mytext, "提示", 0.4, 0.2, "", "upvideo");
            }
            if (i % 2 == 0)
                if (i == gfiles.length - 1)
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span><br/><br/>';
                else
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span>';
            else
                subHtml += '<span style="position:absolute;left:55%;"> ' + file.name + ' (大小: ' + file.size +
                    ') </span><br/><br/>';
        }
        $("#zzsUploadModal .modal-body").css("height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $("#zzsUploadModal .modal-body").css("max-height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $(subHtml).appendTo("#zzsFileHolder");
    });
    $("#zzsDoUpload").on('click', function() {
        $(this).attr('disabled', "true");
        var max = maxSize * 1024 * 1024;
        if(isNullOrEmpty(gfiles)) return;
        for (var i = 0; i < gfiles.length; i++) {
            console.log(maxSize * 1024 * 1024, gfiles[i].size)
            if (gfiles[i].size > max) {
                $("#zzsUploadModal").remove();
                RunPop_4("提示信息", "", "上传文件的大小不能超过" + maxSize + "M!", "警告", 0.4, 0.2, "", "UpLoad1");
                return;
            }
            zzsUploadFile(gfiles[i], i); // call the zzs function to upload the file
        }
    });
    $("#zzsUploadModal").modal();
    $("#zzsUploadModal").on('hidden.bs.modal', function() {
        $("#zzsUploadModal").remove();
        try {
            AjaxUpload.abort();
            mcallback(retValue);
        } catch (e) {
            // console.log(e);
        }
    });
    function zzsUploadFile(file, num) {
        var fd = new FormData();
        fd.append("upload", 1);
        fd.append("file", file); //$("#zzsFileUp").get(0).files[0]
        AjaxUpload = $.ajax({
            url: upurl,
            type: "POST",
            async: true,
            processData: false,
            contentType: false,
            data: fd,
            xhr: function() {
                var xhr = new XMLHttpRequest();
                //使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
                xhr.upload.addEventListener('progress', function(e) {
                    //loaded代表上传了多少
                    //total代表总数为多少
                    let rate = new Number(e.loaded / e.total);
                    var progressRate = ( rate * 100).toFixed(2) + '%';
                    //通过设置进度条的宽度达到效果
                    $('#zzsUpProgressBar').css('width', progressRate);
                    $('#zzsUpProgressBar').html(progressRate);
                })
                return xhr;
            },
            success: function(d) {
                retValue = d; //$.parseJSON(d).data;
                ++num;
                // $('#zzsUpProgressBar').css('width', num / gfiles.length * 100 + '%');
                if (num < gfiles.length)
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%');
                else {
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%  上传成功!');
                    $("#zzsUploadModal").modal('hide');
                }
            }
        });
    }
}

/// zzs 2017-06-01 for commaon load files
function RunUpload(title, btnName, upurl, isMulti, acceptType, sWidth, mcallback) {
    var retValue = '';
    var sWpx = Math.ceil(sWidth * $(document).width());
    var gfiles = null,
        btnTitle = null;
    if (btnName == '')
        btnTitle = '上传...';
    else
        btnTitle = btnName;
    if (isMulti == true || isMulti == 'true')
        sBody = '<input type="file" id="zzsFileUp" multiple="multiple" accept=' + acceptType +
            ' class="btn btn-info" style="width:' + Math.floor(sWpx * 0.94) + 'px;"/>';
    else
        sBody = '<input type="file" id="zzsFileUp" accept=' + acceptType + ' class="btn btn-info"/>';
    sBody += '<div id="zzsFileHolder"></div>';
    var sHtml = '<div id="zzsUploadModal" class="modal fade" data-backdrop="false" role="dialog">';
    sHtml += '<div class="modal-dialog" style="width:' + sWpx + 'px;">';
    sHtml += '<div class="modal-content"><div class="modal-header">'
    sHtml += '<button type="button" class="close" data-dismiss="modal">&times;</button>';
    sHtml += ('<h4 class="modal-title">' + title + '</h4></div>');
    sHtml += ('<div class="modal-body" style="height:170px; max-height:170px;">');
    sHtml += sBody;
    sHtml += '<div class="modal-footer">';
    sHtml +=
        '<div id="zzsUpProgressBar" class="progress-bar" role="progressbar" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100" style="width:0%;height:20px;">0%</div><br/>';
    sHtml += '<button type="button" class="btn btn-primary" id="zzsDoUpload" style="margin-top:5px;">' + btnTitle +
        '</button></div>';
    sHtml += '</div></div></div></div>';
    $(sHtml).appendTo("body");
    $("#zzsFileUp").on('click', function () {
        $("#zzsDoUpload").attr('disabled', false);
    });
    $("#zzsFileUp").on('change', function() {
        $("#zzsDoUpload").attr('disabled', false);
        $("#zzsFileHolder").html('<br/>');
        $('#zzsUpProgressBar').css('width', '0%');
        var subHtml = "";
        gfiles = this.files;
        for (var i = 0; i < gfiles.length; i++) {
            var file = gfiles[i];
            if (i % 2 == 0)
                if (i == gfiles.length - 1)
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span><br/><br/>';
                else
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span>';
            else
                subHtml += '<span style="position:absolute;left:55%;"> ' + file.name + ' (大小: ' + file.size +
                    ') </span><br/><br/>';
        }
        $("#zzsUploadModal .modal-body").css("height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $("#zzsUploadModal .modal-body").css("max-height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $(subHtml).appendTo("#zzsFileHolder");
    });
    $("#zzsDoUpload").on('click', function() {
        $(this).attr('disabled', true);
        if ($("#zzsFileHolder").find("span").length === 0) {
            RunPop("提示信息", "", "请选择上传的文件！", "警告", 0.3, 0.2,"","UpLoadFileEmptyMsg");
            $("#UpLoadFileEmptyMsg .modal-dialog").css({"top":"150px","left":(window.screen.availWidth/3) + "px"});
            $("#UpLoadFileEmptyMsg .modal-body").css("height",window.screen.availHeight*0.2);
            return;
        }
        for (var i = 0; i < gfiles.length; i++) {
            zzsUploadFile(gfiles[i], i); // call the zzs function to upload the file
        }
        //$("#zzsUploadModal").modal('hide');
    });
    $("#zzsUploadModal").modal();
    $("#zzsUploadModal").on('hidden.bs.modal', function() {
        $("#zzsUploadModal").remove();
        try {
            mcallback(retValue);
        } catch (e) {
            ;
        }
    });

    function zzsUploadFile(file, num) {
        var fd = new FormData();
        fd.append("upload", 1);
        fd.append("file", file); //$("#zzsFileUp").get(0).files[0]
        $.ajax({
            url: upurl,
            type: "POST",
            async: true,
            processData: false,
            contentType: false,
            data: fd,
            xhr: function() {
                var xhr = new XMLHttpRequest();
                //使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
                xhr.upload.addEventListener('progress', function(e) {
                    // console.log(e);
                    //loaded代表上传了多少
                    //total代表总数为多少
                    var rate = new Number(e.loaded / e.total);
                    var progressRate = ( rate * 100).toFixed(2) + '%';
                    //通过设置进度条的宽度达到效果
                    $('#zzsUpProgressBar').css('width', progressRate);
                    $('#zzsUpProgressBar').html(progressRate);
                })
                return xhr;
            },
            success: function(d) {
                retValue = d; //$.parseJSON(d).data;
                ++num;
                $('#zzsUpProgressBar').css('width', num / gfiles.length * 100 + '%');
                if (num < gfiles.length)
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%');
                else {
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%  上传成功!');
                    $("#zzsUploadModal").modal('hide');
                }
            }
        });
    }
}


function RunUploadFile_Size(title, btnName, upurl, isMulti, acceptType, sWidth, mcallback, maxSize) {
    var retValue='';
    var sWpx = Math.ceil(sWidth * $(document).width());
    var gfiles = null, btnTitle = null;
    if (btnName == '')
        btnTitle = '上传...';
    else
        btnTitle = btnName;
    if (isMulti == true || isMulti == 'true')
        sBody = '<input type="file" id="zzsFileUp" multiple="multiple" accept=' + acceptType + ' class="btn btn-info" style="width:' + Math.floor(sWpx * 0.94) + 'px;"/>';
    else
        sBody = '<input type="file" id="zzsFileUp" accept=' + acceptType + ' class="btn btn-info"/>';
    sBody += '<div id="zzsFileHolder"></div>';
    var sHtml = '<div id="zzsUploadModal" class="modal fade" data-backdrop="false" role="dialog">';
    sHtml += '<div class="modal-dialog" style="width:' + sWpx + 'px;">';
    sHtml += '<div class="modal-content"><div class="modal-header">'
    sHtml += '<button type="button" class="close" data-dismiss="modal">&times;</button>';
    sHtml += ('<h4 class="modal-title">' + title + '</h4></div>');
    sHtml += ('<div class="modal-body" style="height:170px; max-height:170px;">');
    sHtml += sBody;
    sHtml += '<div class="modal-footer">';
    sHtml += '<div id="zzsUpProgressBar" class="progress-bar" role="progressbar" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100" style="width:0%;height:20px;">0%</div><br/>';
    sHtml += '<button type="button" class="btn btn-primary" id="zzsDoUpload" style="margin-top:5px;">' + btnTitle + '</button></div>';
    sHtml += '</div></div></div></div>';
    $(sHtml).appendTo("body");
    $("#zzsFileUp").on('click', function () {
        $("#zzsDoUpload").attr('disabled', false);
    });
    $("#zzsFileUp").on('change', function () {
        $("#zzsDoUpload").attr('disabled', false);
        $("#zzsFileHolder").html('<br/>');
        $('#zzsUpProgressBar').css('width', '0%');
        var subHtml = "";
        gfiles = this.files;
        for (var i = 0; i < gfiles.length; i++) {
            var file = gfiles[i];
            if (i % 2 == 0)
                if (i == gfiles.length - 1)
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size + ') </span><br/><br/>';
                else
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size + ') </span>';
            else
                subHtml += '<span style="position:absolute;left:55%;"> ' + file.name + ' (大小: ' + file.size + ') </span><br/><br/>';
        }
        $("#zzsUploadModal .modal-body").css("height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $("#zzsUploadModal .modal-body").css("max-height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $(subHtml).appendTo("#zzsFileHolder");
    });
    $("#zzsDoUpload").on('click', function () {
        $(this).attr('disabled', true);
        if ($("#zzsFileHolder").find("span").length === 0) {
            RunPop("提示信息", "", "请选择上传的文件！", "警告", 0.3, 0.2,"","UpLoadFileEmptyMsg");
            $("#UpLoadFileEmptyMsg .modal-dialog").css({"top":"150px","left":(window.screen.availWidth/3) + "px"});
            $("#UpLoadFileEmptyMsg .modal-body").css("height",window.screen.availHeight*0.2);
            $("body").css("padding-right","0px");
            return;
        }
        var max = parseInt(parseInt(maxSize) * 1024 * 1024);
        for (var i = 0; i < gfiles.length; i++) {
            if (parseInt(gfiles[i].size) > max) {
                RunPop("提示信息", "", "上传文件的大小不能超过"+maxSize+"M!", "警告", 0.3, 0.2,"","UpLoadFileFailMsg");
                $("#UpLoadFileFailMsg .modal-dialog").css({"top":"150px","left":(window.screen.availWidth/3) + "px"});
                $("#UpLoadFileFailMsg .modal-body").css("height",window.screen.availHeight*0.2);
                $("body").css("padding-right","0px");
                return;
            }
            zzsUploadFile(gfiles[i], i); // call the zzs function to upload the file
        }
    });
    $("#zzsUploadModal").modal();
    $("#zzsUploadModal").on('hidden.bs.modal', function () {
        $("#zzsUploadModal").remove();
        try{
            mcallback(retValue);
        }catch(e){;}
    });

    function zzsUploadFile(file, num) {
        var fd = new FormData();
        fd.append("upload", 1);
        fd.append("file", file); //$("#zzsFileUp").get(0).files[0]
        $.ajax({
            url: upurl,
            type: "POST",
            async: true,
            processData: false,
            contentType: false,
            data: fd,
            xhr: function() {
                var xhr = new XMLHttpRequest();
                //使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
                xhr.upload.addEventListener('progress', function(e) {
                    //loaded代表上传了多少
                    //total代表总数为多少
                    var rate = new Number(e.loaded / e.total);
                    var progressRate = ( rate * 100).toFixed(2) + '%';
                    //通过设置进度条的宽度达到效果
                    $('#zzsUpProgressBar').css('width', progressRate);
                    $('#zzsUpProgressBar').html(progressRate);
                })
                return xhr;
            },
            success: function (d) {
                retValue = d;//$.parseJSON(d).data;
                ++num;
                // $('#zzsUpProgressBar').css('width', num / gfiles.length * 100 + '%');
                if (num < gfiles.length)
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%');
                else {
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%  上传成功!');
                    $("#zzsUploadModal").modal('hide');
                }
            }
        });
    }
}

///form 数据
$.fn.accessFormData = function (myurl, sParas, mcallback) {  //sParas: select & delete must fill,else empty ''
    var form = this;
    var upData = 'oper='+oper +'&';
    if (oper == 'add')
        upData = upData + form.serialize();
    else if (oper == 'edit')
        upData = upData + form.serialize() + sOldValues;
    var thisUrl = myurl.indexOf('?') > 0 ? (myurl + '&' + sParas) : (myurl + '?' + sParas);
    $.ajax({
        url: thisUrl, //myurl + '?' + sParas,
        type: "POST",
        data: upData,
        dataType: "text",
        success: function (d) {
            if (oper == 'select') {
                form.setFormValue($.parseJSON(d).data, mcallback);
            }
            else if (oper == 'edit') {
                try {
                    mcallback($.parseJSON(d).data);
                } catch (e) {;}
            }
            else if (oper == 'add')
                try {
                    mcallback($.parseJSON(d).data);
                } catch (e) {; }
            oper = 'edit';
        }
    });
}

/// form 填写数据
$.fn.setFormValue = function (data, mcallback) {
    sOldValues = "";
    var obj = this;
    $.each(data, function (name, val) {
        var $el = obj.find('input[name="' + name + '"],textarea[name="' + name + '"],select[name="' + name + '"]'),
            type = $el.attr('type');
            
        switch (type) {
            case 'checkbox':
                if ($el.val() == val)
                    $el.prop('checked', true);
                else
                    $el.prop('checked', false);
                break;
            case 'radio':
                $el.filter('[value="' + val + '"]').prop('checked', true);
                $el.filter('[value!="' + val + '"]').prop('checked', false);
                break;
            default:
                $el.val(val);
        }

        if (typeof ($el.attr("data-mark")) != "undefined")
            sOldValues += '&' + name + '_old=' + encodeURI(val) + '-zzs-' + encodeURI($el.attr("data-mark"));     
    });
    try {
        mcallback(data);
    } catch (e) {; }
}

//jqGrid oldValues 
$.fn.saveOldValues = function (rowid) {
    var obj = this;
    jqOldValues = '';
    var jqModel = this.jqGrid("getGridParam", "colModel");
    for (var i = 0; i < jqModel.length; i++) {
        if (jqModel[i].name != 'rn' && jqModel[i].name != 'cb' && jqModel[i].name != 'subgrid' && jqModel[i].name != 'opera') {
            jqOldValues += '&' + jqModel[i].name + '_old=' + this.jqGrid('getCell', rowid, jqModel[i].name).replace(/&|=/g, '') + '-zzs-' + jqModel[i].label.replace(/&|=/g, '');
        }
    }
}

Date.prototype.ToString = function (format) {
    var o = {
        "M+": this.getMonth() + 1,            //月份 
        "d+": this.getDate(),                    //日 
        "H+": this.getHours(),                   //小时 
        "h+": this.getHours(),                   //小时 
        "m+": this.getMinutes(),               //分 
        "s+": this.getSeconds(),               //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds()             //毫秒 
    };
    if (/(y+)/.test(format))
        format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(format))
            format = format.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return format;
}

///Jqgrid全文搜索
var timer;
$("#search_cells").on("keyup", function () {
    var self = this;
    if (timer) {
        clearTimeout(timer);
    }
    timer = setTimeout(function () {
        $("#Grid").jqGrid('filterInput', self.value);
    }, 0);
});


//已发列生成复选框
function signSelected(cellValue, options, row) {
    var op = "";
    if (parseInt(cellValue) == 1){
        op += "<span class=\"glyphicon glyphicon-check\"  style='color: #00bf00'></span>";
    }else {
        op += "<input type=\"checkbox\" disabled=\"true\" value=\"0\">";
    }
    return op;
}




$.fn.filterByText = function (textbox, selectSingleMatch, fn) {
    return this.each(function () {
        var select = this;
        var options = [];
        $(select).find('option').each(function () {
            options.push({ value: $(this).val(), text: $(this).text() });
        });
        $(select).data('options', options);
        $(textbox).bind('change keyup', function () {
            var options = $(select).empty().data('options');
            var search = $.trim($(this).val());
            var regex = new RegExp(search, "gi");
            $.each(options, function (i) {
                var option = options[i];
                if (option.text.match(regex) !== null) {
                    $(select).append(
                        $('<option>').text(option.text).val(option.value)
                    );
                }
            });

            if (selectSingleMatch === true && $(select).children().length === 1) {
                $(select).children().get(0).selected = true;
            }

            if ($.isFunction(fn) && $(select).val()) {
                fn();
            }
        });
    });
}


$(".panel-group .in").prev().addClass("panelCur");
$(".panel-group .panel-heading  a").on("click",function () {
    $(".panel-group .panel-heading").removeClass("panelCur");
    $(this).parents(".panel-heading").addClass("panelCur");
})

//------------------------判断是否不为空---------------------------
function nonNullAndEmpty(obj) {
    return (obj != undefined && obj != null && obj != "");
}

function compareDateEqual(date1, date2) {
    date1 = new Date(date1);
    date2 = new Date(date2);
    if (date1.getFullYear() < date2.getFullYear())
        return true;
    else if (date1.getFullYear() > date2.getFullYear())
        return false;
    else if (date1.getFullYear() === date2.getFullYear()) {
        if (date1.getMonth() < date2.getMonth())
            return true;
        else if (date1.getMonth() > date2.getMonth())
            return false;
        else if (date1.getMonth() === date2.getMonth()) {
            if (date1.getDate() < date2.getDate())
                return true;
            else if (date1.getDate() > date2.getDate())
                return false;
            else if (date1.getDate() === date2.getDate())
                return true;
        }
    }
}


function compareDate(date1, date2) {
    date1 = new Date(date1);
    date2 = new Date(date2);
    if (date1.getFullYear() < date2.getFullYear())
        return true;
    else if (date1.getFullYear() > date2.getFullYear())
        return false;
    else if (date1.getFullYear() === date2.getFullYear()) {
        if (date1.getMonth() < date2.getMonth())
            return true;
        else if (date1.getMonth() > date2.getMonth())
            return false;
        else if (date1.getMonth() === date2.getMonth()) {
            if (date1.getDate() < date2.getDate())
                return true;
            else if (date1.getDate() > date2.getDate())
                return false;
            else if (date1.getDate() === date2.getDate())
                return false;
        }
    }
}


function RunUploadVideo_Size(title, btnName, upurl, isMulti, acceptType, sWidth, mcallback, maxSize) {
    var AjaxUpload = null;
    var retValue = '';
    var sWpx = Math.ceil(sWidth * $(document).width());
    var gfiles = null,
        btnTitle = null;
    if (btnName == '')
        btnTitle = '上传...';
    else
        btnTitle = btnName;
    if (isMulti == true || isMulti == 'true')
        sBody = '<input type="file" id="zzsFileUp" multiple="multiple" accept=' + acceptType +
            ' class="btn btn-info" style="width:' + Math.floor(sWpx * 0.94) + 'px;"/>';
    else
        sBody = '<input type="file" id="zzsFileUp" accept=' + acceptType + ' class="btn btn-info"/>';
    sBody += '<div id="zzsFileHolder"></div>';
    var sHtml = '<div id="zzsUploadModal" class="modal fade" data-backdrop="false" role="dialog">';
    sHtml += '<div class="modal-dialog" style="width:' + sWpx + 'px;">';
    sHtml += '<div class="modal-content"><div class="modal-header">'
    sHtml += '<button type="button" class="close" data-dismiss="modal">&times;</button>';
    sHtml += ('<h4 class="modal-title">' + title + '</h4></div>');
    sHtml += ('<div class="modal-body" style="height:170px; max-height:170px;">');
    sHtml += sBody;
    sHtml += '<div class="modal-footer">';
    sHtml +=
        '<div id="zzsUpProgressBar" class="progress-bar" role="progressbar" aria-valuenow="50" aria-valuemin="0" aria-valuemax="100" style="width:0%;height:20px;">0%</div><br/>';
    sHtml += '<button type="button" class="btn btn-primary" id="zzsDoUpload" style="margin-top:5px;">' + btnTitle +
        '</button></div>';
    sHtml += '</div></div></div></div>';
    $(sHtml).appendTo("body");
    $("#zzsFileUp").on('click', function () {
        $("#zzsDoUpload").attr('disabled', false);
    });
    $("#zzsFileUp").on('change', function() {
        try{
            AjaxUpload.abort();
        }catch(e){
            // console.log(e);
        }
        $("#zzsDoUpload").attr('disabled', false);
        $("#zzsFileHolder").html('<br/>');
        $('#zzsUpProgressBar').css('width', '0%');
        var subHtml = "";
        gfiles = this.files;
        for (var i = 0; i < gfiles.length; i++) {
            var file = gfiles[i];
            try{
                var mp4boxfile = MP4Box.createFile();
                if(file){
                    var reader = new FileReader();
                    var buffer = reader.readAsArrayBuffer(file);
                    reader.onload = function(e) {
                        var arrayBuffer = e.target.result;
                        arrayBuffer.fileStart = 0;
                        mp4boxfile.appendBuffer(arrayBuffer);
                    }
                }
                var is_transcode = 0;
                mp4boxfile.onReady = function(info){
                    var mime = info.mime;
                    var codec = mime.match(/codecs="(\S*),/)[1];
                    if (codec.indexOf('avc') === -1) {
                        is_transcode = 1;
                    }
                    if(is_transcode){
                        $("#zzsUploadModal").modal('hide');
                        var mytext = '抱歉，您提供的视频由于编码格式问题无法在线播放以及在线评审。'+
                        '推荐您从以下链接下载转换工具，转换为浏览器识别的H.264编码格式(转换器默认的输出格式)的mp4视频。'+
                        '请上传转换后的mp4视频。谢谢！<br/>'+
                        '<a title="点击下载转换工具，请耐心等待" style="color: red;" target="_blank" href="https://www.any-video-converter.com/cn/any-video-converter-free.php">'+
                           '转换工具链接'+
                        '</a>';
                        RunPop("提示信息", "", mytext, "提示", 0.35, 0.2,"","UpLoadFileErrorMsg");
                        $("#UpLoadFileErrorMsg .modal-dialog").css({"top":"100px","left":(window.screen.availWidth/3) + "px"});
                        $("#UpLoadFileErrorMsg .modal-body").css("height",window.screen.availHeight*0.2);
                        $("body").css("padding-right","0px");
                    }else {
                        $("#zzsDoUpload").attr("disabled",false)
                    }
                }
            }catch (e) {
                ;
            }
            if (i % 2 == 0)
                if (i == gfiles.length - 1)
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span><br/><br/>';
                else
                    subHtml += '<span style="position:absolute;left:5%;"> ' + file.name + ' (大小: ' + file.size +
                        ') </span>';
            else
                subHtml += '<span style="position:absolute;left:55%;"> ' + file.name + ' (大小: ' + file.size +
                    ') </span><br/><br/>';
        }
        $("#zzsUploadModal .modal-body").css("height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $("#zzsUploadModal .modal-body").css("max-height", Math.ceil(this.files.length / 2) * 40 + 170 + "px");
        $(subHtml).appendTo("#zzsFileHolder");
    });
    $("#zzsDoUpload").on('click', function() {
        $(this).attr('disabled', true);
        if ($("#zzsFileHolder").find("span").length === 0) {
            RunPop("提示信息", "", "请选择上传的文件！", "警告", 0.3, 0.2,"","UpLoadFileEmptyMsg");
            $("#UpLoadFileEmptyMsg .modal-dialog").css({"top":"150px","left":(window.screen.availWidth/3) + "px"});
            $("#UpLoadFileEmptyMsg .modal-body").css("height",window.screen.availHeight*0.2);
            $("body").css("padding-right","0px");
            return;
        }
        var max = maxSize * 1024 * 1024;
        if(isNullOrEmpty(gfiles)) return;
        for (var i = 0; i < gfiles.length; i++) {
            if (gfiles[i].size > max) {
                $("#zzsUploadModal").remove();
                RunPop("提示信息", "", "上传文件的大小不能超过" + maxSize + "M!", "警告", 0.4, 0.2, "", "UpLoadMaxSizeMsg");
                $("#UpLoadFileFailMsg .modal-dialog").css({"top":"150px","left":(window.screen.availWidth/3) + "px"});
                $("#UpLoadFileFailMsg .modal-body").css("height",window.screen.availHeight*0.2);
                $("body").css("padding-right","0px");
                return;
            }
            zzsUploadFile(gfiles[i], i); // call the zzs function to upload the file
        }
    });
    $("#zzsUploadModal").modal();
    $("#zzsUploadModal").on('hidden.bs.modal', function() {
        $("#zzsUploadModal").remove();
        try {
            AjaxUpload.abort();
            mcallback(retValue);
        } catch (e) {
            // console.log(e);
        }
    });
    function zzsUploadFile(file, num) {
        var fd = new FormData();
        fd.append("upload", 1);
        fd.append("file", file); //$("#zzsFileUp").get(0).files[0]
        AjaxUpload = $.ajax({
            url: upurl,
            type: "POST",
            async: true,
            processData: false,
            contentType: false,
            data: fd,
            xhr: function() {
                var xhr = new XMLHttpRequest();
                //使用XMLHttpRequest.upload监听上传过程，注册progress事件，打印回调函数中的event事件
                xhr.upload.addEventListener('progress', function(e) {
                    //loaded代表上传了多少
                    //total代表总数为多少
                    var rate = new Number(e.loaded / e.total);
                    var progressRate = ( rate * 100).toFixed(2) + '%';
                    //通过设置进度条的宽度达到效果
                    $('#zzsUpProgressBar').css('width', progressRate);
                    $('#zzsUpProgressBar').html(progressRate);
                })
                return xhr;
            },
            success: function(d) {
                retValue = d; //$.parseJSON(d).data;
                ++num;
                // $('#zzsUpProgressBar').css('width', num / gfiles.length * 100 + '%');
                if (num < gfiles.length)
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%');
                else {
                    $('#zzsUpProgressBar').html(num / gfiles.length * 100 + '%  上传成功!');
                    $("#zzsUploadModal").modal('hide');
                }
            }
        });
    }
}

