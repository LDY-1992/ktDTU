/**
 * 
 */

// 常量
var Progect_name = "ktDTU";
var Url_GetV = "/" + Progect_name + "/GetVCode";
var Url_Login = "/" + Progect_name + "/customLogin";
var Url_Forget="/" + Progect_name + "/customForget";
// 页面加载完成时执行
$(document).ready(function() {
	ClickCodeImag();
	ClickLogin();
	ClickRegister();
	ForgetPsw();
});

// 验证码图片
function ClickCodeImag() {
	// 点击验证码切换
	$("#img_vcode").click(function() {
		var d = new Date();
		var time = d.getTime();
		$("#img_vcode").attr("src", Url_GetV + "?time=" + time);
	});
}

// 登陆
function ClickLogin() {
	// 点击登陆
	$("#btn_login").click(
			function() {
				$("#btn_login").attr("disabled", true);

				var login_account = $.trim($("#login_account").val());
				var login_psw = $.trim($("#login_psw").val());
				var login_code = $.trim($("#login_code").val());

				if (login_account.length <= 0) {
					$("#btn_login").attr("disabled", false);
					layer.alert('请输入账户!');
					return 0;
				} else if (!isACC(login_account)) {
					$("#btn_login").attr("disabled", false);
					layer.alert('账号必须是字母开头,6-18位的字母数字下划线!');
					return 0;
				}

				if (login_psw.length <= 0) {
					$("#btn_login").attr("disabled", false);
					layer.alert('请输入密码!');
					return 0;
				} else if (!isACC(login_psw)) {
					$("#btn_login").attr("disabled", false);
					layer.alert('密码必须是字母开头,6-18位的字母数字下划线!');
					return 0;
				}

				if (login_code.length <= 0) {
					$("#btn_login").attr("disabled", false);
					layer.alert('请输入验证码!');
					return 0;
				} else if (login_code.length != 4) {
					$("#btn_login").attr("disabled", false);
					layer.alert('验证码为四位数字!');
					return 0;
				}

				layer.msg('正在登陆。。。', {
					icon : 16,
					shade : 0.3,
					time : 0
				});

				// 处理信息
				var json_data = {
					"action" : "login",
					"login_account" : login_account,
					"login_psw" : login_psw,
					"login_code" : login_code
				};

				$.ajax({
					type : "POST",
					url : Url_Login,
					data : JSON.stringify(json_data),
					success : function(data) {
						$("#btn_login").attr("disabled", false);
						layer.closeAll();
						$("#img_vcode").click();
						if (data.length > 3) {
							var substrArray = data.split(",");
							if (substrArray[0] == "106") {
								window.location.href = "custom.html?account="
										+ substrArray[1] + "&d_code="
										+ substrArray[2] + "&num="
										+ substrArray[3];
							}

						} else if (data == "103") {

							layer.alert('验证码不正确，请重新输入!');
						} else if (data == "104") {
							layer.alert('未进行邮箱激活，请查看注册邮箱激活账户!');
						} else {
							layer.alert('账户和密码不正确!');
						}
					},
					error : function(xhr, ajaxOptions, thrownError) {
						$("#btn_register").attr("disabled", false);
						layer.closeAll();
						$("#img_vcode").click();
						layer.alert('未知网络错误!');
					}
				});
			});
}

// 注册
function ClickRegister() {
	// 点击注册
	$("#btn_register").click(function() {

		$("#btn_register").attr("disabled", true);

		var register_account = $.trim($("#register_account").val());
		var register_psw = $.trim($("#register_psw").val());
		var register_cpsw = $.trim($("#register_cpsw").val());
		var register_name = $.trim($("#register_name").val());
		var register_email = $.trim($("#register_email").val());
		var register_addr = $.trim($("#register_addr").val());
		var register_phone = $.trim($("#register_phone").val());

		if (register_account.length <= 0) {
			$("#btn_register").attr("disabled", false);
			layer.alert('请输入账户!');
			return 0;
		} else if (!isACC(register_account)) {
			$("#btn_register").attr("disabled", false);
			layer.alert('账号必须是字母开头,6-18位的字母数字下划线!');
			return 0;
		}

		if (register_psw.length <= 0) {
			$("#btn_register").attr("disabled", false);
			layer.alert('请输入密码!');
			return 0;
		} else if (!isACC(register_psw)) {
			$("#btn_register").attr("disabled", false);
			layer.alert('密码必须是字母开头,6-18位的字母数字下划线!');
			return 0;
		} else if (register_psw != register_cpsw) {
			$("#btn_register").attr("disabled", false);
			layer.alert('两次输入密码不相同!');
			return 0;
		}

		if (register_name.length <= 0) {
			$("#btn_register").attr("disabled", false);
			layer.alert('请输入公司名称!');
			return 0;
		} else if (register_name.length > 40) {
			$("#btn_register").attr("disabled", false);
			layer.alert('公司名称不能超过40个字符!');
			return 0;
		} else if (!isName(register_name)) {
			$("#btn_register").attr("disabled", false);
			layer.alert('公司名称必须是中文英文数字!');
			return 0;
		}

		if (register_email.length <= 0) {
			$("#btn_register").attr("disabled", false);
			layer.alert('请输入公司邮箱!');
			return 0;
		} else if (register_email.length > 40) {
			$("#btn_register").attr("disabled", false);
			layer.alert('公司邮箱不能超过40个字符!');
			return 0;
		} else if (!isEmail(register_email)) {
			$("#btn_register").attr("disabled", false);
			layer.alert('输入邮箱格式不正确!');
			return 0;
		}

		if (register_addr.length <= 0) {
			register_addr = "无";

		} else if (register_addr.length > 40) {
			$("#btn_register").attr("disabled", false);
			layer.alert('公司地址不能超过40个字符!');
			return 0;
		}

		if (register_phone.length <= 0) {
			register_phone = "无";

		} else if (!isAllPhone(register_phone)) {
			$("#btn_register").attr("disabled", false);
			layer.alert('电话号码格式不正确!');
			return 0;
		}

		layer.msg('正在注册。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "register",
			"register_account" : register_account,
			"register_psw" : register_psw,
			"register_name" : register_name,
			"register_email" : register_email,
			"register_addr" : register_addr,
			"register_phone" : register_phone,
		};

		$.ajax({
			type : "POST",
			url : Url_Login,
			data : JSON.stringify(json_data),
			success : function(data) {
				$("#btn_register").attr("disabled", false);
				layer.closeAll();
				console.log("data=" + data);
				if (data == "206") {
					$('#modal_register').modal('toggle');
					layer.alert('注册成功!邮箱验证已经发送，请进入注册邮箱点击验证，验证成功后方可登陆!');

					// 清空注册模态框
					$("#register_account").val("");
					$("#register_psw").val("");
					$("#register_cpsw").val("");
					$("#register_name").val("");
					$("#register_email").val("");
					$("#register_addr").val("");
					$("#register_phone").val("");
				} else if (data == "201") {
					layer.alert('此账户已经注册，如果忘记密码请邮箱找回!');
				} else if (data == "608") {
					layer.alert('此邮箱已经注册，请更换其它未注册邮箱!');
				}else {
					layer.alert('未知错误，注册失败!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				$("#btn_register").attr("disabled", false);
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});
}

// 忘记密码
function ForgetPsw() {
	$("#btn_forget_email").click(function() {
		var forget_mes = $.trim($("#input_forget").val());

		if (forget_mes.length <= 0) {
			layer.alert('请输入账户或者注册邮箱!');
			return;
		} else if (!isEmail(forget_mes)) {
			layer.alert('请输入正确格式的注册邮箱!');
			return;
		}
		
		layer.msg('正在发送邮件。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});
		
		$.ajax({
			type : "GET",
			url : Url_Forget+"?email="+forget_mes,
			success : function(data) {
				layer.closeAll();
				if (data == "202") {
					$('#modal_forget').modal('toggle');
					layer.alert('邮件发送成功，请到邮箱重置密码!');
					
				} else if (data == "609") {
					layer.alert('此邮箱不存在!');
				} else {
					layer.alert('邮箱发送失败!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});
		
	});
}