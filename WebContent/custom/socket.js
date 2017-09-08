/**
 * WebSocket
 */

var Url_socket = "ws://112.64.201.138:10005/ktDTU/websocket";

var ws;
var layer_virtual;
var isWebSocket = false;

// 是否连接
var iscon1 = false;
var iscon2 = false;

// 加载完毕
$(document).ready(function() {
	// UI操作
	layui.use('layer', function() { // 初始化layer
		layer_virtual = layui.layer;
	});
	// 虚拟设备管理
	handle_virtual();
});

// 虚拟设备管理
function handle_virtual() {
	//WebSocket_handle();
	$("#a_virtual_tab").click(function() {
		if ("WebSocket" in window) {
			if (!isWebSocket) {
				WebSocket_handle();
			}

		} else {
			layer_virtual.alert('很遗憾您的浏览器不支持此功能（WebSocket）,请不要使用IE9以下版本浏览器!');
		}
	});
}

var num1;
var num2;

function WebSocket_handle() {

	// 打开一个 web socket
	ws = new WebSocket(Url_socket);

	ws.onopen = function() {
		// Web Socket 已连接上，使用 send() 方法发送数据
		// ws.send("这里是 浏览器虚拟设备端。");
		console.log("WebSocket成功连接...");
		isWebSocket = true;
	};

	ws.onmessage = function(evt) {
		var received_msg = evt.data;
		console.log("接受数据：" + received_msg);

		var get_json = JSON.parse(received_msg);
		if (get_json.action == "register") {
			if (get_json.status == "offline") {
				layer_virtual.alert('链接失败！编号为[' + get_json.id + ']设备离线！');

				if (get_json.id == num1) {
					iscon1 = false;
					$("#input_a_virtual_to_d").attr("disabled", false);
					$("#btn_a_con_d").attr("disabled", false);
					$("#btn_a_con_d").text("连接设备");
					$("#div_send_a").hide();
				} else if (get_json.id == num2) {
					iscon2 = false;
					$("#input_b_virtual_to_d").attr("disabled", false);
					$("#btn_b_con_d").attr("disabled", false);
					$("#btn_b_con_d").text("连接设备");
					$("#div_send_b").hide();
				}

			} else if (get_json.status == "online") {
				layer_virtual.alert('成功连接编号为[' + get_json.id + ']设备！');

				if (get_json.id == num1) {
					iscon1 = true;

					$("#div_send_a").show();
					$("#btn_a_con_d").attr("disabled", false);
					$("#btn_a_con_d").text("断开连接");
					var obj = document.getElementById("text_a_virtual_show");
					obj.value = "";
					obj.value += "状态：设备[" + get_json.id + "]连接成功，开始接受数据！"
							+ "\n";
				} else if (get_json.id == num2) {
					iscon2 = true;
					$("#div_send_b").show();
					$("#btn_b_con_d").attr("disabled", false);
					$("#btn_b_con_d").text("断开连接");
					var obj = document.getElementById("text_b_virtual_show");
					obj.value = "";
					obj.value += "状态：设备[" + get_json.id + "]连接成功，开始接受数据！"
							+ "\n";
				}
			}
		} else if (get_json.action == "send") {
			if (get_json.id == num1) {
				var obj = document.getElementById("text_a_virtual_show");
				obj.value += "透传数据[" + get_json.date + "]：\n"
						+ get_json.content + "\n";
				obj.scrollTop = obj.scrollHeight;
			} else if (get_json.id == num2) {
				var obj = document.getElementById("text_b_virtual_show");
				obj.value += "透传数据[" + get_json.date + "]：\n"
						+ get_json.content + "\n";
				obj.scrollTop = obj.scrollHeight;
			}
		} else if (get_json.action == "change") {    //设备状态改变
			if (get_json.action == "offline") {
				$('#table_device tbody tr').each(function() {
					var td=$(this).find('td:eq(4)');
					if(td.text()=="在线"){
						td.text("离线");
					}
				});

			} else if (get_json.action == "online") {
				$('#table_device tbody tr').each(function() {
					var td=$(this).find('td:eq(4)');
					if(td.text()=="离线"){
						td.text("在线");
					}
				});
			}
		}else if (get_json.action == "closeWebSocket"){
			
			
		}
	};

	ws.onclose = function() {
		console.log("WebSocket连接已关闭...");
		isWebSocket = false;
	};

	// 点击连接设备
	$("#btn_a_con_d").click(function() {
		var d_id = $.trim($("#input_a_virtual_to_d").val());
		if (iscon1) {
			iscon1 = false;
			$("#input_a_virtual_to_d").attr("disabled", false);
			$("#btn_a_con_d").attr("disabled", false);
			$("#btn_a_con_d").text("连接设备");
			$("#div_send_a").hide();

			// 关闭连接
			var json_data = {
				"action" : "closeWebSocket",
				"id" : d_id
			};

			ws.send(JSON.stringify(json_data));
		} else {
			$("#input_a_virtual_to_d").attr("disabled", true);
			$("#btn_a_con_d").attr("disabled", true);

			if (d_id.length <= 0) {
				layer_virtual.alert('请输入设备编号！');
				$("#input_a_virtual_to_d").attr("disabled", false);
				$("#btn_a_con_d").attr("disabled", false);
				return 0;
			} else if (d_id.length != 20) {
				layer_virtual.alert('设备编号为20位字符！');
				$("#input_a_virtual_to_d").attr("disabled", false);
				$("#btn_a_con_d").attr("disabled", false);
				return 0;
			}

			num1 = d_id;

			var json_data = {
				"action" : "register",
				"id" : d_id
			};

			layer_virtual.msg('正在连接编号为[' + d_id + ']的设备', {
				icon : 16,
				shade : 0.3,
				time : 0
			});

			ws.send(JSON.stringify(json_data));
		}
	});

	$("#btn_b_con_d").click(function() {
		var d_id = $.trim($("#input_b_virtual_to_d").val());
		if (iscon2) {

			iscon2 = false;
			$("#input_b_virtual_to_d").attr("disabled", false);
			$("#btn_b_con_d").attr("disabled", false);
			$("#btn_b_con_d").text("连接设备");
			$("#div_send_b").hide();

			var json_data = {
				"action" : "closeWebSocket",
				"id" : d_id
			};

			ws.send(JSON.stringify(json_data));
		} else {
			$("#input_b_virtual_to_d").attr("disabled", true);
			$("#btn_b_con_d").attr("disabled", true);

			if (d_id.length <= 0) {
				layer_virtual.alert('请输入设备编号！');
				$("#input_b_virtual_to_d").attr("disabled", false);
				$("#btn_b_con_d").attr("disabled", false);
				return 0;
			} else if (d_id.length != 20) {
				layer_virtual.alert('设备编号为20位字符！');
				$("#input_b_virtual_to_d").attr("disabled", false);
				$("#btn_b_con_d").attr("disabled", false);
				return 0;
			}

			num2 = d_id;

			var json_data = {
				"action" : "register",
				"id" : d_id
			};
			ws.send(JSON.stringify(json_data));
		}

	});

	// 点击发送信息
	$("#btn_a_sen_d").click(function() {
		if (iscon1) {
			var d_id = $.trim($("#input_a_virtual_to_d").val());
			var d_mes = $.trim($("#input_a_to_d").val());

			if (d_mes.length <= 0) {
				layer_virtual.alert('请输入发送信息！');
				return;
			}

			var json_data = {
				"action" : "send",
				"content" : d_mes,
				"id" : d_id
			};

			ws.send(JSON.stringify(json_data));
			var obj = document.getElementById("text_a_virtual_show");
			obj.value += "前端向设备[" + d_id + "]发送数据：\n" + d_mes + "\n";
			obj.scrollTop = obj.scrollHeight;
		} else {
			layer_virtual.alert('未连接设备！');
		}
	});

	$("#btn_b_sen_d").click(function() {
		if (iscon2) {
			var d_id = $.trim($("#input_b_virtual_to_d").val());
			var d_mes = $.trim($("#input_b_to_d").val());

			if (d_mes.length <= 0) {
				layer_virtual.alert('请输入发送信息！');
				return;
			}

			var json_data = {
				"action" : "send",
				"content" : d_mes,
				"id" : d_id
			};

			ws.send(JSON.stringify(json_data));

			var obj = document.getElementById("text_b_virtual_show");
			obj.value += "前端向设备[" + d_id + "]发送数据：\n" + d_mes + "\n";
			obj.scrollTop = obj.scrollHeight;

		} else {
			layer_virtual.alert('未连接设备！');
		}
	});
}
