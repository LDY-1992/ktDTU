/**
 * 客户管理界面
 */
// 常量
var Progect_name = "ktDTU";
var Session_URL = "/" + Progect_name + "/SessionCheck";
var Device_URL = "/" + Progect_name + "/cusDevice";
var Group_URL = "/" + Progect_name + "/cusGroup";
var Custom_URL = "/" + Progect_name + "/modifyCustom";
var GetEmailCode_URL = "/" + Progect_name + "/GetEmailCode";

var table_loading_gif = '<tr><td class="kt-table-loading" colspan="20"><img src="pic/loading.gif" alt="some_text"></td></tr>';

var custom_account;
var d_code;
var num = null;

// layui 对象
var layer;
var laypage;
var laypage_group;
var laypage_bool = true;
var laypage_group_bool = true;

var page_max_row = 16;

// 检查登陆
function CheckLogin() {
	$.get(Session_URL, function(data, status) {
		console.log("数据: " + data + "\n状态: " + status);
		if (status == "success") {
			if (data == "1") {

			} else {
				window.location.href = "DtuLogin.html";
			}
		} else {
			window.location.href = "DtuLogin.html";
		}
	});
}

// 获取url中的参数
function getUrlParam(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); // 构造一个含有目标参数的正则表达式对象
	var r = window.location.search.substr(1).match(reg); // 匹配目标参数
	if (r != null)
		return unescape(r[2]);
	return null; // 返回参数值
}

// 检查登陆
CheckLogin();
// 加载完毕
$(document).ready(function() {
	// UI操作
	layui.use('layer', function() { // 初始化layer
		layer = layui.layer;
	});
	layui.use('laypage', function() { // 初始化laypage
		laypage = layui.laypage;
		laypage_group = layui.laypage;
	});
	getAccount(); // 设置账户
	setSideBar(); // 设置侧边框
	setDeviceSearch(); // 设备搜索框
	selectAllDeviceTab();// 设置设备列表全选
	selectAllGroup_side();// 设置透传组设备选择表全选
	setCusMes(); // 设置用户资料
	// 设备管理
	handle_device();
	// 透传组管理
	handle_group();
});

function setSideBar() {
	$("#a_tab_home").click(function() {
		$("#li_tab_d").attr("class", "");
		$("#li_tab_g").attr("class", "");
		$("#li_tab_v").attr("class", "");
	});
}

// 设置用户资料
function setCusMes() {
	// 修改用户资料
	$("#a_modify_cus_mes").click(function() {
		layer.msg('正在获取用户信息。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		$.ajax({
			type : "GET",
			url : Custom_URL + "?type=all&key=" + custom_account,
			dataType : "json",
			success : function(result) {
				layer.closeAll();
				var status = result.status;
				if (status == "606") {
					var data = result.data;
					$("#input_modify_account").val(data.account);
					$("#input_modify_name").val(data.cor_name);
					$("#input_modify_email").val(data.cor_email);
					$("#input_modify_addr").val(data.cor_addr);
					$("#input_modify_phone").val(data.cor_phone);
				} else {

				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});
	});

	// 获取邮箱验证码
	$("#btn_get_e_code").click(function() {
		layer.msg('正在获取验证码。。。', {
			icon : 16,
			shade : 0,
			time : 0
		});

		$.ajax({
			type : "GET",
			url : GetEmailCode_URL,
			dataType : "json",
			success : function(result) {
				layer.closeAll();
				var status = result.status;
				if (status == "202") {
					layer.alert('邮箱验证码发送成功，请到邮箱查看!');
					$('#btn_get_e_code').attr("disabled", true);
					var t_n = 60;
					var kt_timer = setInterval(function() {
						t_n = t_n - 1;
						$('#btn_get_e_code').text(t_n);
						if (t_n == 0) {
							clearInterval(kt_timer);
							$('#btn_get_e_code').attr("disabled", false);
							$('#btn_get_e_code').text("获取验证码");
						}
					}, 1000);
				} else {
					layer.alert('邮箱验证码发送失败，请稍后重试!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});
	});

	// 点击提交
	$("#btn_submit_modify_cus").click(function() {

		var register_name = $.trim($("#input_modify_name").val());
		var register_email = $.trim($("#input_modify_email").val());
		var register_addr = $.trim($("#input_modify_addr").val());
		var register_phone = $.trim($("#input_modify_phone").val());

		if (register_name.length <= 0) {
			layer.alert('请输入公司名称!');
			return 0;
		} else if (register_name.length > 40) {
			layer.alert('公司名称不能超过40个字符!');
			return 0;
		} else if (!isName(register_name)) {
			layer.alert('公司名称必须是中文英文数字!');
			return 0;
		}

		if (register_email.length <= 0) {
			layer.alert('请输入公司邮箱!');
			return 0;
		} else if (register_email.length > 40) {
			layer.alert('公司邮箱不能超过40个字符!');
			return 0;
		} else if (!isEmail(register_email)) {
			layer.alert('输入邮箱格式不正确!');
			return 0;
		}

		if (register_addr.length <= 0) {
			register_addr = "无";

		} else if (register_addr.length > 40) {
			layer.alert('公司地址不能超过40个字符!');
			return 0;
		}

		if (register_phone.length <= 0) {
			register_phone = "无";

		} else if (!isAllPhone(register_phone)) {
			layer.alert('电话号码格式不正确!');
			return 0;
		}

		layer.msg('正在提交。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "modify_cus",
			"modify_name" : register_name,
			"modify_email" : register_email,
			"modify_addr" : register_addr,
			"modify_phone" : register_phone,
		};

		$.ajax({
			type : "POST",
			url : Custom_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#modal_modify_custom').modal('toggle');
					layer.alert('用户信息修改成功！');

					// 清空模态框
					$("#input_modify_account").val("");
					$("#input_modify_name").val("");
					$("#input_modify_email").val("");
					$("#input_modify_addr").val("");
					$("#register_phone").val("");
				} else {
					layer.alert('提交失败，请稍后重试!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});

	// 点击提交密码
	$("#btn_modify_psw").click(function() {
		var old_psw = $.trim($("#input_old_psw").val());
		var new_psw = $.trim($("#input_new_psw").val());
		var dnew_psw = $.trim($("#input_new_psw_d").val());

		if (old_psw.length <= 0) {
			layer.alert('请输入原始密码!');
			return 0;
		} else if (!isACC(old_psw)) {
			layer.alert('密码必须是字母开头,6-18位的字母数字下划线!');
			return 0;
		}

		if (new_psw.length <= 0) {
			layer.alert('请输入密码!');
			return 0;
		} else if (!isACC(new_psw)) {
			layer.alert('密码必须是字母开头,6-18位的字母数字下划线!');
			return 0;
		} else if (new_psw != dnew_psw) {
			layer.alert('两次输入新密码不相同!');
			return 0;
		}

		layer.msg('正在提交。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "modify_psw",
			"old_psw" : old_psw,
			"new_psw" : new_psw
		};

		$.ajax({
			type : "POST",
			url : Custom_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#modal_modify_psw').modal('toggle');
					layer.alert('用户密码修改成功！');

					// 清空模态框
					$("#input_old_psw").val("");
					$("#input_new_psw").val("");
					$("#input_new_psw_d").val("");
				} else if (data.status == "102") {
					layer.alert('原密码错误，提交失败!');
				} else {
					layer.alert('提交失败，请稍后重试!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});

	$("#btn_modify_d_psw").click(function() {
		var new_psw = $.trim($("#input_default_d_psw").val());

		if (new_psw.length != 8) {
			layer.alert('设备密码必须8位字符!');
			return 0;
		} else if (!isNumberAndE(new_psw)) {
			layer.alert('设备密码必须是数字字母!');
			return 0;
		}

		layer.msg('正在提交。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "modify_d_psw",
			"new_psw" : new_psw
		};

		$.ajax({
			type : "POST",
			url : Custom_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#modal_modify_d_psw').modal('toggle');
					layer.alert('设备默认密码修改成功！');

					// 清空模态框
					$("#input_default_d_psw").val("");
				} else {
					layer.alert('提交失败，请稍后重试!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});
	});
}

// 获取设备表
function getDeviceList(key, val, p1, p2) {
	$("#table_device tbody").empty();
	$("#table_device tbody").append(table_loading_gif);
	$.ajax({
		type : "GET",
		url : Device_URL + "?type=" + key + "&val=" + val + "&p1=" + p1
				+ "&p2=" + p2,
		dataType : "json",
		success : function(result) {

			var status = result.status;
			if (status == "606") {
				var to_tab = result.to_tab;
				if (to_tab == "device") {
					setDeviceTable(result, key, val);
				} else if (to_tab == "group") {
					setGroupSelectDeviceTab(result);
				}

			}
		},
		error : function(xhr, ajaxOptions, thrownError) {
			$("#table_device tbody").empty();
			layer.alert('未知网络错误!');
		}
	});
}

// 获取透传组表
function getGroupList(key, val, p1, p2) {
	$("#table_group tbody").empty();
	$("#table_group tbody").append(table_loading_gif);
	$.ajax({
		type : "GET",
		url : Group_URL + "?type=" + key + "&val=" + val + "&p1=" + p1 + "&p2="
				+ p2,
		dataType : "json",
		success : function(result) {

			var status = result.status;
			if (status == "606") {
				var to_tab = result.to_tab;
				if (to_tab == "group") {
					setGroupTable(result);
				}
			} else {
				layer.alert('获取透传组列表失败!');
			}
		},
		error : function(xhr, ajaxOptions, thrownError) {
			$("#table_group tbody").empty();
			layer.alert('未知网络错误!');
		}
	});
}

// 删除设备
function deleteDevice(t, k) {
	var json_data;
	if (t == "single") {
		// 处理信息
		json_data = {
			"action" : "delete_single",
			"num" : k,
		};

	} else if (t == "list") {
		json_data = {
			"action" : "delete_list",
			"num" : k,
		};
	}

	$.ajax({
		type : "POST",
		url : Device_URL,
		data : JSON.stringify(json_data),
		dataType : "json",
		success : function(data) {
			layer.closeAll();
			if (data.status == "606") {
				laypage_bool = true;
				$("#input_device_box").prop('checked', false);
				getDeviceList("all", "", "1", page_max_row);
			} else {
				layer.alert('删除设备失败!');
			}
		},
		error : function(xhr, ajaxOptions, thrownError) {
			layer.closeAll();
			layer.alert('未知网络错误!');
		}
	});
}

// 删除透传组
function deleteGroup(t, k) {
	var json_data;
	if (t == "single") {
		// 处理信息
		json_data = {
			"action" : "delete_single",
			"num" : k,
		};
	} else if (t == "list") {
		json_data = {
			"action" : "delete_list",
			"num" : k,
		};
	}

	$.ajax({
		type : "POST",
		url : Group_URL,
		data : JSON.stringify(json_data),
		dataType : "json",
		success : function(data) {
			layer.closeAll();
			if (data.status == "606") {
				laypage_group_bool = true;
				$("#input_group_box").prop('checked', false);
				getGroupList("all", "", "1", page_max_row);
			} else {
				layer.alert('删除透传组失败!');
			}
		},
		error : function(xhr, ajaxOptions, thrownError) {
			layer.closeAll();
			layer.alert('未知网络错误!');
		}
	});
}

// 设置设备表格
function setDeviceTable(result, key, val) {
	$("#table_device tbody").empty();
	var data = result.data;
	for (i in data) {

		var sta = "";
		if (data[i].tab_status == "离线") {
			sta = '<p class="text-danger">离线</p>';
		} else {
			sta = '<p class="text-primary">在线</p>';
		}

		var tr_content = '<tr><td><input type="checkbox"></td><td>'
				+ data[i].tab_num
				+ '</td><td>'
				+ data[i].tab_name
				+ '</td><td>'
				+ data[i].tab_psw
				+ '</td><td>'
				+ sta
				+ '</td><td><a id="a_tab_d_check">点击查看</a></td><td><a id="a_tab_d_modify">修改</a>&nbsp;&nbsp;&nbsp;<a id="a_tab_d_delete">删除</a></td></tr>';
		$("#table_device tbody").append(tr_content);
	}
	if (laypage_bool) {
		laypage.render({
			elem : 'div_device_page',
			count : result.count,
			limit : page_max_row,
			jump : function(obj, first) {
				if (!first) {
					getDeviceList(key, val, obj.curr, obj.limit);
				}
			}
		});
		laypage_bool = false;
	}
	$("#table_device tbody").off();
	$('#table_device tbody').on('click', '#a_tab_d_delete', function() {

		var tr = $(this).parents("tr");
		var th = tr.find("td");
		var cars = new Array();
		var n = 0;
		th.each(function() {
			var ths = $(this);
			cars[n] = ths.text();
			n++;
		});
		layer.open({
			title : '删除设备',
			btn : [ '确定', '取消' ],
			yes : function(index, layero) {
				layer.closeAll();
				layer.msg('正在删除设备。。。', {
					icon : 16,
					shade : 0.3,
					time : 0
				});
				deleteDevice("single", cars[1]);

			},
			content : '是否要删除编号为【' + cars[1] + '】名称为【' + cars[2] + '】的设备吗？'
		});

	});

	$('#table_device tbody').on('click', '#a_tab_d_check', function() {

		// 清空
		$('#lable_d_id').text("");
		$('#lable_d_name').text("");
		$('#lable_d_psw').text("");
		$('#lable_d_status').text("");
		$('#lable_d_c_time').text("");
		$('#lable_d_m_time').text("");
		$('#lable_d_mes').text("");

		$('#modal_device_detail').modal('toggle');

		var tr = $(this).parents("tr");
		var th = tr.find("td");
		var cars = new Array();
		var n = 0;
		th.each(function() {
			var ths = $(this);
			cars[n] = ths.text();
			n++;
		});

		layer.msg('正在查询。。。', {
			icon : 16,
			shade : 0,
			time : 0
		});

		$.ajax({
			type : "GET",
			url : Device_URL + "?type=single&val=" + cars[1],
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					var jo = data.data;
					$('#lable_d_id').text(jo.tab_num);
					$('#lable_d_name').text(jo.tab_name);
					$('#lable_d_psw').text(jo.tab_psw);
					$('#lable_d_status').text(jo.tab_status);
					$('#lable_d_c_time').text(jo.c_time);
					$('#lable_d_m_time').text(jo.m_time);
					$('#lable_d_mes').text(jo.mes);

				} else {
					layer.alert('获取设备信息失败!');
					$('#modal_device_detail').modal('toggle');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});

	$('#table_device tbody').on('click', '#a_tab_d_modify', function() {

		// 清空
		$('#input_m_m_d_num').val("");
		$('#input_m_m_d_name').val("");
		$('#input_m_m_d_psw').val("");
		$('#input_m_m_d_mes').val("");

		$('#modal_modify_device').modal('toggle');

		var tr = $(this).parents("tr");
		var th = tr.find("td");
		var cars = new Array();
		var n = 0;
		th.each(function() {
			var ths = $(this);
			cars[n] = ths.text();
			n++;
		});

		layer.msg('正在查询。。。', {
			icon : 16,
			shade : 0,
			time : 0
		});

		$.ajax({
			type : "GET",
			url : Device_URL + "?type=single&val=" + cars[1],
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					var jo = data.data;
					$('#input_m_m_d_num').val(jo.tab_num);
					$('#input_m_m_d_name').val(jo.tab_name);
					$('#input_m_m_d_psw').val(jo.tab_psw);
					$('#input_m_m_d_mes').text(jo.mes);

				} else {
					layer.alert('获取设备信息失败!');
					$('#modal_modify_device').modal('toggle');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});
}

// 设置透传组表格
function setGroupTable(result) {
	$("#table_group tbody").empty();
	var data = result.data;
	for (i in data) {
		var tr_content = '<tr><td><input type="checkbox"></td><td>'
				+ data[i].tab_num
				+ '</td><td>'
				+ data[i].tab_name
				+ '</td><td>'
				+ data[i].a_account
				+ '</td><td>'
				+ data[i].b_account
				+ '</td><td><a id="a_tab_g_detail">点击查看</a></td><td><a id="a_tab_g_modify">修改</a>&nbsp;&nbsp;&nbsp;<a id="a_tab_g_delete">删除</a></td></tr>';
		$("#table_group tbody").append(tr_content);
	}
	if (laypage_group_bool) {
		laypage_group.render({
			elem : 'div_group_page',
			count : result.count,
			limit : page_max_row,
			jump : function(obj, first) {
				if (!first) {
					getGroupList("all", "", obj.curr, obj.limit);
				}
			}
		});
		laypage_group_bool = false;
	}
	$("#table_group tbody").off();

	$('#table_group tbody').on(
			'click',
			'#a_tab_g_detail',
			function() {

				$('#input_m_s_g_id').val("");
				$('#input_m_s_g_name').val("");
				$('#text_s_a_side').val("");
				$('#text_s_b_side').val("");

				var tr = $(this).parents("tr");
				var th = tr.find("td");
				var cars = new Array();
				var n = 0;
				th.each(function() {
					var ths = $(this);
					cars[n] = ths.text();
					n++;
				});

				$('#modal_group_detail').modal('toggle');

				layer.msg('正在查询。。。', {
					icon : 16,
					shade : 0.1,
					time : 0
				});

				$.ajax({
					type : "GET",
					url : Group_URL + "?type=single&val=" + cars[1]
							+ "&p1=1&p2=" + page_max_row,
					dataType : "json",
					success : function(result) {
						layer.closeAll();
						var status = result.status;
						if (status == "606") {
							$('#input_m_s_g_id').text(result.data.id);
							$('#input_m_s_g_name').text(result.data.name);
							$('#text_s_a_side').val(result.data.a);
							$('#text_s_b_side').val(result.data.b);
						} else {
							$('#modal_group_detail').modal('toggle');
							layer.alert('查询出错，请稍后重试!');
						}
					},
					error : function(xhr, ajaxOptions, thrownError) {
						layer.closeAll();
						layer.alert('未知网络错误!');
					}
				});

			});

	$('#table_group tbody').on('click', '#a_tab_g_delete', function() {

		var tr = $(this).parents("tr");
		var th = tr.find("td");
		var cars = new Array();
		var n = 0;
		th.each(function() {
			var ths = $(this);
			cars[n] = ths.text();
			n++;
		});
		layer.open({
			title : '删除透传组',
			btn : [ '确定', '取消' ],
			yes : function(index, layero) {
				layer.closeAll();
				layer.msg('正在删除透传组。。。', {
					icon : 16,
					shade : 0.3,
					time : 0
				});
				deleteGroup("single", cars[1]);

			},
			content : '是否要删除编号为【' + cars[1] + '】名称为【' + cars[2] + '】的透传组吗？'
		});

	});

	$('#table_group tbody').on('click', '#a_tab_g_modify', function() {

		// 清空
		$('#input_m_m_g_name').val("");
		$('#text_m_a_side').val("");
		$('#text_m_b_side').val("");
		$("#table_m_select_a tbody").empty();
		$("#table_m_select_a tbody").append(table_loading_gif);

		$("#table_m_select_b tbody").empty();
		$("#table_m_select_b tbody").append(table_loading_gif);

		$('#modal_modify_group').modal('toggle');

		var tr = $(this).parents("tr");
		var th = tr.find("td");
		var cars = new Array();
		var n = 0;
		th.each(function() {
			var ths = $(this);
			cars[n] = ths.text();
			n++;
		});

		layer.msg('正在查询。。。', {
			icon : 16,
			shade : 0.1,
			time : 0
		});

		$.ajax({
			type : "GET",
			url : Device_URL + "?type=all_for_g&val=&p1=1&p2=" + page_max_row,
			dataType : "json",
			success : function(result) {

				var status = result.status;
				if (status == "606") {
					setModifyGroupSelectDeviceTab(result);
					setModifyGroupStatus(cars[1]);
				} else {
					$('#modal_modify_group').modal('toggle');
					layer.alert('查询出错，请稍后重试!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				$("#table_m_select_a tbody").empty();
				$("#table_m_select_b tbody").empty();
				layer.alert('未知网络错误!');
			}
		});

	});
}

// 设置修改透传组表格状态
function setModifyGroupStatus(key) {
	$.ajax({
		type : "GET",
		url : Group_URL + "?type=single&val=" + key + "&p1=1&p2="
				+ page_max_row,
		dataType : "json",
		success : function(result) {
			layer.closeAll();
			var status = result.status;
			if (status == "606") {
				var get_a = result.data.a;
				var get_b = result.data.b;

				$("#input_m_m_g_id").val("");
				$("#input_m_m_g_name").val("");

				$("#input_m_m_g_id").val(result.data.id);
				$("#input_m_m_g_name").val(result.data.name);

				var arry_a = get_a.split(",");
				var arry_b = get_b.split(",");
				// console.log("1="+arry_a[0]);
				for ( var n in arry_a) {
					$('#table_m_select_a tbody tr').each(function() {
						var td_num = $(this).find("td:eq(1)").text();
						if (td_num == arry_a[n]) {
							var tem = $(this).find("input:eq(0)");
							// tem.prop('checked', true);
							tem.click();
						}
					});
				}

				for ( var n in arry_b) {
					$('#table_m_select_b tbody tr').each(function() {
						var td_num = $(this).find("td:eq(1)").text();
						if (td_num == arry_b[n]) {
							var tem = $(this).find("input:eq(0)");
							// tem.prop('checked', true);
							tem.click();
						}
					});
				}

			} else {
				$('#modal_modify_group').modal('toggle');
				layer.alert('查询出错，请稍后重试!');
			}
		},
		error : function(xhr, ajaxOptions, thrownError) {
			layer.closeAll();
			layer.alert('未知网络错误!');
		}
	});
}

// 设置透传组选择设备表格
function setGroupSelectDeviceTab(result) {
	$("#table_select_a tbody").off();
	$("#table_select_b tbody").off();
	$("#table_select_a tbody").empty();
	$("#table_select_b tbody").empty();
	var data = result.data;
	for (i in data) {
		var tr_content = '<tr><td><input type="checkbox"></td><td>'
				+ data[i].tab_num + '</td><td>' + data[i].tab_name
				+ '</td></tr>'
		$("#table_select_a tbody").append(tr_content);
		$("#table_select_b tbody").append(tr_content);
	}
	// 设置互斥
	$('#table_select_a tbody input:checkbox').each(function() {
		$(this).click(function() {
			var trs = $(this).parents("tr");
			var td_num = trs.find("td:eq(1)").text();
			var btrs = $("#table_select_b tbody").find("tr");
			if ($(this).prop('checked')) {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						cb.prop('checked', false);
						cb.prop('disabled', true);
					}
				});
			} else {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', true);
						cb.prop('disabled', false);
					}
				});
			}
		});
	});
	$('#table_select_b tbody input:checkbox').each(function() {
		$(this).click(function() {
			var trs = $(this).parents("tr");
			var td_num = trs.find("td:eq(1)").text();
			var btrs = $("#table_select_a tbody").find("tr");
			if ($(this).prop('checked')) {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						cb.prop('checked', false);
						cb.prop('disabled', true);
					}
				});
			} else {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', true);
						cb.prop('disabled', false);
					}
				});
			}
		});
	});
}

// 设置修改透传组选择设备表格
function setModifyGroupSelectDeviceTab(result) {

	$("#table_m_select_a tbody").off();
	$("#table_m_select_b tbody").off();
	$("#table_m_select_a tbody").empty();
	$("#table_m_select_b tbody").empty();
	var data = result.data;
	for (i in data) {
		var tr_content = '<tr><td><input type="checkbox"></td><td>'
				+ data[i].tab_num + '</td><td>' + data[i].tab_name
				+ '</td></tr>'
		$("#table_m_select_a tbody").append(tr_content);
		$("#table_m_select_b tbody").append(tr_content);
	}
	// 设置互斥
	$('#table_m_select_a tbody input:checkbox').each(function() {
		$(this).click(function() {
			var trs = $(this).parents("tr");
			var td_num = trs.find("td:eq(1)").text();
			var btrs = $("#table_m_select_b tbody").find("tr");
			if ($(this).prop('checked')) {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						cb.prop('checked', false);
						cb.prop('disabled', true);
					}
				});
			} else {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', true);
						cb.prop('disabled', false);
					}
				});
			}
		});
	});
	$('#table_m_select_b tbody input:checkbox').each(function() {
		$(this).click(function() {
			var trs = $(this).parents("tr");
			var td_num = trs.find("td:eq(1)").text();
			var btrs = $("#table_m_select_a tbody").find("tr");
			if ($(this).prop('checked')) {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						cb.prop('checked', false);
						cb.prop('disabled', true);
					}
				});
			} else {
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', true);
						cb.prop('disabled', false);
					}
				});
			}
		});
	});
}

// 设置账户
function getAccount() {
	custom_account = getUrlParam("account");
	d_code = getUrlParam("d_code");

	num = getUrlParam("num");
	if (d_code != null) {
		$("#input_m_d_psw").val(d_code);
		$("#input_m_l_d_psw").val(d_code);
	}
	if (custom_account != null) {
		$("#a_custom_account").text(custom_account);
		$("#a_custom_account").append('<span class="caret"></span>');
	} else {
		$("#a_custom_account").text("获取账户失败");
	}
}

// 设备搜索框
function setDeviceSearch() {
	$("#a_search_type_num")
			.click(
					function() {
						$("#input_device_search")
								.attr("placeholder", "请输入设备编号");
						$("#input_device_search").attr("name", "d_s_num");
						$("#btn_d_s_c").text("设备编号");
						$("#btn_d_s_c")
								.append(
										'&nbsp;<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>');
					});
	$("#a_search_type_name")
			.click(
					function() {
						$("#input_device_search")
								.attr("placeholder", "请输入设备名称");
						$("#input_device_search").attr("name", "d_s_name");
						$("#btn_d_s_c").text("设备名称");
						$("#btn_d_s_c")
								.append(
										'&nbsp;<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>');
					});
	$("#a_search_type_belong")
			.click(
					function() {
						$("#input_device_search")
								.attr("placeholder", "请输入所属用户");
						$("#input_device_search").attr("name", "d_s_belong");
						$("#btn_d_s_c").text("所属用户");
						$("#btn_d_s_c")
								.append(
										'&nbsp;<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>');
					});
	$("#a_search_type_status")
			.click(
					function() {
						$("#input_device_search").attr("placeholder",
								"请输入设备状态  在线 或者 离线");
						$("#input_device_search").attr("name", "d_s_status");
						$("#btn_d_s_c").text("设备状态");
						$("#btn_d_s_c")
								.append(
										'&nbsp;<span class="caret"></span> <span class="sr-only">Toggle Dropdown</span>');
					});
}

// 全选设备列表
function selectAllDeviceTab() {
	$("#input_device_box").click(function() {
		if ($(this).prop('checked')) {
			$('#table_device tbody input:checkbox').each(function() {
				$(this).prop('checked', true);
			});
			// console.log("选中");
		} else {
			$('#table_device tbody input:checkbox').each(function() {
				$(this).prop('checked', false);
			});
			// console.log("未选中");
		}
	});
}

function selectAllGroup_side() {
	$("#input_select_a").click(function() {
		if ($(this).prop('checked')) {
			$('#table_select_a tbody input:checkbox').each(function() {
				if (!$(this).prop('disabled')) {
					$(this).prop('checked', true);
					var trs = $(this).parents("tr");
					var td_num = trs.find("td:eq(1)").text();
					var btrs = $("#table_select_b tbody").find("tr");
					btrs.each(function() {
						var btd_num = $(this).find("td:eq(1)").text();
						if (td_num == btd_num) {
							var cb = $(this).find("input:eq(0)");
							cb.prop('checked', false);
							cb.prop('disabled', true);
						}
					});
				}
			});
			// console.log("选中");
		} else {
			$('#table_select_a tbody input:checkbox').each(function() {
				$(this).prop('checked', false);

				var trs = $(this).parents("tr");
				var td_num = trs.find("td:eq(1)").text();
				var btrs = $("#table_select_b tbody").find("tr");
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', false);
						cb.prop('disabled', false);
					}
				});
			});
			// console.log("未选中");
		}
	});
	$("#input_select_b").click(function() {
		if ($(this).prop('checked')) {
			$('#table_select_b tbody input:checkbox').each(function() {
				if (!$(this).prop('disabled')) {
					$(this).prop('checked', true);
					var trs = $(this).parents("tr");
					var td_num = trs.find("td:eq(1)").text();
					var btrs = $("#table_select_a tbody").find("tr");
					btrs.each(function() {
						var btd_num = $(this).find("td:eq(1)").text();
						if (td_num == btd_num) {
							var cb = $(this).find("input:eq(0)");
							cb.prop('checked', false);
							cb.prop('disabled', true);
						}
					});
				}
			});
			// console.log("选中");
		} else {
			$('#table_select_b tbody input:checkbox').each(function() {
				$(this).prop('checked', false);
				var trs = $(this).parents("tr");
				var td_num = trs.find("td:eq(1)").text();
				var btrs = $("#table_select_a tbody").find("tr");
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', false);
						cb.prop('disabled', false);
					}
				});
			});
			// console.log("未选中");
		}
	});

	$("#input_m_select_a").click(function() {
		if ($(this).prop('checked')) {
			$('#table_m_select_a tbody input:checkbox').each(function() {
				if (!$(this).prop('disabled')) {
					$(this).prop('checked', true);
					var trs = $(this).parents("tr");
					var td_num = trs.find("td:eq(1)").text();
					var btrs = $("#table_m_select_b tbody").find("tr");
					btrs.each(function() {
						var btd_num = $(this).find("td:eq(1)").text();
						if (td_num == btd_num) {
							var cb = $(this).find("input:eq(0)");
							cb.prop('checked', false);
							cb.prop('disabled', true);
						}
					});
				}
			});
			// console.log("选中");
		} else {
			$('#table_m_select_a tbody input:checkbox').each(function() {
				$(this).prop('checked', false);

				var trs = $(this).parents("tr");
				var td_num = trs.find("td:eq(1)").text();
				var btrs = $("#table_m_select_b tbody").find("tr");
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', false);
						cb.prop('disabled', false);
					}
				});
			});
			// console.log("未选中");
		}
	});
	$("#input_m_select_b").click(function() {
		if ($(this).prop('checked')) {
			$('#table_m_select_b tbody input:checkbox').each(function() {
				if (!$(this).prop('disabled')) {
					$(this).prop('checked', true);
					var trs = $(this).parents("tr");
					var td_num = trs.find("td:eq(1)").text();
					var btrs = $("#table_m_select_a tbody").find("tr");
					btrs.each(function() {
						var btd_num = $(this).find("td:eq(1)").text();
						if (td_num == btd_num) {
							var cb = $(this).find("input:eq(0)");
							cb.prop('checked', false);
							cb.prop('disabled', true);
						}
					});
				}
			});
			// console.log("选中");
		} else {
			$('#table_m_select_b tbody input:checkbox').each(function() {
				$(this).prop('checked', false);
				var trs = $(this).parents("tr");
				var td_num = trs.find("td:eq(1)").text();
				var btrs = $("#table_m_select_a tbody").find("tr");
				btrs.each(function() {
					var btd_num = $(this).find("td:eq(1)").text();
					if (td_num == btd_num) {
						var cb = $(this).find("input:eq(0)");
						// cb.prop('checked', false);
						cb.prop('disabled', false);
					}
				});
			});
			// console.log("未选中");
		}
	});
}

// 设备管理
function handle_device() {
	// 点击添加单个设备
	$("#btn-m-d-add").click(function() {

		var s_d_nmae = $.trim($("#input_m_d_name").val());
		var s_d_num = $.trim($("#input_m_d_num").val());
		var s_d_psw = $.trim($("#input_m_d_psw").val());
		var s_d_mes = $.trim($("#input_m_d_mes").val());

		if (s_d_nmae.length <= 0) {
			layer.alert('请输入设备名称!');
			return 0;
		} else if (s_d_nmae.length > 40) {
			layer.alert('设备名称不能超过40个字符!');
			return 0;
		} else if (!isDeviceName(s_d_nmae)) {
			layer.alert('设备名称必须是中文英文数字 "_" "-" ');
			return 0;
		}

		if (s_d_num.length <= 0) {
			layer.alert('请输入设备编号!');
			return 0;
		} else if (s_d_num.length > 12) {
			layer.alert('设备编号不能超过12个字符!');
			return 0;
		} else if (!isNumber(s_d_num)) {
			layer.alert('设备编号必须是数字!');
			return 0;
		}

		if (s_d_psw.length != 8) {
			layer.alert('设备密码必须8位字符!');
			return 0;
		} else if (!isNumberAndE(s_d_psw)) {
			layer.alert('设备密码必须是数字字母!');
			return 0;
		}

		if (s_d_mes.length > 40) {
			layer.alert('设备描述不能超过40个字符!');
			return 0;
		}

		if (s_d_mes.length <= 0) {
			s_d_mes = "空";
		}

		layer.msg('正在添加设备。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "add_single",
			"name" : s_d_nmae,
			"num" : s_d_num,
			"psw" : s_d_psw,
			"mes" : s_d_mes
		};

		$.ajax({
			type : "POST",
			url : Device_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#add_device_modal').modal('toggle');
					layer.alert('添加设备成功!');

					laypage_bool = true;
					getDeviceList("all", "", "1", page_max_row);
					// 清空添加单个设备模态窗
					$("#input_m_d_name").val("");
					$("#input_m_d_num").val("");
					// $("#input_m_d_psw").val("");
					$("#input_m_d_mes").val("");

				} else if (data.status == "301") {
					layer.alert('设备编号已经存在!');
				} else {
					layer.alert('添加设备失败!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});

	// 点击批量添加设备
	$("#btn-m-l_d-add").click(function() {

		var s_d_nmae = $.trim($("#input_m_l_d_name").val());
		var s_d_num1 = $.trim($("#input_m_f_d_num").val());
		var s_d_num2 = $.trim($("#input_m_s_d_num").val());
		var s_num_g = $.trim($("#input_m_g_d_num").val());
		var s_d_psw = $.trim($("#input_m_l_d_psw").val());
		var s_d_mes = $.trim($("#input_m_l_d_mes").val());

		if (s_d_nmae.length <= 0) {
			s_d_nmae = "空";
		} else if (s_d_nmae.length > 40) {
			layer.alert('设备名称不能超过40个字符!');
			return 0;
		} else if (!isDeviceName(s_d_nmae)) {
			layer.alert('设备名称必须是中文英文数字 "_" "-" ');
			return 0;
		}

		if (s_d_num1.length <= 0) {
			layer.alert('请输入起始编号!');
			return 0;
		} else if (s_d_num1.length > 12) {
			layer.alert('起始编号不能超过12个字符!');
			return 0;
		} else if (!isNumber(s_d_num1)) {
			layer.alert('起始编号必须是数字!');
			return 0;
		}

		if (s_d_num2.length <= 0) {
			layer.alert('请输入结束编号!');
			return 0;
		} else if (s_d_num2.length > 12) {
			layer.alert('结束编号不能超过12个字符!');
			return 0;
		} else if (!isNumber(s_d_num2)) {
			layer.alert('结束编号必须是数字!');
			return 0;
		}

		if (s_d_num2 <= s_d_num1) {
			layer.alert('结束编号必须大于起始编号!');
			return 0;
		} else if ((parseInt(s_d_num2, 10) - parseInt(s_d_num1, 10)) > 200) {
			layer.alert('编号相差不能大于200!');
			return 0;
		}

		if (s_num_g.length < 1) {
			layer.alert('编号间隔至少为1!');
			return 0;
		} else if (s_num_g.length > 6) {
			layer.alert('编号间隔不能大于6位!');
			return 0;
		} else if (!isNumber(s_d_num2)) {
			layer.alert('结束编号必须是数字!');
			return 0;
		}

		if (s_d_psw.length != 8) {
			layer.alert('设备密码必须8位字符!');
			return 0;
		} else if (!isNumberAndE(s_d_psw)) {
			layer.alert('设备密码必须是数字字母!');
			return 0;
		}

		if (s_d_mes.length > 40) {
			layer.alert('设备描述不能超过40个字符!');
			return 0;
		}

		if (s_d_mes.length <= 0) {
			s_d_mes = "空";
		}

		layer.msg('正在批量添加设备。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "add_list",
			"name" : s_d_nmae,
			"num1" : s_d_num1,
			"num2" : s_d_num2,
			"gap" : s_num_g,
			"psw" : s_d_psw,
			"mes" : s_d_mes
		};

		$.ajax({
			type : "POST",
			url : Device_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#add_list_device_modal').modal('toggle');
					layer.alert('批量添加设备成功!');

					laypage_bool = true;
					getDeviceList("all", "", "1", page_max_row);
					// 清空添加单个设备模态窗
					$("#input_m_d_name").val("");
					$("#input_m_f_d_num").val("");
					$("#input_m_s_d_num").val("");
					$("#input_m_g_d_num").val("");
					// $("#input_m_l_d_psw").val("");
					$("#input_m_l_d_mes").val("");

				} else if (data.status == "301") {
					layer.alert('设备编号已经存在!');
				} else {
					layer.alert('批量添加设备失败!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});

	// 点击批量删除
	$("#btn_delete_list_device").click(function() {
		layer.msg('正在批量删除设备。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});
		var tt = [];
		$('#table_device tbody input:checkbox').each(function() {
			if ($(this).prop('checked')) {
				var tr = $(this).parents("tr");
				var th = tr.find("td");
				var cars = new Array();
				var n = 0;
				th.each(function() {
					var ths = $(this);
					cars[n] = ths.text();
					n++;
				});
				tt.push(cars[1]);
				// console.log(cars[1]);
			}
		});

		if (tt.length <= 0) {
			layer.closeAll();
			layer.alert('请选择要删除设备!');
			return 0;
		}

		deleteDevice("list", tt);
	});

	// 点击设备管理
	$("#a_device_tab").click(function() {
		laypage_bool = true;
		getDeviceList("all", "", "1", page_max_row);
	});

	// 点击提交修改
	$("#btn_m_m_d_submit").click(function() {

		var s_d_nmae = $.trim($("#input_m_m_d_name").val());
		var s_d_num = $.trim($("#input_m_m_d_num").val());
		var s_d_psw = $.trim($("#input_m_m_d_psw").val());
		var s_d_mes = $.trim($("#input_m_m_d_mes").val());

		if (s_d_nmae.length <= 0) {
			layer.alert('请输入设备名称!');
			return 0;
		} else if (s_d_nmae.length > 40) {
			layer.alert('设备名称不能超过40个字符!');
			return 0;
		} else if (!isDeviceName(s_d_nmae)) {
			layer.alert('设备名称必须是中文英文数字 "_" "-" ');
			return 0;
		}

		if (s_d_psw.length != 8) {
			layer.alert('设备密码必须8位字符!');
			return 0;
		} else if (!isNumberAndE(s_d_psw)) {
			layer.alert('设备密码必须是数字字母!');
			return 0;
		}

		if (s_d_mes.length > 40) {
			layer.alert('设备描述不能超过40个字符!');
			return 0;
		}

		if (s_d_mes.length <= 0) {
			s_d_mes = "空";
		}

		layer.msg('正在修改设备信息。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "modify_single_device",
			"num" : s_d_num,
			"name" : s_d_nmae,
			"psw" : s_d_psw,
			"mes" : s_d_mes
		};

		$.ajax({
			type : "POST",
			url : Device_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#modal_modify_device').modal('toggle');
					layer.alert('修改设备信息成功!');

				} else {
					layer.alert('修改设备信息失败!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});

	// 点击设备查询
	$("#btn_query_device").click(function() {

		var t_v = $.trim($("#input_device_search").val());

		if (t_v.length <= 0) {
			layer.alert('请输入查询条件!');
			return;
		}

		var t_n = $("#input_device_search").attr("name");
		var ty = "";
		if (t_n == "d_s_num") {
			ty = "query_num";
			if (!isNumber(t_v)) {
				layer.alert('设备编号必须为数字!');
				return;
			} else if (t_v.length > 20) {
				layer.alert('设备编号不超过20位!');
				return;
			}

		} else if (t_n == "d_s_name") {
			ty = "query_name";
			if (!isDeviceName(t_v)) {
				layer.alert('设备名称必须为中英文数字_-!');
				return;
			} else if (t_v.length > 40) {
				layer.alert('设备名称不超过40位!');
				return;
			}

		} else if (t_n == "d_s_belong") {

		} else if (t_n == "d_s_status") {
			ty = "query_status";
			if ((t_v != "在线") && (t_v != "离线")) {
				layer.alert('请输入“在线”或者“离线”!');
				return;
			}
		}
		laypage_bool = true;
		getDeviceList(ty, t_v, "1", page_max_row);
	});

	// 查询返回
	$("#input_device_search").bind("input propertychange", function() {
		var t_v = $.trim($("#input_device_search").val());
		if (t_v <= 0) {
			laypage_bool = true;
			getDeviceList("all", "", "1", page_max_row);
		}
	});
}

// 透传组管理
function handle_group() {
	// 点击设备管理
	$("#a_group_tab").click(function() {
		laypage_group_bool = true;
		getGroupList("all", "", "1", page_max_row);
	});
	// 点击添加组
	$("#btn_add_group").click(function() {
		$("#text_a_side").val("");
		$("#text_b_side").val("");
		$("#input_m_g_name").val("");
		getDeviceList("all_for_g", "", "1", page_max_row);
	});

	// 点击添加ab组
	$("#btn_a_side").click(function() {
		var trs = $("#table_select_a tbody").find("tr");
		var content = "";
		var n = 0;
		trs.each(function() {
			var td = $(this).find("td:eq(1)");
			var check_box = $(this).find("input:eq(0)");
			if (check_box.prop('checked')) {
				content = content + "," + td.text();
				n = n + 1;
			}

		});

		if (n == 0) {
			layer.alert('请勾选设备!');
			return 0;
		}

		if (n > 100) {
			layer.alert('勾选设备数量不能超过100!');
			return 0;
		}
		content = content.substring(1);
		$("#text_a_side").val(content);
	});
	$("#btn_b_side").click(function() {

		var trs = $("#table_select_b tbody").find("tr");
		var content = "";
		var n = 0;
		trs.each(function() {
			var td = $(this).find("td:eq(1)");
			var check_box = $(this).find("input:eq(0)");
			if (check_box.prop('checked')) {
				content = content + "," + td.text();
				n = n + 1;
			}

		});

		if (n == 0) {
			layer.alert('请勾选设备!');
			return 0;
		}

		if (n > 100) {
			layer.alert('勾选设备数量不能超过100!');
			return 0;
		}

		content = content.substring(1);
		$("#text_b_side").val(content);

	});
	// 点击添加透传组
	$("#btn-m-g-add").click(function() {
		var g_name = $.trim($("#input_m_g_name").val());
		var g_a_c = $.trim($("#text_a_side").val());
		var g_b_c = $.trim($("#text_b_side").val());

		if (g_name.length <= 0) {
			layer.alert('请输入透传组名称!');
			return 0;
		} else if (g_name.length > 40) {
			layer.alert('透传组名称不能超过40个字符!');
			return 0;
		} else if (!isDeviceName(g_name)) {
			layer.alert('透传组名称必须是中文英文数字 "_" "-" ');
			return 0;
		}
		if (g_a_c.length <= 0) {
			layer.alert('左边设备不能为空!');
			return 0;
		}
		if (g_b_c.length <= 0) {
			layer.alert('右边设备不能为空!');
			return 0;
		}

		layer.msg('正在添加透传组。。。', {
			icon : 16,
			shade : 0.3,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "add_group",
			"name" : g_name,
			"a" : g_a_c,
			"b" : g_b_c
		};

		$.ajax({
			type : "POST",
			url : Group_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#add_group_modal').modal('toggle');
					layer.alert('添加透传组成功!');

					// 清空添加透传组模态窗
					$("#input_m_g_name").val("");
					$("#text_a_side").val("");
					$("#text_b_side").val("");

					laypage_group_bool = true;
					getGroupList("all", "", "1", page_max_row);

				} else if (data.status == "301") {
					layer.alert('组名称已经存在!');
				} else {
					layer.alert('添加透传组失败!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});

	});

	// 点击 重置 添加透传组
	$("#btn-m-g-reset").click(function() {
		$("#text_a_side").val("");
		$("#text_b_side").val("");

		$("#table_select_a input").each(function() {
			$(this).prop('checked', false);
			$(this).prop('disabled', false);

		});
		$("#table_select_b input").each(function() {
			$(this).prop('checked', false);
			$(this).prop('disabled', false);

		});
	});

	// 点击修改组 添加选中
	$("#btn_m_a_side").click(function() {
		var trs = $("#table_m_select_a tbody").find("tr");
		var content = "";
		var n = 0;
		trs.each(function() {
			var td = $(this).find("td:eq(1)");
			var check_box = $(this).find("input:eq(0)");
			if (check_box.prop('checked')) {
				content = content + "," + td.text();
				n = n + 1;
			}

		});

		if (n == 0) {
			layer.alert('请勾选设备!');
			return 0;
		}

		if (n > 100) {
			layer.alert('勾选设备数量不能超过100!');
			return 0;
		}
		content = content.substring(1);
		$("#text_m_a_side").val(content);
	});
	$("#btn_m_b_side").click(function() {
		var trs = $("#table_m_select_b tbody").find("tr");
		var content = "";
		var n = 0;
		trs.each(function() {
			var td = $(this).find("td:eq(1)");
			var check_box = $(this).find("input:eq(0)");
			if (check_box.prop('checked')) {
				content = content + "," + td.text();
				n = n + 1;
			}

		});

		if (n == 0) {
			layer.alert('请勾选设备!');
			return 0;
		}

		if (n > 100) {
			layer.alert('勾选设备数量不能超过100!');
			return 0;
		}
		content = content.substring(1);
		$("#text_m_b_side").val(content);
	});

	// 点击修改 透传组
	$("#btn_m_m_g_modify").click(function() {
		var g_id = $.trim($("#input_m_m_g_id").val());
		var g_name = $.trim($("#input_m_m_g_name").val());
		var g_a_c = $.trim($("#text_m_a_side").val());
		var g_b_c = $.trim($("#text_m_b_side").val());

		if (g_name.length <= 0) {
			layer.alert('请输入透传组名称!');
			return 0;
		} else if (g_name.length > 40) {
			layer.alert('透传组名称不能超过40个字符!');
			return 0;
		} else if (!isDeviceName(g_name)) {
			layer.alert('透传组名称必须是中文英文数字 "_" "-" ');
			return 0;
		}
		if (g_a_c.length <= 0) {
			layer.alert('左边设备不能为空!');
			return 0;
		}
		if (g_b_c.length <= 0) {
			layer.alert('右边设备不能为空!');
			return 0;
		}

		layer.msg('正在修改透传组。。。', {
			icon : 16,
			shade : 0.1,
			time : 0
		});

		// 处理信息
		var json_data = {
			"action" : "modify_group",
			"id" : g_id,
			"name" : g_name,
			"a" : g_a_c,
			"b" : g_b_c
		};

		$.ajax({
			type : "POST",
			url : Group_URL,
			data : JSON.stringify(json_data),
			dataType : "json",
			success : function(data) {
				layer.closeAll();
				if (data.status == "606") {
					$('#modal_modify_group').modal('toggle');
					layer.alert('修改透传组成功!');

					// 清空添加透传组模态窗
					$("#text_m_a_side").val("");
					$("#text_m_b_side").val("");

					laypage_group_bool = true;
					getGroupList("all", "", "1", page_max_row);

				} else if (data.status == "301") {
					layer.alert('组名称已经存在!');
				} else {
					layer.alert('修改透传组失败，请稍后重试!');
				}
			},
			error : function(xhr, ajaxOptions, thrownError) {
				layer.closeAll();
				layer.alert('未知网络错误!');
			}
		});
	});

	// 点击查询 透传组
	$("#btn_group_search").click(function() {
		var g_name = $.trim($("#input_group_search").val());
		if (g_name.length <= 0) {
			layer.alert('请输入透传组名称!');
			return 0;
		} else if (g_name.length > 40) {
			layer.alert('透传组名称不能超过40个字符!');
			return 0;
		} else if (!isDeviceName(g_name)) {
			layer.alert('透传组名称必须是中文英文数字 "_" "-" ');
			return 0;
		}

		laypage_group_bool = true;
		getGroupList("query_name", g_name, "1", page_max_row);

	});

	// 查询返回
	$("#input_group_search").bind("input propertychange", function() {
		var t_v = $.trim($("#input_group_search").val());
		if (t_v <= 0) {
			laypage_group_bool = true;
			getGroupList("all", "", "1", page_max_row);
		}
	});
}
