$(function (){
    var role = $.trim(getGlobalVar("YRole"));
    var userno = $.trim(getGlobalVar("YLoginId"));

    var Address = "/ps/ChangePwd";
    console.log(userno)
    $("#userno_ZY").val(userno);
    $("#userno_ZY").attr("disabled", "disabled");

    changePwdFmValid();
    function changePwdFmValid() {
        $("#changePwdFm_ZY").bootstrapValidator({
            changePwdFm_ZY: 'This value is not valid',
            fields:
                {
                    //初始密码是否为空
                    pwd: {
                        validators: {
                            notEmpty: {
                                message: '请填写初始密码'
                            }
                        }
                    },
                    //修改密码是否为空
                    newPwd: {
                        validators: {
                            notEmpty: {
                                message: '请填写修改密码'
                            },
                            stringLength: {//检测长度
                                min: 6,
                                max: 12,
                                message: '长度必须在6-12之间'
                            },
                            // identical: {//与指定文本框比较内容相同
                            //     field: "pwd",
                            //     message: '两次密码不一样，请重新输入!',
                            // },
                        }
                    },
                    //确认密码是否为空
                    confirmPwd: {
                        validators: {
                            notEmpty: {
                                message: '请确认密码'
                            },
                            stringLength: {//检测长度
                                min: 6,
                                max: 12,
                                message: '长度必须在6-12之间'
                            },
                            identical: {//与指定文本框比较内容相同,若是不同使用different
                                field: "newPwd",
                                message: '两次密码不一样，请重新输入!',
                            },
                        }
                    }
                },
        })
            //验证成功后的事件
            .on('success.form.bv', function (e) {
                //阻止submit动作
                e.preventDefault();
                var $form = $(e.target);
                $form.data('bootstrapValidator');
                uChangePwd()

            });

        //修改密码
        function uChangePwd() {
            const pwd = $.trim($("#pwd_ZY").val());
            const newPwd = $.trim($("#newPwd_ZY").val());
            const confirmPwd = $.trim($("#confirmPwd_ZY").val());
            $.ax(Address+"/ChangePwd",{pwd:pwd, newPwd:newPwd, confirmPwd:confirmPwd},false,"POST","json","",function (d) {
                if (d.msg.indexOf("成功") > -1){
                    RunPop("提示", "","密码修改成功，请重新登录。", "成功", 0.3, 0.2,function () {
                        parent.$('#changeFlag').val('1');
                        parent.$('#ChangePwdModel').modal('hide');

                    },"changeConfirm");
                    $("#changeConfirm button[data-dismiss='modal']").hide();
                } else {
                    RunPop("提示", "",d.data, "提示", 0.3, 0.2);
                }
            });


            // doFunUser("GOEXIT");
        }

        //返回按钮
        $('#btnExit_ZY').click(function (){
            $("#changePwdFm_ZY").data('bootstrapValidator').destroy();
            $("#changePwdFm_ZY").data('bootstrapValidator',null);
            parent.$('#ChangePwdModel').modal('hide');

        })


    }



})