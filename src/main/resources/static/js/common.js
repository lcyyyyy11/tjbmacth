var moduleRecordMsg = "请注意：该模块已过规定操作时间，您是否继续？";
var matchRecordMsg = "请注意：该赛事已过规定操作时间，不能继续再进行操作！";
var moduleRecordMsgY = "请注意：该模块已过规定操作时间，不能继续再进行操作！";
function isCellPhone(phone) {
    var re = /^1(3\d|5\d|7\d|8\d|9\d)\d{8}$/;
    //  var re = /^1\d{10}$/;
    return re.test(phone);
}
function isEmail(email) {
    // var re = /^[\w-]+(\.[\w-]+)*@([\w-]+\.)+[a-zA-Z]+$/;
    var re = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return re.test(email);
}
function onlyNum(str) {
    var re = /^[0-9]*[1-9][0-9]*$/;
    return re.test(str);
}
function isFunction(fn) {
    return Object.prototype.toString.call(fn) === '[object Function]';
}

///身份证验证 没有省份校验
function IdCardValidate(idCard) {
    var idCards = $.trim(idCard.replace(/ /g, ''));
    function isTrueValidateCodeBy18IdCard(aIdCard) {
        var Wi = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1];
        var ValideCode = ['1', '0', '10', '9', '8', '7', '6', '5', '4', '3', '2'];
        var sum = 0;
        var AidCard = aIdCard;
        if (AidCard[17].toLowerCase() === 'x') {
            AidCard[17] = '10';
        }
        for (var i = 0; i < 17; i += 1) {
            sum += Wi[i] * AidCard[i];
        }
        var valCodePosition = sum % 11;
        if (AidCard[17] === ValideCode[valCodePosition]) {
            return true;
        } else {
            return false;
        }
    }

    function isValidityBrithBy18IdCard(idCard18) {
        var year = idCard18.substring(6, 10);
        var month = idCard18.substring(10, 12);
        var day = idCard18.substring(12, 14);
        var tempDate = new Date(year, parseFloat(month) - 1, parseFloat(day));
        // 这里用getFullYear()获取年份，避免千年虫问题
        if (tempDate.getFullYear()
            !== parseFloat(year)
            || tempDate.getMonth()
            !== parseFloat(month) - 1
            || tempDate.getDate()
            !== parseFloat(day)) {
            return false;
        } else {
            return true;
        }
    }

    function isValidityBrithBy15IdCard(idCard15){
        var year = idCard15.substring(6, 8);
        var month = idCard15.substring(8, 10);
        var day = idCard15.substring(10, 12);
        var tempDate = new Date(year, parseFloat(month) - 1, parseFloat(day));
        if (tempDate.getYear()
            !== parseFloat(year) ||
            tempDate.getMonth()
            !== parseFloat(month) - 1 || tempDate.getDate() !== parseFloat(day)) {
            return false;
        } else {
            return true;
        }
    }
    if (idCard.length === 15) {
        return isValidityBrithBy15IdCard(idCards);
    } else if (idCards.length === 18) {
        var idCardS = idCards.split(''); // 得到身份证数组
        if (isValidityBrithBy18IdCard(idCards) && isTrueValidateCodeBy18IdCard(idCardS)) {
            return true;
        } else {
            return false;
        }
    } else {
        return false;
    }
}



/**
 * create by Jaeger 2022-07-28
 * 获取CKEditor中的图片数和图片对应文件大小
 * 返回一个数组:
 * [{base64: "", //图片对应的不带base64头的base64编码
 fileLength: ""}] //图片对应文件流大小，单位为字节
 * @param {String} CKData  CKEDITOR.instances.content.getData()方法获取的数据
 *
 */
function getPicInfofromCK(CKData) {
    let rex = /src="data:image(\S*)"/g;
    let matches = CKData.match(rex);
    let picInfos = [];
    if(Array.isArray(matches) && matches.length > 0){
        for(str of matches){
            let pic = {
                base64: "",
                fileLength: ""
            }
            let rex1 = /;base64,(\S*)/;
            str = str.match(rex1)[1]; //图片对应的不带base64头的base64编码
            pic.base64 = str;
            let equalIndex = str.indexOf('=');
            if(str.indexOf('=')>0){
                str = str.substring(0, equalIndex);
            }
            let strLength = str.length;
            pic.fileLength = parseInt(strLength - (strLength/8) * 2);
            picInfos.push(pic);
        }
    }
    return picInfos;
}







//设置内容
function setEditorContent(id,data) {
    CKEDITOR.instances[id].on('instanceReady',function(e) {
        CKEDITOR.instances[id].setData(decodeURIComponent(data));
    });
    CKEDITOR.instances[id].setData(decodeURIComponent(data));
}

/*****************************************************************
 jQuery Ajax封装通用类
 *****************************************************************/

/**
 * ajax封装
 * url 发送请求的地址
 * data 发送到服务器的数据，json数组存储
 * async 默认值: true。默认设置下，所有请求均为异步请求。如果需要发送同步请求，请将此选项设置为 false。
 * type 请求方式("POST" 或 "GET")， 默认为 "GET"
 * dataType 预期服务器返回的数据类型，常用的如：xml、html、json、text
 * beforeSend 服务器未发送结果回来之前加载缓冲图片
 * successfn 成功回调函数
 * errorfn 失败回调函数
 */
jQuery.ax = function (url, data, async, type, dataType, beforefn, successfn, errorfn,completefn) {
    async = (async == null || async == "" || typeof (async) == "undefined") ? "true" : async;
    type = (type == null || type == "" || typeof (type) == "undefined") ? "post" : type;
    dataType = (dataType == null || dataType == "" || typeof (dataType) == "undefined") ? "json" : dataType;
    data = (data == null || data == "" || typeof (data) == "undefined") ? { "date": new Date().getTime() } : data;
    $.ajax({
        type: type,
        async: async,
        data: data,
        url: url,
        dataType: dataType,
        // beforeSend: function (reqObj, settings) {
        //     reqObj.setRequestHeader('X-SESSION-TOKEN',getGlobalVar("STOKEN"));
        //     // console.log(getGlobalVar("STOKEN"));
        //     if (isFunction(beforefn)) {
        //         beforefn(reqObj, settings);
        //     }
        // },
        success: function (d, textStatus, resObj) {
            if (isFunction(successfn)) {
                successfn(d, textStatus, resObj);
            }
        },
        error: function (resObj, textStatus, errorThrown) {
            if (isFunction(errorfn)) {
                errorfn(resObj, textStatus, errorThrown);
            }
        },
        complete: function (resObj, textStatus) {
            if(isFunction(completefn)) {
                completefn(resObj, textStatus);
            }
        }
    });
};

/**
 * ajax封装
 * url 发送请求的地址
 * data 发送到服务器的数据，数组存储，如：{"date": new Date().getTime(), "state": 1}
 * successfn 成功回调函数
 * errorfn 失败回调函数
 */
jQuery.axse = function (url, data, beforefn, successfn, errorfn) {
    data = (data == null || data == "" || typeof (data) == "undefined") ? { "date": new Date().getTime() } : data;
    $.ajax({
        type: "post",
        data: data,
        url: url,
        dataType: "json",
        // beforeSend: function (reqObj, settings) {
        //     reqObj.setRequestHeader('X-SESSION-TOKEN',getGlobalVar("STOKEN"));
        //     if (isFunction(beforefn)) {
        //         beforefn(reqObj, settings);
        //     }
        // },
        success: function (d, textStatus, resObj) {
            if (isFunction(successfn)) {
                successfn(d, textStatus, resObj);
            }
        },
        error: function (resObj, textStatus, errorThrown) {
            if (isFunction(errorfn)) {
                errorfn(resObj, textStatus, errorThrown);
            }
            else if (errorfn == '') {
                alert('数据加载失败');
            }
        }
    });
};

// Array.prototype.indexValue = function (ar) {
//     for (var i = 0,max=this.length; i < max; i++) {
//         if (this[i] == ar) {
//             return i;
//         }
//     }
//     return -1;
// };

Storage.prototype.setExpire = function (key, value, expire){
    var obj = {
        data: value,
        time: Date.now(),
        expire: expire
    };
    try {
        // console.log("size:"+JSON.stringify(obj).length);
        //localStorage 设置的值不能为对象,转为json字符串
        localStorage.setItem(key, JSON.stringify(obj));
    }catch (e) {
        if(e.name.toUpperCase().indexOf('QUOTA')>=0){
            localStorage.clear();
            localStorage.setItem(key, JSON.stringify(obj));
        }
    }
};

Storage.prototype.getExpire = function (key) {
    var val = localStorage.getItem(key);
    if (!val) {
        return val;
    }
    val = JSON.parse(val);
    if (Date.now() - val.time > val.expire) {
        localStorage.removeItem(key);
        return null;
    }
    return val.data;
};

//引用外部文件（目前只支持css、js）
function loadFile(url, callback) {
    if (/\.js$/i.test(url)) {
        var _done = false;
        var _script = document.createElement("script");
        _script.type = "text/javascript";
        _script.language = "javascript";
        _script.src = url;
        _script.onload = _script.onreadystatechange = function () {
            if (!_done && (!_script.readyState || _script.readyState == "loaded" || _script.readyState == "complete")) {
                _done = true;
                _script.onload = _script.onreadystatechange = null;
                if (callback) {
                    callback.call(_script);
                }
            }
        }
        document.getElementsByTagName("head")[0].appendChild(_script);
    }
    else if (/\.css$/i.test(url)) {
        var _link = document.createElement("link");
        _link.rel = "stylesheet";
        _link.type = "text/css";
        _link.media = "screen";
        _link.href = url;
        document.getElementsByTagName("head")[0].appendChild(_link);
        if (callback) {
            callback.call(_link);
        }
    }
}
//获取本嵌入文件的目录
//如：<script src="Js/lwCommon.js" type="text/javascript"></script>
//则传入参数 fileName:Common.js  tag:script
//返回值:Js/
function getFolderPath(fileName, tagName) {
    //若fileName为空，则默认查找script标签
    if (!fileName) fileName = "common.js";
    //若tagName为空，则默认查找script标签
    if (!tagName) tagName = "script";

    var tags = document.getElementsByTagName(tagName);
    for (var i = 0; i < tags.length; i++) {
        var src = tags[i].src;
        if (!src) { continue; }

        var _tempIdx = src.lastIndexOf("/") + 1;
        var _tempFileName = src.substring(_tempIdx).toLowerCase();
        var _fileName = fileName.toLowerCase();
        if (_tempFileName == _fileName || _tempFileName.indexOf(_fileName) > -1) {
            return src.substr(0, _tempIdx);
        }
    }
    return "";
}

function getUrlParameter1(name) {
    var href = window.location.href;
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var val = window.location.search.match(new RegExp("[\?\&]" + name + "=([^\&]*)(\&?)", "i"));
    return val ? decodeURIComponent(val[1].replace(/\+/g, " ")) : val;
}

function getGlobalVar(name) {
    // return window.localStorage[name];
    return window.localStorage.getExpire(name);
}

function setGlobalVar(name,val) {
    return window.localStorage.setExpire(name,val,24*3600*1000);
}

function ExcelExport(url) {
    var xhr = new XMLHttpRequest();		//定义http请求对象
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xhr.send();
    xhr.responseType = "blob";  // 返回类型blob
    xhr.onload = function() {   // 定义请求完成的处理函数，请求前也可以增加加载框/禁用下载按钮逻辑
        if (this.status===200) {
            var blob = this.response;
            var temp = xhr.getResponseHeader("content-disposition").split(";")[1].split("filename=")[1];
            var fileName = decodeURIComponent(temp);
            var reader = new FileReader();
            reader.readAsDataURL(blob);  // 转换为base64，可以直接放入a标签href
            reader.onload=function (e) {
                // console.log(e);			//查看有没有接收到数据流
                // 转换完成，创建一个a标签用于下载
                var a = document.createElement('a');
                // .substring(1,fileName.length-1);
                a.download=fileName;			//自定义下载文件名称
                a.href = e.target.result;
                $("body").append(a);    // 修复firefox中无法触发click
                a.click();
                $(a).remove();
            }
        }
        else{
            alert("抱歉。系统繁忙，请稍后再试!");
        }
    }
}

function exportFileProgress(url) {
    // 转换完成，创建一个a标签用于下载
    var a = document.createElement('a');
    a.href = url;
    $("body").append(a);    // 修复firefox中无法触发click
    a.click();
    $(a).remove();
}

CheckBrowser();
function CheckBrowser() {
    ua = navigator.userAgent;
    ua = ua.toLocaleLowerCase();
    if (ua.match(/msie/) != null || ua.match(/trident/) != null) {
        browserType = "IE";
        //检测ie11.0了！
        browserVersion = ua.match(/msie ([\d.]+)/) != null ? ua.match(/msie ([\d.]+)/)[1] : ua.match(/rv:([\d.]+)/)[1];
        // console.log("ie ie ie ie");
        // console.log("browserVersion："+browserVersion);
        document.write("<pre style='text-align:center;color:#fff;background-color:#0cc; height:100%;border:0;position:fixed;top:0;left:0;width:100%;z-index:1234'><h2 style='padding-top:200px;margin:0;font-size: 30px;line-height: 40px;'><strong>系统检测到您使用的浏览器版本过低，<br/>为达到更好的体验效果请升级您的浏览器<br/></strong></h2><h2>推荐使用:<a href='https://www.baidu.com/s?ie=UTF-8&wd=%E8%B0%B7%E6%AD%8C%E6%B5%8F%E8%A7%88%E5%99%A8' target='_blank' style='color:blue;'>谷歌</a>,<a href='https://www.baidu.com/s?ie=UTF-8&wd=%E7%81%AB%E7%8B%90%E6%B5%8F%E8%A7%88%E5%99%A8' target='_blank' style='color:blue;'>火狐</a>,其他双核极速模式</h2><h2 style='line-height: 40px;'><strong>如果您使用的是双核浏览器,请切换到极速模式访问<br/></strong></h2></pre>");
    } else if (ua.match(/firefox/) != null) {
        browserType = "火狐";
        // console.log("firefox");
    } else if (ua.match(/opera/) != null) {
        browserType = "欧朋";
        // console.log("opera");
    } else if (ua.match(/chrome/) != null) {
        browserType = "谷歌";
        // console.log("chrome");
    } else if (ua.match(/safari/) != null) {
        browserType = "Safari";
        // console.log("Safari");
    }
    // var arr = new Array(browserType, browserVersion);
    // return arr;
}

var GLoginHtml = '/LogIn.html';
function doFunUser(puser) {
    var UserID = getGlobalVar("YLoginId");
    var UserRole = getGlobalVar("YRole");
    var Matid = getGlobalVar("YMatid");
    $.ax("/ps/user/checkUserVerify/checkUser?puser="+puser+"&UserID="+UserID+"&UserRole="+UserRole+"&Matid="+Matid, {}, true, 'GET', "text", function (reqObj, settings) { // beforeSend
    }, function (d,textStatus, resObj) { // success
        if(puser == "GOEXIT"){
            window.localStorage.clear();
            window.document.location = GLoginHtml;
        }else if(puser == "ONLINEMARK"){
            setTimeout("doFunUser('ONLINEMARK')", 120000);
        }
    }, function (resObj, textStatus, errorThrown) { // error
        console.log(resObj.responseText);
    },function (resObj, textStatus) { // complete
    });
}

// 若未携带token或无存储，则退出到登录界面
if(window.location.href.indexOf("LogIn.html") == -1){
    if(window.location.href.indexOf("ServiceIndex.html") > -1 || window.location.href.indexOf("MainFrm.html") > -1){
        var YLoginId = getGlobalVar("YLoginId");
        if(!YLoginId){
            window.document.location = GLoginHtml;
        }
        else{
            doFunUser("ONLINEMARK");
        }
    }else{
        if(!getGlobalVar("YLoginId")){
            window.document.location = GLoginHtml;
        }
        else{
            doFunUser("ONLINEMARK");
        }
    }
}

// 退出登录
$("body").on("click",".uexit",function () {
    var r = confirm("您确定退出系统吗?");
    if (r == true) {
        doFunUser("GOEXIT");
    }
});

// -2-已退回；-1-材料审核未通过；0-未提交；1-信息已提交；2-报名审核通过；3-材料已提交；4-材料审核通过；
// 5-已签到；6-现场比赛中；7-现场比赛结束；8-教学反思中；9-比赛完成；
function getStatus (mpstatus) {
    if (mpstatus == '-2') {
        return "已退回";
    }
    else if (mpstatus == '-1') {
        return "材料审核未通过";
    }
    else if (mpstatus == '0') {
        return "未提交";
    }
    else if (mpstatus == '1') {
        return "信息已提交";
    }
    else if (mpstatus == '2') {
        return "报名审核通过";
    }
    else if (mpstatus == '3') {
        return "材料已提交";
    }
    else if (mpstatus == '4') {
        return "材料审核通过";
    }
    else if (mpstatus == '5') {
        return "已签到";
    }
    else if (mpstatus == '6') {
        return "现场比赛中";
    }
    else if (mpstatus == '7') {
        return "现场比赛结束";
    }
    else if (mpstatus == '8') {
        return "教学反思中";
    }
    else if (mpstatus == '9') {
        return "比赛完成";
    }
}

