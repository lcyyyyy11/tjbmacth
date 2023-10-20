clearListCookies();
$(function () {

    var addr = "/ps/user/loginVerify";



    loadValidCode();

    formValidator();


    // 更换验证码
    $("#ReloadCode").click(function () {
        loadValidCode();
    });

    function loadValidCode(){
        var img = document.getElementById('ImgValidCode');
        var url = img.getAttribute('authSrc');
        var request = new XMLHttpRequest();
        request.responseType = 'blob';
        request.open('get', url, true);
        request.onreadystatechange = function (e){
            if ((request.readyState === XMLHttpRequest.DONE) && request.status === 200) {
                img.src = URL.createObjectURL(request.response);
                img.onload = function () {
                    URL.revokeObjectURL(img.src);
                }
            }
        };
        request.send(null);
    }

    function formValidator(){
        $("#loginform").bootstrapValidator({
            message: 'This value is not valid',
            feedbackIcons: {
                valid: 'glyphicon glyphicon-ok text-white',
                invalid: 'glyphicon glyphicon-remove text-white',
                validating: 'glyphicon glyphicon-refresh text-white'
            },
            fields:
                {
                    User: {
                        validators: {
                            notEmpty: {
                                message: '请填写用户名'
                            }
                        }
                    },
                    Pwd: {
                        validators: {
                            notEmpty: {
                                message: '请填写密码'
                            }
                        }
                    },
                    VerifyCode: {
                        threshold:4, // 自个字符请求服务器
                        validators: {
                            notEmpty: {
                                message: '请填写验证码'
                            },
                            // 1、自定义校验规则 OK
                            callback: {
                                message: '验证码错误',
                                callback:function(value, validator){
                                    if(value){
                                        return getVerCodeFlag(value);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
        })
        //验证成功后的事件
        .on('success.form.bv', function(e) {
            //阻止submit动作
            e.preventDefault();
            var $form = $(e.target);
            $form.data('bootstrapValidator');
            uLogin();
        });
    }

    function getVerCodeFlag(val){
        var t;
        $.ajax({
            type: 'POST',
            url: addr + "/validCode",
            data:{
                ImgCode: val.trim()
            },
            async: false,
            dataType: "json",
            success: function (d) {
                t = (d.valid);
            }
        });

        return t;
    }

    function uLogin(){
        var encryptObj=new JSEncrypt();
        // loadValidCode();
        // 1、获取公钥
        $.ax( addr + "/public_key", {}, true, 'POST', "json", '', function (d,textStatus, resObj) { // success
            if(d["PublicKey"]) {
                var publicKey = d["PublicKey"];
                var timeStamp = d["TimeStamp"];
                var token = d["token"];
                encryptObj.setPublicKey(publicKey);
                var user = $.trim($("#User").val());
                var psw =  $.trim($("#Pwd").val());
                window.localStorage.clear();
                $.ax(addr + "/login", {"user": user,"passWord":encryptObj.encrypt(psw),"TimeStamp":timeStamp,"token":token}, false, 'POST', "text", function (reqObj, settings) { // beforeSend
                }, function (d,textStatus, resObj) { // success
                    var msg = $.parseJSON(d).msg;
                    if(msg.indexOf("成功") > -1){
                        setGlobalVar("YUserName",decodeURI(resObj.getResponseHeader("YUserName")));
                        setGlobalVar("YLoginId",resObj.getResponseHeader("YLoginId"));
                        setGlobalVar("YRole",resObj.getResponseHeader("YRole"));
                        setGlobalVar("YRoleName",decodeURI(resObj.getResponseHeader("YRoleName")));
                        setGlobalVar("YMatid",decodeURI(resObj.getResponseHeader("YMatid")));
                        eval($.parseJSON(d).data);
                    }else{
                        if (isNumber($.parseJSON(d).data)) {
                            var curCount1 = 180 - $.parseJSON(d).data;
                            countdown(curCount1);
                            RunPop("提示信息", "", "抱歉！当前用户在线或非正常退出！如果非正常退出，请等待<span id='countdowntext' style='color: #ff0000;'>" + curCount1 + "</span>秒后再登录！", "提示", 0.4, 0.2, function () {
                                clearInterval(InterValObj1);
                            }, 'Countdown');
                            $("#CountdownConfirm").siblings("button").click(function () {
                                clearInterval(InterValObj1);
                            })
                        // RunPop("提示信息", "", $.parseJSON(d).data, "提示", 0.4, 0.2,"","U1");
                        // $("#loginBtn").attr("disabled",false);
                        // loadValidCode();
                        $("#loginform").data('bootstrapValidator').updateStatus('VerifyCode', 'callback').validateField('VerifyCode');
                    }else {

                            RunPop("提示信息", "", $.parseJSON(d).data, "提示", 0.4, 0.2, function () {
                                $("#loginform").data('bootstrapValidator').updateStatus('VerifyCode', 'callback').validateField('VerifyCode');
                                loadValidCode();
                                $("#loginBtn").attr("disabled",false);
                            }, "U1");
                        }}
                }, function (resObj, textStatus, errorThrown) { // error
                    alert(resObj.responseText);
                    // $("#loginBtn").attr("disabled",false);
                },function (resObj, textStatus) { // complete
                });
            }else{
                alert(resObj.responseText);
                $("#loginBtn").attr("disabled",false);
            }
        }, function (resObj, textStatus, errorThrown) { // error
            alert(resObj.responseText);
            $("#loginBtn").attr("disabled",false);
        },function (resObj, textStatus) { // complete
        });
    }
    function countdown(time) {
        InterValObj1 = setInterval(function () {
            if (time <= 1) {
                clearInterval(InterValObj1);//停止计时器
                $('#Countdown').hide();
            } else {
                time = time - 1;
                $('#countdowntext').text(time)
            }
        }, 1000);
    }
});